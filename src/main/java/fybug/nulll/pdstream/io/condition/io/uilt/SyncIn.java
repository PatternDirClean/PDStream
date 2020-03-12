package fybug.nulll.pdstream.io.condition.io.uilt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import fybug.nulll.pdstream.io.foundation.InByte;
import fybug.nulll.pdstream.io.foundation.InString;
import fybug.nulll.pdstream.io.condition.io.HasFiltrer;

import static fybug.nulll.pdstream.io.OPC.BYTE_EMPTY_DATA;
import static fybug.nulll.pdstream.io.OPC.CHAR_EMPTY_DATA;

/**
 * <h2>阻塞式读取器.</h2>
 * <p>
 * 读取的数据为空或失败时返回 {@code null}
 * 读取后的数据使用链式过滤器过滤后返回
 *
 * @author fybug
 * @version 0.0.1
 * @since uilt 0.0.1
 */
public
class SyncIn extends HasFiltrer<SyncIn> {
    public
    SyncIn(@NotNull List<Closeable> stream) {super(stream);}

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取全部字节数据
     *
     * @return bytes
     */
    @Nullable
    public
    byte[] bytes() {
        return (byte[]) runofStream(streams -> {
            if (streams.size() == 0)
                return BYTE_EMPTY_DATA;

            // 是否有数据
            AtomicBoolean nodata = new AtomicBoolean(true);

            var echo = unifiedInput(streams.stream(), true)
                    // 读取所有的数据，并去除无数据的案例
                    .map(v -> new InByte((InputStream) v).readAll())
                    .filter(v -> !Arrays.equals(v, BYTE_EMPTY_DATA))
                    // 整合数据
                    .peek(v -> nodata.set(false))
                    .reduce(new ByteArrayOutputStream(), (a, b) -> {
                        a.writeBytes(b);
                        return a;
                    }, (a, b) -> null);

            // 没有数据
            if (nodata.get() || echo.size() == 0)
                return BYTE_EMPTY_DATA;

            // 数据过滤
            return byteF(echo.toByteArray());
        });
    }

    /**
     * 获取全部字符数据
     *
     * @return string
     */
    @Nullable
    public
    String chars() {
        return (String) runofStream(streams -> {
            if (streams.size() == 0)
                return CHAR_EMPTY_DATA;

            // 是否有数据
            AtomicBoolean nodata = new AtomicBoolean(true);

            var echo = unifiedInput(streams.stream(), false)
                    // 读取所有的数据，并去除无数据的案例
                    .map(v -> new InString((Reader) v).readAll())
                    .filter(v -> !Objects.equals(v, CHAR_EMPTY_DATA))
                    // 整合数据
                    .peek(v -> nodata.set(false))
                    .reduce(new StringBuilder(), StringBuilder::append, (a, b) -> null);

            // 没有数据
            if (nodata.get() || echo.length() == 0)
                return CHAR_EMPTY_DATA;
            echo.trimToSize();

            // 数据过滤
            return charF(echo.toString());
        });
    }
}
