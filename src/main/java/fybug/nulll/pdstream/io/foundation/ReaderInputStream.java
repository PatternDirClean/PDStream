package fybug.nulll.pdstream.io.foundation;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import fybug.nulll.pdstream.io.OPC;
import fybug.nulll.pdstream.strem.IOT;

import static fybug.nulll.pdstream.io.OPC.BYTE_READ_BUFF;

/**
 * <h2>字符转字节读取流.</h2>
 * <p>
 * 使用 {@link ByteBuffer} 作为转换中转区
 * 读取一段长度为 {@link OPC#BYTE_READ_BUFF} 的字符，并转化为字节载入缓冲区中
 * <p>
 * 在每次读取的时候都会检查缓冲区中是否有数据
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.2
 */
public
class ReaderInputStream extends InputStream {
    private InString reader;
    private ByteBuffer nowbuff = ByteBuffer.wrap(new byte[0]);

    /*-------------------------------------------------------------------------------------------*/

    public
    ReaderInputStream(@NotNull Reader reader)
    { this.reader = new InString(IOT.toBuffRead(reader)); }

    /*-------------------------------------------------------------------------------------------*/

    @Override
    public
    int read() {
        if (canread())
            return nowbuff.get();
        return -1;
    }

    private
    boolean canread() {
        if (nowbuff == null)
            return false;
        if (!nowbuff.hasRemaining()) {
            var chars = reader.read(BYTE_READ_BUFF);

            // 读取失败
            if (chars == null || chars.length() == 0)
                return false;

            nowbuff = ByteBuffer.wrap(chars.getBytes());
        }
        return true;
    }

    /*-------------------------------------------------------------------------------------------*/

    @Override
    public
    void close() {
        reader.close();
        nowbuff = null;
    }
}
