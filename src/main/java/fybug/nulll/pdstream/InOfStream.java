package fybug.nulll.pdstream;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * <h2>面向流的读取操作工具.</h2>
 * 该类操作器用于操作流
 * <p>
 * {@link #read(int)} 在不同的流作为操作对象时会有语义上的差别，请查看文档
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public
interface InOfStream<O extends Closeable, D> extends Operator<O> {
    /**
     * 读取操作对象中的所有数据
     *
     * @return all data
     */
    @Nullable
    default
    D readAll() { return read(Integer.MAX_VALUE); }

    /** 同时执行 {@link #readAll()} 和 {@link #close()} */
    @Nullable
    default
    D readAll_And_Close() {
        synchronized ( this ){
            try {
                return readAll();
            } finally {
                close();
            }
        }
    }

    /**
     * 读取指定长度的数据
     *
     * @param size 数据长度
     *
     * @return data
     */
    @Nullable
    D read(int size);

    @Override
    default
    void close() {
        try {
            synchronized ( this ){
                original().close();
            }
        } catch ( IOException ignored ) {
        }
    }
}
