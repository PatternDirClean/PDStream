package fybug.nulll.pdstream.io;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static fybug.nulll.pdstream.RunTest.testdata;

public
class ReaderInputStreamTest {
    private ReaderInputStream stream;

    @Before
    public
    void setUp() { stream = new ReaderInputStream(new StringReader(testdata)); }

    @After
    public
    void tearDown() { stream.close(); }

    @Test
    public
    void read() throws IOException
    { Assert.assertArrayEquals(stream.readAllBytes(), testdata.getBytes()); }
}