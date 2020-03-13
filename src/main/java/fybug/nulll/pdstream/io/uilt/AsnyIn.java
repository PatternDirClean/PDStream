package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <h2>异步读取实现.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.1
 */
@Accessors( fluent = true, chain = true )
@AllArgsConstructor( access = AccessLevel.PACKAGE )
public final
class AsnyIn<O extends Closeable, T> implements InorOut<AsnyIn<O, T>> {
    // 元操作
    private final In<O, T> rea;
    // 数据类型
    private final Class<T> Tcla;

    // 执行用线程池
    @Nullable private final ExecutorService pool;

    //----------------------------------------------------------------------------------------------

    @NotNull
    public
    AsnyIn<O, T> close() {
        rea.close();
        return this;
    }

    @NotNull
    public
    AsnyIn<O, T> exception(@NotNull Consumer<IOException> e) {
        rea.exception(e);
        return this;
    }

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
            re = rea.read();
            if (re == null)
                return;

            callback.accept(re);
        };

        if (pool != null)
            pool.submit(r);
        else
            new Thread(r).start();
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
        if (pool != null)
            return pool.submit(rea::read);
        else {
            new Thread(rea::read).start();
            return null;
        }
    }
}
