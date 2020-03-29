package fybug.nulll.pdstream.io;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdstream.io.expander.expanderTest;
import fybug.nulll.pdstream.io.uilt.uiltTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {InOfTest.class, OutOfTest.class, uiltTest.class, expanderTest.class} )
public
class ioTest {}