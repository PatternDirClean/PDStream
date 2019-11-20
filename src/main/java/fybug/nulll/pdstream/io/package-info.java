/**
 * <h2>基础输出实现包.</h2>
 * 即内部的工具为该包下的所有工具的以流为操作对象的实现
 * <p>
 * 该包下直属的类均已支持 <b>空操作对象兼容和无异常操作</b>
 * 空操作对象的情况下通常使用 {@code nullstream} 进行空操作
 * 发生异常将会返回 {@code null | false}
 * <p>
 * 以流为对象进行操作的操作类群
 *
 * @author fybug
 * @version 0.0.1
 * @since PDStream restart-0.0.1*
 */
package fybug.nulll.pdstream.io;