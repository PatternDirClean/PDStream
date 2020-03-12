package fybug.nulll.pdstream.io.condition;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdstream.io.condition.io.IOFiltrerTest;
import fybug.nulll.pdstream.io.condition.io.uilt.AsnycInTest;
import fybug.nulll.pdstream.io.condition.io.uilt.AsnycOutTest;
import fybug.nulll.pdstream.io.condition.io.uilt.SyncInTest;
import fybug.nulll.pdstream.io.condition.io.uilt.SyncOutTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {IOFiltrerTest.class, SyncInTest.class, AsnycInTest.class, SyncOutTest.class,
                             AsnycOutTest.class} )
public
class RunTest {}
