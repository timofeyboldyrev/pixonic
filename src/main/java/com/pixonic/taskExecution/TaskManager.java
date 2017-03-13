package com.pixonic.taskExecution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@Service
public class TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    @Autowired
    private TaskExecutor taskExecutor;

    public void runActionAtTime(Callable action, LocalDateTime executionTime) {
        taskExecutor.addTaskToQueue(new Task(action, executionTime));
        log.debug("New task with start time at " + executionTime + " was added to queue");
    }

}
