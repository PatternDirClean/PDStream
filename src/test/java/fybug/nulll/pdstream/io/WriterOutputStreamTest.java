package fybug.nulll.pdstream.io;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.io.IOException;

import fybug.nulll.pdstream.io.foundation.WriterOutputStream;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class WriterOutputStreamTest {
    private CharArrayWriter buff;
    private WriterOutputStream stream;

    @Before
    public
    void setUp() {
        buff = new CharArrayWriter();
        stream = new WriterOutputStream(buff);
    }

    @After
    public
    void tearDown() { stream.close(); }

    @Test
    public
    void write() throws IOException {
        stream.write(testdata.getBytes());
        stream.flush();

        Assert.assertEquals(buff.toString(), testdata);
    }
}