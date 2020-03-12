package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * <h2>读取工具.</h2>
 * 该类操作器用于读取数据
 * <p>
 * {@link #read(int)} 在不同的实现中会有语义上的差别，请查看文档
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public
interface InOf<O extends Closeable, D> extends Operator<O> {
    /**
     * 读取全部数据
     *
     * @return 读取的数据
     *
     * @see #read(int)
     */
    @Nullable
    default
    D readAll() throws IOException { return read(Integer.MAX_VALUE); }

    /**
     * 读取指定数量的数据
     *
     * @param size 数据的长度
     *
     * @return 读取的数据，无法读取或 {@code size < 0} 将会返回 {@code null}
     */
    @Nullable
    D read(int size) throws IOException;

    /*---------------------------*/

    /** 同时运行 {@link #readAll()} 和 {@link #close()} */
    @Nullable
    default
    D readAllClose() throws IOException {
        synchronized ( this ){
            try {
                return readAll();
            } finally {
                close();
            }
        }
    }

    /*--------------------------------------------------------------------------------------------*/

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
