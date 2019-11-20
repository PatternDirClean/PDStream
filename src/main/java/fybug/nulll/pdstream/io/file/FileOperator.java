package fybug.nulll.pdstream.io.file;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * <h2>文件操作基类.</h2>
 * 提供绑定和获取操作对象 {@link File} & {@link Path} 的接口
 *
 * @author fybug
 * @version 0.0.1
 * @since file 0.0.1
 */
abstract
class FileOperator<D, O extends FileOperator<?, ?>> {
    /** 操作对象 */
    private Optional<Path> tagert;

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
        tagert = Optional.of(path);
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
}
