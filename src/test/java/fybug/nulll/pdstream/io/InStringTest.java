package fybug.nulll.pdstream.io;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

import fybug.nulll.pdstream.OperatorTOOL;

public
class InStringTest {
    private final InString TESTMODULE = new InString();
    private final String TESTDATA = "asd\nfdgfgdfg\nzxcwerd";

    @Before
    public
    void setUp() { TESTMODULE.bin(new StringReader(TESTDATA)); }

    @Test
    public
    void readLine() {
        assert TESTDATA.substring(0, 3).equals(TESTMODULE.readLine());

        var list = OperatorTOOL.passOfLine(TESTDATA);
        list = list.subList(1, list.size());
        assert list.equals(Arrays.asList(TESTMODULE.readAllLine().toArray()));
    }

    @Test
    public
    void read() {
        var list = OperatorTOOL.passOfLine(TESTDATA);

        assert (list.get(0) + '\n').equals(TESTMODULE.read(list.get(0).length() + 1));
        assert (list.get(1) + '\n' + list.get(2)).equals(TESTMODULE.readAll());
    }
}