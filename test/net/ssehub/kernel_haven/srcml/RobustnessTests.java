package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;

/**
 * Tests that {@link SrcMLExtractor} does not crash when processing real code files.
 * @author El-Sharkawy
 *
 */
public class RobustnessTests extends AbstractSrcMLExtractorTest {
    
    /**
     * Tests that all C-files placed in real test folder are translated without throwing an exception.
     * @throws IOException
     */
    @Test
    public void testAllFiles() throws IOException {
        Path basesPath = AllTests.TESTDATA.toPath();
        Path testFilesPath = new File(AllTests.TESTDATA, "real").toPath();
        List<Path> testFiles = Files.walk(testFilesPath)
            .filter(p -> p.toString().endsWith(".c"))
            .collect(Collectors.toList());
        
        for (Path p : testFiles) {
            Path relativePath = basesPath.relativize(p);
//            if (relativePath.toString().contains("vgetcpu")) {
            SourceFile parsed = loadFile(relativePath.toString());
            Assert.assertNotNull("Could not parse " + relativePath.toString(), parsed);
//            } else {System.out.println("Skipped: " + p.toString());}
        }
    }

}
