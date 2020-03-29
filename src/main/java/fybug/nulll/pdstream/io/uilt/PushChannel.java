package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.trySupplier;
import fybug.nulll.pdstream.io.IOUtil;
import lombok.experimental.Accessors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * <h2>推送通道.</h2>
 * 定义数据容器，并按照指定规则输出数据<br/>
 * 构造请使用 {@link #build()}，普通构造方法建议仅在需要自定义输出规则 && 熟读该工具源码的情况下使用<br/>
 * 每次推送都推送全部数据，使用 {@link #append(Object)} 追加数据的时候标准的流中的表现应该为 {@code {追加前的数据} + {追加前的数据} + {追加的数据}}<br/>
 * 但是使用定时输出则为，在时间节点前完成追加则是 {@code {追加前的数据} + {追加的数据}} 在时间节点后完成追加则是上面相同<br/>
 * 但在开启 {@link Build#clear()} 之后两个数据都将会为 {@code {追加前的数据} + {追加的数据}}<br/>
 * 使用文件作为指向的时候请查看 {@link Build#append()}
 * <br/><br/>
 * <pre>使用示例：
 *    public static
 *     void main(String[] args) {
 *         try ( var channel = PushChannel.build()
 *                                        .point(OutputStream.nullOutputStream())
 *                                        .clear()
 *                                        .sync()
 *         ) {
 *             channel.set("");
 *             channel.append("");
 *             // 强制输出
 *             channel.send();
 *         } catch ( IOException e ) {
 *             e.printStackTrace();
 *         }
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.2
 * @since uilt 0.0.4
 */
public abstract
class PushChannel implements Closeable {
    // 流生产方法
    private trySupplier<? extends Flushable, IOException> OUT;
    // 是否是字符流
    private final boolean IS_WRITE;
    // 是否是流载体
    private final boolean IS_STREAM;

    /** 锁 */
    protected final SyLock LOCK = SyLock.newObjLock();
    /** 数据 */
    protected Object data;
    /** 是否清除残留数据 */
    protected boolean needclear;

    //----------------------------------------------------------------------------------------------

    /**
     * 生成保存通道
     *
     * @param out       指向目标工场方法
     * @param aclass    目标类型
     * @param isstreaem 是否指向直接的流对象
     * @param <O>       指向的对象
     */
    public
    <O extends Flushable> PushChannel(@NotNull trySupplier<O, IOException> out,
                                      @NotNull Class<O> aclass, boolean isstreaem)
    throws IOException
    { this(out, aclass, isstreaem, false); }

    /**
     * 生成保存通道
     *
     * @param out       指向目标工场方法
     * @param aclass    目标类型
     * @param isstreaem 是否指向直接的流对象
     * @param clear     是否在 {@link #send()} 之后清空当前数据
     * @param <O>       指向的对象
     */
    protected
    <O extends Flushable> PushChannel(@NotNull trySupplier<O, IOException> out,
                                      @NotNull Class<O> aclass, boolean isstreaem, boolean clear)
    throws IOException
    {
        OUT = out;
        IS_STREAM = isstreaem;
        /* 校验 */
        if (Writer.class.isAssignableFrom(aclass)) {
            IS_WRITE = true;
            data = "";
        } else if (OutputStream.class.isAssignableFrom(aclass)) {
            IS_WRITE = false;
            data = new byte[0];
        } else
            throw new IOException("Type is not Writer or OutputStream!");
        needclear = clear;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 设置数据，在满足输出条件的时会进行输出
     *
     * @param data byte[] | String | Serializable
     */
    public
    void set(@NotNull Object data) throws IOException {
        // 处理后的数据
        Object da;

        if (IS_WRITE) {
            /* 字符流处理 */
            if (data instanceof String)
                da = data;
            else if (data instanceof byte[])
                da = new String((byte[]) data, UTF_8);
            else
                throw new IOException("data type not is byte[] or String");

        } else {
            /* 字节流处理 */
            if (data instanceof byte[])
                da = data;
            else if (data instanceof String)
                da = ((String) data).getBytes(UTF_8);
            else if (data instanceof Serializable)
                da = IOUtil.serializable((Serializable) data);
            else
                throw new IOException("data type not is byte[] or String or Serializable");
        }
        // 输出处理后的数据
        LOCK.write(() -> this.data = da);

        if (canWrite())
            send();
    }

    /**
     * 追加数据，在满足输出条件时会进行输出
     *
     * @param da byte[] | String | Serializable
     */
    public
    void append(@NotNull Object da) throws IOException {
        // 处理后的数据
        Object ree;

        if (IS_WRITE) {
            // 缓存流
            var out = new StringWriter(((String) this.data).length());
            // 原数据
            out.write((String) this.data);

            /* 字符流处理 */
            if (da instanceof String)
                out.write((String) da);
            else if (da instanceof byte[])
                out.write(new String((byte[]) da, UTF_8));
            else
                throw new IOException("data type not is byte[] or String");

            ree = out.toString();
        } else {
            // 缓存流
            var out = new ByteArrayOutputStream(((byte[]) this.data).length);
            // 原数据
            out.write((byte[]) this.data);

            /* 字节流处理 */
            if (da instanceof byte[])
                out.write((byte[]) da);
            else if (da instanceof String)
                out.write(((String) da).getBytes(UTF_8));
            else if (da instanceof Serializable)
                out.write(IOUtil.serializable((Serializable) da));
            else
                throw new IOException("data type not is byte[] or String or Serializable");

            ree = out.toByteArray();
        }
        // 设置数据
        LOCK.write(() -> this.data = ree);

        if (canWrite())
            send();
    }

    //----------------------------------------------------------------------------------------------

    /** 是否达成输出条件 */
    protected abstract
    boolean canWrite();

    //----------------------------------------------------------------------------------------------

    /**
     * 发送数据
     * <p>
     * 此处会强制输出数据
     */
    public
    void send() throws IOException {
        synchronized ( this ){
            LOCK.trywrite(IOException.class, () -> {
                var out = OUT.get();

                if (IS_WRITE) {
                    ((Writer) out).write((String) data);
                    data = needclear ? "" : data;
                } else {
                    ((OutputStream) out).write((byte[]) data);
                    data = needclear ? new byte[0] : data;
                }
                out.flush();

                if (IS_STREAM)
                    return;

                ((Closeable) out).close();
            });
        }
    }

    @Override
    public
    void close() throws IOException {
        if (IS_STREAM)
            synchronized ( this ){
                ((Closeable) OUT.get()).close();
            }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取构造工场
     *
     * @see Build
     */
    @NotNull
    public static
    Build build() { return new Build(); }

    /**
     * 构造工场
     *
     * @author fybug
     * @version 0.0.1
     * @since PushChannel 0.0.2
     */
    @SuppressWarnings( "all" )
    @Accessors( fluent = true, chain = true )
    public static
    class Build {
        // 指向对象工场方法
        private trySupplier<? extends Flushable, IOException> out = OutputStream::nullOutputStream;
        // 指向类型
        private Class<? extends Flushable> aclass = OutputStream.class;
        // 是否指向流
        private boolean isstreaem = true;

        // 文件采用追加写入
        private boolean append = false;
        // 是否清空
        private boolean clear = false;

        //------------------------------------------------------------------------------------------

        @NotNull
        public
        Build point(@NotNull OutputStream outputStream)
        { return point(() -> outputStream, OutputStream.class, true); }

        @NotNull
        public
        Build point(@NotNull Writer writer)
        { return point(() -> writer, Writer.class, true); }

        @NotNull
        public
        Build point(@NotNull File files) { return point(files.toPath()); }

        @NotNull
        public
        Build point(@NotNull Path path) {
            return point(() -> Files.newOutputStream(path, WRITE, CREATE, append ? APPEND
                    : TRUNCATE_EXISTING), OutputStream.class, false);
        }

        @NotNull
        public
        <O extends Flushable> Build point(@NotNull trySupplier<O, IOException> fun,
                                          @NotNull Class<O> clas, boolean isstreaem)
        {
            out = fun;
            aclass = clas;
            this.isstreaem = isstreaem;
            return this;
        }

        //------------------------------------------------------------------------------------------

        /**
         * 数据采用截断输出还是追加输出
         * <p>
         * 仅在指向文件或路径的时候生效
         */
        @NotNull
        public
        Build append() {
            append = true;
            return this;
        }

        /**
         * 输出完成后是否清除残留数据
         *
         * @see #needclear
         */
        @NotNull
        public
        Build clear() {
            clear = true;
            return this;
        }

        //------------------------------------------------------------------------------------------

        /**
         * 构造实时写入的通道
         *
         * @see SyncChannel
         */
        @NotNull
        public
        SyncChannel sync() throws IOException
        { return new SyncChannel(out, (Class) aclass, isstreaem, clear); }

        /**
         * 构造定时输出通道
         *
         * @param time 操作间隔，单位 毫秒
         *
         * @see TimingChannel
         */
        @NotNull
        public
        TimingChannel timing(long time) throws IOException
        { return new TimingChannel(out, (Class) aclass, isstreaem, clear, time); }

        /**
         * 构造溢出式输出通道
         *
         * @param buffsize 缓存大小，单位 实际数据的长度 [String | byte[]]
         *
         * @see BufferChannel
         */
        @NotNull
        public
        BufferChannel buffer(int buffsize) throws IOException
        { return new BufferChannel(out, (Class) aclass, isstreaem, clear, buffsize); }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 同步式保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的同时会进行 {@link #send()}
     *
     * @author fybug
     * @version 0.0.2
     * @since PushChannel 0.0.1
     */
    public static final
    class SyncChannel extends PushChannel {
        public
        <O extends Flushable> SyncChannel(@NotNull trySupplier<O, IOException> out,
                                          @NotNull Class<O> aclass, boolean isstreaem)
        throws IOException
        { super(out, aclass, isstreaem); }

        <O extends Flushable> SyncChannel(@NotNull trySupplier<O, IOException> out,
                                          @NotNull Class<O> aclass, boolean isstreaem,
                                          boolean clear) throws IOException
        { super(out, aclass, isstreaem, clear); }

        @Override
        protected
        boolean canWrite() { return true; }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 定时保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的时候不会进行 {@link #send()} 而是在等待的时间间隔结束后进行<br/>
     * 时间间隔单位为毫秒
     *
     * @author fybug
     * @version 0.0.2
     * @since PushChannel 0.0.1
     */
    public static final
    class TimingChannel extends PushChannel {

        private ScheduledExecutorService pool;

        //------------------------------------------------------------------------------------------

        public
        <O extends Flushable> TimingChannel(@NotNull trySupplier<O, IOException> out,
                                            @NotNull Class<O> aclass, boolean isstreaem, long time)
        throws IOException
        { this(out, aclass, isstreaem, false, time); }

        <O extends Flushable> TimingChannel(@NotNull trySupplier<O, IOException> out,
                                            @NotNull Class<O> aclass, boolean isstreaem,
                                            boolean clear, long time) throws IOException
        {
            super(out, aclass, isstreaem, clear);
            runing(time);
        }

        //------------------------------------------------------------------------------------------

        // 启动计划任务
        private
        void runing(long time) {
            pool = Executors.newSingleThreadScheduledExecutor();
            pool.scheduleAtFixedRate(() -> {
                try {
                    send();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }, time, time, TimeUnit.MILLISECONDS);
        }

        @Override
        protected
        boolean canWrite() { return false; }

        @Override
        public
        void close() throws IOException {
            synchronized ( this ){
                super.close();
                pool.shutdown();
            }
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 溢出式保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的时候如果数据溢出了缓冲区，无论是到达溢出界限还是已经溢出<br/>
     * 因为这是在单次数据调整结束后进行的检查，所以数据可能会溢出，在溢出后会执行 {@link #send()}，之后数据依旧留在缓存区<br/>
     * 缓存区的单位依照实际的数据类型长度，数据类型为 {@link String} 时则为检查 {@link String#length()}<br/>
     * 数据类型为 {@code byte[]} 时则检查 {@code byte[].length}
     *
     * @author fybug
     * @version 0.0.2
     * @since PushChannel 0.0.1
     */
    public static final
    class BufferChannel extends PushChannel {

        private final int BUFFSIZE;

        //------------------------------------------------------------------------------------------

        public
        <O extends Flushable> BufferChannel(@NotNull trySupplier<O, IOException> out,
                                            @NotNull Class<O> aclass, boolean isstreaem,
                                            int buffsize) throws IOException
        { this(out, aclass, isstreaem, false, buffsize); }

        <O extends Flushable> BufferChannel(@NotNull trySupplier<O, IOException> out,
                                            @NotNull Class<O> aclass, boolean isstreaem,
                                            boolean clear, int buffsize) throws IOException
        {
            super(out, aclass, isstreaem, clear);
            BUFFSIZE = buffsize;
        }

        //------------------------------------------------------------------------------------------

        @Override
        protected
        boolean canWrite() {
            if (data instanceof byte[])
                return BUFFSIZE <= ((byte[]) data).length;
            else if (data instanceof String)
                return BUFFSIZE <= ((String) data).length();

            return false;
        }
    }
}
