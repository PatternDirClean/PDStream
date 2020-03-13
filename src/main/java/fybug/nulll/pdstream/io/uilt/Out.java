package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>输出工具实现.</h2>
 * <p>
 * 可使用 {@link #append(T)} 进行链式写入，无论设置如何使用该操作都需要手动执行 {@link #flush()}
 *
 * @author fybug
 * @version 0.0.1
 * @see AsnyOut
 * @since uilt 0.0.1
 */
@Accessors( fluent = true, chain = true )
@RequiredArgsConstructor
public abstract
class Out<O extends Flushable & Closeable, T> implements Flushable, InorOut<Out<O, T>> {
    /** 输出对象 */
    protected final O o;

    /** 是否及时刷新 */
    final boolean needFlush;
    // 是否同时关闭
    boolean needClose = false;
    // 数据类型
    private final Class<T> Tcla;

    /** 异常处理接口 */
    @Setter protected Consumer<IOException> exception = e -> {throw new RuntimeException();};

    //----------------------------------------------------------------------------------------------

    @NotNull
    public final
    Out<O, T> close() {
        needClose = true;
        return this;
    }

    //----------------------------------------------------------------------------------------------

    /** 切换为异步操作 */
    @NotNull
    public final
    AsnyOut<O, T> asny() { return asny(null); }

    /**
     * 切换为异步操作
     *
     * @param pool 运行用线程池
     */
    @NotNull
    public final
    AsnyOut<O, T> asny(ExecutorService pool) { return new AsnyOut<>(this, Tcla, pool); }

    //----------------------------------------------------------------------------------------------

    /**
     * 进行写入
     *
     * @param data 输出的数据
     *
     * @return 是否成功
     */
    public final
    boolean write(@NotNull T data) {
        try {
            // 输出
            write0(o, data);
            return true;
        } catch ( IOException e ) {
            // 处理异常
            exception.accept(e);
            return false;
        } finally {
            if (needFlush)
                flush();
        }
    }

    /** 输出实现 */
    protected abstract
    void write0(@NotNull O o, @NotNull T data) throws IOException;

    //------------------

    /**
     * 追加写入
     *
     * @param data 输出的数据
     */
    @NotNull
    public final
    Out<O, T> append(@NotNull T data) {
        try {
            // 输出
            write0(o, data);
        } catch ( IOException e ) {
            // 处理异常
            exception.accept(e);
        }
        return this;
    }

    @Override
    public
    void flush() {
        try {
            o.flush();
        } catch ( IOException e ) {
            exception.accept(e);
        } finally {
            /* 检查关闭 */
            if (needClose)
                try {
                    o.close();
                } catch ( IOException ignored ) {
                }
        }
    }
}
