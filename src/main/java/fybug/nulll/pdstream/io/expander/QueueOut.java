package fybug.nulll.pdstream.io.expander;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import fybug.nulll.pdstream.io.uilt.PushChannel;
import fybug.nulll.task.TaskQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public
class QueueOut implements Flushable, Closeable {
    // 输出通道
    private final PushChannel OUT;
    /** 是否关闭 */
    @Getter private volatile boolean close = false;

    // 任务队列
    private final TaskQueue TASKS;
    // 任务队列 id
    private final int ID_TASKS;
    // 回调处理 id
    private final int ID_RUNNBACK;

    //----------------------------------------------------------------------------------------------

    protected
    QueueOut(@NotNull PushChannel out, @Nullable ExecutorService p)
    { this(out, p, new TaskQueue()); }

    protected
    QueueOut(@NotNull PushChannel out, @Nullable ExecutorService p, @NotNull TaskQueue taskQueue) {
        this.OUT = out;
        this.TASKS = taskQueue;

        if (p == null)
            ID_TASKS = taskQueue.addQueue();
        else
            ID_TASKS = taskQueue.addQueue(p);
        // 处理回调用
        ID_RUNNBACK = taskQueue.addQueue(Executors.newSingleThreadExecutor());
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
        try {
            TASKS.addtask(() -> {
                try {
                    canrun();
                    OUT.append(da);
                    // 回调
                    TASKS.addtask(callback, ID_RUNNBACK);
                } catch ( IOException e ) {
                    erun.accept(e);
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }, ID_TASKS);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }

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
        try {
            TASKS.addtask(() -> {
                try {
                    canrun();
                    OUT.append(da);
                    // 回调
                    TASKS.addtask(callback, ID_RUNNBACK);
                } catch ( IOException e ) {
                    erun.accept(e);
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }, ID_TASKS);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
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
    boolean isWrite() { return TASKS.hasTask(ID_TASKS); }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    void flush() throws IOException {
        canrun();
        try {
            TASKS.addtask(() -> {
                try {
                    OUT.send();
                } catch ( IOException ignored ) {
                }
            }, ID_TASKS);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public
    void close() {
        try {
            TASKS.addtask(() -> {
                close = true;
                try {
                    OUT.close();
                } catch ( IOException ignored ) {
                }
            }, ID_TASKS);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
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

    /** 获取构造工具 */
    @NotNull
    public static
    Build build() { return new Build(); }

    @Accessors( chain = true, fluent = true )
    public static
    class Build {
        /** 执行用任务队列 */
        @Setter private TaskQueue taskQueue = new TaskQueue();
        /** 执行用线程池，注册给任务队列使用 */
        @Setter private ExecutorService pool = null;
        private Function<PushChannel.Build, PushChannel> channel = (build) -> {
            try {
                return build.sync();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            return null;
        };

        //------------------------------------------------------------------------------------------

        @NotNull
        public
        Build sync() {
            channel = (build) -> {
                try {
                    return build.sync();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return null;
            };
            return this;
        }

        @NotNull
        public
        Build timing(int time) {
            channel = (build) -> {
                try {
                    return build.timing(time);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return null;
            };
            return this;
        }

        @NotNull
        public
        Build buffer(int buffsize) {
            channel = (build) -> {
                try {
                    return build.buffer(buffsize);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return null;
            };
            return this;
        }

        //------------------------------------------------------------------------------------------

        @NotNull
        public
        QueueOut build(@NotNull Writer writer) {
            return new QueueOut(channel.apply(PushChannel.build()
                                                         .point(writer)
                                                         .clear()), pool, taskQueue);
        }

        @NotNull
        public
        QueueOut build(@NotNull OutputStream outputStream) {
            return new QueueOut(channel.apply(PushChannel.build()
                                                         .point(outputStream)
                                                         .clear()), pool, taskQueue);
        }

        @NotNull
        public
        QueueOut build(@NotNull File file) { return build(file.toPath()); }

        @NotNull
        public
        QueueOut build(@NotNull Path path) {
            return new QueueOut(channel.apply(PushChannel.build()
                                                         .point(path)
                                                         .append()
                                                         .clear()), pool, taskQueue);
        }
    }
}
