package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import fybug.nulll.pdstream.io.uilt.Out;
import fybug.nulll.pdstream.io.uilt.OutBuild;
import lombok.experimental.UtilityClass;

/**
 * <h2>输出工具.</h2>
 * <p>
 * 构造生成的工具类为 {@link Out}，对应的异步工具类为 {@link Out.AsyncOut}<br/>
 * 读取过程失败返回 {@code false}
 * <br/>
 * <pre>使用示例
 *     public static
 *     void main(String[] args) {
 *         var base = new StringWriter();
 *         // 追加式写入
 *         OutOf.write(base).append("a").append("v").append("qwe").append("\n")
 *                          .flush();
 *         OutOf.writeFlush(base)
 *              // 输出完成自动关闭
 *              .close()
 *              // 异常处理
 *              .exception(e -> e.printStackTrace(System.out))
 *              // 输出
 *              .write("poip");
 *         System.out.println(base.toString());
 *     }
 * </pre>
 * <pre>使用异步处理
 *     public static
 *     void main(String[] args) {
 *         var base = new StringWriter();
 *         // 追加式写入
 *         OutOf.writeFlush(base).async().append("a").append("v").append("qwe").append("\n")
 *                                      .write();
 *         OutOf.writeFlush(base)
 *              // 启用异步操作
 *              .async().close()
 *              // 异常处理
 *              .exception(e -> e.printStackTrace(System.out))
 *              // 输出
 *              .write("poip", succer -> {System.out.println(base.toString());});
 *     }
 * </pre>
 * 关于构造器的示例请查看 {@link OutBuild}
 *
 * @author fybug
 * @version 0.0.3
 * @since io 0.0.1
 */
@UtilityClass
public
class OutOf {

    /**
     * 字节输出工具实现
     *
     * @author fybug
     * @version 0.0.1
     * @since OutOf 0.0.2
     */
    private final static
    class bytew extends Out<byte[]> {
        private
        bytew(OutputStream outputStream, boolean needFlush)
        { super(() -> outputStream, byte[].class, needFlush); }

        @Override
        protected
        void write0(@NotNull Closeable o, @NotNull byte[] data) throws IOException
        { ((OutputStream) o).write(data); }
    }

    /**
     * 生成输出工具
     *
     * @param outputStream 输出的流
     *
     * @since OutOf 0.0.2
     */
    @NotNull
    public
    Out<byte[]> write(@NotNull OutputStream outputStream)
    { return new bytew(outputStream, false); }

    /**
     * 生成自动刷入内容的输出工具
     *
     * @param outputStream 输出的流
     *
     * @since OutOf 0.0.2
     */
    @NotNull
    public
    Out<byte[]> writeFlush(@NotNull OutputStream outputStream)
    { return new bytew(outputStream, true); }

    //----------------------------------------------------------------------------------------------

    /**
     * 字符输出工具实现
     *
     * @author fybug
     * @version 0.0.1
     * @since OutOf 0.0.2
     */
    private final static
    class charw extends Out<CharSequence> {
        private
        charw(Writer writer, boolean needFlush)
        { super(() -> writer, CharSequence.class, needFlush); }

        @Override
        protected
        void write0(@NotNull Closeable o, @NotNull CharSequence data) throws IOException
        { ((Writer) o).write(data.toString()); }
    }

    /**
     * 生成输出工具
     *
     * @param writer 输出的流
     *
     * @since OutOf 0.0.2
     */
    @NotNull
    public
    Out<CharSequence> write(@NotNull Writer writer)
    { return new charw(writer, false); }

    /**
     * 生成自动刷入内容的输出工具
     *
     * @param writer 输出的流
     *
     * @since OutOf 0.0.2
     */
    @NotNull
    public
    Out<CharSequence> writeFlush(@NotNull Writer writer)
    { return new charw(writer, false); }

    //----------------------------------------------------------------------------------------------

    /**
     * 创建输出工具构造器
     *
     * @param autoflush 是否自动 Flush
     *
     * @since OutBuild 0.0.3
     */
    @NotNull
    public
    OutBuild build(boolean autoflush) {
        return new OutBuild() {
            @NotNull
            protected
            Out<byte[]> build(@NotNull OutputStream outputStream)
            { return new bytew(outputStream, autoflush); }

            @NotNull
            protected
            Out<CharSequence> build(@NotNull Writer writer)
            { return new charw(writer, autoflush); }
        };
    }
}
