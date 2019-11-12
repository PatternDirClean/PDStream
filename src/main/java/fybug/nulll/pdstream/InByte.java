package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public
class InByte implements Closeable {
    /** 操作目标 */
    @NotNull private Optional<InputStream> target;

    public
    InByte() {target = Optional.empty();}

    public
    InByte(InputStream inputStream) {target = Optional.ofNullable(inputStream);}

    @Nullable
    public
    byte[] read() { return read(Integer.MAX_VALUE); }

    @Nullable
    public
    byte[] readAndClose() {
        synchronized ( this ){
            try {
                return read();
            } finally {
                close();
            }
        }
    }

    @Nullable
    public
    byte[] read(int size) {
        synchronized ( this ){
            if (target.isEmpty())
                return null;

            try {
                return target.get().readNBytes(size);
            } catch ( IOException e ) {
                return null;
            }
        }
    }

    @NotNull
    public
    InByte bin(InputStream inputStream) {
        synchronized ( this ){
            target = Optional.ofNullable(inputStream);
        }
        return this;
    }

    @NotNull
    public
    InputStream toByteStream() { return target.orElse(InputStream.nullInputStream()); }

    @Override
    public
    void close() {
        synchronized(this){
            try {
                target.orElse(InputStream.nullInputStream()).close();
            } catch (IOException e) {
            }
        }
    }
}
