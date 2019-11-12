package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public
class OutByte implements Closeable, Flush {
    /** 操作目标 */
    @NotNull private Optional<OutputStream> target;

    public
    OutByte() {target = Optional.empty();}

    public
    OutByte(OutputStream outputsteam) {target = Optional.ofNullable(outputsteam);}

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
    OutByte writer(int size) {
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
    OutByte bin(OutputStream outputsteam) {
        synchronized ( this ){
            target = Optional.ofNullable(outputsteam);
        }
        return this;
    }

    @NotNull
    public
    OutputStream toByteStream() { return target.orElse(OutputStream.nullInputStream()); }

    @Override
    public void flush() {
        synchronized(this){
            try {
                target.orElse(OutputStream.nullInputStream()).flush();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public
    void close() {
        synchronized(this){
            try {
                target.orElse(OutputStream.nullInputStream()).close();
            } catch (IOException e) {
            }
        }
    }
}
