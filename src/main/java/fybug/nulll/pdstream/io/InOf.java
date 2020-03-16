package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import fybug.nulll.pdstream.io.uilt.In;
import fybug.nulll.pdstream.io.uilt.InBuild;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * <h2>读取工具工场.</h2>
 * <p>
 * 构造生成的工具类为 {@link In}，对应的异步工具类为 {@link In.AsyncIn}<br/>
 * 读取过程失败返回 {@code null}
 * <br/>
 * <pre>使用示例
 *     public static
 *     void main(String[] args) {
 *         var base = new StringReader("asdas");
 *         System.out.println(InOf.readAll(base)
 *                                // 读取完成后自动关闭
 *                                .close()
 *                                // 异常处理接口
 *                                .exception(e -> e.printStackTrace(System.out))
 *                                // 触发
 *                                .read());
 *     }
 * </pre>
 * <pre>启用异步操作
 *     public static
 *     void main(String[] args) {
 *         var base = new StringReader("asdas");
 *         InOf.readAll(base)
 *             // 启用异步读取
 *             .async().close()
 *             // 异常处理接口
 *             .exception(e -> e.printStackTrace(System.out))
 *             // 触发
 *             .read(System.out::println);
 *     }
 * </pre>
 * 关于构造器的示例请查看 {@link InBuild}
 *
 * @author fybug
 * @version 0.0.3
 * @since io 0.0.1
 */
@UtilityClass
public
class InOf {

    /**
     * 字节读取工具实例
     *
     * @author fybug
     * @version 0.0.2
     * @since InOf 0.0.3
     */
    private final static
    class byter extends In<byte[]> {
        // 读取的最大字节数
        private final int MAX_SIZE;

        byter(@NonNull InputStream inputStream, int size) {
            super(inputStream, byte[].class);
            this.MAX_SIZE = size;
        }

        @NotNull
        protected
        byte[] read0(@NotNull Closeable o) throws IOException
        { return ((InputStream) o).readNBytes(MAX_SIZE); }
    }

    /**
     * 生成读取工具，指定读取长度
     *
     * @param input   读取的流
     * @param maxsize 读取的最大长度
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<byte[]> read(@NotNull InputStream input, int maxsize)
    { return new byter(input, maxsize); }

    /**
     * 生成读取全部数据的读取工具
     *
     * @param input 读取的流
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<byte[]> readAll(@NotNull InputStream input)
    { return read(input, Integer.MAX_VALUE); }

    //----------------------------------------------------------------------------------------------

    /**
     * 字符读取工具实例
     *
     * @author fybug
     * @version 0.0.1
     * @since InOf 0.0.3
     */
    private final static
    class charr extends In<CharSequence> {
        // 最大读取的字符数量
        private final int MAX_SIZE;

        charr(@NonNull Reader reader, int size) {
            super(reader, CharSequence.class);
            this.MAX_SIZE = size;
        }

        @NotNull
        protected
        CharSequence read0(@NotNull Closeable o) throws IOException {
            var canRead = MAX_SIZE;
            // 数据缓存
            var builder = new StringBuilder();
            // 读取缓冲
            var buff = new char[1024];
            // 载入缓冲的数据量
            int readsize;

            while( ((Reader) o).ready() && canRead > 0 ){
                // 读取数据到缓冲区
                readsize = ((Reader) o).read(buff, 0, Math.min(1024, canRead));

                // 莫得了
                if (readsize < 1)
                    break;

                // 递减可读取数量
                canRead -= readsize;
                // 缓存数据
                builder.append(buff, 0, readsize);
            }

            /* 无数据 */
            if (builder.length() == 0)
                return "";

            // 释放一下
            buff = null;
            var re = builder.toString();
            builder = null;
            return re;
        }
    }

    /**
     * 生成读取工具，指定读取长度
     *
     * @param input   读取的流
     * @param maxsize 读取的最大长度
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<CharSequence> read(@NotNull Reader input, int maxsize)
    { return new charr(input, maxsize); }

    /**
     * 生成读取全部数据的读取工具
     *
     * @param input 读取的流
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<CharSequence> readAll(@NotNull Reader input)
    { return read(input, Integer.MAX_VALUE); }

    //----------------------------------------------------------------------------------------------

    /**
     * 创建读取工具构造器
     * <p>
     * 该构造器构造的工具会读取全部数据
     *
     * @since InOf 0.0.3
     */
    @NotNull
    public
    InBuild inBuild() { return inBuild(Integer.MAX_VALUE); }

    /**
     * 创建读取工具构造器
     * <p>
     * 该构造器构造的工具仅读取指定数量的数据
     *
     * @since InOf 0.0.3
     */
    @NotNull
    public
    InBuild inBuild(int maxsize) {
        return new InBuild() {
            @Override
            protected @NotNull
            In<byte[]> build(@NotNull InputStream inputStream)
            { return new byter(inputStream, maxsize); }

            @Override
            protected @NotNull
            In<CharSequence> build(@NotNull Reader reader)
            { return new charr(reader, maxsize); }
        };
    }
}
