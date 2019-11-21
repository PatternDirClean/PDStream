package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <h2>面向流的输出操作工具.</h2>
 * {@link #write(Object, int)} 在不同的流作为操作对象时会有语义上的差别，请查看文档
 * <pre>可使用操作缓存器 {@link #append(Object)} 进行并联操作
 * var out = new OutByte();
 * var bytes = new byte[0];
 * out.append(bytes).append(bytes).append(bytes).flush();
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public
interface OutOfStream<O extends Closeable & Flushable, D> extends Operator<O>, Flushable {
    /**
     * 输出数据到操作对象中
     *
     * @param data data
     *
     * @return 是否成功操作
     */
    default
    boolean write(@Nullable D data) { return write(data, Integer.MAX_VALUE); }

    /**
     * 截取指定长度数据输出
     *
     * @param data data
     * @param len  输出长度
     *
     * @return 是否成功操作
     */
    boolean write(@Nullable D data, int len);

    /** 同时执行 {@link #write(D)} 和 {@link #flush()} 操作 */
    default
    boolean write_And_Flush(@Nullable D data) {
        synchronized ( this ){
            try {
                return write(data);
            } finally {
                flush();
            }
        }
    }

    /**
     * 通过操作缓存器进行并联操作
     *
     * @param data 第一个数据
     *
     * @return 操作缓存器
     *
     * @see Buff
     */
    @NotNull
    Buff<O, D> append(@Nullable D data);

    @Override
    default
    void flush() {
        try {
            synchronized ( this ){
                original().flush();
            }
        } catch ( IOException ignored ) {
        }
    }

    @Override
    default
    void close() {
        try {
            synchronized ( this ){
                original().close();
            }
        } catch ( IOException ignored ) {
        }
    }

    /**
     * <h2>输出操作缓存器.</h2>
     * 用于缓存输出的数据并提供链式操作
     * 在 {@link #flush()} 后将数据整合并刷新到对象中
     *
     * <b>该操作线程安全</b>
     *
     * @author fybug
     * @version 0.0.1
     * @since OutOfStream 0.0.1
     */
    abstract
    class Buff<O extends Closeable & Flushable, D> implements Closeable, Flushable {
        /** 操作对象 */
        @NotNull protected final O tagre;
        /** 操作数据缓存链 */
        @NotNull private final LinkedBlockingQueue<@NotNull D> linkedList =
                new LinkedBlockingQueue<>();

        /**
         * 构造新的操作缓存，传入对象和第一个操作的数据
         *
         * @param out  操作对象
         * @param data first data
         */
        protected
        Buff(@NotNull O out, @Nullable D data) {
            tagre = out;
            append(data);
        }

        /**
         * 检查追加的数据
         *
         * @param data 当前追加的数据
         */
        protected
        boolean check(@Nullable D data) {return data != null;}

        /**
         * 数据追加
         *
         * @param data data
         *
         * @return this
         */
        @NotNull
        public
        Buff<O, D> append(@Nullable D data) {
            if (check(data))
                linkedList.offer(data);
            return this;
        }

        @Override
        public
        void close() {
            flush();

            try {
                tagre.close();
            } catch ( IOException ignored ) {
            }
        }

        /**
         * 输出到流中
         * 在 {@link #flush()} 的过程中用于输出的操作
         *
         * @param data 当前数据
         */
        protected abstract
        void flush0(@NotNull D data) throws Exception;

        @Override
        public
        void flush() {
            try {
                for ( @NotNull D d : linkedList )
                    flush0(d);

                linkedList.clear();
                tagre.flush();
            } catch ( Exception ignored ) {
            }
        }
    }
}
