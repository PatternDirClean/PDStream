package fybug.nulll.pdstream.strem.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import fybug.nulll.pdstream.io.InByte;
import fybug.nulll.pdstream.io.InString;
import fybug.nulll.pdstream.strem.io.AsnycRun;
import fybug.nulll.pdstream.strem.io.HasFiltrer;

import static fybug.nulll.pdstream.OPC.BYTE_DEFAULT_DATA;
import static fybug.nulll.pdstream.OPC.CHAR_DEFAULT_DATA;

/**
 * <h2>异步读取器.</h2>
 * <p>
 * 使用 {@link SyncIn} 读取数据
 * 使用回调进行数据处理
 * 使用分段读取 {@link #subBytes(Consumer)} 和 {@link #subChars(Consumer)} 函数不会使用过滤器
 *
 * @author fybug
 * @version 0.0.1
 * @see SyncIn
 * @see AsnycRun
 * @since uilt 0.0.1
 */
public
class AsnycIn extends AsnycRun<AsnycIn> {
    /** 读取用 */
    private final SyncIn in;

    /*--------------------------------------------------------------------------------------------*/

    public
    AsnycIn(@NotNull List<Closeable> streams) {
        in = new SyncIn(streams);
        threadPool(Executors.newSingleThreadExecutor());
    }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    public
    AsnycIn filtrerBytes(Function<byte[], byte[]>... function) {
        in.filtrerBytes(function);
        return this;
    }

    @NotNull
    @Override
    public
    AsnycIn filtrerChars(Function<String, String>... Function) {
        in.filtrerChars(Function);
        return this;
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    protected
    void close0() { in.close(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取全部字节数据
     *
     * @param callback 回调接口
     *
     * @return this
     */
    @NotNull
    public
    AsnycIn bytes(@NotNull Consumer<@Nullable byte[]> callback) {
        appendRun(() -> callback.accept(in.bytes()));
        return this;
    }

    /**
     * 获取全部字符数据
     *
     * @param callback 回调接口
     *
     * @return this
     */
    @NotNull
    public
    AsnycIn chars(@NotNull Consumer<@Nullable String> callback) {
        appendRun(() -> callback.accept(in.chars()));
        return this;
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 分段获取全部字节数据
     * <p>
     * 在有多个流需要读取情况下，在每个流中的数据读取完成后都会触发回调
     *
     * @param callback 回调接口
     *
     * @return this
     */
    @NotNull
    public
    AsnycIn subBytes(@NotNull Consumer<@Nullable byte[]> callback) {
        appendRun(() -> in.runofStream(streams -> {
            // 检查长度
            if (streams.size() == 0)
                callback.accept(BYTE_DEFAULT_DATA);

            // 读取
            HasFiltrer.unifiedInput(streams.stream(), true)
                      .forEach(v -> callback.accept(new InByte((InputStream) v).readAll()));

            return null;
        }));
        return this;
    }

    /**
     * 分段获取全部字符数据
     * <p>
     * 在有多个流需要读取情况下，在每个流中的数据读取完成后都会触发回调
     *
     * @param callback 回调接口
     *
     * @return this
     */
    @NotNull
    public
    AsnycIn subChars(@NotNull Consumer<@Nullable String> callback) {
        appendRun(() -> in.runofStream(streams -> {
            // 检查长度
            if (streams.size() == 0)
                callback.accept(CHAR_DEFAULT_DATA);

            // 读取
            HasFiltrer.unifiedInput(streams.stream(), false)
                      .forEach(v -> callback.accept(new InString((Reader) v).readAll()));

            return null;
        }));
        return this;
    }
}
