package fybug.nulll.pdstream.io.condition.io;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * 带过滤的处理器
 * <p>
 * 可使用对应的过滤器对数据进行链式过滤
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
@SuppressWarnings( "All" )
abstract
class IOFiltrer<O extends IOFiltrer<?>> implements Closeable {
    /** 是否关闭 */
    private final AtomicBoolean isClose = new AtomicBoolean(false);
    // 数据过滤器
    private final List<Function<byte[], byte[]>> byteF = new ArrayList<>();
    private final List<Function<String, String>> charF = new ArrayList<>();
    // 过滤器的锁
    private ReadWriteLock bytelock = new ReentrantReadWriteLock();
    private ReadWriteLock charlock = new ReentrantReadWriteLock();

    /*--------------------------------------------------------------------------------------------*/

    protected
    IOFiltrer() {}

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 添加字节数据过滤器
     *
     * @param function 用于过滤的函数
     *
     * @return this
     */
    @NotNull
    public
    O filtrerBytes(Function<byte[], byte[]>... function) {
        if (!isClose()) {
            bytelock.writeLock().lock();
            Arrays.stream(function).filter(Objects::nonNull).forEach(byteF::add);
            bytelock.writeLock().unlock();
        }
        return (O) this;
    }

    /**
     * 添加字符串过滤器
     *
     * @param Function 用于过滤的函数
     *
     * @return this
     */
    @NotNull
    public
    O filtrerChars(Function<String, String>... Function) {
        if (!isClose()) {
            charlock.writeLock().lock();
            Arrays.stream(Function).filter(Objects::nonNull).forEach(charF::add);
            charlock.writeLock().unlock();
        }
        return (O) this;
    }

    /*----------------------------------*/

    /** 使用过滤器进行数据过滤 */
    protected
    byte[] byteF(byte[] bytes) {
        bytelock.readLock().lock();
        var stream = byteF.stream();
        bytelock.readLock().unlock();

        return stream.reduce(bytes, (a, b) -> b.apply(a), (a, b) -> null);
    }

    /** 使用过滤器进行数据过滤 */
    protected
    String charF(String string) {
        charlock.readLock().lock();
        var stream = charF.stream();
        charlock.readLock().unlock();

        return stream.reduce(string, (a, b) -> b.apply(a), (a, b) -> null);
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public
    void close() {
        bytelock.writeLock().lock();
        charlock.writeLock().lock();
        byteF.clear();
        charF.clear();
        bytelock.writeLock().unlock();
        charlock.writeLock().unlock();
    }

    /*---------------------*/

    protected
    void markClose() {isClose.set(true);}

    public
    boolean isClose() {return isClose.get();}
}
