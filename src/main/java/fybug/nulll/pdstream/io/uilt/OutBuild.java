package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdstream.io.OutOf;
import lombok.experimental.Accessors;

/**
 * todo doc
 *
 * @author fybug
 * @version 0.0.1
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
    Out<OutputStream, byte[]> of(@NotNull OutputStream outputStream)
    { return pushSet(build(outputStream)); }

    /** 构造输出工具 */
    @NotNull
    protected abstract
    Out<OutputStream, byte[]> build(@NotNull OutputStream outputStream);

    //-----------------------------------

    /**
     * 使用这里的配置构造输出工具
     *
     * @see OutOf#write(Writer)
     * @see OutOf#writeFlush(Writer)
     */
    @NotNull
    public
    Out<Writer, CharSequence> of(@NotNull Writer writer) { return pushSet(build(writer)); }

    /** 构造输出工具 */
    @NotNull
    protected abstract
    Out<Writer, CharSequence> build(@NotNull Writer writer);

    /*--------------------------------------------------------------------------------------------*/

    /**
     * todo doc
     *
     * @author fybug
     * @version 0.0.1
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
        Out<OutputStream, byte[]>.AsyncOut of(@NotNull OutputStream outputStream)
        {return OutBuild.this.of(outputStream).async(pool);}

        //-----------------------------------

        /**
         * 使用这里的配置构造输出工具
         *
         * @see OutBuild#of(Writer)
         */
        @NotNull
        public
        Out<Writer, CharSequence>.AsyncOut of(@NotNull Writer writer)
        {return OutBuild.this.of(writer).async(pool);}
    }
}
