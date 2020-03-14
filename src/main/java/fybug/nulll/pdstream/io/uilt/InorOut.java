package fybug.nulll.pdstream.io.uilt;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * <h2>读写工具.</h2>
 * <p>
 * 包含描述操作后处理的函数<br/>
 * {@link #close()} 操作完成后关闭<br/>
 * {@link #exception(Consumer)} 操作发生异常的处理
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.1
 */
public
interface InorOut<U extends InorOut<U>> {
    /** 操作完成后关闭 */
    @NotNull
    U close();

    /** 异常处理接口 */
    @NotNull
    U exception(@NotNull Consumer<IOException> e);
}
