package fybug.nulll.pdstream.io.expander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fybug.nulll.pdconcurrent.ReLock;
import fybug.nulll.pdconcurrent.SyLock;
import lombok.Getter;

/**
 * 异步队列输出工具
 * <p>
 * 每次进行输出都会包装为一个操作接口插入执行队列中 {@link #flush()} 与 {@link #close()} 都是<br/>
 * 可使用 {@link #isWrite()} 检查是否有操作在等待<br/>
 * 可追加完成回调与异常处理
 * <br/><br/>
 * 通过 {@code of(0} 方法获取构造器，在构造器中赋予属性后调用 {@code build()} 构造实例
 *
 * @author fybug
 * @version 0.0.1
 * @since expander 0.0.1
 */
public abstract
class QueueOut implements Flushable, Closeable {
    Supplier<Closeable> o;
    /** 是否关闭 */
    @Getter private volatile boolean close = false;

    // 队列锁
    private final ReLock LOCK = SyLock.newReLock();
    // 队列管理
    private final Condition QUEUE_WAIT = LOCK.newCondition();

    // 操作队列
    private final Queue<Runnable> QUEUE = new LinkedList<>();
    // 线程池
    private final Optional<ExecutorService> pool;

    //----------------------------------------------------------------------------------------------

    private
    QueueOut(@Nullable Supplier<Closeable> o, @Nullable ExecutorService p) {
        this.o = o;
        this.pool = Optional.ofNullable(p);

        this.pool.ifPresentOrElse(pool -> pool.submit(this::threadTask),
                                  // 单独一个线程
                                  () -> new Thread(this::threadTask).start());
    }

    // 线程任务代码
    private
    void threadTask() {
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

    /** @see #write(byte[], Runnable) */
    public final
    void write(@NotNull byte[] da) {write(da, () -> {});}

    /** @see #write(byte[], Runnable, Consumer) */
    public final
    void write(@NotNull byte[] da, @NotNull Runnable callback) {write(da, callback, y -> {});}

    /**
     * 加入一段数据到输出队列
     *
     * @param da       输出的数据
     * @param callback 成功回调
     * @param erun     异常处理
     */
    public final
    void write(@NotNull byte[] da, @NotNull Runnable callback, @NotNull Consumer<IOException> erun)
    {
        Optional.ofNullable(o.get()).ifPresent((o) -> LOCK.write(() -> {
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

    //---------------------------------------------

    /** 输出实现 */
    protected abstract
    void write0(@NotNull Closeable o, @NotNull byte[] da) throws IOException;

    //----------------------------------------------------------------------------------------------

    /** @see #print(CharSequence, Runnable) */
    public final
    void print(@NotNull CharSequence da) {print(da, () -> {});}

    /**
     * @param da       输出的数据
     * @param callback 成功回调
     *
     * @see #print(CharSequence, Runnable, Consumer)
     */
    public final
    void print(@NotNull CharSequence da, @NotNull Runnable callback) {print(da, callback, t -> {});}

    /**
     * 加入一段字符到输出队列
     *
     * @param da       输出的数据
     * @param callback 成功回调
     * @param erun     异常处理
     */
    public final
    void print(@NotNull CharSequence da, @NotNull Runnable callback,
               @NotNull Consumer<IOException> erun)
    {
        Optional.ofNullable(o.get()).ifPresent(o -> LOCK.write(() -> {
            QUEUE.add(() -> {
                try {
                    canrun();
                    print0(o, da);
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

    /** @see #println(CharSequence, Runnable) */
    public final
    void println(@NotNull CharSequence da)
    {println(da, () -> {});}

    /** @see #println(CharSequence, Runnable, Consumer) */
    public final
    void println(@NotNull CharSequence da, @NotNull Runnable callback)
    {println(da, callback, y -> {});}

    /**
     * {@code print() + [\n | \r\n]}
     *
     * @see #print(CharSequence, Runnable, Consumer)
     */
    public final
    void println(@NotNull CharSequence da, @NotNull Runnable callback,
                 @NotNull Consumer<IOException> erun)
    {print(da, callback, erun);}

    //---------------------------------------------

    /** 输出实现 */
    protected abstract
    void print0(@NotNull Closeable o, @NotNull CharSequence da) throws IOException;

    //---------------------------------------------

    /**
     * 追加一段数据到输出队列中
     *
     * @param da 输出的数据
     *
     * @see #write(byte[])
     */
    @NotNull
    public final
    QueueOut append(@NotNull byte[] da) {
        write(da);
        return this;
    }

    /**
     * 追加一段数据到输出队列中
     *
     * @param da 输出的数据
     *
     * @see #print(CharSequence)
     */
    @NotNull
    public final
    QueueOut append(@NotNull CharSequence da) {
        print(da);
        return this;
    }

    /**
     * 追加一行数据到输出队列中
     *
     * @param da 输出的数据
     *
     * @see #println(CharSequence)
     */
    @NotNull
    public final
    QueueOut appendln(@NotNull CharSequence da) {
        println(da);
        return this;
    }

    //----------------------------------------------------------------------------------------------

    /** 是否有在写入 */
    public final
    boolean isWrite() { return QUEUE.size() != 0; }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    void flush() throws IOException {
        canrun();
        Optional.ofNullable(o.get()).ifPresent(o -> LOCK.write(() -> {
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
        Optional.ofNullable(o.get()).ifPresent(o -> LOCK.write(() -> {
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

}
