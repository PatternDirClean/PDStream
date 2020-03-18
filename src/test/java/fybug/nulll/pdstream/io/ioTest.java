package fybug.nulll.pdstream.io;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdstream.io.expander.expanderTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {InOfTest.class, OutOfTest.class, expanderTest.class} )
public
class ioTest {}