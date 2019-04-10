/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;

/**
 * Tests that {@link SrcMLExtractor} does not crash when processing real code files.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
@RunWith(Parameterized.class)
public class RobustnessTests extends AbstractSrcMLExtractorTest {
    
    protected static final Path TEST_FILES_PATH = new File(AllTests.TESTDATA, "real").toPath();
    
    private static final Path BASE_PATH = AllTests.TESTDATA.toPath();
    
    private Path file;
    
    /**
     * Creates a test instance.
     * 
     * @param file The test file to run.
     */
    public RobustnessTests(Path file) {
        this.file = file;
    }
    
    /**
     * Creates the test data.
     * 
     * @return The test data.
     * 
     * @throws IOException If reading file names fails.
     */
    @Parameters(name = "{0}")
    public static Object[] data() throws IOException {
        List<Path> sourceFiles = Files.walk(TEST_FILES_PATH)
                .filter(p ->p.getFileName().toString().endsWith(".c"))
                .filter(p -> !p.getFileName().toString().startsWith("deactivated_"))
                .collect(Collectors.toList());
        
        return sourceFiles.toArray();
    }
    
    /**
     * Tests that all C-files placed in real test folder are translated without throwing an exception.
     */
    @Test
    public void testFile() {
        Path relativePath = BASE_PATH.relativize(file);
        SourceFile<ISyntaxElement> parsed = loadFile(relativePath.toString());
        
        assertNotNull("Could not parse " + relativePath.toString(), parsed);
    }
    
}
