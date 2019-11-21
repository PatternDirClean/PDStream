package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import fybug.nulll.pdstream.OutOfStream;

/**
 * <h2>作用于字节的写入器.</h2>
 * 操作对象为 {@link OutputStream}
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
public
class OutByte implements OutOfStream<OutputStream, byte[]> {
    /** 操作目标 */
    @NotNull private Optional<OutputStream> target;

    /** 空的操作器 */
    public
    OutByte() {target = Optional.empty();}

    /**
     * 初始化操作器
     *
     * @param outputsteam 初始操作目标
     */
    public
    OutByte(@Nullable OutputStream outputsteam) {target = Optional.ofNullable(outputsteam);}

    @Override
    public
    boolean write(@Nullable byte[] data, int len) {
        if (data == null || len < 0)
            return false;
        else if (len == 0 || data.length == 0)
            return true;

        try {
            synchronized ( this ){
                original().write(data, 0, Math.min(data.length, len));
            }
        } catch ( IOException e ) {
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public synchronized
    OutByte.Buffof append(@Nullable byte[] bytes) { return new Buffof(original(), bytes); }

    @Override
    @NotNull
    public
    OutByte bin(@Nullable OutputStream operator) {
        synchronized ( this ){
            target = Optional.ofNullable(operator);
        }
        return this;
    }

    @Override
    @NotNull
    public
    OutputStream original() { return target.orElse(OutputStream.nullOutputStream()); }

    /**
     * <h2>字节缓存桥接.</h2>
     * 所有通过 {@link #append(byte[])} 写入的数据将会缓存直到执行 {@link #close()} | {@link #flush()}
     *
     * @author fybug
     * @version 0.0.1
     * @since OutByte 0.0.1
     */
    public final static
    class Buffof extends Buff<OutputStream, byte[]> {
        private
        Buffof(OutputStream out, byte[] data) { super(out, data); }

        @Override
        protected
        boolean check(@Nullable byte[] data) { return super.check(data) && data.length != 0; }

        @Override
        protected
        void flush0(@NotNull byte[] data) throws Exception { tagre.write(data); }
    }
}
