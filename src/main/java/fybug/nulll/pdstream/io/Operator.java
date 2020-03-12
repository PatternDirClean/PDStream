package fybug.nulll.pdstream.io;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * <h2>操作器基类.</h2>
 * 操作器是读写工具的基类
 * 定义了绑定接口 {@link #bin(Object)} 并提供获原始操作对象的接口 {@link #original()}
 *
 * @param <O> 操作对象
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1
 */
interface Operator<O> extends Closeable {
    /**
     * 修改要操作的对象
     *
     * @param operator 要操作的对象
     *
     * @return this
     */
    @NotNull
    Operator<O> bin(O operator);

    /**
     * 获取原始操作对象
     *
     * @return 原始操作对象
     */
    @NotNull
    O original();

    @Override
    void close();
}
