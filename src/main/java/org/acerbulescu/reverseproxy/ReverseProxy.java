package org.acerbulescu.reverseproxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.acerbulescu.instancemanager.InstanceManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ReverseProxy {
  private static final Long NO_REQUEST_TIMEOUT = 10L;

  protected InstanceManager instanceManager;

  protected String instanceName;
  protected Integer publicPort;
  protected Integer privatePort;

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private ScheduledFuture<?> scheduledFuture;

  public abstract void start();

  public abstract void stop();

  public abstract Status getStatus();

  public enum Status {
    BUSY, IDLE,
  }

  protected synchronized void scheduleSuspend() {
    cancelTask();
    scheduledFuture = scheduler.schedule(() -> instanceManager.attemptSuspend(instanceName), NO_REQUEST_TIMEOUT, TimeUnit.SECONDS);
  }

  private synchronized void cancelTask() {
    if (scheduledFuture != null && !scheduledFuture.isDone()) {
      scheduledFuture.cancel(false);
    }
  }
}
