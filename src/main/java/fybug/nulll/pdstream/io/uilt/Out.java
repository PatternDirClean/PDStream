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

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>输出工具实现.</h2>
 * <p>
 * 可使用 {@link #append(T)} 进行链式写入，无论设置如何使用该操作都需要手动执行 {@link #flush()}
 *
 * @author fybug
 * @version 0.0.2
 * @see AsyncOut
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
    AsyncOut async() { return async(null); }

    /**
     * 切换为异步操作
     *
     * @param pool 运行用线程池
     */
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

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步输出实现.</h2>
     *
     * @author fybug
     * @version 0.0.1
     * @since Out 0.0.2
     */
    @Accessors( fluent = true, chain = true )
    @AllArgsConstructor
    public final
    class AsyncOut implements Flushable, InorOut<AsyncOut> {
        // 执行用线程池
        @Nullable private final ExecutorService pool;

        //----------------------------------------------------------------------------------------------

        @NotNull
        public
        Out.AsyncOut close() {
            Out.this.close();
            return this;
        }

        @NotNull
        public
        Out.AsyncOut exception(@NotNull Consumer<IOException> e) {
            Out.this.exception(e);
            return this;
        }

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
        Out.AsyncOut append(@NotNull T data) {
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
            if (pool != null)
                pool.submit(() -> callback.accept(w()));
            else
                new Thread(() -> callback.accept(w())).start();
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
            if (pool != null)
                return pool.submit(this::w);
            else {
                new Thread(this::w).start();
                return null;
            }
        }

        // 整合输出
        private
        Boolean w() {
            try {
                /* 取出数据并写入 */
                for ( var data = datalist.poll(); data != null; data = datalist.poll() )
                    Out.this.write0(Out.this.o, data);
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
            if (pool != null)
                pool.submit(() -> callback.accept(Out.this.write(data)));
            else
                new Thread(() -> callback.accept(Out.this.write(data))).start();
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
            if (pool != null)
                return pool.submit(() -> Out.this.write(data));
            else {
                new Thread(() -> Out.this.write(data)).start();
                return null;
            }
        }

        public
        void flush() {Out.this.flush();}
    }
}
