package fybug.nulll.pdstream.io;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public
class InByteTest {
    private final InByte TESTMODEL = new InByte();
    private final String TESTDATA = "asdzxcsdfas";

    @Before
    public
    void setUp() { TESTMODEL.bin(new ByteArrayInputStream(TESTDATA.getBytes())); }

    @Test
    public
    void read() {
        assert new String(TESTMODEL.read(TESTDATA.substring(0, 3).getBytes().length), UTF_8).equals(
                TESTDATA.substring(0, 3));
        assert TESTDATA.substring(3).equals(new String(TESTMODEL.readAll(), UTF_8));
    }
}