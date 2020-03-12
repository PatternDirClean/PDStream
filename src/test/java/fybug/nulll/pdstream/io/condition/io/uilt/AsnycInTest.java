package fybug.nulll.pdstream.io.condition.io.uilt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;

import fybug.nulll.pdstream.io.OPC;
import fybug.nulll.pdstream.strem.IOT;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class AsnycInTest {
    private AsnycIn in;

    @Before
    public
    void befo() {
        in = IOT.R(new ByteArrayInputStream(testdata.getBytes()))
                .append(new CharArrayReader(testdata.toCharArray()))
                .async();
    }

    @After
    public
    void tearDown() throws InterruptedException {
        Thread.sleep(300);
        in.close();
    }

    @Test
    public
    void bytes()
    { in.bytes(v -> Assert.assertEquals(new String(v, OPC.CHARSET), testdata + testdata)); }

    @Test
    public
    void chars() { in.chars(v -> Assert.assertEquals(v, testdata + testdata)); }

    @Test
    public
    void subBytes() { in.subBytes(v -> Assert.assertArrayEquals(v, testdata.getBytes())); }

    @Test
    public
    void subChars() { in.subChars(v -> Assert.assertEquals(v, testdata)); }
}