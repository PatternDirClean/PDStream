package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * <h2>面向流的读取操作工具.</h2>
 * 该类操作器用于操作流
 * <p>
 * {@link #read(int)} 在不同的流作为操作对象时会有语义上的差别，请查看实现文档
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public
interface InOfStream<O extends Closeable, D> extends Operator<O> {
    /** null BuffReader */
    BufferedReader EMPY_BUFF_READ = new BufferedReader(Reader.nullReader());
    /** null BuffInput */
    BufferedInputStream EMPY_BUFF_INPUT = new BufferedInputStream(InputStream.nullInputStream());

    /**
     * 从操作对象中读取所有的数据
     *
     * @return all data,can`t read will to {@code null}
     */
    @Nullable
    default
    D readAll() { return read(Integer.MAX_VALUE); }

    /** run task of {@link #readAll()} and {@link #close()} */
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
     * 从操作对象中读取指定数量的数据
     *
     * @param size data`s size
     *
     * @return data, can`t read and {@code size < 0} will to {@code null}
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

    /**
     * 转化为缓冲实现流
     *
     * @param reader input
     *
     * @return {@link #EMPY_BUFF_INPUT} of param is null
     */
    @NotNull
    static
    BufferedInputStream ofBuffStream(@Nullable InputStream reader) {
        if (reader == null)
            return EMPY_BUFF_INPUT;
        if (reader instanceof BufferedInputStream)
            return (BufferedInputStream) reader;
        return new BufferedInputStream(reader);
    }

    /**
     * 转化为缓冲实现流
     *
     * @param reader reader
     *
     * @return {@link #EMPY_BUFF_READ} of param is null
     */
    @NotNull
    static
    BufferedReader ofBuffStream(@Nullable Reader reader) {
        if (reader == null)
            return EMPY_BUFF_READ;
        if (reader instanceof BufferedReader)
            return (BufferedReader) reader;
        return new BufferedReader(reader);
    }
}
