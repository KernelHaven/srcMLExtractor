package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.BranchStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CodeList;
import net.ssehub.kernel_haven.code_model.ast.Comment;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.CppStatement.Type;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ICode;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElementVisitor;
import net.ssehub.kernel_haven.code_model.ast.Label;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.SwitchStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;
import net.ssehub.kernel_haven.srcml.SrcMLExtractor;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Replaces #include directives with the parsed contents of the header they reference. TODO: Currently only supports
 * quote includes (#include "header.h") that are relative to the file being parsed.
 * 
 * @author Adam
 */
public class IncludeExpander implements ISyntaxElementVisitor {
    
    private static final Logger LOGGER = Logger.get();
    
    /**
     * The folder of the currently parsed C file.
     */
    private @NonNull File folder;
    
    private @NonNull SrcMLExtractor extractor;
    
    private @NonNull Deque<@NonNull ISyntaxElement> parents;
    
    public IncludeExpander(@NonNull File absoulteTarget, @NonNull SrcMLExtractor extractor) {
        this.extractor = extractor;
        this.folder = notNull(absoulteTarget.getParentFile());
        this.parents = new LinkedList<>();
    }
    
    public void expand(@NonNull ISyntaxElement file) throws FormatException {
        file.accept(this);
    }

    private @Nullable File findSystemFile(@NonNull String include) {
        return null; // TODO AK: currently not supported
    }
    
    private @Nullable File findQuoteFile(@NonNull String include) {
        File result = null;
        
        // try relative to currently parsed file
        result = new File(folder, include);
        if (!result.isFile()) {
            result = null;
        }
        
        // if no "local" file was found, fall back to system include
        if (result == null) {
            result = findSystemFile(include);
        }
        
        return result;
    }
    
    private @Nullable File findFile(@NonNull String include) {
        File result = null;
        
        if (include.startsWith("<")) {
            if (!include.endsWith(">")) {
                LOGGER.logWarning("Found invalud include directive: " + include);
            }
            result = findSystemFile(notNull(include.substring(1, include.length() - 1).trim()));
            
        } else if (include.startsWith("\"")) {
            if (!include.endsWith("\"")) {
                LOGGER.logWarning("Found invalud include directive: " + include);
            }
            result = findQuoteFile(notNull(include.substring(1, include.length() - 1).trim()));
            
        } else {
            LOGGER.logWarning("Found invalud include directive: " + include);
        }
        
        return result;
    }
    
    @Override
    public void visitCppStatement(@NonNull CppStatement cppStatement) {
        if (cppStatement.getType() == Type.INCLUDE) {
            ICode code = cppStatement.getExpression();
            if (code instanceof Code) {
                String include = notNull(((Code) code).getText().trim());
                File file = findFile(include);
                
                if (file != null) {
                    LOGGER.logDebug("Parsing include: " + include, "-> " + file);
                    
                    try {
                        SourceFile header = extractor.parseFile(file, file);
                        
                        LOGGER.logDebug("Replacing #include with parsed header " + file);
                        
                        ISyntaxElement parent = notNull(parents.peek());
                        parent.replaceNestedElement(cppStatement, (ISyntaxElement) header.getElement(0));
                        
                    } catch (CodeExtractorException e) {
                        LOGGER.logException("Can't parse header " + file, e);
                    }
                    
                } else {
                    LOGGER.logWarning("Could not find header " + include);
                }
                
            }
        }
        
        // no recursion needed
    }
    
    /*
     * All other visit*() methods just update the parents stack
     */
    
    @Override
    public void visitBranchStatement(@NonNull BranchStatement branchStatement) {
        parents.push(branchStatement);
        ISyntaxElementVisitor.super.visitBranchStatement(branchStatement);
        parents.pop();
    }
    
    @Override
    public void visitCaseStatement(@NonNull CaseStatement caseStatement) {
        parents.push(caseStatement);
        ISyntaxElementVisitor.super.visitCaseStatement(caseStatement);
        parents.pop();
    }
    
    @Override
    public void visitCode(@NonNull Code code) {
        parents.push(code);
        ISyntaxElementVisitor.super.visitCode(code);
        parents.pop();
    }
    
    @Override
    public void visitCodeList(@NonNull CodeList code) {
        parents.push(code);
        ISyntaxElementVisitor.super.visitCodeList(code);
        parents.pop();
    }
    
    @Override
    public void visitComment(@NonNull Comment comment) {
        parents.push(comment);
        ISyntaxElementVisitor.super.visitComment(comment);
        parents.pop();
    }
    
    @Override
    public void visitCompoundStatement(@NonNull CompoundStatement block) {
        parents.push(block);
        ISyntaxElementVisitor.super.visitCompoundStatement(block);
        parents.pop();
    }
    
    @Override
    public void visitCppBlock(@NonNull CppBlock block) {
        parents.push(block);
        ISyntaxElementVisitor.super.visitCppBlock(block);
        parents.pop();
    }
    
    @Override
    public void visitFile(net.ssehub.kernel_haven.code_model.ast.@NonNull File file) {
        parents.push(file);
        ISyntaxElementVisitor.super.visitFile(file);
        parents.pop();
    }
    
    @Override
    public void visitFunction(@NonNull Function function) {
        parents.push(function);
        ISyntaxElementVisitor.super.visitFunction(function);
        parents.pop();
    }
    
    @Override
    public void visitLabel(@NonNull Label label) {
        parents.push(label);
        ISyntaxElementVisitor.super.visitLabel(label);
        parents.pop();
    }
    
    @Override
    public void visitLoopStatement(@NonNull LoopStatement loop) {
        parents.push(loop);
        ISyntaxElementVisitor.super.visitLoopStatement(loop);
        parents.pop();
    }
    
    @Override
    public void visitSingleStatement(@NonNull SingleStatement statement) {
        parents.push(statement);
        ISyntaxElementVisitor.super.visitSingleStatement(statement);
        parents.pop();
    }
    
    @Override
    public void visitSwitchStatement(@NonNull SwitchStatement switchStatement) {
        parents.push(switchStatement);
        ISyntaxElementVisitor.super.visitSwitchStatement(switchStatement);
        parents.pop();
    }
    
    @Override
    public void visitTypeDefinition(@NonNull TypeDefinition typeDef) {
        parents.push(typeDef);
        ISyntaxElementVisitor.super.visitTypeDefinition(typeDef);
        parents.pop();
    }
    
}
