package fybug.nulll.pdstream.strem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import fybug.nulll.pdstream.OPC;
import fybug.nulll.pdstream.strem.io.uilt.AsnycIn;
import fybug.nulll.pdstream.strem.io.uilt.AsnycOut;
import fybug.nulll.pdstream.strem.io.uilt.SyncIn;
import fybug.nulll.pdstream.strem.io.uilt.SyncOut;

import static fybug.nulll.pdstream.OPC.EMPY_BUFF_INPUT;
import static fybug.nulll.pdstream.OPC.EMPY_BUFF_OUTPUT;
import static fybug.nulll.pdstream.OPC.EMPY_BUFF_READ;
import static fybug.nulll.pdstream.OPC.EMPY_BUFF_WRITER;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * <h2>操作器工场.</h2>
 * <p>
 * 用于构造 <b>操作器工场和默认配置生成的操作器</b> 包含转化流为缓冲流的工具
 *
 * @author fybug
 * @version 0.0.1
 * @since stream 0.0.1
 */
public
class IOT {
    /**
     * 获取操作器构造工具
     *
     * @param inputStream 要读取的流
     *
     * @return 读取操作器构造工具
     */
    @NotNull
    public static
    In R(@NotNull InputStream inputStream) { return new In(inputStream); }

    /**
     * 获取操作器构造工具
     *
     * @param reader 要读取的流
     *
     * @return 读取操作器构造工具
     */
    @NotNull
    public static
    In R(@NotNull Reader reader) { return new In(reader); }

    /**
     * 获取操作器构造工具
     *
     * @param file 要读取的文件
     *
     * @return 读取操作器构造工具
     */
    @NotNull
    public static
    In R(@NotNull File file) { return R(file.toPath()); }

    /**
     * 获取操作器构造工具
     *
     * @param path 要读取的路径
     *
     * @return 读取操作器构造工具
     */
    @NotNull
    public static
    In R(@NotNull Path path) {
        InputStream stream;

        try {
            stream = Files.newInputStream(path);
        } catch ( IOException e ) {
            stream = EMPY_BUFF_INPUT;
        }

        return R(stream);
    }

    /*-------------------------------------------------*/

    /**
     * 获取阻塞式操作器
     *
     * @param inputStream 要读取的流
     *
     * @return 阻塞式读取工具
     */
    @NotNull
    public static
    SyncIn RS(@NotNull InputStream inputStream) { return R(inputStream).sync(); }

    /**
     * 获取阻塞式操作器
     *
     * @param reader 要读取的流
     *
     * @return 阻塞式读取工具
     */
    @NotNull
    public static
    SyncIn RS(@NotNull Reader reader) { return R(reader).sync(); }

    /**
     * 获取阻塞式操作器
     *
     * @param file 要读取的文件
     *
     * @return 阻塞式读取工具
     */
    @NotNull
    public static
    SyncIn RS(@NotNull File file) { return R(file).sync(); }

    /**
     * 获取阻塞式操作器
     *
     * @param path 要读取的路径
     *
     * @return 阻塞式读取工具
     */
    @NotNull
    public static
    SyncIn RS(@NotNull Path path) { return R(path).sync(); }

    /*-------------------------------------------------*/

    /**
     * 获取异步操作器
     *
     * @param inputStream 要读取的流
     *
     * @return 异步读取工具
     */
    @NotNull
    public static
    AsnycIn RA(@NotNull InputStream inputStream) { return R(inputStream).async(); }

    /**
     * 获取异步操作器
     *
     * @param reader 要读取的流
     *
     * @return 异步读取工具
     */
    @NotNull
    public static
    AsnycIn RA(@NotNull Reader reader) { return R(reader).async(); }

    /**
     * 获取异步操作器
     *
     * @param file 要读取的文件
     *
     * @return 异步读取工具
     */
    @NotNull
    public static
    AsnycIn RA(@NotNull File file) { return R(file).async(); }

