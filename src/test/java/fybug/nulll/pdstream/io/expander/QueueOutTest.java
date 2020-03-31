package fybug.nulll.pdstream.io.expander;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;

import static fybug.nulll.pdstream.RunTest.testdata;
public
class QueueOutTest {
    private QueueOut queueOut;
    private StringWriter writer;

    @Before
    public
    void setUp() { queueOut = QueueOut.build().build((writer = new StringWriter())); }

    @After
    public
    void tearDown() { queueOut.close(); }

    @Test
    public
    void write() {
        queueOut.print(testdata, () -> {}, e -> e.printStackTrace(System.out));
        queueOut.print(testdata, () -> {}, e -> e.printStackTrace(System.out));

        while( queueOut.isWrite() )
            ;
        Assert.assertEquals(writer.toString(), testdata.concat(testdata));
    }
}