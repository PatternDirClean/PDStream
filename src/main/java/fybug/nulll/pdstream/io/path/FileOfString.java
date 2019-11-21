package fybug.nulll.pdstream.io.path;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * <h2>面向字符串的文件操作器.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @since path 0.0.1
 */
public
class FileOfString extends FileOperator<String, FileOfString> {
    /** 空的操作器 */
    protected
    FileOfString() { super(); }

    /**
     * 使用文件初始化操作器
     *
     * @param file 文件路径
     */
    protected
    FileOfString(File file) { super(file); }

    /**
     * 使用路径初始化操作器
     *
     * @param path 路径
     */
    protected
    FileOfString(Path path) { super(path); }

    /**
     * 读取第一行数据
     *
     * @return line
     */
    @Nullable
    public
    String readFirstLine() {
        String readdata;

        synchronized ( this ){
            try ( var stream = Files.newBufferedReader(toPath()) ) {
                readdata = stream.readLine();
            } catch ( IOException e ) {
                return null;
            }
        }

        if (readdata == null)
            return null;
        return readdata.trim();
    }

    /**
     * 读取所有的行
     * 按照 {@code '\r\n' | '\n'} 分割
     *
     * @return lines
     */
    @Nullable
    public
    List<String> readAllLine() {
        List<String> list;

        synchronized ( this ){
            try {
                list = Files.readAllLines(toPath());
            } catch ( IOException e ) {
                return null;
            }
        }

        if (list.size() == 0)
            return null;
        return list;
    }

    /**
     * 写入一行数据
     * 使用 {@link #writer(String)} 写入并追加 {@link System#lineSeparator()}
     *
     * @param data lines list
     */
    public
    boolean writerLine(@Nullable String data) { return writer(data + System.lineSeparator()); }

    @Nullable
    @Override
    public
    String readFirst(int maxSize) {
        if (maxSize < 0)
            return null;
        else if (maxSize == 0)
            return "";

        var buff = new char[maxSize];
        var readsize = 0;

        synchronized ( this ){
            try ( var stream = Files.newBufferedReader(toPath()) ) {
                readsize = stream.read(buff);
            } catch ( IOException e ) {
                return null;
            }
        }

        if (readsize > 0)
            return new String(buff, 0, readsize).trim();
        else
            return null;
    }

    @Override
    public
    boolean writer(@Nullable String data, int len) {
        if (data == null || len < 0)
            return false;
        else if (data.length() == 0 || len == 0)
            return true;

        try {
            synchronized ( this ){
                Files.writeString(toPath(), data.substring(Math.min(data.length(), len)).trim(),
                                  WRITE, CREATE, APPEND);
            }
        } catch ( IOException e ) {
            return false;
        }

        return true;
    }

    @Override
    public
    boolean rewrite(@Nullable String data) {
        if (data == null)
            return false;
        else if (data.length() == 0)
            return true;

        try {
            synchronized ( this ){
                Files.writeString(toPath(), data.trim(), WRITE, CREATE, APPEND, TRUNCATE_EXISTING);
            }
        } catch ( IOException e ) {
            return false;
        }

        return true;
    }
}
