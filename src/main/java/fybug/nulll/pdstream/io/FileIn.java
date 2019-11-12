package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import fybug.nulll.pdstream.In;

public
class FileIn implements In<File, byte[]> {
    @NotNull private Optional<Path> fileOptional;

    public
    FileIn() { fileOptional = Optional.empty(); }

    public
    FileIn(File file) {links(file);}

    public
    FileIn(Path path) {this(path.toFile());}

    @Override
    public @Nullable
    byte[] reader() {
        try {
            synchronized ( this ){
                if (fileOptional.isEmpty())
                    return null;

                return Files.readAllBytes(fileOptional.get());
            }
        } catch ( IOException e ) {
            return null;
        }
    }

    @Override
    public @NotNull
    FileIn links(@NotNull File links) {
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
     * 读取文件中的数据
     *
     * @param file 要读取的文件
     *
     * @return byte[]
     */
    @Nullable
    public static
    byte[] read(@NotNull File file) {return new FileIn(file).reader();}

    /**
     * 从路径中读取数据
     *
     * @param path 要读取的路径
     *
     * @return byte[]
     */
    @Nullable
    public static
    byte[] read(@NotNull Path path) { return new FileIn(path).reader(); }
}
