package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;

import static fybug.nulll.pdstream.OPC.CHARSET;

/**
 * <h2>字节转字符输出流.</h2>
 * <p>
 * 使用 {@link ByteArrayOutputStream} 作为缓冲区，将所有数据缓存到缓冲区内
 * 在 {@link #flush()} 的时候全部输出
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.2
 */
public
class WriterOutputStream extends OutputStream {
    private OutString stream;
    private ByteArrayOutputStream buff = new ByteArrayOutputStream();

    /*-------------------------------------------------------------------------------------------*/

    public
    WriterOutputStream(@NotNull Writer writer) { stream = new OutString(writer); }

    /*-------------------------------------------------------------------------------------------*/

    @Override
    public
    void write(int b) {
        synchronized ( this ){
            buff.write(b);
        }
    }

    /*-------------------------------------------------------------------------------------------*/

    @Override
    public
    void flush() {
        byte[] bs;
        synchronized ( this ){
            bs = buff.toByteArray();
            buff = new ByteArrayOutputStream();
        }
        stream.writeFlush(new String(bs, CHARSET));
    }

    @Override
    public
    void close() {
        flush();
        stream.close();
    }
}
