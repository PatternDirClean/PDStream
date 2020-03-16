/**
 * <h2>读写工具包.</h2>
 * <p>
 * 读写工具实现基本的 {@link fybug.nulll.pdstream.io.uilt.IOtool} 接口，可标识操作后的处理<br/>
 * 使用 {@link fybug.nulll.pdstream.io.InOf} 或 {@link fybug.nulll.pdstream.io.OutOf} 生成对应的读写工具实例，
 * 工具实例为 {@link fybug.nulll.pdstream.io.uilt.IOtool} 的读取或输出实现，附加更多的操作接口<br/>
 * 读写操作都可以转化为异步操作，异步操作接口于原来的类似，但是返回变成回调，使用返回 {@link java.util.concurrent.Future} 回调的接口
 * 则需要绑定线程池，不绑定线程池均返回 {@code null}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.1.1
 */
package fybug.nulll.pdstream.io;