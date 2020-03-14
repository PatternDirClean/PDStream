package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import fybug.nulll.pdstream.io.uilt.AsnyIn;
import fybug.nulll.pdstream.io.uilt.In;
import lombok.experimental.UtilityClass;

/**
 * <h2>读取工具工场.</h2>
 * <p>
 * 构造生成的工具类为 {@link In}，对应的异步工具类为 {@link AsnyIn}<br/>
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
 *             .asny().close()
 *             // 异常处理接口
 *             .exception(e -> e.printStackTrace(System.out))
 *             // 触发
 *             .read(System.out::println);
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.2
 * @since io 0.0.1
 */
@UtilityClass
public
class InOf {
    /**
     * 生成读取工具，指定读取长度
     *
     * @param input 读取的流
     * @param size  读取的最大长度
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<InputStream, byte[]> read(@NotNull InputStream input, int size) {
        return new In<>(input, byte[].class) {
            @Override
            protected @NotNull
            byte[] read0(@NotNull InputStream inputStream) throws IOException {

                var re = input.readNBytes(size);

                if (re.length == 0)
                    return new byte[0];
                return re;
            }
        };
    }

    /**
     * 生成读取工具，指定读取长度
     *
     * @param input    读取的流
     * @param readsize 读取的最大长度
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<Reader, CharSequence> read(@NotNull Reader input, int readsize) {
        return new In<>(input, CharSequence.class) {
            int size = readsize;

            @Override
            protected @NotNull
            CharSequence read0(@NotNull Reader reader) throws IOException {
                // 数据缓存
                var builder = new StringBuilder();
                // 读取缓冲
                var buff = new char[8192];
                // 载入缓冲的数据量
                int readsize;

                while( input.ready() && size > 0 ){
                    // 读取数据到缓冲区
                    readsize = input.read(buff, 0, Math.min(8192, size));

                    // 莫得了
                    if (readsize < 1)
                        break;

                    // 递减可读取数量
                    size -= readsize;
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
        };
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 生成读取全部数据的读取工具
     *
     * @param input 读取的流
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<InputStream, byte[]> readAll(@NotNull InputStream input)
    { return read(input, Integer.MAX_VALUE); }

    /**
     * 生成读取全部数据的读取工具
     *
     * @param input 读取的流
     *
     * @since InOf 0.0.2
     */
    @NotNull
    public
    In<Reader, CharSequence> readAll(@NotNull Reader input)
    { return read(input, Integer.MAX_VALUE); }
}
