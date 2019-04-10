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

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;

/**
 * Tests that invalid source files are correctly detected.
 * 
 * @author Adam
 */
public class InvalidFileTest extends AbstractSrcMLExtractorTest {

    /**
     * Tests that running on a C++ file correctly throws an exception.
     * 
     * @throws SetUpException unwanted.
     * @throws ExtractorException expected.
     */
    @Test(expected = ExtractorException.class)
    public void testCplusplusFile() throws SetUpException, ExtractorException {
        String file = "invalidFile.cpp";
        
        Properties props = new Properties();
        props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
        props.setProperty("source_tree", "testdata/");
        props.setProperty("code.extractor.files", file);
        
        TestConfiguration config = new TestConfiguration(props);
        
        SrcMLExtractor extractor = new SrcMLExtractor();
        extractor.init(config);
        extractor.runOnFile(new File(file));
    }
    
    /**
     * Tests that running on a non-existing file correctly throws an exception.
     * 
     * @throws SetUpException unwanted.
     * @throws ExtractorException expected.
     */
    @Test(expected = ExtractorException.class)
    public void notExistingFile() throws SetUpException, ExtractorException {
        String file = "doesntExist.cpp";
        
        Properties props = new Properties();
        props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
        props.setProperty("source_tree", "testdata/");
        props.setProperty("code.extractor.files", file);
        
        TestConfiguration config = new TestConfiguration(props);
        
        SrcMLExtractor extractor = new SrcMLExtractor();
        extractor.init(config);
        extractor.runOnFile(new File(file));
    }
    
}
