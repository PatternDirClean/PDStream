package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fybug.nulll.pdstream.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.OPC.CHARSET;
import static fybug.nulll.pdstream.OPC.CHAR_EMPTY_DATA;
import static fybug.nulll.pdstream.OPC.CHAR_READ_BUFF;
import static fybug.nulll.pdstream.strem.IOT.toBuffRead;

/**
 * <h2>PDStream 附加工具包.</h2>
 * 主要用于增强该包的功能
 * 并用于衔接不同操作器间的数据操作
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public
class OPT {
    /**
     * 整合字符串
     *
     * @param strings string[]
     *
     * @return string
     */
    @NotNull
    public static
    String combineSring(String... strings) {
        if (strings.length == 0)
            return CHAR_EMPTY_DATA;

        return Arrays.stream(strings)
                     .filter(Objects::nonNull)
                     .reduce(new StringBuilder(CHAR_READ_BUFF), StringBuilder::append, (a, b) -> null)
                     .toString();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 按行拆分字符串
     * <p>
     * 空的行不会被解析
     * {@code '\r\n','\n'} 作为换行均可
     *
     * @param data string
     *
     * @return list
     */
    @NotNull
    public static
    List<String> passOfLine(@Nullable String data) {
        if (data == null || data.length() == 0)
            return Collections.emptyList();

        return toBuffRead(new StringReader(data)).lines()
                                                 .map(String::trim)
                                                 .filter(v -> !v.isEmpty())
                                                 .collect(Collectors.toList());
    }

    /**
     * 整合行数据列表
     *
     * @param collection lines list
     *
     * @return string
     */
    @NotNull
    public static
    String linesString(@Nullable Collection<String> collection) {
        if (collection == null || collection.size() == 0)
            return CHAR_EMPTY_DATA;

        // 换行符
        var lineseparator = System.lineSeparator();
        // 缓冲区
        var strbuff = collection.stream()
                                .filter(v -> v != null && !v.trim().isEmpty())
                                .map(v -> v.trim() + lineseparator)
                                .reduce(new StringBuilder(CHAR_READ_BUFF), StringBuilder::append, (a, b) -> null);

        /* 移除尾部换行符 */
        if (strbuff.length() > 0)
            strbuff.setLength(strbuff.length() - lineseparator.length());
        strbuff.trimToSize();

        return strbuff.toString();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 字节解析为字符串
     *
     * @param bytes byte
     *
     * @return string
     */
    @NotNull
    public static
    String stringByte(@Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return CHAR_EMPTY_DATA;

        return new String(bytes, CHARSET);
    }

    /**
     * 整合字符串并转化为字节
     * <p>
     * 仅将字符串拼接并转换
     *
     * @param s 转化的字符串
     *
     * @return byte
     */
    @NotNull
    public static
    byte[] byteString(String... s) {
        if (s.length == 0)
            return BYTE_EMPTY_DATA;

        return Arrays.stream(s)
                     .filter(Objects::nonNull)
                     .reduce(new StringBuilder(CHAR_READ_BUFF), StringBuilder::append, (a, v) -> null)
                     .toString()
                     .getBytes();
    }

    /*----------------------------------------------------------------------------------------*/

    /**
     * 反序列化
     * <p>
     * 空数据会返回 {@code null}
     *
     * @param bytes byte
     *
     * @return Serializable
     */
    @Nullable
    public static
    java.io.Serializable byteSerializable(@Nullable byte[] bytes) {
        // check
        if (bytes == null || bytes.length == 0)
            return null;

        var buffRead = new ByteArrayInputStream(bytes);

        try {
            var objRead = new ObjectInputStream(buffRead);

            var ob = objRead.readObject();
            objRead.close();

            return (Serializable) ob;
        } catch ( IOException | ClassNotFoundException e ) {
            return null;
        }
    }

    /**
     * 序列化对象为字节
     *
     * @param o Serializable
     *
     * @return byte
     */
    @NotNull
    public static
    byte[] serializable(@Nullable java.io.Serializable o) {
        /* check:校验 */
        if (o == null)
            return BYTE_EMPTY_DATA;
        if (o instanceof String)
            return byteString((String) o);
        /* // check */

        var buff = new ByteArrayOutputStream();

        try {
            var objout = new ObjectOutputStream(buff);

            objout.writeObject(o);
            objout.close();

            return buff.toByteArray();
        } catch ( IOException e ) {
            return new byte[0];
        }
    }
}
