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
