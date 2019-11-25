package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import fybug.nulll.pdstream.InOfStream;

/**
 * <h2>作用于字节的读取器.</h2>
 * 操作对象为 {@link InputStream}
 *
 * @author fybug
 * @version 0.0.1
 * @see InOfStream#EMPY_BUFF_INPUT
 * @see InOfStream#ofBuffStream(InputStream)
 * @since io 0.0.1
 */
public
class InByte implements InOfStream<InputStream, byte[]> {
    /** 操作目标 */
    @NotNull private BufferedInputStream target;

    /** 空的操作器 */
    public
    InByte() { target = InOfStream.EMPY_BUFF_INPUT; }

    /**
     * 初始化操作器
     *
     * @param inputStream 初始操作目标
     */
    public
    InByte(@Nullable InputStream inputStream) {target = InOfStream.ofBuffStream(inputStream);}

    @Override
    @Nullable
    public
    byte[] read(int size) {
        /* check:指定长度是否超过位置 */
        if (size < 0)
            return null;
        else if (size == 0)
            return new byte[0];
        /* // check */

        byte[] bytes;

        try {
            synchronized ( this ){
                bytes = original().readNBytes(size);
            }
        } catch ( IOException e ) {
            return null;
        }

        if (bytes.length == 0)
            return null;
        return bytes;
    }

    @NotNull
    @Override
    public
    InByte bin(@Nullable InputStream operator) {
        synchronized ( this ){
            target = InOfStream.ofBuffStream(operator);
        }
        return this;
    }

    @NotNull
    @Override
    public
    InputStream original() { return target; }
}
