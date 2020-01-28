package fybug.nulll.pdstream.strem;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdstream.strem.io.IOFiltrerTest;
import fybug.nulll.pdstream.strem.io.uilt.AsnycInTest;
import fybug.nulll.pdstream.strem.io.uilt.AsnycOutTest;
import fybug.nulll.pdstream.strem.io.uilt.SyncInTest;
import fybug.nulll.pdstream.strem.io.uilt.SyncOutTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {IOFiltrerTest.class, SyncInTest.class, AsnycInTest.class, SyncOutTest.class,
                             AsnycOutTest.class} )
public
class RunTest {}
