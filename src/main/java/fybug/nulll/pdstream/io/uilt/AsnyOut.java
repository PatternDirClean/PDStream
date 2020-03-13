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
import lombok.experimental.Accessors;

/**
 * <h2>异步输出实现.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.1
 */
@Accessors( fluent = true, chain = true )
@AllArgsConstructor
public final
class AsnyOut<O extends Flushable & Closeable, T> implements Flushable, InorOut<AsnyOut<O, T>> {
    // 元操作
    private final Out<O, T> rea;
    // 数据类型
    private final Class<T> Tcla;

    // 执行用线程池
    @Nullable private final ExecutorService pool;

    //----------------------------------------------------------------------------------------------

    @NotNull
    public
    AsnyOut<O, T> close() {
        rea.close();
        return this;
    }

    @NotNull
    public
    AsnyOut<O, T> exception(@NotNull Consumer<IOException> e) {
        rea.exception(e);
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
    AsnyOut<O, T> append(@NotNull T data) {
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
                rea.write0(rea.o, data);
            return true;
        } catch ( IOException e ) {
            // 异常处理
            rea.exception.accept(e);
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
            pool.submit(() -> callback.accept(rea.write(data)));
        else
            new Thread(() -> callback.accept(rea.write(data))).start();
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
    Future<Boolean> read(@NotNull T data) {
        if (pool != null)
            return pool.submit(() -> rea.write(data));
        else {
            new Thread(() -> rea.write(data)).start();
            return null;
        }
    }

    public
    void flush() {rea.flush();}
}
