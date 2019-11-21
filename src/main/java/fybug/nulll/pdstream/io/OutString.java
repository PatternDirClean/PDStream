package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import fybug.nulll.pdstream.OutOfStream;

/**
 * <h2>作用于字符串的写入器.</h2>
 * 操作对象为 {@link Writer}
 * 可进行以单行字符为对象的操作
 * 操作缓存器 {@link BuffOf} 追加行操作
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
public
class OutString implements OutOfStream<Writer, String> {
    /** 操作目标 */
    @NotNull private Optional<Writer> target;

    /** 空的操作器 */
    public
    OutString() {target = Optional.empty();}

    /**
     * 初始化操作器
     *
     * @param writer 初始操作目标
     */
    public
    OutString(@Nullable Writer writer) {target = Optional.ofNullable(writer);}

    /**
     * 写入一行数据
     * 输入当前系统的换行符 {@link System#lineSeparator()}
     *
     * @param data 输出的一行数据
     *
     * @return 是否成功
     */
    public
    boolean writeLine(@Nullable String data) { return write(data + System.lineSeparator()); }

    @Override
    public
    boolean write(@Nullable String data, int len) {
        if (data == null || len < 0)
            return false;
        else if (data.length() == 0 || len == 0)
            return true;

        try {
            synchronized ( this ){
                original().write(data, 0, Math.min(data.length(), len));
            }
        } catch ( IOException e ) {
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public synchronized
    OutString.BuffOf append(@Nullable String data) { return new BuffOf(original(), data); }

    @NotNull
    @Override
    public
    OutString bin(@Nullable Writer operator) {
        synchronized ( this ){
            target = Optional.ofNullable(operator);
        }
        return this;
    }

    @NotNull
    @Override
    public
    Writer original() { return target.orElse(Writer.nullWriter()); }

    /**
     * <h2>字符缓存桥接.</h2>
     * 所有通过 {@link #append(String)} 写入的数据将会缓存直到执行 {@link #close()} | {@link #flush()}
     * <p>
     * 追加行输出 {@link #appendLine(String)}
     *
     * @author fybug
     * @version 0.0.1
     * @since OutString 0.0.1
     */
    public final static
    class BuffOf extends Buff<Writer, String> {
        private
        BuffOf(@NotNull Writer out, @Nullable String data) { super(out, data); }

        @Override
        protected
        boolean check(@Nullable String data) { return super.check(data) && data.length() != 0; }

        @Override
        protected
        void flush0(@NotNull String data) throws Exception { tagre.write(data); }

        /**
         * 追加数据并增加换行符
         *
         * @see #append(String)
         */
        @NotNull
        public
        BuffOf appendLine(@Nullable String data) {
            super.append(data + System.lineSeparator());
            return this;
        }
    }
}
