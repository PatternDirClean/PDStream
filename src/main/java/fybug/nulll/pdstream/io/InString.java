package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;

import fybug.nulll.pdstream.InOfStream;

/**
 * <h2>作用于字符串的读取器.</h2>
 * 操作对象为 {@link Reader}
 * 可进行以单行字符为对象的操作
 *
 * @author fybug
 * @version 0.0.1
 * @see InOfStream#EMPY_BUFF_READ
 * @see InOfStream#ofBuffStream(Reader)
 * @since io 0.0.1
 */
public
class InString implements InOfStream<Reader, String> {
    /** 操作目标 */
    @NotNull private BufferedReader target;

    /** 空的操作器 */
    public
    InString() { target = EMPY_BUFF_READ; }

    /**
     * 初始化操作器
     *
     * @param reader 初始操作对象
     */
    public
    InString(@Nullable Reader reader) { target = InOfStream.ofBuffStream(reader); }

    /**
     * reads lines data
     *
     * @return line, can`t reads will to {@code null}
     */
    @Nullable
    public
    String readLine() {
        String readdata;

        try {
            synchronized ( this ){
                readdata = ((BufferedReader) original()).readLine();
            }
        } catch ( IOException e ) {
            return null;
        }

        if (readdata == null)
            return null;
        return readdata.trim();
    }

    /**
     * 处理所有行数据
     *
     * @return lines stream
     */
    @Nullable
    public
    Stream<String> readAllLine() {
        synchronized ( this ){
            return ((BufferedReader) original()).lines().parallel();
        }
    }

    @Nullable
    @Override
    public
    String read(int size) {
        if (size < 0)
            return null;
        else if (size == 0)
            return "";

        var buff = new char[size];
        var readsize = 0;

        try {
            synchronized ( this ){
                readsize = original().read(buff);
            }
        } catch ( IOException e ) {
            return null;
        }

        if (readsize > 0)
            return new String(buff, 0, readsize);
        return null;
    }

    @NotNull
    @Override
    public
    InString bin(@Nullable Reader operator) {
        synchronized ( this ){
            target = InOfStream.ofBuffStream(operator);
        }
        return this;
    }

    @NotNull
    @Override
    public
    Reader original() { return target; }
}
