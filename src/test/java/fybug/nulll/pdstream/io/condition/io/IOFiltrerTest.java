package fybug.nulll.pdstream.io.condition.io;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import fybug.nulll.pdstream.io.OPC;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class IOFiltrerTest {
    private IOFiltrer<?> filtrer;

    @Before
    public
    void setUp() {
        filtrer = new IOFiltrer<>() {};
        filtrer.filtrerBytes(v -> new String(v, StandardCharsets.UTF_8).concat("a")
                                                                       .getBytes(), v -> {
            Assert.assertEquals(testdata.concat("a"), new String(v, OPC.CHARSET));
            return v;
        });
        filtrer.filtrerChars(v -> v + "a", v -> {
            Assert.assertEquals(testdata.concat("a"), v);
            return v;
        });
    }

    @After
    public
    void tearDown() { filtrer.close(); }

    @Test
    public
    void byteF() {
        Assert.assertEquals(new String(filtrer.byteF(testdata.getBytes()), OPC.CHARSET), testdata + "a");
    }

    @Test
    public
    void charF() { Assert.assertEquals(filtrer.charF(testdata), testdata + "a"); }
}