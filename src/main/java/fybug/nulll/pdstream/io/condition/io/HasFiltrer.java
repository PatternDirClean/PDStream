package fybug.nulll.pdstream.io.condition.io;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import fybug.nulll.pdstream.io.OutOf;
import fybug.nulll.pdstream.io.foundation.OutByte;
import fybug.nulll.pdstream.io.foundation.OutString;
import fybug.nulll.pdstream.io.foundation.ReaderInputStream;
import fybug.nulll.pdstream.io.foundation.WriterOutputStream;

/**
 * <h2>带数据过滤器的操作器.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
public abstract
class HasFiltrer<O extends HasFiltrer<?>> extends IOFiltrer<O> {
    /** 流集合 */
    private final List<Closeable> streams;

    /*--------------------------------------------------------------------------------------------*/

    protected
    HasFiltrer(List<Closeable> streams) { this.streams = streams; }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 处理流
     * <p>
     * 使用接口进行处理，内部使用锁
     *
     * @param function 处理流的代码
     */
    public synchronized
    Object runofStream(Function<List<Closeable>, Object> function) {
        if (isClose())
            return null;
        return function.apply(getStreams());
    }

    /*----------------------------------*/

    /**
     * 统一流的类型
     * <p>
     * 转化为字符或字节流
     *
     * @param stream 要转化的流集合
     * @param bytes  是否转化为字节流
     *
     * @return param1
     */
    public static
    Stream<Closeable> unifiedInput(Stream<Closeable> stream, boolean bytes) {
        return stream.map(v -> {
            if (bytes && v instanceof Reader)
                return new ReaderInputStream((Reader) v);
            else if (!bytes && v instanceof InputStream)
                return new InputStreamReader((InputStream) v);

            return v;
        }).distinct();
    }

    /**
     * 统一流的类型
     * <p>
     * 转化为字符或字节流
     *
     * @param stream 要转化的流集合
     * @param bytes  是否转化为字节流
     *
     * @return param1
     */
    protected static
    Stream<OutOf> unifiedOutput(Stream<Closeable> stream, boolean bytes) {
        return (stream).map(v -> {
            if (bytes && v instanceof OutString)
                return new OutByte(new WriterOutputStream(((OutString) v).original()));
            else if (!bytes && v instanceof OutByte)
                return new OutString(new OutputStreamWriter(((OutByte) v).original()));

            return (OutOf) v;
        });
    }

    /*----------------------------------*/

    /**
     * 获取流集合
     *
     * @return streams
     */
    protected
    List<Closeable> getStreams() {return streams;}

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public
    void close() {
        if (!isClose())
            runofStream(stream -> {
                markClose();
                stream.forEach(v -> {
                    try {
                        v.close();
                    } catch ( IOException ignored ) {
                    }
                });
                // clear
                streams.clear();
                super.close();

                return null;
            });
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 检查读取结果 */
    protected static
    boolean isSuccessful(Stream<Boolean> stream) { return stream.reduce((a, b) -> a || b).get(); }
}
