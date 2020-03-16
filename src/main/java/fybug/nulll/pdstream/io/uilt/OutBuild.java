package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdstream.io.OutOf;
import fybug.nulll.pdstream.io.uilt.Out.AsyncOut;
import lombok.experimental.Accessors;

/**
 * <h2>输出工具构造器.</h2>
 * <p>
 * 用于根据构造器的的配置构造输出工具<br/>
 * 构造器赋予配置的方法和 {@link Out} 一样
 * <br/>
 * <pre>使用示例
 *     public static
 *     void main(String[] args) {
 *         var build = OutOf.build(true);
 *         // 赋予配置，自动关闭，异常处理
 *         build.close().exception(e -> e.printStackTrace(System.out));
 *
 *         // 使用以上配置构造并输入数据
 *         var w = new StringWriter();
 *         build.of(w).write("asdqwdqd");
 *         System.out.println(w);
 *     }
 * </pre>
 * <pre>
 *     public static
 *     void main(String[] args) {
 *         var build = OutOf.build(true)
 *                          // 赋予配置，自动关闭，异常处理
 *                          .close().exception(e -> e.printStackTrace(System.out))
 *                          // 启用异步
 *                          .async();
 *
 *         // 使用以上配置构造并输入数据
 *         var w = new StringWriter();
 *         build.of(w).write("asdqwdqd", suerr -> System.out.println(w));
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.2
 * @since uilt 0.0.2
 */
@SuppressWarnings( "unchecked" )
@Accessors( fluent = true, chain = true )
public abstract
class OutBuild extends Build<OutBuild> {

    @NotNull
    public
    AsyncOutBuild async() { return async(null); }

    @NotNull
    public
    AsyncOutBuild async(@Nullable ExecutorService pool) { return new AsyncOutBuild(pool); }

    //----------------------------------------------------------------------------------------------

    /**
     * 使用这里的配置构造输出工具
     *
     * @see OutOf#write(OutputStream)
     * @see OutOf#writeFlush(OutputStream)
     */
    @NotNull
    public
    Out<byte[]> of(@NotNull OutputStream outputStream)
    { return pushSet(build(outputStream)); }

    /** 构造输出工具 */
    @NotNull
    protected abstract
    Out<byte[]> build(@NotNull OutputStream outputStream);

    //-----------------------------------

    /**
     * 使用这里的配置构造输出工具
     *
     * @see OutOf#write(Writer)
     * @see OutOf#writeFlush(Writer)
     */
    @NotNull
    public
    Out<CharSequence> of(@NotNull Writer writer) { return pushSet(build(writer)); }

    /** 构造输出工具 */
    @NotNull
    protected abstract
    Out<CharSequence> build(@NotNull Writer writer);

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步输出工具构造器.</h2>
     *
     * @author fybug
     * @version 0.0.1
     * @see AsyncOut
     * @since OutBuild 0.0.1
     */
    public final
    class AsyncOutBuild extends AsyncBuild<AsyncOutBuild> {

        private
        AsyncOutBuild(ExecutorService pool) { super(pool); }

        //----------------------------------------------------------------------------------------------

        /**
         * 使用这里的配置构造输出工具
         *
         * @see OutBuild#of(OutputStream)
         */
        @NotNull
        public
        AsyncOut of(@NotNull OutputStream outputStream)
        {return OutBuild.this.of(outputStream).async(pool);}

        //-----------------------------------

        /**
         * 使用这里的配置构造输出工具
         *
         * @see OutBuild#of(Writer)
         */
        @NotNull
        public
        AsyncOut of(@NotNull Writer writer)
        {return OutBuild.this.of(writer).async(pool);}
    }
}
