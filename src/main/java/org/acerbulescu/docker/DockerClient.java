package org.acerbulescu.docker;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.log4j.Log4j2;
import org.acerbulescu.models.DockerInstance;
import org.acerbulescu.models.ServerInstance;

import java.nio.file.Paths;

@Log4j2
public class DockerClient {

  private final DockerClientImpl dockerClient;

  public static final String JAVA_21_IMAGE = "eclipse-temurin:21-jdk";
  public static final String SERVER_PATH = "/server";

  public DockerClient() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    dockerClient = DockerClientImpl
        .getInstance(config)
        .withDockerCmdExecFactory(new NettyDockerCmdExecFactory());

    try {
      dockerClient.pullImageCmd(JAVA_21_IMAGE).start().awaitCompletion();
    } catch (Exception e) {
      log.error("Could not pull image: " + JAVA_21_IMAGE, e);
      throw new RuntimeException(e);
    }
  }

  public void startContainer(DockerInstance instance) {
    log.info("Starting container for instance={}", instance.getName());
    dockerClient.startContainerCmd(instance.getId()).exec();
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
    var hostPath = Paths.get(instance.getPath()).toAbsolutePath().toString();
    var volumeBinds = new Bind(hostPath, new Volume(DockerClient.SERVER_PATH));
    var portBindings = new Ports();

    portBindings.bind(ExposedPort.tcp(instance.getPrivatePort()), Ports.Binding.bindPort(instance.getPrivatePort()));
    portBindings.bind(ExposedPort.udp(instance.getPrivatePort()), Ports.Binding.bindPort(instance.getPrivatePort()));

    return dockerClient
        .createContainerCmd(DockerClient.JAVA_21_IMAGE)
        .withName(instance.getName())
        .withWorkingDir(DockerClient.SERVER_PATH)
        .withHostConfig(
            HostConfig.newHostConfig()
                .withAutoRemove(true)
                .withBinds(volumeBinds)
                .withPortBindings(portBindings)
        )
        .withExposedPorts(
            new ExposedPort(instance.getPrivatePort(), InternetProtocol.TCP),
            new ExposedPort(instance.getPrivatePort(), InternetProtocol.UDP)
        )
        .withCmd(instance.getStartCommand().split(" "))
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true)
        .withTty(true)
        .exec();
  }
}
