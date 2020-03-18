package fybug.nulll.pdstream.io.expander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;

import fybug.nulll.pdconcurrent.ReLock;
import fybug.nulll.pdconcurrent.SyLock;
import lombok.Getter;

/**
 * 异步队列输出工具
 * <p>
 * 每次进行输出都会包装为一个操作接口插入执行队列中 {@link #flush()} 与 {@link #close()} 都是<br/>
 * 可使用 {@link #isWrite()} 检查是否有操作在等待<br/>
 * <p>
 * 可追加完成回调与异常处理
 *
 * @author fybug
 * @version 0.0.1
 * @see byteQ
 * @see charQ
 * @since expander 0.0.1
 */
public abstract
class QueueOut<T> implements Flushable, Closeable {
    /** 是否关闭 */
    @Getter private volatile boolean close = false;
    /** 换行数据 */
    protected T LN = null;
    // 输出对象
    private final Optional<Closeable> o;

    /** 队列锁 */
    private final ReLock LOCK = SyLock.newReLock();
    /** 队列管理 */
    private final Condition QUEUE_WAIT = LOCK.newCondition();

    /** 操作队列 */
    private final Queue<Runnable> QUEUE = new LinkedList<>();
    /** 线程池 */
    protected final Optional<ExecutorService> pool;

    //----------------------------------------------------------------------------------------------

    private
    QueueOut(@Nullable Closeable o, @Nullable ExecutorService p) {
        this.o = Optional.ofNullable(o);
        this.pool = Optional.ofNullable(p);

        this.pool.ifPresentOrElse(pool -> pool.submit(this::poolTask),
                                  // 单独一个线程
                                  () -> new Thread(this::poolTask).start());
    }

    // 操作线程内容
    private
    void poolTask() {
        while( !close ){
            LOCK.write(() -> Optional.ofNullable(QUEUE.peek()).ifPresentOrElse(run -> {
                // 执行操作
                run.run();
                QUEUE.poll();
            }, () -> {
                try {
                    // 等待数据
                    QUEUE_WAIT.await();
                } catch ( InterruptedException e ) {
                    close = true;
                }
            }));
        }
    }

    //----------------------------------------------------------------------------------------------

    /** @see #write(T, Runnable) */
    public final
    void write(@NotNull T da) {write(da, () -> {});}

    /** @see #write(T, Runnable, Consumer) */
    public final
    void write(@NotNull T da, @NotNull Runnable callback) {write(da, callback, y -> {});}

    /**
     * 加入一段数据到输出队列
     *
     * @param da       输出的数据
     * @param callback 成功回调
     * @param erun     异常处理
     */
    public final
    void write(@NotNull T da, @NotNull Runnable callback, @NotNull Consumer<IOException> erun) {
        o.ifPresent((o) -> LOCK.write(() -> {
            QUEUE.add(() -> {
                try {
                    canrun();
                    write0(o, da);
                    // 回调
                    pool.ifPresentOrElse(pool -> pool.submit(callback), callback);
                } catch ( IOException e ) {
                    erun.accept(e);
                }
            });
            QUEUE_WAIT.signalAll();
        }));
    }

    //--------------------------------------------

    /** @see #println(T, Runnable) */
    public final
    void println(@NotNull T da) {println(da, () -> {});}

    /**
     * @param da       输出的数据
     * @param callback 成功回调
     *
     * @see #println(T, Runnable, Consumer)
     */
    public final
    void println(@NotNull T da, @NotNull Runnable callback) {println(da, callback, t -> {});}

    /**
     * 加入一段数据到输出队列
     * <p>
     * 输出 da + [\n | \r\n]
     *
     * @param da       输出的数据
     * @param callback 成功回调
     * @param erun     异常处理
     */
    public final
    void println(@NotNull T da, @NotNull Runnable callback, @NotNull Consumer<IOException> erun) {
        o.ifPresent(o -> LOCK.write(() -> {
            QUEUE.add(() -> {
                try {
                    canrun();
                    write0(o, da);
                    write0(o, LN);
                    // 回调
                    pool.ifPresentOrElse(pool -> pool.submit(callback), callback);
                } catch ( IOException e ) {
                    erun.accept(e);
                }
            });
            QUEUE_WAIT.signal();
        }));
    }

    //---------------------------------------------

    /**
     * 追加一段数据到输出队列中
     *
     * @param da 输出的数据
     *
     * @see #write(T)
     */
    @NotNull
    public final
    QueueOut<T> append(@NotNull T da) {
        write(da);
        return this;
    }

    /**
     * 追加一行数据到输出队列中
     *
     * @param da 输出的数据
     *
     * @see #println(T)
     */
    @NotNull
    public final
    QueueOut<T> appendln(@NotNull T da) {
        println(da);
        return this;
    }

    //---------------------------------------------

    /** 输出实现 */
    protected abstract
    void write0(@NotNull Closeable o, @NotNull T da) throws IOException;

    //----------------------------------------------------------------------------------------------

    /** 是否有在写入 */
    public final
    boolean isWrite() { return QUEUE.size() != 0; }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    void flush() throws IOException {
        canrun();
        o.ifPresent(o -> LOCK.write(() -> {
            QUEUE.add(() -> {
                try {
                    ((Flushable) o).flush();
                } catch ( IOException ignored ) {
                }
            });
            QUEUE_WAIT.signal();
        }));
    }

    @Override
    public
    void close() {
        o.ifPresent(o -> LOCK.write(() -> {
            if (!isClose()) {
                QUEUE.add(() -> {
                    close = true;
                    try {
                        o.close();
                    } catch ( IOException ignored ) {
                    }
                });
                QUEUE_WAIT.signal();
            }
        }));
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 检查是否可用
     *
     * @throws IOException 被关闭
     */
    protected
    void canrun() throws IOException {
        if (isClose())
            throw new IOException();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 构造输出队列
     *
     * @param out 输出的流
     */
    @NotNull
    public static
    byteQ of(OutputStream out) {return new byteQ(out);}

    /**
     * 构造输出队列
     *
     * @param out  输出的流
     * @param pool 执行用线程池
     */
    @NotNull
    public static
    byteQ of(OutputStream out, ExecutorService pool) {return new byteQ(out, pool);}

    /** 字节输出类型 */
    public static final
    class byteQ extends QueueOut<byte[]> {
        private
        byteQ(@Nullable OutputStream o) { this(o, null); }

        private
        byteQ(@Nullable OutputStream o, @Nullable ExecutorService p) {
            super(o, p);
            LN = System.lineSeparator().getBytes();
        }

        protected
        void write0(@NotNull Closeable o, @NotNull byte[] da) throws IOException
        { ((OutputStream) o).write(da); }
    }

    //--------------------------------------------------

    /**
     * 构造输出队列
     *
     * @param writer 输出的流
     */
    @NotNull
    public static
    charQ of(Writer writer) {return new charQ(writer);}

    /**
     * 构造输出队列
     *
     * @param writer 输出的流
     * @param pool   执行用线程池
     */
    @NotNull
    public static
    charQ of(Writer writer, ExecutorService pool) {return new charQ(writer, pool);}

    /** 字符输出类型 */
    private static final
    class charQ extends QueueOut<CharSequence> {
        private
        charQ(@Nullable Writer o) { this(o, null); }

        private
        charQ(@Nullable Writer o, @Nullable ExecutorService p) {
            super(o, p);
            LN = System.lineSeparator();
        }

        protected
        void write0(@NotNull Closeable o, @NotNull CharSequence da) throws IOException
        { ((Writer) o).write(da.toString()); }
    }
}
