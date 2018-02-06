package net.ssehub.kernel_haven.srcml;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All tests for this project.
 * 
 * @author Adam
 */
@RunWith(Suite.class)
@SuiteClasses({
    CppTest.class,
    })
public class AllTests {
    public static final File TESTDATA = new File("testdata");
}
