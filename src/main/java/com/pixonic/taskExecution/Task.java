package com.pixonic.taskExecution;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
class Task implements Comparable<Task>{

    private final Callable action;
    private final LocalDateTime executionTime;
    private final LocalDateTime createdTime;

    Task(Callable action, LocalDateTime executionTime) {
        createdTime = LocalDateTime.now();
        Objects.requireNonNull(action, "Parameter action is null");
        Objects.requireNonNull(executionTime, "Parameter executionTime is null");
        if(executionTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Execution time in the past");
        }
        this.action = action;
        this.executionTime = executionTime;
    }

    Callable getAction() {
        return action;
    }

    LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public int compareTo(Task otherTask) {
        int compareResult = this.getExecutionTime().compareTo(otherTask.getExecutionTime());
        if(compareResult==0) {
            compareResult = createdTime.compareTo(otherTask.createdTime);
        }
        return compareResult;
    }
}
