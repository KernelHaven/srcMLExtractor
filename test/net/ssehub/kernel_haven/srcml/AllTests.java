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
    CTest.class,
    IncludeTest.class,
    InvalidFileTest.class,
    RobustnessTests.class,
    XmlParserTest.class,
    XmlPreprocessorTest.class,
    XmlPreprocessorNegativeTest.class,
    })
public class AllTests {
    public static final File TESTDATA = new File("testdata");
}
