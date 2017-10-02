package net.ssehub.kernel_haven.srcml;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SrcMLExtractorTest.class })
public class AllTests {
    public static final File TESTDATA = new File("testdata");
}
