package fybug.nulll.pdstream.strem;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h2>操作器工场.</h2>
 * 记录当前的流并使用它们生成操作器
 * <p>
 * 生成操作器后会清空当前记录的流
 *
 * @author fybug
 * @version 0.0.1
 */
@SuppressWarnings( "all" )
abstract
class OperatorFactory<O extends OperatorFactory, R, E extends Closeable> {
    /** 流集合 */
    private List<Closeable> streamarray = new ArrayList<>();

    /** 获取流集合 */
    protected
    List<Closeable> getStreams() {
        var streams = new ArrayList<Closeable>();
        streamarray.stream().filter(Objects::nonNull).forEach(streams::add);
        streamarray = new ArrayList<>();
        return streams;
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 记录流 */
    public
    O append(R closeable) {
        streamarray.add((Closeable) closeable);
        return (O) this;
    }

    /** 记录流 */
    public
    O append(E closeable) {
        streamarray.add(closeable);
        return (O) this;
    }
}
