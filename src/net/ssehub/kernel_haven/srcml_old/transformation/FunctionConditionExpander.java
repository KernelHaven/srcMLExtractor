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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElementVisitor;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement.Type;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Expands the function definition with conditions surrounding previous declaration for this function.
 * 
 * @author Adam
 */
public class FunctionConditionExpander implements ISyntaxElementVisitor {
    
    private static final Logger LOGGER = Logger.get();

    /**
     * Maps function name -> presence condtion(s) of declaration(s).
     */
    private Map<String, List<@NonNull Formula>> declPcs;
    
    /**
     * Creates a new {@link FunctionConditionExpander}.
     */
    public FunctionConditionExpander() {
        declPcs = new HashMap<>();
    }
    
    /**
     * Does the condition expansion for the given AST.
     * 
     * @param unit The AST to expand conditions in. Typically, this a the complete {@link File}.
     */
    public void expand(@NonNull ISyntaxElement unit) {
        unit.accept(this);
    }

    /*
     * The following functions are used for filling declPcs
     */
    
    /**
     * Returns the name of the function that is declared inside the given {@link SingleStatement}.
     * 
     * @param functionDecl The statement to search the function name in. Its type should be
     *      {@link SingleStatement.Type#FUNCTION_DECLARATION}.
     *      
     * @return The name of the function, or <code>null</code> if it could not be found.
     */
    private @Nullable String getFunctionName(@NonNull SingleStatement functionDecl) {
        String result = null;
        
        if (functionDecl.getCode() instanceof Code) {
            String declStr = ((Code) functionDecl.getCode()).getText();
            List<String> tokens = new ArrayList<>(Arrays.asList(declStr.split(" ")));
            // copy into array list, because we want to modify the list length
            
            // there may be "()" tokens; split them up
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).equals("()")) {
                    tokens.set(i, "(");
                    tokens.add(i + 1, ")");
                }
            }
            
            /*
             * a function declaration looks like this:
             *      <something> NAME ( <something> ) <something (usually just the semicolon)>
             * 
             * every <something>, except the last one, may contain brackets
             * 
             * strategy:
             *  - find rightmost closing bracket
             *  - find the corresponding opening bracket, by counting how many brackets are opened and closed in the
             *    <something> in the brackets
             *  - the name is whatever is to the left of the opening bracket
             */
            
            // find rightmost closing bracket
            int closingBracket;
            for (closingBracket = tokens.size() - 1; closingBracket >= 0; closingBracket--) {
                if (tokens.get(closingBracket).equals(")")) {
                    break;
                }
            }
            
            // find corresponding opening bracket
            int depth = 0;
            int openingBracket;
            for (openingBracket = closingBracket - 1; openingBracket >= 0; openingBracket--) {
                String token = tokens.get(openingBracket);
                
                if (token.equals(")")) {
                    depth++;
                    
                } else if (token.equals("(")) {
                    
                    if (depth == 0) {
                        // we found the corresponding opening bracket
                        break;
                        
                    } else {
                        // we just found the closing bracket for some opening bracket in between
                        depth--;
                    }
                }
            }

            // the name is whatever is to the left of the opening bracket
            if (openingBracket > 0) {
                result = tokens.get(openingBracket - 1);
            }
            
        }
        return result;
    }
    
    /**
     * Adds a presence condition of a declaration of a function to its list. This is called for all function
     * declarations that are found.
     * 
     * @param name The name of the function that was declared.
     * @param pc The presence condition of the declaration.
     */
    private void putDeclPc(@NonNull String name, @NonNull Formula pc) {
        // ignore True constants
        if (pc == True.INSTANCE) {
            return;
        }
        
        List<@NonNull Formula> pcs = declPcs.get(name);
        if (pcs == null) {
            pcs = new LinkedList<>();
            declPcs.put(name, pcs);
        }
        pcs.add(pc);
    }
    
    @Override
    public void visitSingleStatement(@NonNull SingleStatement statement) {
        if (statement.getType() == Type.FUNCTION_DECLARATION) {
            String name = getFunctionName(statement);
            if (name != null) {
                LOGGER.logDebug("Found PC for declaration of " + name + ": " + statement.getPresenceCondition());
                putDeclPc(name, statement.getPresenceCondition());
            }
        }
    }
    
    /*
     * The following functions are used for applying the new conditions for the function
     */
    
    /**
     * Recursively appends a {@link Formula} to all presence conditions of sub-tree.
     * 
     * @param element The element to add the new part to (including all nested elements).
     * @param newPart The new part to add to all presence conditions (using a {@link Conjunction}).
     */
    private void updateAllPcs(@NonNull ISyntaxElement element, @NonNull Formula newPart) {
        Formula previousPc = element.getPresenceCondition();
        if (previousPc != True.INSTANCE) {
            element.setPresenceCondition(new Conjunction(previousPc, newPart));
        } else {
            element.setPresenceCondition(newPart);
        }
        
        for (ISyntaxElement child : element) {
            updateAllPcs(child, newPart);
        }
    }
    
    @Override
    public void visitFunction(@NonNull Function function) {
        List<@NonNull Formula> declPcs = this.declPcs.get(function.getName());
        
        if (declPcs != null) {
            // there is always at least one element in declPcs list
            Iterator<@NonNull Formula> declPcIt = declPcs.iterator();
            Formula newPart = notNull(declPcIt.next());
            while (declPcIt.hasNext()) {
                newPart = new Disjunction(newPart, declPcIt.next());
            }
            
            LOGGER.logDebug("Expanding condition of function " + function.getName() + " with " + newPart);
            
            // update the immediate condition of the function...
            Formula previousCondition = function.getCondition();
            if (previousCondition != null) {
                function.setCondition(new Conjunction(previousCondition, newPart));
            } else {
                function.setCondition(newPart);
            }
            
            // ... and the PCs of the function and all nested elements
            updateAllPcs(function, newPart);
        }
        
        // no recursion needed, since no functions can be nested inside functions
    }
    
}
