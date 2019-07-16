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
package net.ssehub.kernel_haven.srcml_old.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.code_model.ast.BranchStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement.CaseType;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CodeList;
import net.ssehub.kernel_haven.code_model.ast.Comment;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ICode;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.Label;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement.LoopType;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.SwitchStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition.TypeDefType;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates the {@link ITranslationUnit}-structure into a {@link ISyntaxElement} (AST).
 * This is the final parsing step.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class TranslationUnitToAstConverter {

//    private static final Logger LOGGER = Logger.get();
    
    private @NonNull Deque<@NonNull Formula> cppPresenceConditions;
    private @NonNull Deque<@NonNull Formula> cppEffectiveConditions;
    private @NonNull Map<net.ssehub.kernel_haven.srcml_old.transformation.PreprocessorBlock, CppBlock> translatedBlocks;
    
    /**
     * Holds all switch statements while preserving nesting. This is needed to find the correct switch a
     * {@link CaseStatement} belongs to.
     * 
     * @see #convertCaseStatement(TranslationUnit, int, CaseType)
     */
    private @NonNull Deque<@NonNull SwitchStatement> switchs;
    
    private java.io.@NonNull File sourceFile;
    
    /**
     * Creates a {@link TranslationUnitToAstConverter} for the given source file.
     * 
     * @param sourceFile The source file that the translation units stem from.
     */
    public TranslationUnitToAstConverter(java.io.@NonNull File sourceFile) {
        cppPresenceConditions = new ArrayDeque<>();
        cppEffectiveConditions = new ArrayDeque<>();
        translatedBlocks = new HashMap<>();
        switchs = new ArrayDeque<>();
        this.sourceFile = sourceFile;
    }
    
    /**
     * Returns the current presence condition, considering the conditions of all surrounding CPP blocks, but won't pop
     * any elements from the stack.
     * @return The current presence condition (may be {@link True#INSTANCE} in case of no presence condition).
     */
    private @NonNull Formula getPc() {
        return cppPresenceConditions.isEmpty() ? True.INSTANCE : notNull(cppPresenceConditions.peek());
    }
    
    /**
     * Returns the currently effective condition. The currently effective condition is the condition that is directly
     * surrounding the current element.
     * 
     * @return The currently effective condition.
     */
    private @NonNull Formula getEffectiveCondition() {
        return cppEffectiveConditions.isEmpty() ? True.INSTANCE : notNull(cppEffectiveConditions.peek());
    }
    
    /**
     * Will compute a new presence condition based on the given formula on the current state of the stack and pushes
     * the result on top of the stack.
     * 
     * @param cppCondition The new encountered CPP condition.
     */
    private void pushFormula(@NonNull Formula cppCondition) {
        cppEffectiveConditions.push(cppCondition);
        if (cppPresenceConditions.isEmpty()) {
            cppPresenceConditions.push(cppCondition);
        } else {
            // TODO SE: Use of a cache?
            cppPresenceConditions.push(new Conjunction(cppPresenceConditions.peek(), cppCondition));
        }
    }
    
    /**
     * Pops the previously pushed CPP condition. 
     * 
     * @see #pushFormula(Formula)
     */
    private void popFormula() {
        cppPresenceConditions.pop();
        cppEffectiveConditions.pop();
    }
    
    /**
     * Translates the {@link ITranslationUnit}-structure into a {@link ISyntaxElement} (AST).
     * 
     * @param element The root element representing the complete file.
     * 
     * @return The translated AST still representing the complete file.
     * 
     * @throws FormatException If converting the given {@link ITranslationUnit} fails.
     */
    public @NonNull ISyntaxElement convert(@NonNull ITranslationUnit element) throws FormatException {
        ISyntaxElement result;
        
        if (element instanceof TranslationUnit) {
            result = convertTranslationUnit((TranslationUnit) element);
        } else if (element instanceof PreprocessorIf || element instanceof PreprocessorElse) {
            result = convertPreprocessorBlock((PreprocessorBlock) element);
        } else {
            throw ExceptionUtil.makeException("Illegal element " + element.getClass().getName(), element);
        }
        
        return result;
    }
    
    /**
     * Converts the given {@link TranslationUnit} based on its {@link TranslationUnit#getType()}. Recursively
     * descends into nested elements.
     * 
     * @param unit The unit to convert.
     * 
     * @return The result of the conversion.
     * 
     * @throws FormatException If converting the element fails.
     */
    // CHECKSTYLE:OFF // method is too long
    private @NonNull ISyntaxElement convertTranslationUnit(@NonNull TranslationUnit unit) throws FormatException {
    // CHECKSTYLE:ON
        Formula pc = getPc();
        ISyntaxElement result;
        
        switch (unit.getType()) {
        
        case "function_decl":
            result = createSingleStatement(unit, SingleStatement.Type.FUNCTION_DECLARATION);
            break;
            
        case "struct_decl": // falls through
        case "decl_stmt":
            result = createSingleStatement(unit, SingleStatement.Type.DECLARATION);
            break;
            
        case "expr_stmt": // falls through
        case "continue":  // falls through
        case "break":     // falls through
        case "goto":      // falls through 
        case "return":    // falls through
        case "empty_stmt":
            result = createSingleStatement(unit, SingleStatement.Type.INSTRUCTION);
            break;
            
        case "macro":
            result = createSingleStatement(unit, SingleStatement.Type.PREPROCESSOR_MACRO);
            break;
        
        case "label": {
            Label label = new Label(pc, makeCode(unit, 0, unit.size() - 1, false));
            label.setSourceFile(sourceFile);
            label.setCondition(getEffectiveCondition());
            label.setLineStart(unit.getStartLine());
            label.setLineEnd(unit.getEndLine());
            result = label;
            break;
        }
        
        case "comment": {
            Comment comment = new Comment(pc, makeCode(unit, 0, unit.size() - 1, false));
            comment.setSourceFile(sourceFile);
            comment.setCondition(getEffectiveCondition());
            comment.setLineStart(unit.getStartLine());
            comment.setLineEnd(unit.getEndLine());
            result = comment;
            break;
        }
        
        case "for": {
            // Last nested is the loop block, everything before is the condition
            result = createLoop(unit, LoopType.FOR, unit.size() - 1, 0, unit.size() - 2);
            break;
        }
            
        case "while": {
            // Last nested is the loop block, everything before is the condition
            result = createLoop(unit, LoopType.WHILE, unit.size() - 1, 0, unit.size() - 2);
            break;
        }
        
        case "do": {
            // 2nd is block everything after is condition, we skip last element (a semicolon)
            result = createLoop(unit, LoopType.DO_WHILE, 1, 3, unit.size() - 2);
            break;
        }
        
        case "if": {
            // the last elements are block(s), elseif(s) and else(s)
            // find the first code element starting from the right (this is most likely the closing paranthesis).
            int lastCodeElement = unit.size() - 1;
            while (lastCodeElement > 0 && !isCode(unit.getNestedElement(lastCodeElement))) {
                lastCodeElement--;
            }
            
            BranchStatement ifStatement = new BranchStatement(pc, BranchStatement.Type.IF,
                    // allow translation units, since there may be ELSE statements from ternary opeartors
                    makeCode(unit, 0, lastCodeElement, true));
            ifStatement.setSourceFile(sourceFile);
            ifStatement.setCondition(getEffectiveCondition());
            ifStatement.setLineStart(unit.getStartLine());
            ifStatement.setLineEnd(unit.getEndLine());
            ifStatement.addSibling(ifStatement);
            for (int i = lastCodeElement + 1; i < unit.size(); i++) {
                ISyntaxElement child = convert(unit.getNestedElement(i));
                
                addIfSibling(child, ifStatement);
                
                ifStatement.addNestedElement(child);
            }
            
            // add sibling references to all siblings
            for (int i = 1; i < ifStatement.getSiblingCount(); i++) {
                ifStatement.getSibling(i).addSibling(ifStatement);
                for (int j = 1; j < ifStatement.getSiblingCount(); j++) {
                    ifStatement.getSibling(i).addSibling(ifStatement.getSibling(j));
                }
            }
            
            result = ifStatement;
            break;
        }
            
        case "elseif": {
            // the last elements are block(s), elseif(s) and else(s)
            // find the first code element starting from the right (this is most likely the closing paranthesis).
            int lastCodeElement = unit.size() - 1;
            while (lastCodeElement > 0 && !isCode(unit.getNestedElement(lastCodeElement))) {
                lastCodeElement--;
            }
            
            // allow translation units, since there may be ELSE statements from ternary opeartors
            ICode condition = makeCode(unit, 0, lastCodeElement, true);
            BranchStatement elifBlock = new BranchStatement(pc, BranchStatement.Type.ELSE_IF, condition);
            elifBlock.setSourceFile(sourceFile);
            elifBlock.setCondition(getEffectiveCondition());
            elifBlock.setLineStart(unit.getStartLine());
            elifBlock.setLineEnd(unit.getEndLine());
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                // ignore { and }
                if (!(child instanceof CodeUnit)) {
                    elifBlock.addNestedElement(convert(child));
                }
            }
            result = elifBlock;
            break;
        }
        
        case "else": {
            BranchStatement elseBlock = new BranchStatement(pc, BranchStatement.Type.ELSE, null);
            elseBlock.setSourceFile(sourceFile);
            elseBlock.setCondition(getEffectiveCondition());
            elseBlock.setLineStart(unit.getStartLine());
            elseBlock.setLineEnd(unit.getEndLine());
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                // ignore { and }
                if (!(child instanceof CodeUnit)) {
                    elseBlock.addNestedElement(convert(child));
                }
            }
            
            result = elseBlock;
            break;
        }
        
        case "enum": {
            result = createTypeDef(unit, pc, TypeDefType.ENUM);
            break;
        }
            
        case "struct": {
            result = createTypeDef(unit, pc, TypeDefType.STRUCT);
            break;
        }
            
        case "typedef": {
            result = createTypeDef(unit, pc, TypeDefType.TYPEDEF);
            break;
        }
            
        case "union": {
            result = createTypeDef(unit, pc, TypeDefType.UNION);
            break;
        }
            
        case "unit": {
            File file = new File(pc, sourceFile);
            file.setSourceFile(sourceFile);
            file.setCondition(getEffectiveCondition());
            file.setLineStart(unit.getStartLine());
            file.setLineEnd(unit.getEndLine());
            for (int i = 0; i < unit.size(); i++) {
                file.addNestedElement(convert(unit.getNestedElement(i)));
            }
            
            result = file;
            break;
        }
        
        case "function": {
            Function f = new Function(pc, notNull(unit.getFunctionName()), // notNull, since this is a function
                    // last element is the block
                    // ignore TranslationUnits, since parameters may be FUNCTION_DECL
                    makeCode(unit, 0, unit.size() - 2, true));
            f.setSourceFile(sourceFile);
            f.setCondition(getEffectiveCondition());
            
            f.setLineStart(unit.getStartLine());
            f.setLineEnd(unit.getEndLine());
            
            f.addNestedElement(convert(unit.getNestedElement(unit.size() - 1)));
            
            result = f;
            break;
        }
        
        case "block": {
            CompoundStatement block = new CompoundStatement(pc);
            block.setSourceFile(sourceFile);
            block.setCondition(getEffectiveCondition());
            block.setLineStart(unit.getStartLine());
            block.setLineEnd(unit.getEndLine());
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                // ignore { and }
                if (!(child instanceof CodeUnit)) {
                    block.addNestedElement(convert(child));
                }
            }
            
            result = block;
            break;
        }
        
        case "switch": {
            /*
             * Last element is switch-Block, before comes the condition
             */
            SwitchStatement switchStatement = new SwitchStatement(getPc(), makeCode(unit, 0, unit.size() - 2, false));
            switchStatement.setSourceFile(sourceFile);
            switchStatement.setCondition(getEffectiveCondition());
            switchStatement.setLineStart(unit.getStartLine());
            switchStatement.setLineEnd(unit.getEndLine());
            switchs.push(switchStatement);
            switchStatement.addNestedElement(convert(unit.getNestedElement(unit.size() - 1)));
            switchs.pop();
            result = switchStatement;
            break;
        }
        
        case "case": {
            /*
             * 3-4 Elements belong to the condition, afterwards come nested statements
             */
            int lastElementIndex = 2;
            boolean colonFound = false;
            
            // Search the colon
            while (unit.size() > lastElementIndex && !colonFound) {
                ITranslationUnit nested = unit.getNestedElement(lastElementIndex);
                if (isCode(nested)) {
                    if (nested instanceof CodeUnit && ":".equals(((CodeUnit) nested).getCode())) {
                        colonFound = true;
                    } else {
                        lastElementIndex++;
                    }
                }
            }
            result = convertCaseStatement(unit, lastElementIndex, CaseType.CASE);
            break;
        }
        
        case "default": {
            /*
             * First Element is the condition, afterwards come nested statements
             */
            result = convertCaseStatement(unit, 0, CaseType.DEFAULT);
            break;
        }
            
            
        case "cpp:define": {
            result = convertCppStatement(unit, CppStatement.Type.DEFINE);
            break;
        }

        case "cpp:undef": {
            result = convertCppStatement(unit, CppStatement.Type.UNDEF);
            break;
        }
            
        case "cpp:include": {
            result = convertCppStatement(unit, CppStatement.Type.INCLUDE);
            break;
        }
            
        case "cpp:pragma": {
            result = convertCppStatement(unit, CppStatement.Type.PRAGMA);
            break;
        }
            
        case "cpp:error": {
            result = convertCppStatement(unit, CppStatement.Type.ERROR);
            break;
        }
            
        case "cpp:warning": {
            result = convertCppStatement(unit, CppStatement.Type.WARNING);
            break;
        }
            
        case "cpp:line": {
            result = convertCppStatement(unit, CppStatement.Type.LINE);
            break;
        }
            
        case "cpp:empty": {
            result = convertCppStatement(unit, CppStatement.Type.EMPTY);
            break;
        }
        
        default:
            throw ExceptionUtil.makeException("Unexpected unit type: " + unit.getType(), unit);
        }

        return result;
    }
    
    /**
     * Creates a {@link SingleStatement} based on the given {@link TranslationUnit}. The nested elements inside the
     * given unit are converted to {@link Code} (see {@link #makeCode(ITranslationUnit, int, int, boolean)}).
     * 
     * @param unit The unit that a {@link SingleStatement} should be created for.
     * @param type The type of {@link SingleStatement} that should be created.
     * 
     * @return The created {@link SingleStatement}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull SingleStatement createSingleStatement(@NonNull TranslationUnit unit,
            SingleStatement.@NonNull Type type) throws FormatException {
        
        // allow translationUnits in makeCode, since e.g. decl_stmts may contain blocks
        SingleStatement singleStatement = new SingleStatement(getPc(), makeCode(unit, 0, unit.size() - 1, true), type);
        singleStatement.setSourceFile(sourceFile);
        singleStatement.setCondition(getEffectiveCondition());
        singleStatement.setLineStart(unit.getStartLine());
        singleStatement.setLineEnd(unit.getEndLine());
        return singleStatement;
    }
    
    /**
     * Creates a {@link CppStatement} based on the given {@link TranslationUnit}. The nested elements inside the
     * given unit are converted to {@link Code} (except the first two)
     * (see {@link #makeCode(ITranslationUnit, int, int, boolean)}).
     * 
     * @param unit The unit that a {@link CppStatement} should be created for.
     * @param type The type of {@link CppStatement} that should be created.
     * 
     * @return The created {@link CppStatement}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull CppStatement convertCppStatement(@NonNull TranslationUnit unit, CppStatement.@NonNull Type type)
            throws FormatException {
        ICode expression = null;
        // first two strings inside are # and the type, skip these
        if (unit.size() > 2) {
            expression = makeCode(unit, 2, unit.size() - 1, false);
        }
        
        CppStatement statement = new CppStatement(getPc(), type, expression);
        statement.setSourceFile(sourceFile);
        statement.setCondition(getEffectiveCondition());
        statement.setLineStart(unit.getStartLine());
        statement.setLineEnd(unit.getEndLine());
        return statement;
    }

    /**
     * Creates a {@link CaseStatement} based on the given {@link TranslationUnit}. The nested elements inside the
     * given unit up to condEndIndex are converted to {@link Code}
     * (see {@link #makeCode(ITranslationUnit, int, int, boolean)}). Further nested elements are recursively
     * converted and added as children.
     * 
     * @param unit The unit that a {@link CaseStatement} should be created for.
     * @param condEndIndex The index of nested elements where the case condition ends.
     * @param type The type of {@link CaseStatement} that should be created.
     * 
     * @return The created {@link CaseStatement}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull CaseStatement convertCaseStatement(@NonNull TranslationUnit unit, int condEndIndex,
            @NonNull CaseType type) throws FormatException {
        
        if (switchs.isEmpty()) {
            throw ExceptionUtil.makeException("Found " + type + " outside of switch ", unit);
        }
        SwitchStatement switchStmt = notNull(switchs.peek());
        
        ICode conditionCode = null;
        if (type == CaseType.CASE) {
            conditionCode = makeCode(unit, 0, condEndIndex, false);
        }
        CaseStatement caseStatement
                = new CaseStatement(getPc(), conditionCode, type, switchStmt);
        caseStatement.setSourceFile(sourceFile);
        caseStatement.setCondition(getEffectiveCondition());
        caseStatement.setLineStart(unit.getStartLine());
        caseStatement.setLineEnd(unit.getEndLine());
        switchStmt.addCase(caseStatement);
        for (int i = condEndIndex + 1; i < unit.size(); i++) {
            caseStatement.addNestedElement(convert(unit.getNestedElement(i)));
        }
        return caseStatement;
    }

    /**
     * Creates a {@link TypeDefinition} out of the given {@link TranslationUnit}.
     * 
     * @param unit The unit to convert to a {@link TypeDefinition}.
     * @param pc The presence condition of the {@link TypeDefinition}.
     * @param type The type of the {@link TypeDefinition}.
     * 
     * @return The converted {@link TypeDefinition}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull TypeDefinition createTypeDef(@NonNull TranslationUnit unit, @NonNull Formula pc,
            @NonNull TypeDefType type) throws FormatException {
        /*
         * Find last <block> element
         * (<block> refers to anything that isn't code, e.g. also <struct>)
         */
        int blockIndex = -1;
        for (int i = unit.size() - 1; i >= 0; i--) {
            ITranslationUnit child = unit.getNestedElement(i);
            if (!isCode(child)) {
                blockIndex = i;
                break;
            }
        }
        
        // parse everything up to the <block> into a Code
        int endIndex;
        if (blockIndex == -1) {
            endIndex = unit.size() - 1;
        } else {
            endIndex = blockIndex - 1;
        }
        
        ICode code = makeCode(unit, 0, endIndex, false);
        
        // if there is something to the right of the <block> append it to the code
        // endIndex + 1 is the <block>, endIndex + 2 may be further code to parse
        if (endIndex + 2 < unit.size()) {
            code = joinCodes(code, makeCode(unit, endIndex + 2, unit.size() - 1, false));
            
            // TODO: now we don't know where in the code the <block> would be located at...
        }
        
        ISyntaxElement nested = null;
        
        if (blockIndex != -1) {
            ITranslationUnit block = unit.getNestedElement(blockIndex);
            
            if (type == TypeDefType.ENUM) {
                // don't parse the <block> of enums as a statement
                ICode blockCode = makeCode(block, 0, block.size() - 1, false);
                
                // join blockCode with previous declaration
                code = joinCodes(code, blockCode);
                
            } else {
                // add the <block> as a parsed statement to everything else
                nested = convert(block);
            }
        }
        
        TypeDefinition typeDef = new TypeDefinition(pc, code, type);
        typeDef.setSourceFile(sourceFile);
        typeDef.setCondition(getEffectiveCondition());
        typeDef.setLineStart(unit.getStartLine());
        typeDef.setLineEnd(unit.getEndLine());
        if (nested != null) {
            typeDef.addNestedElement(nested);
        }
        
        return typeDef;
    }

    /**
     * Creates a {@link LoopStatement} from the given {@link TranslationUnit}.
     * 
     * @param unit The unit to convert to a {@link LoopStatement}.
     * @param type The type of {@link LoopStatement} to create.
     * @param blockIndex The index of nested elements where the loop body block is located at.
     * @param condStartIndex The index of nested elements where the first condition element is located at. 
     * @param condEndIndex The index of nested elements where the last condition element is located at.
     *  
     * @return The created {@link LoopStatement}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull LoopStatement createLoop(@NonNull TranslationUnit unit, @NonNull LoopType type, int blockIndex,
            int condStartIndex, int condEndIndex) throws FormatException {
        
        ICode condition = makeCode(unit, condStartIndex, condEndIndex, false);
        LoopStatement loop = new LoopStatement(getPc(),  condition, type);
        loop.setSourceFile(sourceFile);
        loop.setCondition(getEffectiveCondition());
        loop.setLineStart(unit.getStartLine());
        loop.setLineEnd(unit.getEndLine());
        loop.addNestedElement(convert(unit.getNestedElement(blockIndex)));
        return loop;
    }
    
    /**
     * Converts the given {@link PreprocessorBlock} to a {@link CppBlock}.
     * 
     * @param cppBlock The {@link PreprocessorBlock} to convert.
     * 
     * @return The converted {@link CppBlock}.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull CppBlock convertPreprocessorBlock(@NonNull PreprocessorBlock cppBlock) throws FormatException {
        Formula condition = notNull(cppBlock.getEffectiveCondition()); // all CPP blocks have effective conditions now
        pushFormula(condition);
        CppBlock translatedBlock = createCppBlock(cppBlock);
        translatedBlock.setSourceFile(sourceFile);
        translatedBlock.setCondition(getEffectiveCondition());
        translatedBlock.setLineStart(cppBlock.getStartLine());
        translatedBlock.setLineEnd(cppBlock.getEndLine());
        
        for (int i = 0; i < cppBlock.size(); i++) {
            ITranslationUnit child = cppBlock.getNestedElement(i);
            translatedBlock.addNestedElement(convert(child));
        }
        popFormula();
        
        return translatedBlock;
    }

    /**
     * Creates the {@link CppBlock} and handles correct setting of siblings.
     * @param cppBlock The currently translated preprocessor block.
     * @return <tt>cppBlock</tt> translated to an {@link CppBlock}.
     */
    private CppBlock createCppBlock(PreprocessorBlock cppBlock) {
        // All CPP blocks have effective conditions now
        CppBlock translatedBlock = new CppBlock(getPc(), notNull(cppBlock.getEffectiveCondition()),
            notNull(Type.valueOf(cppBlock.getType())));
        translatedBlock.setSourceFile(sourceFile);
        translatedBlock.setCondition(getEffectiveCondition());
        translatedBlock.setLineStart(cppBlock.getStartLine());
        translatedBlock.setLineEnd(cppBlock.getEndLine());
        translatedBlocks.put(cppBlock, translatedBlock);
        
        PreprocessorIf start = cppBlock.getStartingIf();
        // we will always have a start block, but it may be == translatedBlock
        CppBlock startBlock = translatedBlocks.get(start);
        
        // add this block as sibling to the first block
        startBlock.addSibling(translatedBlock);
        
        // copy all siblings from the start block to this new block
        if (startBlock != translatedBlock) {
            for (int i = 0; i < startBlock.getSiblingCount(); i++) {
                translatedBlock.addSibling(startBlock.getSibling(i));
            }
        }
        
        // add to all other else-siblings that have been translated so far
        for (int i = 0; i < start.getNumberOfElseBlocks(); i++) {
            CppBlock siblingBlock = translatedBlocks.get(start.getElseBlock(i));
            // blocks may not have been translated -> check != null
            // block may be the currently created block (and we already added it to itself) -> check != translatedBlock
            if (siblingBlock != null && siblingBlock != translatedBlock) {
                siblingBlock.addSibling(translatedBlock);
            }
        }
        
        return translatedBlock;
    }
    
    /**
     * Adds the given element as a sibling to ifStatement, if its a {@link BranchStatement}. If element is a
     * {@link CppBlock}, then search for {@link BranchStatement}s inside of it.
     * 
     * @param element The element that may be added as a sibling to ifStatement.
     * @param ifStatement The ifStatement that element may be a sibling of.
     */
    private void addIfSibling(ISyntaxElement element, BranchStatement ifStatement) {
        if (element instanceof BranchStatement) {
            ifStatement.addSibling((BranchStatement) element);
            
        } else if (element instanceof CppBlock) {
            for (int i = 0; i < element.getNestedElementCount(); i++) {
                addIfSibling(element.getNestedElement(i), ifStatement);
            }
        }
    }
    
    /**
     * Converts the given nested elements of the given unit to {@link ICode}. 
     * 
     * @param unit The unit that the nested elements should be converted for.
     * @param start The index of the first element to convert; inclusive.
     * @param end The index of the last element to convert, inclusive.
     * @param allowTranslationUnits Whether {@link TranslationUnit}s should be recursively turned into {@link ICode}.
     * 
     * @return An {@link ICode} representation of the given nested elements.
     * 
     * @throws FormatException If converting the elmeents fails.
     */
    // CHECKSTYLE:OFF // method is too long
    private @NonNull ICode makeCode(@NonNull ITranslationUnit unit, int start, int end,
            boolean allowTranslationUnits) throws FormatException {
    // CHECKSTYLE:ON
        
        StringBuilder code = new StringBuilder();
        int firstCodeLine = -1; // the starting line number of the first element in "code"
        int lastCodeLine = -1; // the end line number of the last element in "code"
        List<@NonNull ICode> result = new LinkedList<>();
        
        for (int i = start; i <= end; i++) {
            ITranslationUnit child = unit.getNestedElement(i);
            
            if (child instanceof CodeUnit) {
                if (code.length() != 0) {
                    code.append(' ');
                    // update lastCodeLine
                    lastCodeLine = child.getEndLine();
                } else {
                    // initialize firstCodeLine and lastCodeLine
                    firstCodeLine = child.getStartLine(); 
                    lastCodeLine = child.getStartLine(); 
                }
                code.append(((CodeUnit) child).getCode());
                
            } else if ("comment".equals(child.getType())) {
                if (code.length() > 0) { // handle remainder of code
                    Code codeElement = new Code(getPc(), notNull(code.toString()));
                    codeElement.setSourceFile(sourceFile);
                    codeElement.setCondition(getEffectiveCondition());
                    codeElement.setLineStart(firstCodeLine);
                    codeElement.setLineEnd(lastCodeLine);
                    result.add(codeElement);
                    code = new StringBuilder();
                    firstCodeLine = 0;
                    lastCodeLine = 0;
                }
                
                Comment comment = (Comment) convert(child);
                result.add(comment);
                
            } else if (child instanceof PreprocessorBlock) {
                if (code.length() > 0) { // handle remainder of code
                    Code codeElement = new Code(getPc(), notNull(code.toString()));
                    codeElement.setSourceFile(sourceFile);
                    codeElement.setCondition(getEffectiveCondition());
                    codeElement.setLineStart(firstCodeLine);
                    codeElement.setLineEnd(lastCodeLine);
                    result.add(codeElement);
                    code = new StringBuilder();
                    firstCodeLine = 0;
                    lastCodeLine = 0;
                }
                
                // all CPP blocks have effective conditions now
                Formula condition = notNull(((PreprocessorBlock) child).getEffectiveCondition());
                pushFormula(condition);
                CppBlock cppif = createCppBlock((PreprocessorBlock) child);
                cppif.setSourceFile(sourceFile);
                cppif.setCondition(getEffectiveCondition());
                cppif.setLineStart(child.getStartLine());
                cppif.setLineEnd(child.getEndLine());
                ICode nested = makeCode(child, 0, child.size() - 1, allowTranslationUnits);
                if (nested instanceof CodeList) {
                    for (int j = 0; j < nested.getNestedElementCount(); j++) {
                        cppif.addNestedElement(nested.getNestedElement(j));
                    }
                } else {
                    cppif.addNestedElement(nested);
                }
                
                result.add(cppif);
                popFormula();
                
            } else if (allowTranslationUnits // this means we should recursively walk through non-allowed elements
                || child.getType().equals("macro") // sometimes srcML randomly interprets function calls as macros...
            ) { 
                // TODO: log a warning here?
                
                // save everything up to this point
                if (code.length() > 0) {
                    Code codeElement = new Code(getPc(), notNull(code.toString()));
                    codeElement.setSourceFile(sourceFile);
                    codeElement.setCondition(getEffectiveCondition());
                    codeElement.setLineStart(firstCodeLine);
                    codeElement.setLineEnd(lastCodeLine);
                    result.add(codeElement);
                    code = new StringBuilder();
                    firstCodeLine = 0;
                    lastCodeLine = 0;
                }
                
                // recursively transform everything into code
                result.add(makeCode(child, 0, child.size() - 1, allowTranslationUnits));
                
            } else {
                throw ExceptionUtil.makeException("makeCode() Expected "
                        + CodeUnit.class.getSimpleName() + " or " + PreprocessorBlock.class.getSimpleName()
                        + ", got " + child.getClass().getSimpleName() + " (type = " + child.getType() + ")", unit);
            }
        }
        
        if (code.length() > 0) {
            Code codeElement = new Code(getPc(), notNull(code.toString()));
            codeElement.setSourceFile(sourceFile);
            codeElement.setCondition(getEffectiveCondition());
            codeElement.setLineStart(firstCodeLine);
            codeElement.setLineEnd(lastCodeLine);
            result.add(codeElement);
        }
        
        ICode resultCode;
        
        if (result.size() == 0) {
            throw ExceptionUtil.makeException("makeCode() Found no elements to make code", unit);
            
        } else if (result.size() == 1) {
            resultCode = notNull(result.get(0));
            
        } else {
            resultCode = notNull(result.get(0));
            
            for (int i = 1; i < result.size(); i++) {
                resultCode = joinCodes(resultCode, result.get(i));
            }
        }
        
        return resultCode;
    }
    
    /**
     * Joins the two unparsed {@link ICode} objects together into one. If both are instances of {@link Code} then
     * a new {@link Code} with <code>code1.text + code2.text</code> is returned. Else, a {@link CodeList} with the
     * proper children is returned.
     * 
     * @param code1 The left side.
     * @param code2 The right side.
     * 
     * @return The joined code. The presence condition and condition are taken from code1.
     */
    private @NonNull ICode joinCodes(@NonNull ICode code1, @NonNull ICode code2) {
        ICode result;
        
        if (code1 instanceof Code && code2 instanceof Code) {
            result = new Code(code1.getPresenceCondition(), ((Code) code1).getText() + ' '
                    + ((Code) code2).getText());
            result.setCondition(code1.getCondition());
            result.setSourceFile(sourceFile);
            result.setLineStart(code1.getLineStart());
            result.setLineEnd(code2.getLineEnd());
            
        } else {
            result = new CodeList(code1.getPresenceCondition());
            result.setCondition(code1.getCondition());
            result.setSourceFile(sourceFile);
            result.setLineStart(code1.getLineStart());
            result.setLineEnd(code2.getLineEnd());
            
            if (code1 instanceof CodeList) {
                CodeList list = (CodeList) code1;
                for (int i = 0; i < list.getNestedElementCount(); i++) {
                    result.addNestedElement(list.getNestedElement(i));
                }
            } else {
                result.addNestedElement(code1);
            }
            
            if (code2 instanceof CodeList) {
                CodeList list = (CodeList) code2;
                for (int i = 0; i < list.getNestedElementCount(); i++) {
                    result.addNestedElement(list.getNestedElement(i));
                }
            } else {
                result.addNestedElement(code2);
            }
        }
        
        return result;
    }
    
    /**
     * Helper method to determine if the given {@link ITranslationUnit} can be converted to {@link Code}.
     * 
     * @param unit The unit to test.
     * 
     * @return Whether the given unit can safely be considered to be a {@link Code}.
     */
    private boolean isCode(ITranslationUnit unit) {
        return !(unit instanceof TranslationUnit) || unit.getType().equals("comment") || unit.getType().equals("macro");
    }
    
}
