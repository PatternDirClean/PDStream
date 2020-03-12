package fybug.nulll.pdstream.io.condition.io;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * <h2>异步任务工具.</h2>
 * <p>
 * 任务队列的运行使用线程池进行，可使用 {@link #threadPool(ExecutorService)} 修改线程池
 * <p>
 * 内部使用任务队列进行，关闭时会等待前面任务完成后关闭
 *
 * @author fybug
 * @version 0.0.1
 * @see TaskLoop
 * @since io 0.0.1
 */
public abstract
class AsnycRun<T extends AsnycRun<?>> extends IOFiltrer<T> {
    /** 当前任务对象 */
    private TaskLoop looptask;

    /*--------------------------------------------------------------------------------------------*/

    protected
    AsnycRun() {threadPool(Executors.newSingleThreadExecutor());}

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 指定运行用线程池
     *
     * @param service 后续运行用的线程池
     *
     * @return this
     */
    public
    T threadPool(@NotNull ExecutorService service) {
        if (isClose())
            return (T) this;

        // 停止当前任务处理
        if (looptask != null) {
            looptask.close(v -> {});
            looptask = new TaskLoop(looptask.awaitEnd());
        } else
            looptask = new TaskLoop();

        service.submit(looptask::run);

        return (T) this;
    }

    /*----------------------------*/

    /**
     * <h2>任务处理模块.</h2>
     * <p>
     * 不断处理内部任务直到被停止，可使用 {@link #awaitEnd()} 等待内部任务停止
     * 等待完成后会返回剩余的任务列表
     *
     * @author fybug
     * @version 0.0.1
     * @since AsnycRun 0.0.1
     */
    private static
    class TaskLoop {
        /** 是否停止 */
        private volatile boolean stop = false;
        /** 是否可以检查关闭状态 */
        private boolean canCheckEnd = false;

        /** 任务队列 */
        private final LinkedList<Consumer<TaskLoop>> runlist;
        // 队列管理
        private final Lock LIST_lock = new ReentrantLock();
        private final Condition noemp = LIST_lock.newCondition();

        // 任务管理
        private final Lock TASK_lock = new ReentrantLock();
        private final Condition taskend = TASK_lock.newCondition();

        /*----------------------------------------------------------------------------------------*/

        private
        TaskLoop() { this(new LinkedList<>()); }

        private
        TaskLoop(LinkedList<Consumer<TaskLoop>> list) {runlist = list;}

        /*----------------------------------------------------------------------------------------*/

        private
        void run() {
            // 处理任务
            while( !stop )
                pollRun().accept(this);
        }

        // 关闭后将结束任务处理，并通知等待返回剩余的任务
        private
        void close(Consumer<TaskLoop> runnable) {
            // 追加关闭任务
            appendRun(v -> {
                /* 停止任务 */
                TASK_lock.lock();
                stop = true;

                runnable.accept(this);

                // 通知已结束
                taskend.signalAll();
                TASK_lock.unlock();
            });
            canCheckEnd = true;
        }

        // 等待结束，返回剩余任务队列
        private
        LinkedList<Consumer<TaskLoop>> awaitEnd() {
            if (!canCheckEnd)
                return new LinkedList<>();
            TASK_lock.lock();
            try {
                // 未结束
                if (!stop)
                    taskend.await();
            } catch ( InterruptedException e ) {
                TASK_lock.unlock();
            }
            return new LinkedList<>(runlist);
        }

        /*----------------------------------------------------------------------------------------*/

        // 添加任务
        private
        void appendRun(Consumer<TaskLoop> run) {
            LIST_lock.lock();
            runlist.add(run);
            // 通知处理队列
            noemp.signal();
            LIST_lock.unlock();
        }

        // 获取当前任务
        private
        Consumer<TaskLoop> pollRun() {
            Consumer<TaskLoop> c;

            try {
                LIST_lock.lock();
                // 获取
                while( (c = runlist.poll()) == null )
                    // 等待任务
                    noemp.await();
                return c;
            } catch ( Exception ignored ) {
                return v -> {};
            } finally {
                LIST_lock.unlock();
            }
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 添加任务 */
    protected
    void appendRun(Runnable run) {
        if (isClose())
            return;
        looptask.appendRun(v -> run.run());
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 关闭后不可继续添加任务 */
    @Override
    public final
    void close() {
        if (isClose())
            return;
        // 标记
        markClose();
        looptask.close(v -> {
            v.LIST_lock.lock();
            v.runlist.clear();
            v.LIST_lock.unlock();
            super.close();
            close0();
        });
    }

    /** 关闭事件 */
    protected abstract
    void close0();
}
