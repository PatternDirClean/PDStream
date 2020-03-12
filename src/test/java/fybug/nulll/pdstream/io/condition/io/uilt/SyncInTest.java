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
class SyncInTest {
    private SyncIn in;

    @Before
    public
    void befo() {
        in = IOT.R(new ByteArrayInputStream(testdata.getBytes()))
                .append(new CharArrayReader(testdata.toCharArray()))
                .sync();
    }

    @After
    public
    void tearDown() { in.close(); }

    @Test
    public
    void bytes()
    { Assert.assertEquals(new String(in.bytes(), OPC.CHARSET), testdata + testdata); }

    @Test
    public
    void chars()
    { Assert.assertEquals(in.chars(), testdata + testdata); }
}