package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <h2>输出工具实现.</h2>
 * <p>
 * 可使用 {@link #append(T)} 进行链式写入，无论设置如何使用该操作都需要手动执行 {@link #flush()}
 *
 * @author fybug
 * @version 0.0.4
 * @since uilt 0.0.1
 */
@SuppressWarnings( "unchecked" )
public abstract
class Out<T> extends IOtool<Out<T>, T> implements Flushable {
    /** 是否及时刷新 */
    final boolean needFlush;

    Out(Supplier<Closeable> o, Class<T> Tcla, boolean needClose, Consumer<IOException> exception,
        boolean needFlush)
    {
        super(o, Tcla, needClose, exception);
        this.needFlush = needFlush;
    }

    /**
     * 构造一个输出工具
     *
     * @param o         获取流的接口
     * @param Tcla      数据类型
     * @param needFlush 是否自动刷入数据
     */
    public
    Out(@NotNull Supplier<Closeable> o, @NotNull Class<T> Tcla, boolean needFlush) {
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

    /** @see #write(Object, Consumer) */
    public final
    boolean write(@NotNull T data) {return write(data, t -> {});}

    /**
     * 进行写入
     *
     * @param data 输出的数据
     * @param erun 异常处理接口
     *
     * @return 是否成功
     *
     * @since Out 0.0.4
     */
    public final
    boolean write(@NotNull T data, @NotNull Consumer<IOException> erun) {
        final boolean[] succ = {true};

        Optional.ofNullable(o.get()).ifPresent(o -> {
            try {
                // 输出
                write0(o, data);
            } catch ( IOException e ) {
                // 处理异常
                exception.accept(e);
                erun.accept(e);
                succ[0] = false;
            } finally {
                if (needFlush)
                    flush();
            }
        });

        return succ[0];
    }

    /** 输出实现 */
    protected abstract
    void write0(@NotNull Closeable o, @NotNull T data) throws IOException;

    //------------------

    /**
     * 追加写入
     *
     * @param data 输出的数据
     */
    @NotNull
    public final
    Out<T> append(@NotNull T data) {
        Optional.ofNullable(o.get()).ifPresent(o -> {
            try {
                // 输出
                write0(o, data);
            } catch ( IOException e ) {
                // 处理异常
                exception.accept(e);
            }
        });
        return this;
    }

    @Override
    public
    void flush() {
        Optional.ofNullable(o.get()).ifPresent(o -> {
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
        });
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
         * 写入的数据缓存到 {@link #datalist} 中，需要执行 {@link #write()} 或 {@link #write(Runnable)} 写入
         *
         * @param data 写入的数据
         */
        @NotNull
        public
        AsyncOut append(@NotNull T data) {
            datalist.add(data);
            return this;
        }

        //-----------------------------------------------

        /** @see #write(Runnable, Consumer) */
        public
        void write(@NotNull Runnable callback) {write(callback, y -> {});}

        /**
         * 写入缓存中的数据
         * <p>
         * 写入完成后会执行 {@link #flush()} 无论配置如何
         *
         * @param callback 写入状态回调
         * @param erun     异常处理回调
         *
         * @since Out 0.0.4
         */
        public
        void write(@NotNull Runnable callback, @NotNull Consumer<IOException> erun) {
            synchronized ( this ){
                pool.ifPresentOrElse(pool -> pool.submit(() -> {
                    try {
                        w();
                        pool.submit(callback);
                    } catch ( IOException e ) {
                        exception.accept(e);
                        erun.accept(e);
                    }
                }), () -> new Thread(() -> {
                    try {
                        w();
                        callback.run();
                    } catch ( IOException e ) {
                        exception.accept(e);
                        erun.accept(e);
                    }
                }).start());
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
            final Future<Boolean>[] futures = new Future[]{null};

            synchronized ( this ){
                pool.ifPresentOrElse(pool -> futures[0] = pool.submit(this::w),
                                     // 不使用线程池
                                     () -> new Thread(() -> {
                                         try {
                                             w();
                                         } catch ( IOException e ) {
                                             exception.accept(e);
                                         }
                                     }).start());

            }
            return futures[0];
        }

        // 整合输出
        private
        boolean w() throws IOException {
            var os = Optional.ofNullable(Out.this.o.get());
            // 检查
            if (os.isEmpty())
                return false;
            var o = os.get();

            var ref = new Object() {
                boolean succ = false;
            };

            try {
                /* 取出数据并写入 */
                for ( var data = datalist.poll(); data != null; data = datalist.poll() )
                    Out.this.write0(o, data);
                ref.succ = true;
            } finally {
                flush();
            }

            return ref.succ;
        }

        //----------------------------------------------------------------------------------------------

        /** @see #write(Object, Runnable, Consumer) */
        public
        void write(@NotNull T data, @NotNull Runnable callback)
        {write(data, callback, y -> {});}

        /**
         * 进行输出
         *
         * @param data     输出数据
         * @param callback 输出状态回调
         * @param erun     异常处理接口
         *
         * @since Out 0.0.4
         */
        public
        void write(@NotNull T data, @NotNull Runnable callback, @NotNull Consumer<IOException> erun)
        {
            synchronized ( this ){
                pool.ifPresentOrElse(pool -> pool.submit(() -> {
                                         if (Out.this.write(data, erun))
                                             pool.submit(callback);
                                     }),
                                     // 不使用线程池
                                     () -> new Thread(() -> {
                                         Out.this.write(data, erun);
                                         callback.run();
                                     }).start());
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
            final Future<Boolean>[] flushable = new Future[]{null};

            synchronized ( this ){
                Optional.ofNullable(o.get()).ifPresent(o -> {
                    // 检查线程池
                    pool.ifPresentOrElse(pool -> flushable[0] = pool.submit(() -> {
                                             Out.this.write0(o, data);
                                             return true;
                                         }),
                                         // 不使用线程池
                                         () -> new Thread(() -> Out.this.write(data)).start());
                });
            }
            return flushable[0];
        }

        public
        void flush() {Out.this.flush();}
    }
}
