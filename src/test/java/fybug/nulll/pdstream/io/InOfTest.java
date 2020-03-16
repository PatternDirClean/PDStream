package fybug.nulll.pdstream.io;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import static fybug.nulll.pdstream.RunTest.pool;
import static fybug.nulll.pdstream.RunTest.testdata;

public
class InOfTest {
    private StringReader reader;

    @Before
    public
    void setUp() { reader = new StringReader(testdata); }

    @Test
    public
    void read() {
        Assert.assertEquals(InOf.read(reader, 10)
                                .exception(System.out::println)
                                .close()
                                .read(), testdata.substring(0, 10));
    }

    @Test
    public
    void testRead() throws ExecutionException, InterruptedException {
        Assert.assertEquals(InOf.read(reader, 10)
                                .async(pool)
                                .exception(System.out::println)
                                .close()
                                .read()
                                .get(), testdata.substring(0, 10));
    }
}