package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdstream.io.InOf;
import lombok.experimental.Accessors;

/**
 * todo doc
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
     * todo doc
     *
     * @author fybug
     * @version 0.0.2
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
        In<InputStream, byte[]>.AsyncIn of(@NotNull InputStream inputStream)
        { return InBuild.this.of(inputStream).async(pool); }

        /**
         * 使用这里的配置构造读取工具
         *
         * @see InBuild#of(Reader)
         */
        @NotNull
        public
        In<Reader, CharSequence>.AsyncIn of(@NotNull Reader reader)
        { return InBuild.this.of(reader).async(pool); }
    }
}
