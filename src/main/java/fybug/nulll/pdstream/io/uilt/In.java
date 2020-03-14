package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>读取工具实现.</h2>
 *
 * @author fybug
 * @version 0.0.2
 * @see AsyncIn
 * @since uilt 0.0.1
 */
@Accessors( fluent = true, chain = true )
@RequiredArgsConstructor
@AllArgsConstructor( access = AccessLevel.PACKAGE )
public abstract
class In<O extends Closeable, T> implements InorOut<In<O, T>> {
    /** 读取对象 */
    @NonNull protected O o;

    // 数据类型
    private final Class<T> Tcla;
    // 是否同时关闭
    boolean needClose = false;

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
    AsyncIn async() { return async(null); }

    /**
     * 切换为异步操作
     *
     * @param pool 运行用线程池
     */
    @NotNull
    public final
    AsyncIn async(@Nullable ExecutorService pool) { return new AsyncIn(pool); }

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

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步读取实现.</h2>
     *
     * @author fybug
     * @version 0.0.1
     * @since In 0.0.2
     */
    @Accessors( fluent = true, chain = true )
    @AllArgsConstructor( access = AccessLevel.PACKAGE )
    public final
    class AsyncIn implements InorOut<AsyncIn> {
        // 执行用线程池
        @Nullable private final ExecutorService pool;

        //----------------------------------------------------------------------------------------------

        @NotNull
        public
        AsyncIn close() {
            In.this.close();
            return this;
        }

        @NotNull
        public
        AsyncIn exception(@NotNull Consumer<IOException> e) {
            In.this.exception(e);
            return this;
        }

        //----------------------------------------------------------------------------------------------

        /**
         * 进行读取
         *
         * @param callback 读取到数据后的回调
         */
        public
        void read(@NotNull Consumer<@NotNull T> callback) {
            Runnable r = () -> {
                // 读取的数据
                T re;

                /* 校验 */
                re = In.this.read();
                if (re == null)
                    return;

                callback.accept(re);
            };

            if (pool != null)
                pool.submit(r);
            else
                new Thread(r).start();
        }

        /**
         * 执行读取
         * <p>
         * 不使用线程池会返回 {@code null}
         *
         * @see Future
         */
        @Nullable
        public
        Future<@Nullable T> read() {
            if (pool != null)
                return pool.submit(In.this::read);
            else {
                new Thread(In.this::read).start();
                return null;
            }
        }
    }
}
