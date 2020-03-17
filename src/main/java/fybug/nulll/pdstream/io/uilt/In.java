package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * <h2>读取工具实现.</h2>
 *
 * @author fybug
 * @version 0.0.4
 * @since uilt 0.0.1
 */
@SuppressWarnings( "unchecked" )
public abstract
class In<T> extends IOtool<In<T>, T> {

    /**
     * 构造一个读取工具
     *
     * @param o    读取的流
     * @param Tcla 数据类型
     */
    public
    In(@NotNull Closeable o, @NotNull Class<T> Tcla) { super(o, Tcla); }

    In(Closeable o, Class<T> Tcla, boolean needClose, Consumer<IOException> exception)
    { super(o, Tcla, needClose, exception); }

    //----------------------------------------------------------------------------------------------

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

    /** @see #read(Consumer) */
    @Nullable
    public final
    T read() {return read(t -> {});}

    /**
     * 进行读取
     *
     * @param erun 异常处理
     *
     * @return 读取的数据
     *
     * @since In 0.0.4
     */
    @Nullable
    public final
    T read(@NotNull Consumer<IOException> erun) {
        var ref = new Object() {
            T base = null;
        };

        o.ifPresent(o -> {
            try {
                // 读取
                ref.base = read0(o);
            } catch ( IOException e ) {
                // 处理异常
                exception.accept(e);
                erun.accept(e);
            } finally {
                /* 检查关闭 */
                if (needClose)
                    try {
                        o.close();
                    } catch ( IOException ignored ) {
                    }
            }
        });

        return ref.base;
    }

    /** 读取实现 */
    @NotNull
    protected abstract
    T read0(@NotNull Closeable o) throws IOException;

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步读取实现.</h2>
     *
     * @author fybug
     * @version 0.0.2
     * @since In 0.0.2
     */
    public final
    class AsyncIn extends IOtool<In<T>, T>.AsyncTool<AsyncIn> {

        /**
         * 构造一个异步读取工具
         *
         * @param pool 进行异步操作的线程池
         */
        public
        AsyncIn(@Nullable ExecutorService pool) { super(pool); }

        //----------------------------------------------------------------------------------------------

        /** @see #read(Consumer, Consumer) */
        public
        void read(@NotNull Consumer<@NotNull T> callback) {read(callback, t -> {});}

        /**
         * 进行读取
         *
         * @param callback 读取到数据后的回调
         * @param erun     异常处理
         *
         * @since In 0.0.4
         */
        public
        void read(@NotNull Consumer<@NotNull T> callback, @NotNull Consumer<IOException> erun) {
            synchronized ( this ){
                pool.ifPresentOrElse(pool -> {
                    // 启用读取
                    pool.submit(() -> {
                        // 调取回调
                        Optional.ofNullable(In.this.read(erun))
                                .ifPresent(re -> pool.submit(() -> callback.accept(re)));
                    });
                }, () -> {
                    // 启用线程
                    new Thread(() -> Optional.ofNullable(In.this.read(erun))
                                             .ifPresent(callback)).start();
                });
            }
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
            final Future<T>[] future = new Future[]{null};

            o.ifPresent(o -> {
                synchronized ( this ){
                    // 检查线程池
                    pool.ifPresent(pool -> future[0] = pool.submit(() -> read0(o)));
                }
            });
            return future[0];
        }
    }
}
