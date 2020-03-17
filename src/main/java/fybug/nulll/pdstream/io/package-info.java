/**
 * <h2>读写工具包.</h2>
 * <p>
 * 读写工具实现基本的 {@link fybug.nulll.pdstream.io.uilt.InorOut} 接口，可标识操作后的处理<br/>
 * 使用 {@link fybug.nulll.pdstream.io.InOf} 或 {@link fybug.nulll.pdstream.io.OutOf} 生成对应的读写工具实例，
 * 工具实例为 {@link fybug.nulll.pdstream.io.uilt.IOtool} 的读取或输出实现，附加更多的操作接口<br/>
 * 工具均可使用异常处理回调处理发生的异常，这与全局异常回调不冲突<br/>
 * 读写操作都可以转化为异步操作，异步操作接口于原来的类似，但是返回数据变成回调处理<br/>
 * 使用返回 {@link java.util.concurrent.Future} 回调的接口则需要绑定线程池，不绑定线程池均返回 {@code null}<br/>
 * 使用线程池处理时回调会异步于操作，但异常处理仍旧同步
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.1.1
 */
package fybug.nulll.pdstream.io;