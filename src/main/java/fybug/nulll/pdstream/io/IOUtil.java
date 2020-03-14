package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * <h2>PDStream 附加工具包.</h2>
 * 并用于转换数据的类型
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.1.1
 */
@UtilityClass
public
class IOUtil {
    /**
     * 整合字符串
     *
     * @param strings string[]
     *
     * @return string
     */
    @NotNull
    public
    String combineSring(String... strings) {
        return Arrays.stream(strings)
                     .filter(Objects::nonNull)
                     .reduce(new StringBuilder(1024), StringBuilder::append, (a, b) -> null)
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
    public
    List<String> passOfLine(String data) {
        return new BufferedReader(new StringReader(data)).lines()
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
    public
    String linesString(Collection<String> collection) {
        // 换行符
        var lineseparator = System.lineSeparator();
        // 缓冲区
        var strbuff = collection.stream()
                                .filter(v -> v != null && !v.trim().isEmpty())
                                .map(v -> v.trim() + lineseparator)
                                .reduce(new StringBuilder(1024), StringBuilder::append, (a, b) -> null);

        /* 移除尾部换行符 */
        if (strbuff.length() > 0)
            strbuff.setLength(strbuff.length() - lineseparator.length());
        strbuff.trimToSize();

        return strbuff.toString();
    }

    /*--------------------------------------------------------------------------------------------*/

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
    public
    byte[] byteString(String... s) {
        return Arrays.stream(s)
                     .filter(Objects::nonNull)
                     .reduce(new StringBuilder(1024), StringBuilder::append, (a, v) -> null)
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
    public
    Serializable byteSerializable(byte[] bytes) {
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
    public
    byte[] serializable(Serializable o) {
        if (o instanceof String)
            return ((String) o).getBytes();

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
