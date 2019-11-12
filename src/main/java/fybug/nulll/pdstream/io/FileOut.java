package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import fybug.nulll.pdstream.Out;

public
class FileOut implements Out<File, byte[]> {
    @NotNull private Optional<Path> fileOptional;

    public
    FileOut() {fileOptional = Optional.empty();}

    public
    FileOut(File file) {links(file);}

    public
    FileOut(Path path) {this(path.toFile());}

    @Override
    public
    boolean writer(@NotNull byte[] daras) {
        try {
            synchronized ( this ){
                if (fileOptional.isEmpty())
                    return false;

                Files.write(fileOptional.get(), daras, StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            }
            return true;
        } catch ( IOException e ) {
            return false;
        }
    }

    @Override
    public
    void flush() {}

    @Override
    public @NotNull
    FileOut links(@NotNull File links) {
        synchronized ( this ){
            fileOptional = Optional.ofNullable(links.toPath());
        }
        return this;
    }

    @Override
    public
    void close() {
        synchronized ( this ){
            fileOptional = Optional.empty();
        }
    }

    /**
     * 写入数据到文件中
     *
     * @param file  要写入的文件
     * @param bytes byte[]
     *
     * @return 是否写入成功
     */
    public static
    boolean write(@NotNull File file, @NotNull byte[] bytes)
    { return new FileOut(file).writerAndFlush(bytes); }

    /**
     * 写入数据到路径中
     *
     * @param path  要写入的路径
     * @param bytes byte[]
     *
     * @return 是否写入成功
     */
    public static
    boolean write(@NotNull Path path, @NotNull byte[] bytes)
    {return new FileOut(path).writerAndFlush(bytes);}
}
