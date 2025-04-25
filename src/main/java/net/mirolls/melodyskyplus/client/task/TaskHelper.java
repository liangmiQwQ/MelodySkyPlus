package net.mirolls.melodyskyplus.client.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskHelper {
  private final List<Runnable> tasks = new ArrayList<>();

  public void addTask(Runnable task) {
    tasks.add(task);
  }

  public void run() {
    ExecutorService executor = Executors.newFixedThreadPool(tasks.size()); // 或者其他线程池策略

    for (Runnable task : tasks) {
      executor.submit(task);
    }

    executor.shutdown(); // 拒绝新任务
    try {
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // 超时强制停止
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
