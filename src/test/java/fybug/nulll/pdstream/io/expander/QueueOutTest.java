package fybug.nulll.pdstream.io.expander;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;

import static fybug.nulll.pdstream.RunTest.pool;
import static fybug.nulll.pdstream.RunTest.testdata;

public
class QueueOutTest {
    private StringWriter writer;
    private QueueOut<CharSequence> queueOut;

    @Before
    public
    void setUp() {
        writer = new StringWriter();
        queueOut = QueueOut.of(writer);
    }

    @After
    public
    void tearDown() {
        queueOut.close();
    }

    @Test
    public
    void write() {
        queueOut.write(testdata, () -> {}, e -> e.printStackTrace(System.out));
        queueOut.write(testdata, () -> {}, e -> e.printStackTrace(System.out));

        while( queueOut.isWrite() )
            ;
        Assert.assertEquals(writer.toString(), testdata.concat(testdata));
    }
}