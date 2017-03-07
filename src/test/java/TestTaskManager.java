import com.pixonic.taskExecution.TaskManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@ContextConfiguration(classes = TestConfig.class)
@RunWith(SpringRunner.class)
public class TestTaskManager {

    @Autowired
    private TaskManager taskManager;

    @Test(expected = NullPointerException.class)
    public void nullAction() {
        taskManager.runActionAtTime(null, LocalDateTime.now());
    }

    @Test(expected = NullPointerException.class)
    public void nullTime() {
        taskManager.runActionAtTime(()-> null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalDate() {
        taskManager.runActionAtTime(() -> null, LocalDateTime.now().minusSeconds(5));
    }

    @Test
    public void runAtExactTime() {
        class Flag {
            private boolean value;
            public boolean getValue() {
                return value;
            }
            public void setValue(boolean value) {
                this.value = value;
            }
        }
        Flag flag = new Flag();
        LocalDateTime executionTime = LocalDateTime.now().plusSeconds(2);
        taskManager.runActionAtTime(() -> {
            flag.setValue(true);
            return null;
        }, executionTime);
        while (LocalDateTime.now().isBefore(executionTime)) {
            assertFalse(flag.getValue());
        }
        while (LocalDateTime.now().isEqual(executionTime)) {}
        assertTrue(flag.getValue());
    }

    @Test
    public void orderOfDifferentTimeTasks() throws InterruptedException {
        LocalDateTime currentTime = LocalDateTime.now();
        List<LocalDateTime> executionTimeList = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newCachedThreadPool();
        int countOfTasks = 100;
        int timeInterval = 10;
        for (Integer addedTime : listOfDifferentNumbers(countOfTasks, timeInterval)) {
            LocalDateTime executionTime = currentTime.plus(500 + addedTime, ChronoUnit.MILLIS);
            executorService.execute(addTaskToTaskManager(latch, executionTimeList, executionTime));
        }
        executorService.shutdown();
        latch.countDown();
        Thread.sleep(countOfTasks*timeInterval + 1500);
        assertEquals(countOfTasks, executionTimeList.size());
        for (int i = 1; i < executionTimeList.size(); i++) {
            assertTrue(executionTimeList.get(i).compareTo(executionTimeList.get(i-1))>=0);
        }
    }

    private List<Integer> listOfDifferentNumbers(int size, int interval) {
        List<Integer> list = IntStream.range(0, size).map(i -> i * interval).boxed().collect(Collectors.toList());
        Collections.shuffle(list);
        return list;
    }

    private Runnable addTaskToTaskManager(CountDownLatch latch, List<LocalDateTime> executionTimeList, LocalDateTime executionTime) {
        return () -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            taskManager.runActionAtTime(() -> {
                executionTimeList.add(executionTime);
                return null;
            }, executionTime);
        };
    }

    // test is correct only if thread poll size is 1
    @Test
    public void orderOfSameTimeTasks() throws InterruptedException {
        LocalDateTime executionTime = LocalDateTime.now().plusSeconds(1);
        List<Integer> list = Collections.synchronizedList(new ArrayList<>());
        int countOfTasks = 100;
        for (int i = 0; i < countOfTasks; i++) {
            taskManager.runActionAtTime(action(list, i), executionTime);
            Thread.sleep(1);
        }
        Thread.sleep(countOfTasks*10 + 1000);
        assertEquals(Integer.valueOf(list.size()), Integer.valueOf(countOfTasks));
        for (int i = 0; i < countOfTasks; i++) {
            assertEquals(list.get(i), Integer.valueOf(i));
        }
    }

    private Callable action(List<Integer> list, Integer number) {
        return () -> {
            list.add(number);
            return null;
        };
    }


}
