package fybug.nulll.pdstream.io;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <h2>PDStream 配置对象.</h2>
 *
 * @author fybug
 * @version 0.0.1
 */
public final
class OPC {
    // 无流可读取的值
    public static volatile String CHAR_EMPTY_DATA = "";
    public static volatile byte[] BYTE_EMPTY_DATA = {};
    // 转化过程中的缓冲区大小
    public static volatile int CHAR_READ_BUFF = 1024;
    public static volatile int BYTE_READ_BUFF = 255;
    /** 字节转字符的编码 */
    public static volatile Charset CHARSET = StandardCharsets.UTF_8;

    // 空流
    public final static BufferedReader EMPY_BUFF_READ = new BufferedReader(Reader.nullReader());
    public final static BufferedInputStream EMPY_BUFF_INPUT =
            new BufferedInputStream(InputStream.nullInputStream());
    public final static BufferedWriter EMPY_BUFF_WRITER = new BufferedWriter(Writer.nullWriter());
    public final static BufferedOutputStream EMPY_BUFF_OUTPUT =
            new BufferedOutputStream(OutputStream.nullOutputStream());
}
