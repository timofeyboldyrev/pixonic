package com.pixonic.taskExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@Service
class TaskExecutor {

    @Autowired
    private ExecutorService executorService;
    private final PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>();

    void addTaskToQueue(Task task) {
        queue.offer(task);
    }

    @PostConstruct
    private void initWatchingThread() {
        ExecutorService watchingThread = Executors.newSingleThreadExecutor();
        watchingThread.execute(this::watchQueue);
        watchingThread.shutdown();
    }

    private void watchQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            Task taskFromQueue = queue.peek();
            if(needToRunTask(taskFromQueue)) {
                executorService.submit(taskFromQueue.getAction());
                queue.remove(taskFromQueue);
            }
        }
    }

    private boolean needToRunTask(Task task) {
        return task!=null && task.getExecutionTime().compareTo(LocalDateTime.now())<=0;
    }

}
