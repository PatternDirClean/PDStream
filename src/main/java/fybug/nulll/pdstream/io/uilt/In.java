package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>读取工具实现.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @see AsnyIn
 * @since uilt 0.0.1
 */
@Accessors( fluent = true, chain = true )
@RequiredArgsConstructor
public abstract
class In<O extends Closeable, T> implements InorOut<In<O, T>> {
    /** 读取对象 */
    protected final O o;

    // 是否同时关闭
    boolean needClose = false;
    // 数据类型
    private final Class<T> Tcla;

    /** 异常处理接口 */
    @Setter Consumer<IOException> exception = e -> {throw new RuntimeException();};

    //----------------------------------------------------------------------------------------------

    @NotNull
    public final
    In<O, T> close() {
        needClose = true;
        return this;
    }

    //----------------------------------------------------------------------------------------------

    /** 切换为异步操作 */
    @NotNull
    public final
    AsnyIn<O, T> asny() { return asny(null); }

    /**
     * 切换为异步操作
     *
     * @param pool 运行用线程池
     */
    @NotNull
    public final
    AsnyIn<O, T> asny(ExecutorService pool) { return new AsnyIn<>(this, Tcla, pool); }

    //----------------------------------------------------------------------------------------------

    /**
     * 进行读取
     *
     * @return 读取的数据
     */
    @Nullable
    public final
    T read() {
        try {
            // 读取
            return read0(o);
        } catch ( IOException e ) {

            // 处理异常
            exception.accept(e);
            return null;

        } finally {
            /* 检查关闭 */
            if (needClose)
                try {
                    o.close();
                } catch ( IOException ignored ) {
                }
        }
    }

    /** 读取实现 */
    @NotNull
    protected abstract
    T read0(@NotNull O o) throws IOException;
}
