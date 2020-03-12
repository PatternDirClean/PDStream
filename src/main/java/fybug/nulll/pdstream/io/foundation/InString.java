package fybug.nulll.pdstream.io.foundation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;

import fybug.nulll.pdstream.io.InOf;
import fybug.nulll.pdstream.io.OPC;

import static fybug.nulll.pdstream.io.OPC.CHAR_EMPTY_DATA;
import static fybug.nulll.pdstream.io.OPC.CHAR_READ_BUFF;
import static fybug.nulll.pdstream.io.OPC.EMPY_BUFF_READ;

/**
 * <h2>作用于字符串的读取器.</h2>
 * 操作对象为 {@link Reader}
 *
 * @author fybug
 * @version 0.0.1
 * @see OPC#CHAR_EMPTY_DATA
 * @see OPC#CHAR_READ_BUFF
 * @since io 0.0.1
 */
public
class InString implements InOf<Reader, String> {
    /** 操作目标 */
    @NotNull private Reader target;

    /*--------------------------------------------------------------------------------------------*/

    /** 空的操作器 */
    public
    InString() { target = EMPY_BUFF_READ; }

    /**
     * 初始化操作器
     *
     * @param reader 初始操作对象
     */
    public
    InString(@NotNull Reader reader) { target = reader; }

    /*--------------------------------------------------------------------------------------------*/

    @Nullable
    @Override
    public
    String read(int size) throws IOException {
        if (size < 0)
            throw new IOException("read size < 0");
        else if (size == 0)
            return CHAR_EMPTY_DATA;

        var builder = new StringBuilder();
        var mark = 0;
        var buff = new char[CHAR_READ_BUFF];

        while( original().ready() ){
            if (mark >= size)
                break;

            var readsize = original().read(buff, 0, mark + CHAR_READ_BUFF < size ? CHAR_READ_BUFF
                    : size - mark);
            if (readsize == -1)
                break;
            mark += readsize;

            builder.append(buff, 0, readsize);
        }

        if (builder.length() == 0)
            return CHAR_EMPTY_DATA;

        return builder.toString();
    }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    public
    InString bin(@NotNull Reader operator) {
        synchronized ( this ){
            target = operator;
        }
        return this;
    }

    @NotNull
    @Override
    public
    Reader original() { return target; }
}
