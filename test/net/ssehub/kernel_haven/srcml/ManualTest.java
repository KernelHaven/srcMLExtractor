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

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.Util;

/**
 * Helper class for manual testing of the extractor.
 * 
 * @author Adam
 */
public class ManualTest extends AbstractSrcMLExtractorTest {
    
    /**
     * Dummy "test" that simply logs the result to console.
     */
    @Test
    public void test() {
        Logger.get().setLevel(Level.DEBUG);
        PerformanceProbe.setEnabled(true);
        
        long t0 = System.currentTimeMillis();
//        SourceFile<ISyntaxElement> ast = loadFile("FunctionsAndCPP.c");
//        SourceFile ast = loadFile("NestedCppIfs.c");
        SourceFile<ISyntaxElement> ast = loadFile("test5.c");
//        SourceFile<ISyntaxElement> ast = loadFile("FunctionWithIfdefHeader.c");
//        SourceFile<ISyntaxElement> ast = loadFile("test2.c", HeaderHandling.EXPAND_FUNCTION_CONDITION);
//        SourceFile ast = loadFile("../real/Linux4.15/pci_stub.c");
        
        long t1 = System.currentTimeMillis();
        
        System.out.println(ast.getElement(0));
        
        System.out.println("Duration: " + Util.formatDurationMs(t1 - t0));
        PerformanceProbe.printResult();
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file, HeaderHandling headerHandling) {
        return super.loadFile("manual/" + file, headerHandling);
    }
    
}
