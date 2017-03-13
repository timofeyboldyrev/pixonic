package com.pixonic.taskExecution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@Service
class TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    @Autowired
    private ExecutorService executorService;
    private final PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>();

    void addTaskToQueue(Task task) {
        queue.offer(task);
        synchronized (this) {
            notify();
        }
    }

    @PostConstruct
    private void initWatchingThread() {
        ExecutorService watchingThread = Executors.newSingleThreadExecutor();
        watchingThread.execute(this::watchQueue);
        watchingThread.shutdown();
    }

    private void watchQueue() {
        Task taskFromQueue;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (this) {
                    while (!needToRunTask(taskFromQueue = queue.peek())) {
                        if (taskFromQueue == null) {
                            wait();
                        } else {
                            long taskTime = taskFromQueue.getExecutionTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            long waitTime = taskTime - System.currentTimeMillis();
                            wait(waitTime < 0 ? 0 : waitTime);
                        }
                    }
                }
                executorService.submit(taskFromQueue.getAction());
                queue.remove(taskFromQueue);
            }
            log.error("Watching thread was interrupted");
        } catch (InterruptedException e) {
            log.error("Watching thread was interrupted", e);
        }
    }

    private boolean needToRunTask(Task task) {
        return task!=null && task.getExecutionTime().compareTo(LocalDateTime.now())<=0;
    }

}
