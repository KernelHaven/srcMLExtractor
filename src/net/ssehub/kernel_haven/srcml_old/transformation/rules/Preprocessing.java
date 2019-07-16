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
package net.ssehub.kernel_haven.srcml_old.transformation.rules;

import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml_old.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml_old.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml_old.transformation.PreprocessorEndIf;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Restructures the {@link ITranslationUnit}s to be more close to the target {@link ISyntaxElement}-structure and to
 * simplify further processing.
 * More precisely it will do the following steps (in the given order):
 * <ol>
 *     <li>Translations of {@link ITranslationUnit}s into {@link PreprocessorBlock}s</li>
 *     <li>Moves/nests elements, which are encapsulated between two {@link PreprocessorBlock}s into the 
 *         responsible {@link PreprocessorBlock}. Will also delete {@link PreprocessorEndIf}'s as they are no longer
 *         needed after this processing.</li>
 *     <li>Computation and parsing of the {@link PreprocessorBlock#getEffectiveCondition()}s</li>
 * </ol>
 * @author El-Sharkawy
 *
 */
public class Preprocessing {
    
    /**
     * Restructures the {@link ITranslationUnit}s.
     * 
     * @param baseUnit The root element of the AST, which represents to complete parsed file.
     * 
     * @throws FormatException If converting the translation unit fails.
     */
    public void convert(@NonNull ITranslationUnit baseUnit) throws FormatException {
        /*
         *  Preprocessor statements
         */
        
        // Convert <cpp:{if, else, elif, else, endif}> to PreprocessorBlocks
        // set their condition as the CodeUnit texts separated by ' ', e.g. "defined ( A ) & & defined ( B )"
        new PreprocessorTranslation().transform(baseUnit);

        // remove 'extern "C" { ... }'
        new RemoveExternC().transform(baseUnit);
        
        // Add nested elements between PreprocessorBlocks as their children; remove PreprocessorEndifs
        new PreprocessorBlockStructure().transform(baseUnit);
        
        // Remove children for every PreprocessorBlocks that have condition "0"
        new IfZeroWorkaround().transform(baseUnit);
        
        // Replace all variables in PreprocessorBlock conditions that have no surrounding "defined()" with "0"
        // Removes the spaces between the elements, e.g. "defined ( A ) & & defined ( B )" -> "defined(A)&&defined(B)"
        new MissingDefined().transform(baseUnit);
        
        // Parse the conditions of PreprocessorBlocks
        new PreprocessorConditionComputationRule().transform(baseUnit);
        
        /*
         *  C-Code
         */
        
        new SingleStatementStructures().transform(baseUnit);
        new ElseIfFixture().transform(baseUnit);
        new SwitchCaseStructure().transform(baseUnit);
    }
}
