package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * <h2>读取工具实现.</h2>
 *
 * @author fybug
 * @version 0.0.3
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
            return read0();
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
    T read0() throws IOException;

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

            synchronized ( this ){
                if (pool != null)
                    pool.submit(r);
                else
                    new Thread(r).start();
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
            synchronized ( this ){
                if (pool != null)
                    return pool.submit(In.this::read);
                else
                    new Thread(In.this::read).start();
            }
            return null;
        }
    }
}
