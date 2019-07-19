/*
 * Copyright 2019 University of Hildesheim, Software Systems Engineering
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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AbstractAnalysis;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ErrorElement;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElementVisitor;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * An analysis that collects statistics about files and functions parseable by this srcML extractor.
 *
 * @author Adam
 */
public class ParsingStatistics extends AbstractAnalysis implements ISyntaxElementVisitor {

    private int numExceptions;
    
    private int numFiles;
    
    private int numFilesWithErrorElement;
    
    private int numFunctions;
    
    private int numFunctionsWithErrorElement;
    
    private boolean currentFunctionContainsErrorElement;
    
    private boolean currentFileContainsErrorElement;
    
    /**
     * Creates this analysis.
     * 
     * @param config The pipelien configuration.
     */
    public ParsingStatistics(@NonNull Configuration config) {
        super(config);
    }

    @Override
    public void run() {
        try {
            cmProvider.start();
        } catch (SetUpException e) {
            LOGGER.logException("Can't start CM extractor", e);
        }
        
        SourceFile<?> file;
        while ((file = cmProvider.getNextResult()) != null) {
            numFiles++;
            
            currentFileContainsErrorElement = false;
            for (ISyntaxElement element : file.castTo(ISyntaxElement.class)) {
                element.accept(this);
            }
            
            if (currentFileContainsErrorElement) {
                numFilesWithErrorElement++;
            }
        }
        
        while (cmProvider.getNextException() != null) {
            numExceptions++;
        }
        
        // print statistics
        String[] lines = {
            "srcML extractor parsing statistics:",
            " - Number of parsed files: " + numFiles,
            " - Number of unparsed files (exceptions): " + numExceptions,
            " - Number of files containing error element: " + numFilesWithErrorElement
                    + " (" + asPercent(numFiles, numFilesWithErrorElement) + ")",
            " - Number of functions found: " + numFunctions,
            " - Number of functions with error element: " + numFunctionsWithErrorElement
                    + " (" + asPercent(numFunctions, numFunctionsWithErrorElement) + ")"
        };
        LOGGER.logInfo(lines);
    }
    
    /**
     * Formats a ratio as percent.
     * 
     * @param full The full number.
     * @param part The part number.
     * 
     * @return The ratio between full and part as percent.
     */
    private static @NonNull String asPercent(int full, int part) {
        double ratio = (double) part / full;
        return notNull(String.format("%.2f%%", ratio));
    }
    
    @Override
    public void visitFunction(@NonNull Function function) {
        numFunctions++;
        
        currentFunctionContainsErrorElement = false;
        ISyntaxElementVisitor.super.visitFunction(function);
        
        if (currentFunctionContainsErrorElement) {
            numFunctionsWithErrorElement++;
        }
    }
    
    @Override
    public void visitErrorElement(@NonNull ErrorElement error) {
        ISyntaxElementVisitor.super.visitErrorElement(error);
        
        currentFileContainsErrorElement = true;
        currentFunctionContainsErrorElement = true;
    }

}
