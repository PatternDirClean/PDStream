package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdstream.io.InOf;
import fybug.nulll.pdstream.io.uilt.In.AsyncIn;
import lombok.experimental.Accessors;

/**
 * <h2>读取工具构造器.</h2>
 * <p>
 * 用于根据构造器的的配置构造读取工具<br/>
 * 构造器赋予配置的方法和 {@link In} 一样
 * <br/>
 * <pre>使用示例
 *     public static
 *     void main(String[] args) {
 *         var build = InOf.inBuild();
 *         // 赋予配置，自动关闭，异常处理
 *         build.close().exception(e -> e.printStackTrace(System.out));
 *
 *         // 使用以上配置构造并读取
 *         System.out.println(build.of(new StringReader("asdqw")).read());
 *         System.out.println(build.of(new StringReader("poipo")).read());
 *     }
 * </pre>
 * <pre>
 *    public static
 *     void main(String[] args) {
 *         var build = InOf.inBuild()
 *                         // 赋予配置，自动关闭，异常处理
 *                         .close().exception(e -> e.printStackTrace(System.out))
 *                         // 启动异步，构造器的类型会变化
 *                         .async();
 *
 *         // 使用以上配置构造并读取
 *         build.of(new StringReader("asdqw")).read(System.out::println);
 *         build.of(new StringReader("poipo")).read(System.out::println);
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
class InBuild extends Build<InBuild> {

    @NotNull
    public
    AsyncInBuild async() { return async(null); }

    @NotNull
    public
    AsyncInBuild async(@Nullable ExecutorService pool) { return new AsyncInBuild(pool); }

    //----------------------------------------------------------------------------------------------

    /**
     * 使用这里的配置构造读取工具
     *
     * @see InOf#read(InputStream, int)
     * @see InOf#readAll(InputStream)
     */
    @NotNull
    public
    In<InputStream, byte[]> of(@NotNull InputStream inputStream)
    { return pushSet(build(inputStream)); }

    /** 构造读取工具 */
    @NotNull
    protected abstract
    In<InputStream, byte[]> build(@NotNull InputStream inputStream);

    //-----------------------------------

    /**
     * 使用这里的配置构造读取工具
     *
     * @see InOf#read(Reader, int)
     * @see InOf#readAll(Reader)
     */
    @NotNull
    public
    In<Reader, CharSequence> of(@NotNull Reader reader) { return pushSet(build(reader)); }

    /** 构造读取工具 */
    @NotNull
    protected abstract
    In<Reader, CharSequence> build(@NotNull Reader reader);

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>异步读取工具构造器.</h2>
     *
     * @author fybug
     * @version 0.0.2
     * @see AsyncIn
     * @since InBuild 0.0.1
     */
    public final
    class AsyncInBuild extends AsyncBuild<AsyncInBuild> {

        private
        AsyncInBuild(ExecutorService pool) { super(pool); }

        //----------------------------------------------------------------------------------------------

        /**
         * 使用这里的配置构造读取工具
         *
         * @see InBuild#of(InputStream)
         */
        @NotNull
        public
        AsyncIn of(@NotNull InputStream inputStream)
        { return InBuild.this.of(inputStream).async(pool); }

        /**
         * 使用这里的配置构造读取工具
         *
         * @see InBuild#of(Reader)
         */
        @NotNull
        public
        AsyncIn of(@NotNull Reader reader)
        { return InBuild.this.of(reader).async(pool); }
    }
}
