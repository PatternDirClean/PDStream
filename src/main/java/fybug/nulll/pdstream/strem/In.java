package fybug.nulll.pdstream.strem;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.Reader;

import fybug.nulll.pdstream.strem.io.uilt.AsnycIn;
import fybug.nulll.pdstream.strem.io.uilt.SyncIn;

/**
 * <h2>读取操作器构造工具.</h2>
 * <p>
 * 用于生成不同实现的读取器
 * 可使用 {@code append()} 绑定多个流交由读取器读取
 * 生成操作器时绑定的流会被转移到操作器中并清空
 *
 * @author fybug
 * @version 0.0.1
 * @since stream 0.0.1
 */
public
class In extends OperatorFactory<In, Reader, InputStream> {
    In(InputStream inputStream) { append(inputStream); }

    In(Reader reader) { append(reader); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 使用阻塞式处理工具
     *
     * @return 包含当前流集合的阻塞式读取工具
     */
    @NotNull
    public
    SyncIn sync() { return new SyncIn(getStreams()); }

    /**
     * 使用异步处理工具
     *
     * @return 包含当前流集合异步读取工具
     */
    @NotNull
    public
    AsnycIn async() { return new AsnycIn(getStreams()); }
}
