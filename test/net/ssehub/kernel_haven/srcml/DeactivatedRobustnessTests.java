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
    
    @Parameters(name = "{0}")
    public static Object[] data() throws IOException {
        List<Path> sourceFiles = Files.walk(TEST_FILES_PATH)
                .filter(p ->p.getFileName().toString().endsWith(".c"))
                .filter(p -> p.getFileName().toString().startsWith("deactivated_"))
                .collect(Collectors.toList());
        
        return sourceFiles.toArray();
    }
    
    public DeactivatedRobustnessTests(Path file) {
        super(file);
    }

}
