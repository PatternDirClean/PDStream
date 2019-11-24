package fybug.nulll.pdstream.io.path;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * <h2>面向字节的文件操作器.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @since path 0.0.1
 */
public
class FileOfByte extends FileOperator<byte[], FileOfByte> {
    /** 空的操作器 */
    public
    FileOfByte() { super(); }

    /**
     * 使用文件初始化操作器
     *
     * @param file 文件路径
     */
    public
    FileOfByte(File file) { super(file); }

    /**
     * 使用路径初始化操作器
     *
     * @param path 路径
     */
    public
    FileOfByte(Path path) { super(path); }

    @Nullable
    @Override
    public
    byte[] readFirst(int maxSize) {
        if (maxSize < 0)
            return null;
        else if (maxSize == 0)
            return new byte[0];

        byte[] readdata;

        synchronized ( this ){
            try ( var stream = new BufferedInputStream(Files.newInputStream(toPath())) ) {
                readdata = stream.readNBytes(maxSize);
            } catch ( IOException e ) {
                return null;
            }
        }

        if (readdata.length > 0)
            return readdata;
        return null;
    }

    @Override
    public
    boolean writer(@Nullable byte[] data, int len) {
        if (data == null || len < 0)
            return false;
        else if (data.length == 0 || len == 0)
            return true;

        try {
            synchronized ( this ){
                Files.write(toPath(), Arrays.copyOf(data, Math.min(data.length, len)), WRITE,
                            CREATE, APPEND);
            }
        } catch ( IOException e ) {
            return false;
        }
        return true;
    }

    @Override
    public
    boolean rewrite(@Nullable byte[] data) {
        if (data == null)
            return false;

        try {
            synchronized ( this ){
                Files.write(toPath(), data, WRITE, CREATE, APPEND, TRUNCATE_EXISTING);
            }
        } catch ( IOException e ) {
            return false;
        }
        return true;
    }
}
