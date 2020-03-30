package fybug.nulll.task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import fybug.nulll.pdstream.RunTest;

public
class TaskQueueTest {
    TaskQueue tasks = new TaskQueue();
    StringWriter writer;
    int id;

    @Before
    public
    void setUp() {
        id = tasks.addQueue(RunTest.pool);
        writer = new StringWriter();
    }

    @After
    public
    void tearDown() throws IOException {
        tasks.close(() -> {}, id);
        writer.close();
    }

    @Test
    public
    void addtask() throws InterruptedException {
        var s = tasks.addtask(() -> {
            try {
                Thread.sleep(1000);
            } catch ( InterruptedException ignored ) {
            }
            writer.write("task");
        }, id);
        Thread.sleep(600);
        writer.write("run");
        s.sync();
        writer.write("pring");

        Assert.assertEquals(writer.toString(), "runtaskpring");
    }

    @Test
    public
    void addtask2() throws InterruptedException {
        var s = tasks.addtask(() -> {
            try {
                Thread.sleep(1000);
            } catch ( InterruptedException ignored ) {
            }
            writer.write("task");
        }, id);
        Thread.sleep(1500);
        writer.write("run");
        s.sync();
        writer.write("pring");

        Assert.assertEquals(writer.toString(), "taskrunpring");
    }
}