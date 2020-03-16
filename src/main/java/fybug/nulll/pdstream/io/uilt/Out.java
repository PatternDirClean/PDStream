package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * <h2>输出工具实现.</h2>
 * <p>
 * 可使用 {@link #append(T)} 进行链式写入，无论设置如何使用该操作都需要手动执行 {@link #flush()}
 *
 * @author fybug
 * @version 0.0.3
 * @since uilt 0.0.1
 */
@SuppressWarnings( "unchecked" )
public abstract
class Out<T> extends IOtool<Out<T>, T> implements Flushable {
    /** 是否及时刷新 */
    final boolean needFlush;

    Out(Closeable o, Class<T> Tcla, boolean needClose, Consumer<IOException> exception,
        boolean needFlush)
    {
        super(o, Tcla, needClose, exception);
        this.needFlush = needFlush;
    }

    /**
     * 构造一个输出工具
     *
     * @param o         输出对象
     * @param Tcla      数据类型
     * @param needFlush 是否自动刷入数据
     */
    public
    <O extends Closeable & Flushable> Out(@NotNull O o, @NotNull Class<T> Tcla, boolean needFlush)
    {
        super(o, Tcla);
        this.needFlush = needFlush;
    }

    //----------------------------------------------------------------------------------------------

    @NotNull
    public final
    AsyncOut async() { return async(null); }

    @NotNull
    public final
    AsyncOut async(@Nullable ExecutorService pool) { return new AsyncOut(pool); }

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
            write0(data);
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
    void write0(@NotNull T data) throws IOException;

    //------------------

    /**
     * 追加写入
     *
     * @param data 输出的数据
     */
    @NotNull
    public final
    Out<T> append(@NotNull T data) {
        try {
            // 输出
            write0(data);
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
            ((Flushable) o).flush();
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

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步输出实现.</h2>
     *
     * @author fybug
     * @version 0.0.2
     * @since Out 0.0.2
     */
    public final
    class AsyncOut extends IOtool<Out<T>, T>.AsyncTool<AsyncOut> implements Flushable {

        /**
         * 构造一个异步输出工具
         *
         * @param pool 操作用线程池
         */
        public
        AsyncOut(@Nullable ExecutorService pool) { super(pool); }

        //----------------------------------------------------------------------------------------------

        // 输出缓存
        private final LinkedList<T> datalist = new LinkedList<>();

        /**
         * 追加写入
         * <p>
         * 写入的数据缓存到 {@link #datalist} 中，需要执行 {@link #write()} 或 {@link #write(Consumer)} 写入
         *
         * @param data 写入的数据
         */
        @NotNull
        public
        AsyncOut append(@NotNull T data) {
            datalist.add(data);
            return this;
        }

        /**
         * 写入缓存中的数据
         * <p>
         * 写入完成后会执行 {@link #flush()} 无论配置如何
         *
         * @param callback 写入状态回调
         */
        public
        void write(@NotNull Consumer<Boolean> callback) {
            synchronized ( this ){
                if (pool != null)
                    pool.submit(() -> callback.accept(w()));
                else
                    new Thread(() -> callback.accept(w())).start();
            }
        }

        /**
         * 写入缓存中的数据
         * <p>
         * 写入完成后会执行 {@link #flush()} 无论配置如何<br/>
         * 没有使用线程池的情况会返回 {@code null}
         */
        @Nullable
        public
        Future<Boolean> write() {
            synchronized ( this ){
                if (pool != null)
                    return pool.submit(this::w);
                else
                    new Thread(this::w).start();
            }
            return null;
        }

        // 整合输出
        private
        Boolean w() {
            try {
                /* 取出数据并写入 */
                for ( var data = datalist.poll(); data != null; data = datalist.poll() )
                    Out.this.write0(data);
                return true;
            } catch ( IOException e ) {
                // 异常处理
                Out.this.exception.accept(e);
                return false;
            } finally {
                flush();
            }
        }

        //----------------------------------------------------------------------------------------------

        /**
         * 进行输出
         *
         * @param data     输出数据
         * @param callback 输出状态回调
         */
        public
        void write(@NotNull T data, @NotNull Consumer<Boolean> callback) {
            synchronized ( this ){
                if (pool != null)
                    pool.submit(() -> callback.accept(Out.this.write(data)));
                else
                    new Thread(() -> callback.accept(Out.this.write(data))).start();
            }
        }

        /**
         * 执行写入
         * <p>
         * 在没有使用线程池的情况下会返回 {@code null}
         *
         * @param data 输出的数据
         *
         * @see Future
         */
        @Nullable
        public
        Future<Boolean> write(@NotNull T data) {
            synchronized ( this ){
                if (pool != null)
                    return pool.submit(() -> Out.this.write(data));
                else
                    new Thread(() -> Out.this.write(data)).start();
            }
            return null;
        }

        public
        void flush() {Out.this.flush();}
    }
}