    /**
     * 获取异步操作器
     *
     * @param path 要读取的路径
     *
     * @return 异步读取工具
     */
    @NotNull
    public static
    AsnycIn RA(@NotNull Path path) { return R(path).async(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取操作器构造工具
     *
     * @param outputStream 要写入的流
     *
     * @return 输出操作器构造工具
     */
    @NotNull
    public static
    Out O(@NotNull OutputStream outputStream) { return new Out(outputStream); }

    /**
     * 获取操作器构造工具
     *
     * @param writer 要写入的流
     *
     * @return 输出操作器构造工具
     */
    @NotNull
    public static
    Out O(@NotNull Writer writer) { return new Out(writer); }

    /**
     * 获取操作器构造工具
     *
     * @param file 要写入的文件
     *
     * @return 输出操作器构造工具
     */
    @NotNull
    public static
    Out O(@NotNull File file) { return O(file.toPath()); }

    /**
     * 获取操作器构造工具
     *
     * @param path 要写入的路径
     *
     * @return 输出操作器构造工具
     */
    @NotNull
    public static
    Out O(@NotNull Path path) {
        OutputStream stream;

        try {
            stream = Files.newOutputStream(path, WRITE, APPEND, CREATE);
        } catch ( IOException e ) {
            stream = EMPY_BUFF_OUTPUT;
        }

        return O(stream);
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取阻塞式操作器
     *
     * @param outputStream 要写入的流
     *
     * @return 阻塞式写入工具
     */
    @NotNull
    public static
    SyncOut OS(@NotNull OutputStream outputStream) { return O(outputStream).sync(); }

    /**
     * 获取阻塞式操作器
     *
     * @param writer 要写入的流
     *
     * @return 阻塞式写入工具
     */
    @NotNull
    public static
    SyncOut OS(@NotNull Writer writer) { return O(writer).sync(); }

    /**
     * 获取阻塞式操作器
     *
     * @param file 要写入的文件
     *
     * @return 阻塞式写入工具
     */
    @NotNull
    public static
    SyncOut OS(@NotNull File file) { return OS(file.toPath()); }

    /**
     * 获取阻塞式操作器
     *
     * @param path 要写入的路径
     *
     * @return 阻塞式写入工具
     */
    @NotNull
    public static
    SyncOut OS(@NotNull Path path) { return O(path).sync(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取异步操作器
     *
     * @param outputStream 要写入的流
     *
     * @return 异步输出工具
     */
    @NotNull
    public static
    AsnycOut OA(@NotNull OutputStream outputStream) { return O(outputStream).async(); }

    /**
     * 获取异步操作器
     *
     * @param writer 要写入的流
     *
     * @return 异步输出工具
     */
    @NotNull
    public static
    AsnycOut OA(@NotNull Writer writer) { return O(writer).async(); }

    /**
     * 获取异步操作器
     *
     * @param file 要写入的文件
     *
     * @return 异步输出工具
     */
    @NotNull
    public static
    AsnycOut OA(@NotNull File file) { return OA(file.toPath()); }

    /**
     * 获取异步操作器
     *
     * @param path 要写入的路径
     *
     * @return 异步输出工具
     */
    @NotNull
    public static
    AsnycOut OA(@NotNull Path path) { return O(path).async(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 转化为缓冲实现流
     *
     * @param reader input
     *
     * @return {@link OPC#EMPY_BUFF_INPUT} of param is null
     */
    @NotNull
    public static
    BufferedInputStream toBuffRead(@Nullable InputStream reader) {
        if (reader == null)
            return EMPY_BUFF_INPUT;
        if (reader instanceof BufferedInputStream)
            return (BufferedInputStream) reader;
        return new BufferedInputStream(reader);
    }

    /**
     * 转化为缓冲实现流
     *
     * @param reader reader
     *
     * @return {@link OPC#EMPY_BUFF_READ} of param is null
     */
    @NotNull
    public static
    BufferedReader toBuffRead(@Nullable Reader reader) {
        if (reader == null)
            return EMPY_BUFF_READ;
        if (reader instanceof BufferedReader)
            return (BufferedReader) reader;
        return new BufferedReader(reader);
    }

    /**
     * 转化为缓冲实现流
     *
     * @param writer writer
     *
     * @return {@link OPC#EMPY_BUFF_WRITER} of param is null
     */
    @NotNull
    public static
    BufferedWriter toBuffWriter(@Nullable Writer writer) {
        if (writer == null)
            return EMPY_BUFF_WRITER;
        if (writer instanceof BufferedWriter)
            return (BufferedWriter) writer;
        return new BufferedWriter(writer);
    }

    /**
     * 转化为缓冲实现流
     *
     * @param writer output
     *
     * @return {@link OPC#EMPY_BUFF_OUTPUT} of param is null
     */
    @NotNull
    public static
    BufferedOutputStream toBuffWriter(@Nullable OutputStream writer) {
        if (writer == null)
            return EMPY_BUFF_OUTPUT;
        if (writer instanceof BufferedOutputStream)
            return (BufferedOutputStream) writer;
        return new BufferedOutputStream(writer);
    }
}
