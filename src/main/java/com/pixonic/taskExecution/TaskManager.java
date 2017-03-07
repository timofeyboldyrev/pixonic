package com.pixonic.taskExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@Service
public class TaskManager {

    @Autowired
    private TaskExecutor taskExecutor;

    public void runActionAtTime(Callable action, LocalDateTime executionTime) {
        taskExecutor.addTaskToQueue(new Task(action, executionTime));
    }

}
