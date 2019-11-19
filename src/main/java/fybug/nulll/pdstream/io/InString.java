package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import fybug.nulll.pdstream.InOfStream;

/**
 * <h2>作用于字符串的读取器.</h2>
 * 操作对象为 {@link Reader}
 * 可进行以单行字符为对象的操作
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
public
class InString implements InOfStream<Reader, String> {
    /** 操作目标 */
    @NotNull private Optional<Reader> target;

    /** 空的操作器 */
    public
    InString() { target = Optional.empty(); }

    /**
     * 初始化操作器
     *
     * @param reader 初始操作对象
     */
    public
    InString(@Nullable Reader reader) { target = Optional.ofNullable(reader); }

    /**
     * 读取一行数据
     *
     * @return line
     */
    @Nullable
    public
    String readLine() {return readLine(Integer.MAX_VALUE);}

    /**
     * 读取一行数据
     * <p>
     * 超过指定数量会自动判断为一行
     *
     * @param maxSize 最大数量
     *
     * @return line
     */
    @Nullable
    public
    String readLine(int maxSize) {
        var stringBuilder = new StringBuilder(1024);
        var c = 0;

        try {
            synchronized ( this ){
                var nowstream = original();
                for ( var i = 0; i < maxSize; i++ ){
                    c = nowstream.read();

                    if (c == -1 || c == '\r')
                        break;
                    if (c == '\n') {
                        if (i > 0)
                            break;
                        continue;
                    }

                    stringBuilder.append((char) c);
                }
            }
        } catch ( IOException e ) {
            return null;
        }

        if (stringBuilder.length() == 0)
            return null;

        stringBuilder.trimToSize();
        return stringBuilder.toString();
    }

    @Nullable
    @Override
    public
    String read(int size) {
        var stringBuilder = new StringBuilder(1024);
        var c = 0;

        try {
            synchronized ( this ){
                var nowstream = original();
                for ( var i = 0; i < size; i++ ){
                    c = nowstream.read();

                    if (c == -1)
                        break;

                    stringBuilder.append((char) c);
                }
            }
        } catch ( IOException e ) {
            return null;
        }

        if (stringBuilder.length() == 0)
            return null;

        stringBuilder.trimToSize();
        return stringBuilder.toString();
    }

    @NotNull
    @Override
    public
    InString bin(@Nullable Reader operator) {
        synchronized ( this ){
            target = Optional.ofNullable(operator);
        }
        return this;
    }

    @NotNull
    @Override
    public
    Reader original() { return target.orElse(Reader.nullReader()); }
}
