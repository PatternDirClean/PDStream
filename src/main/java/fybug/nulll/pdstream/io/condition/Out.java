package fybug.nulll.pdstream.io.condition;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import fybug.nulll.pdstream.io.foundation.OutByte;
import fybug.nulll.pdstream.io.foundation.OutString;
import fybug.nulll.pdstream.io.condition.io.uilt.AsnycOut;
import fybug.nulll.pdstream.io.condition.io.uilt.SyncOut;

import static fybug.nulll.pdstream.io.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.io.OPC.CHAR_EMPTY_DATA;

/**
 * <h2>输出操作器构造工具.</h2>
 * <p>
 * 用于生成不同实现的输出器
 * 可使用 {@code append()} 绑定多个流进行输出
 * 生成操作器时绑定的流会被转移到操作器中并清空
 * <p>
 * 可使用 {@code binData} 绑定多个数据，绑定的数据不会被过滤，多次输出
 *
 * @author fybug
 * @version 0.0.1
 * @since stream 0.0.1
 */
public
class Out extends OperatorFactory<Out, Writer, OutputStream>
{
    /** 绑定数据 */
    private ArrayList<byte[]> dataarray = new ArrayList<>();

    /*--------------------------------------------------------------------------------------------*/

    Out(OutputStream outputStream) { append(outputStream); }

    Out(Writer writer) { append(writer); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 绑定固定数据
     *
     * @param adta 要绑定的数据
     *
     * @return this
     */
    @NotNull
    public
    Out binData(@NotNull byte[] adta) {
        if (!Arrays.equals(adta, BYTE_EMPTY_DATA))
            dataarray.add(adta);
        return this;
    }

    /**
     * 绑定固定数据
     *
     * @param adta 要绑定的数据
     *
     * @return this
     */
    @NotNull
    public
    Out binData(@NotNull String adta) {
        if (!Objects.equals(adta, CHAR_EMPTY_DATA))
            binData(adta.getBytes());
        return this;
    }

    /**
     * 清除固定的数据
     *
     * @return this
     */
    @NotNull
    public
    Out clearBin() {
        dataarray.clear();
        return this;
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 使用阻塞式处理工具
     *
     * @return 包含当前流和数据集合的阻塞式输出工具
     */
    @NotNull
    public
    SyncOut sync() {
        return new SyncOut(getStreams().stream().map(v -> {
            if (v instanceof OutputStream)
                return new OutByte((OutputStream) v);
            return new OutString((Writer) v);
        }).collect(Collectors.toList()), new ArrayList<>(dataarray));
    }

    /**
     * 使用异步处理工具
     *
     * @return 包含当前流和数据集合的异步输出工具
     */
    @NotNull
    public
    AsnycOut async() {
        return new AsnycOut(getStreams().stream().map(v -> {
            if (v instanceof OutputStream)
                return new OutByte((OutputStream) v);
            return new OutString((Writer) v);
        }).collect(Collectors.toList()), new ArrayList<>(dataarray));
    }

    /*-----------------------*/

    /**
     * 使用阻塞式打印工具
     * 输出一次数据后会在数据后追加 {@link System#lineSeparator()}
     *
     * @return 包含当前流和数据集合的阻塞式输出工具
     */
    @NotNull
    public
    SyncOut syncPrintln() {
        return new SyncOut(getStreams().stream().map(v -> {
            if (v instanceof OutputStream)
                return new OutByte((OutputStream) v);
            return new OutString((Writer) v);
        }).collect(Collectors.toList()), new ArrayList<>(dataarray), System.lineSeparator());
    }

    /**
     * 使用异步打印工具
     * 输出一次数据后会在数据后追加 {@link System#lineSeparator()}
     *
     * @return 包含当前流和数据集合的异步输出工具
     */
    public
    AsnycOut asyncPrintln() {
        return new AsnycOut(getStreams().stream().map(v -> {
            if (v instanceof OutputStream)
                return new OutByte((OutputStream) v);
            return new OutString((Writer) v);
        }).collect(Collectors.toList()), new ArrayList<>(dataarray), System.lineSeparator());
    }
}
