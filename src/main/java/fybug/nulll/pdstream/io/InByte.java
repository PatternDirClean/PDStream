package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import fybug.nulll.pdstream.InOfStream;

/**
 * <h2>作用于字节的读取器.</h2>
 * 操作对象为 {@link InputStream}
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
public
class InByte implements InOfStream<InputStream, byte[]> {
    /** 操作目标 */
    @NotNull private Optional<InputStream> target;

    /** 空的操作器 */
    public
    InByte() { target = Optional.empty(); }

    /**
     * 初始化操作器
     *
     * @param inputStream 初始操作目标
     */
    public
    InByte(@Nullable InputStream inputStream) {target = Optional.ofNullable(inputStream);}

    @Override
    @Nullable
    public
    byte[] read(int size) {
        if (size < 0)
            return null;
        else if (size == 0)
            return new byte[0];

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
            target = Optional.ofNullable(operator);
        }
        return this;
    }

    @NotNull
    @Override
    public
    InputStream original() { return target.orElse(InputStream.nullInputStream()); }
}
