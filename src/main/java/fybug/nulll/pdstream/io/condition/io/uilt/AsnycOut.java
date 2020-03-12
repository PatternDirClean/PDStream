package fybug.nulll.pdstream.io.condition.io.uilt;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import fybug.nulll.pdstream.io.condition.io.AsnycRun;

/**
 * <h2>异步输出工具.</h2>
 * <p>
 * 使用 {@link SyncOut} 工具进行输出，可使用接口监听输出结果
 * 接口在输出完成后调用，传入当前是否成功
 *
 * @author fybug
 * @version 0.0.1
 * @see AsnycRun
 * @see SyncOut
 * @since uilt 0.0.1
 */
public
class AsnycOut extends AsnycRun<AsnycOut> implements Flushable {
    /** 输出用 */
    private SyncOut out;

    /*--------------------------------------------------------------------------------------------*/

    public
    AsnycOut(@NotNull List<Closeable> streams, @NotNull List<byte[]> datas)
    { this(streams, datas, ""); }

    public
    AsnycOut(@NotNull List<Closeable> streams, @NotNull List<byte[]> datas, @NotNull String append)
    { out = new SyncOut(streams, datas, append); }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    public
    AsnycOut filtrerBytes(Function<byte[], byte[]>... function) {
        out.filtrerBytes(function);
        return this;
    }

    @NotNull
    @Override
    public
    AsnycOut filtrerChars(Function<String, String>... Function) {
        out.filtrerChars(Function);
        return this;
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    protected
    void close0() {
        out.close();
    }

    @Override
    public
    void flush() { appendRun(() -> out.flush()); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 输出指定数据
     * <p>
     * 使用回调监听输出状态
     *
     * @param data 输出的数据
     * @param fun  状态监听
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut bytes(@NotNull byte[] data, @NotNull Consumer<Boolean> fun) {
        appendRun(() -> fun.accept(out.bytes(data)));
        return this;
    }

    /**
     * 输出指定数据
     * <p>
     * 使用回调监听输出状态
     *
     * @param data 输出的数据
     * @param fun  状态监听
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut chars(@NotNull String data, @NotNull Consumer<Boolean> fun) {
        appendRun(() -> fun.accept(out.chars(data)));
        return this;
    }

    /**
     * 输出绑定的数据
     * <p>
     * 使用回调监听输出状态
     *
     * @param fun 状态监听
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut echoData(@NotNull Consumer<Boolean> fun) {
        appendRun(() -> fun.accept(out.echoData()));
        return this;
    }

    /*-------------------------------------*/

    /**
     * 输出指定数据
     *
     * @param data 输出的数据
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut bytes(@NotNull byte[] data) { return bytes(data, v -> {}); }

    /**
     * 输出指定数据
     *
     * @param data 输出的数据
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut chars(@NotNull String data) { return chars(data, v -> {}); }

    /**
     * 输出绑定的数据
     *
     * @return this
     */
    @NotNull
    public
    AsnycOut echoData() { return echoData(v -> {}); }
}
