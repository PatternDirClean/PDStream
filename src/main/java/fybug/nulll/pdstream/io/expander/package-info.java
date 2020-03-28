/**
 * 读写工具拓展包
 * <p>
 * 包含更复杂的读写工具
 *
 * @author fybug
 * @version 0.0.1
 * @since io 0.0.2
 */
package fybug.nulll.pdstream.io.expander;



///*--------------------------------------------------------------------------------------------*/
//@Accessors( chain = true, fluent = true )
//public static abstract
//class Build {
//    @Setter protected ExecutorService pool;
//
//    @NotNull
//    public abstract
//    QueueOut build();
//}
//
//    /*--------------------------------------------------------------------------------------------*/
//
//    /** 获取构造工具 */
//    @NotNull
//    public static
//    Build of(@NotNull Writer writer) { return new charQ.Build(writer); }
//
///** 字符流输出实现 */
//private static final
//class charQ extends QueueOut {
//
//    private
//    charQ(@Nullable Writer o, @Nullable ExecutorService p) { super(o, p); }
//
//    protected
//    void write0(@NotNull Closeable o, @NotNull byte[] da) throws IOException
//    { ((Writer) o).write(new String(da, UTF_8)); }
//
//    protected
//    void print0(@NotNull Closeable o, @NotNull CharSequence da) throws IOException
//    { ((Writer) o).write(da.toString()); }
//
//    /** 构造器 */
//    @RequiredArgsConstructor
//    private static final
//    class Build extends QueueOut.Build {
//        @Nullable private final Writer writer;
//
//        @NotNull
//        public
//        QueueOut build() { return new charQ(writer, pool); }
//    }
//}
//
//    //------------------------------------------
//
//    /** 获取构造工具 */
//    @NotNull
//    public static
//    Build of(@NotNull OutputStream outputStream) { return new byteQ.Build(outputStream); }
//
///** 字节流输出实现 */
//private static final
//class byteQ extends QueueOut {
//
//    private
//    byteQ(@Nullable OutputStream o, @Nullable ExecutorService p) { super(o, p); }
//
//    protected
//    void write0(@NotNull Closeable o, @NotNull byte[] da) throws IOException
//    { ((OutputStream) o).write(da); }
//
//    protected
//    void print0(@NotNull Closeable o, @NotNull CharSequence da) throws IOException
//    { ((OutputStream) o).write(da.toString().getBytes(UTF_8)); }
//
//    /** 构造器 */
//    @RequiredArgsConstructor
//    private static final
//    class Build extends QueueOut.Build {
//        @Nullable private final OutputStream outputStream;
//
//        @NotNull
//        public
//        QueueOut build() { return new byteQ(outputStream, pool); }
//    }
//}
//
//    //------------------------------------------
//
//    @NotNull
//    public static
//    Build of(@NotNull Path path) {return of(path.toFile());}
//
//    @NotNull
//    public static
//    Build of(@NotNull File file) {return null;}
//
//// todo
//private static final
//class fileQ extends QueueOut {
//
//    private
//    fileQ(@Nullable Closeable o, @Nullable ExecutorService p) {
//        super(o, p);
//    }
//
//    @Override
//    protected
//    void write0(@NotNull Closeable o, @NotNull byte[] da) throws IOException {
//
//    }
//
//    @Override
//    protected
//    void print0(@NotNull Closeable o, @NotNull CharSequence da) throws IOException {
//
//    }
//}