package org.acerbulescu.docker;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.config.ConfigRepresentation;
import org.acerbulescu.models.ServerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class DockerClient {
  private final DockerClientImpl dockerClient;

  @Autowired
  private ConfigRepresentation config;

  public static final String JAVA_21_IMAGE = "eclipse-temurin:21-jdk";
  public static final String SERVER_PATH = "/server";

  public DockerClient() {
    var dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    dockerClient = DockerClientImpl
        .getInstance(dockerConfig)
        .withDockerCmdExecFactory(new NettyDockerCmdExecFactory());
    try {
      dockerClient.pullImageCmd(JAVA_21_IMAGE).start().awaitCompletion();
    } catch (Exception e) {
      log.error("Could not pull image={}", JAVA_21_IMAGE, e);
      throw new RuntimeException(e);
    }
  }

  public DockerClient(ConfigRepresentation config) {
    this();
    this.config = config;
  }

  public void startContainer(ServerInstance instance) {
    log.info("Starting container for instance={}", instance.getName());
    dockerClient.startContainerCmd(instance.getName()).exec();
  }

  public void pauseContainer(ServerInstance instance) {
    log.info("Pausing container for instance={}", instance.getName());
    dockerClient.pauseContainerCmd(instance.getName()).exec();
  }

  public void unpauseContainer(ServerInstance instance) {
    log.info("Unpausing container for instance={}", instance.getName());
    dockerClient.unpauseContainerCmd(instance.getName()).exec();
  }

  public void stopInstance(ServerInstance instance) {
    log.info("Stopping container for instance={}", instance.getName());
    dockerClient.stopContainerCmd(instance.getName()).exec();
  }

  public CreateContainerResponse createServerContainer(ServerInstance instance) {
    log.info("Creating container definition for instance={}", instance.getName());
    log.info("Creating volume bind instance={} host={} target={}", instance.getName(), instance.getPath(), DockerClient.SERVER_PATH);
    var volumeBinds = new Bind(instance.getPath(), new Volume(DockerClient.SERVER_PATH));

    var hostConfig = HostConfig.newHostConfig()
        .withAutoRemove(Boolean.TRUE)
        .withBinds(volumeBinds)
        .withNetworkMode(config.getDockerNetwork());

    List<ExposedPort> exposedPorts = new ArrayList<>();

    if (System.getProperty("containerised") == null) {
      var portBindings = new Ports();

      log.info("Creating port={} bindings for instance={}", instance.getPrivatePort(), instance.getName());
      portBindings.bind(ExposedPort.tcp(instance.getPrivatePort()), Ports.Binding.bindPort(instance.getPrivatePort()));
      portBindings.bind(ExposedPort.udp(instance.getPrivatePort()), Ports.Binding.bindPort(instance.getPrivatePort()));

      exposedPorts.add(new ExposedPort(instance.getPrivatePort(), InternetProtocol.TCP));
      exposedPorts.add(new ExposedPort(instance.getPrivatePort(), InternetProtocol.UDP));

      instance.getPorts().forEach(e -> {
        switch (e.getProtocol()) {
          case TCP:
            portBindings.bind(ExposedPort.tcp(e.getPrivatePort()), Ports.Binding.bindPort(e.getPrivatePort()));
            exposedPorts.add(new ExposedPort(e.getPrivatePort(), InternetProtocol.TCP));
            break;
          case UDP:
            portBindings.bind(ExposedPort.udp(e.getPrivatePort()), Ports.Binding.bindPort(e.getPrivatePort()));
            exposedPorts.add(new ExposedPort(e.getPrivatePort(), InternetProtocol.UDP));
            break;
          default:
            throw new RuntimeException("Invalid protocol specified");
        }
      });

      hostConfig.withPortBindings(portBindings);
    }


    return dockerClient
        .createContainerCmd(DockerClient.JAVA_21_IMAGE)
        .withName(instance.getName())
        .withWorkingDir(DockerClient.SERVER_PATH)
        .withHostConfig(hostConfig)
        .withExposedPorts(exposedPorts)
        .withCmd(instance.getStartCommand().split(" "))
        .withAttachStdout(Boolean.TRUE)
        .withAttachStderr(Boolean.TRUE)
        .withStdinOpen(Boolean.TRUE)
        .withTty(Boolean.TRUE)
        .exec();
  }

  public void attachToContainerIo(String containerName, WebSocketSession session) {
    try {
      var stdinPipeOut = new PipedOutputStream();
      var stdinPipeIn = new PipedInputStream(stdinPipeOut);

      dockerClient.attachContainerCmd(containerName)
          .withStdOut(true)
          .withStdErr(true)
          .withStdIn(stdinPipeIn)
          .withLogs(true)
          .withFollowStream(true)
          .exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
              if (session.isOpen()) {
                try {
                  session.sendMessage(new TextMessage(new String(frame.getPayload())));
                } catch (IOException e) {
                  log.error("Could not pipe IO from container={}", containerName, e);
                  throw new RuntimeException(e.getMessage());
                }
              } else {
                log.info("Webhook session is closed for container={}", containerName);
                try {
                  this.close();
                } catch (IOException e) {
                  log.error("Could not disconnect socket stream from container={}", containerName, e);
                }
              }
            }
          });
    } catch (IOException e) {
      log.error("Could not pipe IO from container={}", containerName, e);
      throw new RuntimeException(e.getMessage());
    }
  }

  public void writeToContainerIo(String containerName, String message) {
    log.info("Forwarding message=\"{}\" to container={}", message, containerName);
    try {
      var stdinPipeOut = new PipedOutputStream();
      var stdinPipeIn = new PipedInputStream(stdinPipeOut);

      dockerClient.attachContainerCmd(containerName)
          .withStdIn(stdinPipeIn)
          .withStdOut(true)
          .withStdErr(true)
          .withFollowStream(true)
          .exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onError(Throwable throwable) {
              log.error("Error writing to container with id={}", containerName, throwable);
            }
          });

      stdinPipeOut.write((message + "\n").getBytes());
      stdinPipeOut.flush();
    } catch (Exception e) {
      log.error("Could not write to container with id={}", containerName, e);
      throw new RuntimeException(e.getMessage());
    }
  }
}
