package fybug.nulll.pdstream.strem.io.uilt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;

import fybug.nulll.pdstream.strem.IOT;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class AsnycOutTest {
    private AsnycOut out;
    private ByteArrayOutputStream bytestream;
    private CharArrayWriter charstream;

    @Before
    public
    void setUp() {
        bytestream = new ByteArrayOutputStream();
        charstream = new CharArrayWriter();
        out = IOT.O(bytestream).binData(testdata).append(charstream).async();
    }

    @After
    public
    void tearDown() { out.close(); }

    @Test
    public
    void bytes() throws InterruptedException {
        out.bytes(testdata.getBytes()).flush();

        Thread.sleep(300);

        Assert.assertArrayEquals(bytestream.toByteArray(), testdata.getBytes());
        Assert.assertEquals(charstream.toString(), testdata);
    }

    @Test
    public
    void chars() throws InterruptedException {
        out.chars(testdata).flush();

        Thread.sleep(300);

        Assert.assertArrayEquals(bytestream.toByteArray(), testdata.getBytes());
        Assert.assertEquals(charstream.toString(), testdata);
    }

    @Test
    public
    void echoData() throws InterruptedException {
        out.echoData().flush();

        Thread.sleep(300);

        Assert.assertArrayEquals(bytestream.toByteArray(), testdata.getBytes());
        Assert.assertEquals(charstream.toString(), testdata);
    }
}