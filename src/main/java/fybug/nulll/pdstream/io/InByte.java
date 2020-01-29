package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import fybug.nulll.pdstream.InOf;
import fybug.nulll.pdstream.OPC;

import static fybug.nulll.pdstream.OPC.BYTE_DEFAULT_DATA;
import static fybug.nulll.pdstream.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.OPC.EMPY_BUFF_INPUT;

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
    byte[] read(int size) {
        /* check:指定长度是否超过位置 */
        if (size < 0)
            return BYTE_EMPTY_DATA;
        else if (size == 0)
            return BYTE_DEFAULT_DATA;
        /* // check */

        byte[] bytes;

        try {
            synchronized ( this ){
                bytes = original().readNBytes(size);
            }
        } catch ( IOException e ) {
            return BYTE_EMPTY_DATA;
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
