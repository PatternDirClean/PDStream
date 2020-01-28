/**
 * <h2>状态式 io流 工具.</h2>
 * <p>
 * 该包下的工具内部均使用 {@link fybug.nulll.pdstream.io} 包下的工具进行
 * <p>
 * 提供 <b>异步和同步</b> 两种运行状态工具，该包下的工具均不会自行执行 {@code close()} 操作
 * 可读取出 {@code byte[]} / {@code string} 两种格式的数据，执行前使用转化流转化为同种类型的 io流 后进行操作
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.1
 */
package fybug.nulll.pdstream.strem.io.uilt;