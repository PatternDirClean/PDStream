/**
 * <h2>PDStream 流操作工具包.</h2>
 * 以操作器的形式提供语义化操作
 * 根据使用场景大部分都提供 <b>无异常操作</b>
 * 详情请查看所属的包描述
 * <p>
 * 操作器使用不同的实例作用于不同的环境和对象
 * 给予统一操作接口并根据操作的流给予特殊操作
 * 大部分情况下返回 {@code null} 为无数据或读取失败
 * 非 {@code null} 的空数据对像为读取成功但是没有数据，如设置读取最大长度为 {@code 0} 的时候
 *
 * @author fybug
 * @version restart-0.0.1
 * @since jdk 13
 * <p>
 * todo 缓存式
 * todo 锁工具
 * todo 文件监听
 * todo 字符流行操作，列表，stream
 */
package fybug.nulll.pdstream;