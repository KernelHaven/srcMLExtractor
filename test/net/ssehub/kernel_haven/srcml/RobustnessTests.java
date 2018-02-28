package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.code_model.SourceFile;

/**
 * Tests that {@link SrcMLExtractor} does not crash when processing real code files.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
@RunWith(Parameterized.class)
public class RobustnessTests extends AbstractSrcMLExtractorTest {
    
    private static final Path BASE_PATH = AllTests.TESTDATA.toPath();
    
    protected static final Path TEST_FILES_PATH = new File(AllTests.TESTDATA, "real").toPath();
    
    @Parameters(name = "{0}")
    public static Object[] data() throws IOException {
        List<Path> sourceFiles = Files.walk(TEST_FILES_PATH)
                .filter(p ->p.getFileName().toString().endsWith(".c"))
                .filter(p -> !p.getFileName().toString().startsWith("deactivated_"))
                .collect(Collectors.toList());
        
        return sourceFiles.toArray();
    }
    
    private Path file;
    
    public RobustnessTests(Path file) {
        this.file = file;
    }
    
    /**
     * Tests that all C-files placed in real test folder are translated without throwing an exception.
     * @throws IOException
     */
    @Test
    public void testFile() throws IOException {
        Path relativePath = BASE_PATH.relativize(file);
        SourceFile parsed = loadFile(relativePath.toString());
        
        assertNotNull("Could not parse " + relativePath.toString(), parsed);
    }
    
}
