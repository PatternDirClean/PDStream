package fybug.nulll.pdstream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <h2>PDStream 附加工具包.</h2>
 * 主要用于增强该包的功能
 * 并用于衔接不同操作器间的数据操作
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
public final
class OperatorTOOL {
    @Deprecated
    private
    OperatorTOOL() {}

    /** @see #passOfLine(String, int) */
    @NotNull
    public static
    List<String> passOfLine(@Nullable String data) {return passOfLine(data, Integer.MAX_VALUE);}

    /**
     * 按行拆分字符串
     * <p>
     * 空的行不会被解析
     * {@code '\r\n','\n'} 作为换行均可
     *
     * @param data    string
     * @param maxSize 单行最大字数
     *
     * @return list
     */
    @NotNull
    public static
    List<String> passOfLine(@Nullable String data, int maxSize) {
        maxSize = Math.max(maxSize, 1);
        if (data == null || data.length() == 0)
            return Collections.emptyList();

        var list = new LinkedList<String>();
        var buff = new StringBuilder(1024);

        for ( char c : data.toCharArray() ){
            // 检查是否到上限
            if (buff.length() == maxSize) {
                list.add(buff.toString());
                buff.setLength(0);
                continue;
            }

            switch ( c ) {
                case '\n':
                case '\r':
                    if (buff.length() > 0) {
                        list.add(buff.toString());
                        buff.setLength(0);
                    }
                    continue;
            }
            buff.append(c);
        }

        if (buff.length() > 0)
            list.add(buff.toString());

        return list;
    }

    /**
     * 字节解析为字符串
     *
     * @param bytes byte
     *
     * @return 传入空数据返回 {@code ""}
     */
    @NotNull
    public static
    String byteOf(@Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return "";
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 整合字符并转化为字节
     * <p>
     * 列表中一个元素将视为一行字符串
     * 在整合过程中会进行 {@link String#trim()}
     * 空字符串不会被整合
     *
     * @param strings 字符串列表
     *
     * @return byte
     */
    @NotNull
    public static
    byte[] ofByte(@NotNull List<String> strings) {
        if (strings.size() == 0)
            return new byte[0];
        if (strings.size() == 1 && strings.get(0) != null)
            return strings.get(0).getBytes();

        var stringbuilder = new StringBuilder();

        strings.forEach(v -> {
            if (v != null && !v.trim().isEmpty())
                stringbuilder.append(v.trim()).append(System.lineSeparator());
        });

        return stringbuilder.toString().trim().getBytes();
    }

    /**
     * 整合为字符串并转化为字节
     * <p>
     * 仅将字符串拼接并转换
     *
     * @param s 转化的字符串
     *
     * @return byte
     */
    @NotNull
    public static
    byte[] ofByte(String... s) {
        if (s.length == 0)
            return new byte[0];
        if (s.length == 1 && s[0] != null)
            return s[0].getBytes();

        var stringBuilder = new StringBuilder();

        for ( String nows : s ){
            if (nows != null)
                stringBuilder.append(nows);
        }

        return stringBuilder.toString().getBytes();
    }

    /**
     * 反序列化
     * <p>
     * 空数据会返回 {@code null}
     *
     * @param bytes byte
     *
     * @return 反序列化后的对象
     */
    @Nullable
    public static
    java.io.Serializable byteSerializable(@Nullable byte[] bytes) {
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
     * @param o 可序列化对象
     *
     * @return byte
     */
    @NotNull
    public static
    byte[] serializable(@Nullable java.io.Serializable o) {
        if (o == null)
            return new byte[0];
        if (o instanceof String)
            return ofByte((String) o);

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
