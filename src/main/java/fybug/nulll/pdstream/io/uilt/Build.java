package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 读写工具构造器
 * <p>
 * 使用构造器中的配置构造读写工具
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.2
 */
@SuppressWarnings( "unchecked" )
@Accessors( fluent = true, chain = true )
abstract
class Build<O extends Build<O>> implements InorOut<O> {

    /** 异常处理接口 */
    protected Consumer<IOException> exception = e -> {throw new RuntimeException();};

    //----------------------------------------------------------------------------------------------

    /** 是否自动关闭 */
    @Getter protected boolean autoClose = false;

    @NotNull
    public final
    O close() {
        autoClose = true;
        return (O) this;
    }

    /** 取消自动关闭 */
    @NotNull
    public final
    O noclose() {
        autoClose = false;
        return (O) this;
    }

    //----------------------------------------------------------------------------------------------

    /** 启动异步处理 */
    @NotNull
    public abstract
    <T extends AsyncBuild<T>> T async();

    /**
     * 启动异步处理
     *
     * @param pool 指定执行用线程池
     */
    @NotNull
    public abstract
    <T extends AsyncBuild<T>> T async(@Nullable ExecutorService pool);

    //----------------------------------------------------------------------------------------------

    @NotNull
    public
    O exception(@NotNull Consumer<IOException> e) {
        exception = e;
        return (O) this;
    }

    //----------------------------------------------------------------------------------------------

    /** 放入配置 */
    @NotNull
    protected
    <T extends InorOut<T>> T pushSet(@NotNull T build) {
        if (autoClose)
            build.close();
        return build.exception(exception);
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 异步读写工具构造器
     *
     * @author fybug
     * @version 0.0.1
     * @since Build 0.0.1
     */
    @RequiredArgsConstructor( access = AccessLevel.PROTECTED )
    protected abstract
    class AsyncBuild<A extends AsyncBuild<A>> implements InorOut<A> {
        // 执行用线程池
        @Nullable protected final ExecutorService pool;

        //------------------------------------------------------------------------------------------

        @NotNull
        public final
        A close() {
            Build.this.close();
            return (A) this;
        }

        /** 取消自动关闭 */
        @NotNull
        public final
        A noclose() {
            Build.this.noclose();
            return (A) this;
        }

        /** 是否自动关闭 */
        public final
        boolean autoClose() {return Build.this.autoClose();}

        //------------------------------------------------------------------------------------------

        /** 取消异步操作 */
        @NotNull
        public final
        O sync() { return (O) Build.this; }

        //------------------------------------------------------------------------------------------

        @NotNull
        public final
        A exception(@NotNull Consumer<IOException> e) {
            Build.this.exception(e);
            return (A) this;
        }
    }
}
