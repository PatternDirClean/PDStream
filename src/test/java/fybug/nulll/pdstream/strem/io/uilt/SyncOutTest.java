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
class SyncOutTest {
    private SyncOut out;
    private ByteArrayOutputStream bytestream;
    private CharArrayWriter charstream;

    @Before
    public
    void setUp() {
        bytestream = new ByteArrayOutputStream();
        charstream = new CharArrayWriter();
        out = IOT.O(bytestream).append(charstream).binData(testdata).sync();
    }

    @After
    public
    void tearDown() {
        out.close();
        Assert.assertArrayEquals(bytestream.toByteArray(), testdata.getBytes());
        Assert.assertEquals(charstream.toString(), testdata);
    }

    @Test
    public
    void bytes() { out.bytes(testdata.getBytes()); }

    @Test
    public
    void chars() { out.chars(testdata); }

    @Test
    public
    void echoData() { out.echoData(); }
}