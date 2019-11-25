package fybug.nulll.pdstream.io;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public
class OutByteTest {
    private final OutByte TESTMODEL = new OutByte();
    private final String TESTDATA = "asdzxcsdfas";
    private ByteArrayOutputStream out;

    @Before
    public
    void setUp() {
        out = new ByteArrayOutputStream();
        TESTMODEL.bin(out);
    }

    @Test
    public
    void write() {
        TESTMODEL.write(TESTDATA.getBytes(), TESTDATA.substring(0, 3).getBytes().length);
        TESTMODEL.flush();
        assert Arrays.equals(TESTDATA.substring(0, 3).getBytes(), out.toByteArray());

        TESTMODEL.write_And_Flush(TESTDATA.getBytes());
        assert Arrays.equals(TESTDATA.substring(0, 3).concat(TESTDATA).getBytes(),
                             out.toByteArray());
    }

    @Test
    public
    void append() {
        TESTMODEL.append("asd".getBytes())
                 .append("zxc".getBytes())
                 .append("sdfas".getBytes())
                 .flush();
        assert Arrays.equals(TESTDATA.getBytes(), out.toByteArray());
    }
}