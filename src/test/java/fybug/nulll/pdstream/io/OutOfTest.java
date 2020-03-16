package fybug.nulll.pdstream.io;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import static fybug.nulll.pdstream.RunTest.pool;
import static fybug.nulll.pdstream.RunTest.testdata;
import static org.junit.Assert.*;
public
class OutOfTest {
    private StringWriter writer;

    @Before
    public
    void setUp() { writer = new StringWriter(); }

    @Test
    public
    void write() {
        OutOf.write(writer).exception(System.out::println).append(testdata).flush();
        Assert.assertEquals(writer.toString(), testdata);
    }

    @Test
    public
    void testWrite() throws ExecutionException, InterruptedException {
        OutOf.write(writer)
             .exception(System.out::println)
             .async(pool)
             .append(testdata)
             .write()
             .get();
        Assert.assertEquals(writer.toString(), testdata);
    }
}