package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.code_model.ast.Code;
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

public class FunctionConditionExpander implements ISyntaxElementVisitor {
    
    private static final Logger LOGGER = Logger.get();

    /**
     * Maps function name -> presence condtion(s) of declaration(s).
     */
    private Map<String, List<@NonNull Formula>> declPcs;
    
    public FunctionConditionExpander() {
        declPcs = new HashMap<>();
    }
    
    public void expand(@NonNull ISyntaxElement unit) {
        unit.accept(this);
    }

    /*
     * The following functions are used for filling declPcs
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
    
    private void updateAllPcs(@NonNull ISyntaxElement element, @NonNull Formula newPart) {
        Formula previousPc = element.getPresenceCondition();
        if (previousPc != True.INSTANCE) {
            element.setPresenceCondition(new Conjunction(previousPc, newPart));
        } else {
            element.setPresenceCondition(newPart);
        }
        
        for (ISyntaxElement child : element.iterateNestedSyntaxElements()) {
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
