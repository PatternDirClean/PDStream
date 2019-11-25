package fybug.nulll.pdstream.io;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;

import fybug.nulll.pdstream.OperatorTOOL;
public
class OutStringTest {
    private final String TESTDATA = "asdas\nasdjhkzx\nuoier";
    private final OutString TESTMODULE = new OutString();
    private StringWriter out;

    @Before
    public
    void setUp() {
        out = new StringWriter();
        TESTMODULE.bin(out);
    }

    @Test
    public
    void writeLine() {
        var list = OperatorTOOL.passOfLine(TESTDATA);

        TESTMODULE.writeLine(list.get(0));
        assert out.toString().equals(list.get(0) + System.lineSeparator());
    }

    @Test
    public
    void write() {
        TESTMODULE.write(TESTDATA, 5);
        TESTMODULE.flush();
        assert TESTDATA.substring(0, 5).equals(out.toString());

        TESTMODULE.write_And_Flush(TESTDATA);
        assert TESTDATA.substring(0, 5).concat(TESTDATA).equals(out.toString());
    }

    @Test
    public
    void append() {
    }
}