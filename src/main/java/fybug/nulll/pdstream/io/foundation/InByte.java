package fybug.nulll.pdstream.io.foundation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import fybug.nulll.pdstream.io.InOf;
import fybug.nulll.pdstream.io.OPC;

import static fybug.nulll.pdstream.io.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.io.OPC.EMPY_BUFF_INPUT;

/**
 * <h2>字节读取器.</h2>
 * 操作对象为 {@link InputStream}
 *
 * @author fybug
 * @version 0.0.1
 * @see OPC#BYTE_EMPTY_DATA
 * @since io 0.0.1
 */
public
class InByte implements InOf<InputStream, byte[]> {
    /** 操作目标 */
    @NotNull private InputStream target;

    /*--------------------------------------------------------------------------------------------*/

    /** 空的操作器 */
    public
    InByte() { target = EMPY_BUFF_INPUT; }

    /**
     * 初始化操作器
     *
     * @param inputStream 初始操作目标
     */
    public
    InByte(@NotNull InputStream inputStream) {target = inputStream;}

    /*--------------------------------------------------------------------------------------------*/

    @Override
    @Nullable
    public
    byte[] read(int size) throws IOException {
        /* check:指定长度是否超过位置 */
        if (size < 0)
            throw new IOException("read size < 0");
        else if (size == 0)
            return BYTE_EMPTY_DATA;
        /* // check */

        byte[] bytes;

        synchronized ( this ){
            bytes = original().readNBytes(size);
        }

        if (bytes.length == 0)
            return BYTE_EMPTY_DATA;
        return bytes;
    }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    public
    InByte bin(@NotNull InputStream operator) {
        synchronized ( this ){
            target = operator;
        }
        return this;
    }

    @NotNull
    @Override
    public
    InputStream original() { return target; }
}
