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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

// todo test
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

    //----------------------------------------------------------------------------------------------

    /**
     * 指向输出流的保存通道
     *
     * @param outputStream 指向的流
     */
    public
    PushChannel(@NotNull OutputStream outputStream) throws IOException
    { this(() -> outputStream, OutputStream.class, true); }

    /**
     * 指向输出流的保存通道
     *
     * @param writer 指向的流
     */
    public
    PushChannel(@NotNull Writer writer) throws IOException
    { this(() -> writer, Writer.class, true); }

    /**
     * 指向文件的保存通道
     *
     * @param file 指向的文件
     */
    public
    PushChannel(@NotNull File file) throws IOException { this(file.toPath()); }

    /**
     * 指向路径的保存通道
     *
     * @param path 指向的路径
     */
    public
    PushChannel(@NotNull Path path) throws IOException
    { this(() -> Files.newOutputStream(path, WRITE, TRUNCATE_EXISTING, CREATE), OutputStream.class, false); }

    /**
     * 生成保存通道
     *
     * @param out       指向目标工场方法
     * @param aclass    目标类型
     * @param isstreaem 是否指向直接的流对象
     * @param <O>       指向的对象
     */
    protected
    <O extends Flushable & Closeable> PushChannel(@NotNull trySupplier<O, IOException> out,
                                                  @NotNull Class<O> aclass, boolean isstreaem)
    throws IOException
    {
        OUT = out;
        IS_STREAM = isstreaem;
        /* 校验 */
        if (Writer.class.isAssignableFrom(aclass))
            IS_WRITE = true;
        else if (OutputStream.class.isAssignableFrom(aclass))
            IS_WRITE = false;
        else
            throw new IOException("Type is not Writer or OutputStream!");
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
     * @param data byte[] | String | Serializable
     */
    public
    void append(@NotNull Object data) throws IOException {
        // 处理后的数据
        Object da;

        if (IS_WRITE) {
            // 缓存流
            var out = new StringWriter(this.data.toString().length());
            // 原数据
            out.write((String) this.data);

            /* 字符流处理 */
            if (data instanceof String)
                out.write((String) data);
            else if (data instanceof byte[])
                out.write(new String((byte[]) data, UTF_8));
            else
                throw new IOException("data type not is byte[] or String");

            da = out.toString();
        } else {
            // 缓存流
            var out = new ByteArrayOutputStream(((byte[]) this.data).length);
            // 原数据
            out.write((byte[]) this.data);

            /* 字节流处理 */
            if (data instanceof byte[])
                out.write((byte[]) data);
            else if (data instanceof String)
                out.write(((String) data).getBytes(UTF_8));
            else if (data instanceof Serializable)
                out.write(IOUtil.serializable((Serializable) data));
            else
                throw new IOException("data type not is byte[] or String or Serializable");

            da = out.toByteArray();
        }
        // 设置数据
        LOCK.write(() -> this.data = da);

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
            LOCK.tryread(IOException.class, () -> {
                var out = OUT.get();
                if (IS_WRITE)
                    ((Writer) out).write((String) data);
                else
                    ((OutputStream) out).write((byte[]) data);
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
     * 构造实时写入的通道
     *
     * @param outputStream 指向的流
     *
     * @see SyncChannel
     */
    @NotNull
    public static
    PushChannel ofSync(@NotNull OutputStream outputStream) throws IOException
    { return new SyncChannel(outputStream); }

    /**
     * 构造实时写入的通道
     *
     * @param writer 指向的流
     *
     * @see SyncChannel
     */
    @NotNull
    public static
    PushChannel ofSync(@NotNull Writer writer) throws IOException
    { return new SyncChannel(writer); }

    /**
     * 构造实时写入的通道
     *
     * @param file 指向的文件
     *
     * @see SyncChannel
     */
    @NotNull
    public static
    PushChannel ofSync(@NotNull File file) throws IOException
    { return new SyncChannel(file); }

    /**
     * 构造实时写入的通道
     *
     * @param pa 指向的路径
     *
     * @see SyncChannel
     */
    @NotNull
    public static
    PushChannel ofSync(@NotNull Path pa) throws IOException
    { return new SyncChannel(pa); }

    /**
     * 同步式保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的同时会进行 {@link #send()}
     *
     * @author fybug
     * @version 0.0.1
     * @since PushChannel 0.0.1
     */
    public static final
    class SyncChannel extends PushChannel {

        public
        SyncChannel(@NotNull OutputStream outputStream) throws IOException { super(outputStream); }

        public
        SyncChannel(@NotNull Writer writer) throws IOException { super(writer); }

        public
        SyncChannel(@NotNull File file) throws IOException { super(file); }

        public
        SyncChannel(@NotNull Path path) throws IOException { super(path); }

        @Override
        protected
        boolean canWrite() { return true; }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 构造定时输出通道
     *
     * @param outputStream 指向的流
     * @param time         操作间隔，单位 毫秒
     *
     * @see TimingChannel
     */
    @NotNull
    public static
    PushChannel ofTiming(@NotNull OutputStream outputStream, long time) throws IOException
    { return new TimingChannel(outputStream, time); }

    /**
     * 构造定时输出通道
     *
     * @param writer 指向的流
     * @param time   操作间隔，单位 毫秒
     *
     * @see TimingChannel
     */
    @NotNull
    public static
    PushChannel ofTiming(@NotNull Writer writer, long time) throws IOException
    { return new TimingChannel(writer, time); }

    /**
     * 构造定时输出通道
     *
     * @param file 指向的文件
     * @param time 操作间隔，单位 毫秒
     *
     * @see TimingChannel
     */
    @NotNull
    public static
    PushChannel ofTiming(@NotNull File file, long time) throws IOException
    { return new TimingChannel(file, time); }

    /**
     * 构造定时输出通道
     *
     * @param pa   指向的路径
     * @param time 操作间隔，单位 毫秒
     *
     * @see TimingChannel
     */
    @NotNull
    public static
    PushChannel ofTiming(@NotNull Path pa, long time) throws IOException
    { return new TimingChannel(pa, time); }


    /**
     * 定时保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的时候不会进行 {@link #send()} 而是在等待的时间间隔结束后进行<br/>
     * 时间间隔单位为毫秒
     *
     * @author fybug
     * @version 0.0.1
     * @since PushChannel 0.0.1
     */
    public static final
    class TimingChannel extends PushChannel {
        private ScheduledExecutorService pool;

        public
        TimingChannel(@NotNull OutputStream outputStream, long time) throws IOException {
            super(outputStream);
            runing(time);
        }

        public
        TimingChannel(@NotNull Writer writer, long time) throws IOException {
            super(writer);
            runing(time);
        }

        public
        TimingChannel(@NotNull File file, long time) throws IOException {
            super(file);
            runing(time);
        }

        public
        TimingChannel(@NotNull Path path, long time) throws IOException {
            super(path);
            runing(time);
        }

        //------------------------------------------------------------------------------------------

        // 启动计划任务
        private
        void runing(long time) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
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
     * 构造溢出式输出通道
     *
     * @param outputStream 指向的流
     * @param buffsize     缓存大小，单位 实际数据的长度 [String | byte[]]
     *
     * @see BufferChannel
     */
    @NotNull
    public static
    PushChannel ofBuffer(@NotNull OutputStream outputStream, int buffsize) throws IOException
    { return new BufferChannel(outputStream, buffsize); }

    /**
     * 构造溢出式输出通道
     *
     * @param writer   指向的流
     * @param buffsize 缓存大小，单位 实际数据的长度 [String | byte[]]
     *
     * @see BufferChannel
     */
    @NotNull
    public static
    PushChannel ofBuffer(@NotNull Writer writer, int buffsize) throws IOException
    { return new BufferChannel(writer, buffsize); }

    /**
     * 构造溢出式输出通道
     *
     * @param file     指向的文件
     * @param buffsize 缓存大小，单位 实际数据的长度 [String | byte[]]
     *
     * @see BufferChannel
     */
    @NotNull
    public static
    PushChannel ofBuffer(@NotNull File file, int buffsize) throws IOException
    { return new BufferChannel(file, buffsize); }

    /**
     * 构造溢出式输出通道
     *
     * @param pa       指向的路径
     * @param buffsize 缓存大小，单位 实际数据的长度 [String | byte[]]
     *
     * @see BufferChannel
     */
    @NotNull
    public static
    PushChannel ofBuffer(@NotNull Path pa, int buffsize) throws IOException
    { return new BufferChannel(pa, buffsize); }

    /**
     * 溢出式保存通道
     * <p>
     * 在 {@link #set(Object)}、{@link #append(Object)} 的时候如果数据溢出了缓冲区，无论是到达溢出界限还是已经溢出<br/>
     * 因为这是在单次数据调整结束后进行的检查，所以数据可能会溢出，在溢出后会执行 {@link #send()}，之后数据依旧留在缓存区<br/>
     * 缓存区的单位依照实际的数据类型长度，数据类型为 {@link String} 时则为检查 {@link String#length()}<br/>
     * 数据类型为 {@link byte[]} 时则检查 {@link byte[]#length}
     *
     * @author fybug
     * @version 0.0.1
     * @since PushChannel 0.0.1
     */
    public static final
    class BufferChannel extends PushChannel {
        private final int BUFFSIZE;

        public
        BufferChannel(@NotNull OutputStream outputStream, int buffsize) throws IOException {
            super(outputStream);
            BUFFSIZE = buffsize;
        }

        public
        BufferChannel(@NotNull Writer writer, int buffsize) throws IOException {
            super(writer);
            BUFFSIZE = buffsize;
        }

        public
        BufferChannel(@NotNull File file, int buffsize) throws IOException {
            super(file);
            BUFFSIZE = buffsize;
        }

        public
        BufferChannel(@NotNull Path path, int buffsize) throws IOException {
            super(path);
            BUFFSIZE = buffsize;
        }

        @Override
        protected
        boolean canWrite() {
            if (data instanceof byte[])
                return BUFFSIZE >= ((byte[]) data).length - 1;
            else if (data instanceof String)
                return BUFFSIZE >= data.toString().length() - 1;

            return false;
        }
    }
}
