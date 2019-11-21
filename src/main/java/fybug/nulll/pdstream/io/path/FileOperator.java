package fybug.nulll.pdstream.io.path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * <h2>文件操作基类.</h2>
 * 提供绑定和获取操作对象 {@link File} & {@link Path} 的接口
 * 提供读与写的基础操作接口
 * 接口中的操作均不会保留，即本次读取完成后下次读取依旧从头开始读取
 *
 * @author fybug
 * @version 0.0.1
 * @since path 0.0.1
 */
abstract
class FileOperator<D, O extends FileOperator<?, ?>> {
    /** 操作对象 */
    private volatile Optional<Path> tagert;

    /** 空的操作器 */
    protected
    FileOperator() {tagert = Optional.empty();}

    /**
     * 使用文件初始化操作器
     *
     * @param file 文件路径
     */
    protected
    FileOperator(File file) {this(file.toPath());}

    /**
     * 使用路径初始化操作器
     *
     * @param path 路径
     */
    protected
    FileOperator(Path path) {bin(path);}

    /**
     * 读取所有的数据
     *
     * @return data
     */
    @Nullable
    public
    D readAll() {return readFirst(Integer.MAX_VALUE);}

    /**
     * 读取位于文件开头指定长度的数据
     *
     * @param maxSize 数据长度
     *
     * @return data
     */
    @Nullable
    public abstract
    D readFirst(int maxSize);

    /**
     * 写入数据到路径中
     *
     * @param data data
     *
     * @return 是否写入成功
     */
    public
    boolean writer(@Nullable D data) {return writer(data, Integer.MAX_VALUE);}

    /**
     * 写入指定长度的数据到路径中
     *
     * @param data data
     * @param len  写入的长度
     *
     * @return 是否写入成功
     */
    public abstract
    boolean writer(@Nullable D data, int len);

    /**
     * 并联写入数据
     * 用于进行链式的写入操作
     * 与 {@link #writer(Object)} 的区别为返回的是该对象而不是是否成功
     *
     * @param data data
     *
     * @return this
     */
    @NotNull
    public
    O append(@Nullable D data) {
        writer(data);
        return (O) this;
    }

    /**
     * 重写文件内的数据
     *
     * @param data data
     *
     * @return 是否成功
     */
    public abstract
    boolean rewrite(@Nullable D data);

    /**
     * 绑定文件路径
     *
     * @param file 要操作的文件路径
     *
     * @return this
     */
    @NotNull
    public
    O bin(File file) {return bin(file.toPath());}

    /**
     * 绑定路径
     *
     * @param path 要操作的路径
     *
     * @return this
     */
    @NotNull
    public
    O bin(Path path) {
        synchronized ( this ){
            tagert = Optional.of(path);
        }
        return (O) this;
    }

    /** 获取操作对象的文件类 */
    @NotNull
    public
    Path toPath() {return tagert.get();}

    /** 获取操作对象的路径类 */
    @NotNull
    public
    File toFile() {return tagert.get().toFile();}

    /** 解除当前绑定的对象 */
    public
    void clear() {
        synchronized ( this ){
            tagert = Optional.empty();
        }
    }
}
