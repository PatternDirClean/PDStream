package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import fybug.nulll.pdstream.io.uilt.AsnyOut;
import fybug.nulll.pdstream.io.uilt.Out;
import lombok.experimental.UtilityClass;

/**
 * <h2>输出工具.</h2>
 * <p>
 * 构造生成的工具类为 {@link Out}，对应的异步工具类为 {@link AsnyOut}<br/>
 * 读取过程失败返回 {@code false}
 *
 * @author fybug
 * @version 0.0.2
 * @since io 0.0.1
 */
@UtilityClass
public // todo test 示例
class OutOf {
    /**
     * 字节输出工具实现
     *
     * @author fybug
     * @version 0.0.1
     * @since OutOf 0.0.2
     */
    private final
    class bytew extends Out<OutputStream, byte[]> {
        private
        bytew(OutputStream outputStream, boolean needFlush)
        { super(outputStream, needFlush, byte[].class); }

        @Override
        protected
        void write0(@NotNull OutputStream outputStream, byte @NotNull [] data) throws IOException
        { outputStream.write(data); }
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
    Out<OutputStream, byte[]> write(@NotNull OutputStream outputStream)
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
    Out<OutputStream, byte[]> writeFlush(@NotNull OutputStream outputStream)
    { return new bytew(outputStream, true); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 字符输出工具实现
     *
     * @author fybug
     * @version 0.0.1
     * @since OutOf 0.0.2
     */
    private final
    class charw extends Out<Writer, CharSequence> {
        private
        charw(Writer writer, boolean needFlush) { super(writer, needFlush, CharSequence.class); }

        @Override
        protected
        void write0(@NotNull Writer writer, @NotNull CharSequence data) throws IOException
        { writer.write(data.toString()); }
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
    Out<Writer, CharSequence> write(@NotNull Writer writer)
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
    Out<Writer, CharSequence> writeFlush(@NotNull Writer writer)
    { return new charw(writer, false); }
}
