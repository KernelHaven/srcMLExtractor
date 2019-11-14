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
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.ErrorElement;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElementVisitor;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.cpp_utils.CppParsingSettings;
import net.ssehub.kernel_haven.cpp_utils.InvalidConditionHandling;
import net.ssehub.kernel_haven.cpp_utils.non_boolean.INonBooleanFormulaVisitor;
import net.ssehub.kernel_haven.cpp_utils.non_boolean.Literal;
import net.ssehub.kernel_haven.cpp_utils.non_boolean.Macro;
import net.ssehub.kernel_haven.cpp_utils.non_boolean.NonBooleanOperator;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * An analysis that collects statistics about files and functions parseable by this srcML extractor.
 *
 * @author Adam
 * @author El-Sharkawy
 */
public class ParsingStatistics extends AbstractAnalysis implements ISyntaxElementVisitor {
    
    /**
     * Checks whether a formula contains PARSING_ERROR.
     * This will only work if {@link CppParsingSettings#INVALID_CONDITION_SETTING} was configured this way. 
     * 
     *
     * @author El-Sharkawy
     */
    private static class FormulaChecker implements INonBooleanFormulaVisitor<Boolean> {

        private boolean allOK = true;
        
        @Override
        public Boolean visitFalse(@NonNull False falseConstant) {
            return allOK;
        }

        @Override
        public Boolean visitTrue(@NonNull True trueConstant) {
            return allOK;
        }

        @Override
        public Boolean visitVariable(@NonNull Variable variable) {
            if ("PARSING_ERROR".equals(variable.getName())) {
                allOK = false;
            }
            return allOK;
        }

        @Override
        public Boolean visitNegation(@NonNull Negation formula) {
            if (allOK) {
                formula.getFormula().accept(this);
            }
            return allOK;
        }

        @Override
        public Boolean visitDisjunction(@NonNull Disjunction formula) {
            if (allOK) {
                formula.getLeft().accept(this);
            }
            if (allOK) {
                formula.getRight().accept(this);
            }
            return allOK;
        }

        @Override
        public Boolean visitConjunction(@NonNull Conjunction formula) {
            if (allOK) {
                formula.getLeft().accept(this);
            }
            if (allOK) {
                formula.getRight().accept(this);
            }
            return allOK;
        }

        @Override
        public Boolean visitNonBooleanOperator(NonBooleanOperator operator) {
            if (allOK) {
                operator.getLeft().accept(this);
            }
            if (allOK) {
                operator.getRight().accept(this);
            }
            return allOK;
        }

        @Override
        public Boolean visitLiteral(Literal literal) {
            return allOK;
        }

        @Override
        public Boolean visitMacro(Macro macro) {
            Formula argument = macro.getArgument();
            if (allOK && null != argument) {
                argument.accept(this);
            }
            return allOK;
        }
        
    }

    private int numExceptions;
    
    private int numFiles;
    
    private int numFilesWithErrorElement;
    
    private int numFunctions;
    
    private int numFunctionsWithErrorElement;
    
    /**
     * All blocks, including if, elif, else.
     */
    private int numCppBlocks;
    
    /**
     * All blocks except for else.
     */
    private int numCppConditions;
    
    /**
     * All blocks except for else that do not contain error elements.
     */
    private int numCppConditionsWithoutErrors;
    /**
     * All blocks except for else that contain error elements.
     */
    private int numCppConditionsErrors;
    
    private boolean currentFunctionContainsErrorElement;
    
    private boolean currentFileContainsErrorElement;
    
    private @NonNull FormulaChecker checker;
    
    private boolean checkFormulas = false;;
    
    /**
     * Creates this analysis.
     * 
     * @param config The pipeline configuration.
     */
    public ParsingStatistics(@NonNull Configuration config) {
        super(config);
        checker = new FormulaChecker();
        InvalidConditionHandling conditionHandling = config.getValue(CppParsingSettings.INVALID_CONDITION_SETTING);
        if (conditionHandling == InvalidConditionHandling.ERROR_VARIABLE) {
            checkFormulas = true;
        }
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
        
        // Print File-based Statistics
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
        
        // Print Cpp Statistics
        if (checkFormulas) {
            String[] cppLines = {
                "CPP parsing statistics:",
                " - Number of parsed CPP blocks (including #else): " + numCppBlocks,
                " - Number of parsed CPP conditions (excluding #else): " + numCppConditions,
                " - Number of succesfully parsed CPP conditions: " + numCppConditionsWithoutErrors
                    + " (" + asPercent(numCppConditions, numCppConditionsWithoutErrors) + ")",
                " - Number of unsuccesfully parsed CPP conditions: " + numCppConditionsErrors
                    + " (" + asPercent(numCppConditionsErrors, numCppConditionsWithoutErrors) + ")",
            };
            LOGGER.logInfo(cppLines);
        }
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
        return notNull(String.format("%.2f%%", ratio * 100));
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

    @Override
    public void visitCppBlock(@NonNull CppBlock block) {
        ISyntaxElementVisitor.super.visitCppBlock(block);
        
        // CPP Stats
        numCppBlocks++;
        if (block.getType() != CppBlock.Type.ELSE) {
            numCppConditions++;
            
            if (checkFormulas) {
                Formula condition = block.getCondition();
                if (null != condition) {
                    checker.allOK = true;
                    if (condition.accept(checker)) {
                        numCppConditionsWithoutErrors++;
                    } else {
                        numCppConditionsErrors++;                    
                    }
                }
            }
        }
    }
}
