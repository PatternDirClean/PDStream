package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <h2>读写操作器.</h2>
 * <p>
 * 提供基础操作
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.3
 */
@SuppressWarnings( "unchecked" )
@RequiredArgsConstructor
@AllArgsConstructor( access = AccessLevel.PACKAGE )
public abstract
class IOtool<U extends IOtool<U, T>, T> implements InorOut<IOtool<U, T>> {
    /** 操作对象 */
    protected final Closeable o;
    // 数据类型
    private final Class<T> Tcla;

    /** 是否同时关闭 */
    protected boolean needClose = false;
    /** 异常处理接口 */
    protected Consumer<IOException> exception = e -> {throw new RuntimeException();};

    //----------------------------------------------------------------------------------------------

    @NotNull
    public final
    U close() {
        needClose = true;
        return (U) this;
    }

    @NotNull
    public
    U exception(@NotNull Consumer<IOException> run) {
        exception = run;
        return (U) this;
    }

    //----------------------------------------------------------------------------------------------

    /** 切换为异步操作 */
    @NotNull
    public abstract
    <A extends AsyncTool<A>> A async();

    /**
     * 切换为异步操作
     *
     * @param pool 运行用线程池
     */
    @NotNull
    public abstract
    <A extends AsyncTool<A>> A async(@Nullable ExecutorService pool);

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步操作器.</h2>
     *
     * @author fybug
     * @version 0.0.1
     * @since IOtool 0.0.1
     */
    @RequiredArgsConstructor
    @Accessors( fluent = true, chain = true )
    public abstract
    class AsyncTool<K extends AsyncTool<K>> implements InorOut<K> {
        /** 执行用线程池 */
        @Nullable protected final ExecutorService pool;

        //------------------------------------------------------------------------------------------

        @NotNull
        public final
        K close() {
            IOtool.this.close();
            return (K) this;
        }

        @NotNull
        public final
        K exception(@NotNull Consumer<IOException> run) {
            IOtool.this.exception(run);
            return (K) this;
        }
    }
}
