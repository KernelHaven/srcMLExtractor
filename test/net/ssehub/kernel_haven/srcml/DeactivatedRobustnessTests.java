package net.ssehub.kernel_haven.srcml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Deactivated {@link RobustnessTests}. We know that these fill crash.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class DeactivatedRobustnessTests extends RobustnessTests {
    
    /**
     * Creates a test instance.
     * 
     * @param file The file to run for.
     */
    public DeactivatedRobustnessTests(Path file) {
        super(file);
    }
    
    /**
     * Returns the test data.
     * 
     * @return The list of files that this test should run on.
     * 
     * @throws IOException If enumerating the files fails.
     */
    @Parameters(name = "{0}")
    public static Object[] data() throws IOException {
        List<Path> sourceFiles = Files.walk(TEST_FILES_PATH)
                .filter(p ->p.getFileName().toString().endsWith(".c"))
                .filter(p -> p.getFileName().toString().startsWith("deactivated_"))
                .collect(Collectors.toList());
        
        return sourceFiles.toArray();
    }
    

}
