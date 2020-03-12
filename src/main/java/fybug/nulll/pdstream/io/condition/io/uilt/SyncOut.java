package fybug.nulll.pdstream.io.condition.io.uilt;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fybug.nulll.pdstream.io.OPT;
import fybug.nulll.pdstream.io.OutOf;
import fybug.nulll.pdstream.io.condition.io.HasFiltrer;

import static fybug.nulll.pdstream.io.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.io.OPC.CHAR_EMPTY_DATA;

/**
 * <h2>阻塞式输出工具.</h2>
 * <p>
 * 可指定每次输出后追加的数据
 * 输出时将传入的数据使用过滤器过滤后输出
 * 过滤后结果等同于 {@link OPT#BYTE_EMPTY_DATA} || {@link OPT#CHAR_EMPTY_DATA} 时不会输出
 * 使用 {@link #echoData()} 输出的数据不会经过过滤
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.1
 */
@SuppressWarnings( "all" )
public
class SyncOut extends HasFiltrer<SyncOut> implements Flushable {
    /** 数据集合 */
    private final List<byte[]> dataarray;

    // 追加数据
    private final String appendDara;
    private final byte[] appendbase;

    /*--------------------------------------------------------------------------------------------*/

    public
    SyncOut(@NotNull List<Closeable> streams, @NotNull List<byte[]> datas)
    { this(streams, datas, ""); }

    public
    SyncOut(@NotNull List<Closeable> streams, @NotNull List<byte[]> datas, @NotNull String append) {
        super(streams);
        dataarray = datas;
        appendDara = append;
        appendbase = append.getBytes();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 输出一段字节数据
     *
     * @param data 要输出的数据
     *
     * @return 是否成功
     */
    public
    boolean bytes(@NotNull byte[] data) {
        return (boolean) runofStream(streams -> {
            if (streams.size() == 0)
                return true;

            // 过滤数据
            var da = byteF(data);
            if (Arrays.equals(da, BYTE_EMPTY_DATA))
                return true;

            return isSuccessful(unifiedOutput(streams.stream(), true)
                                        // 写入两次
                                        .map(v -> v.write(da) && v.writeFlush(appendbase)));
        });
    }

    /**
     * 输出一段字符数据
     *
     * @param data 要输出的数据
     *
     * @return 是否成功
     */
    public
    boolean chars(@NotNull String data) {
        return (boolean) runofStream(strems -> {
            if (strems.size() == 0)
                return true;

            var da = charF(data);
            if (Objects.equals(da, CHAR_EMPTY_DATA))
                return true;

            return isSuccessful(unifiedOutput(strems.stream(), false)
                                        // 写入两次
                                        .map(v -> v.write(data) && v.writeFlush(appendDara)));
        });
    }

    /**
     * 输出一遍指定的数据
     * <p>
     * 输出 {@link #dataarray} 每一个数据输出一次
     *
     * @return 是否成功
     */
    public
    boolean echoData() {
        return (boolean) runofStream(streams -> {
            if (streams.size() == 0)
                return true;

            var stream = unifiedOutput(streams.stream(), true).toArray(OutOf[]::new);

            var suerrs = isSuccessful(
                    // 写入全部
                    Arrays.stream(stream)
                          .map(v -> isSuccessful(dataarray.stream()
                                                          .map(d -> v.write(d) &&
                                                                    v.writeFlush(appendbase)))));

            return suerrs;
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public
    void flush() {
        runofStream(stream -> {
            stream.forEach(v -> {
                try {
                    ((Flushable) v).flush();
                } catch ( IOException ignored ) {
                }
            });
            return null;
        });
    }

    @Override
    public
    void close() {
        flush();
        super.close();
        dataarray.clear();
    }
}
