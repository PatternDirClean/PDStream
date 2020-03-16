package fybug.nulll.pdstream;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fybug.nulll.pdstream.io.ioTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {ioTest.class} )
public
class RunTest {
    public static final String testdata = "akjhdjkxcvxcgjisasduiqewhncxzkj,v xnckvrs nkjflcxdv bzl";
    public static final ExecutorService pool = Executors.newCachedThreadPool();
}