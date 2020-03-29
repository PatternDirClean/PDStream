package fybug.nulll.pdstream.io.uilt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class PushChannelTest {
    private PushChannel channel;
    private StringWriter writer;

    @Before
    public
    void setUp() {
        writer = new StringWriter();
    }

    @After
    public
    void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public
    void sync() throws IOException {
        channel = PushChannel.build().point(writer).sync();
        channel.append(testdata);
        Assert.assertEquals(testdata, writer.toString());
        channel.append(testdata);
        Assert.assertEquals(testdata + testdata + testdata, writer.toString());
    }

    @Test
    public
    void timing() throws IOException, InterruptedException {
        channel = PushChannel.build().point(writer).timing(1000);
        channel.append(testdata);
        Assert.assertEquals("", writer.toString());
        channel.append(testdata);
        Thread.sleep(1500);
        Assert.assertEquals(testdata + testdata, writer.toString());
    }

    @Test
    public
    void buffer() throws IOException {
        channel = PushChannel.build().point(writer).buffer(testdata.length());
        channel.append(testdata);
        Assert.assertEquals(testdata, writer.toString());
        channel.append(testdata);
        Assert.assertEquals(testdata + testdata + testdata, writer.toString());
    }
}