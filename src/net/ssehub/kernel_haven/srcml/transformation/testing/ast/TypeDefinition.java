package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represents a declaration (and initialization) of a new data structure, outside of a function. More precisely one of:
 * <ul>
 *     <li><a href="http://www.srcml.org/doc/c_srcML.html#struct-definition">Struct definition/declaration</a></li>
 * </ul>
 * @author El-Sharkawy
 *
 */
public class TypeDefinition extends SyntaxElementWithChildreen {

    public static enum TypeDefType {
        STRUCT, ENUM;
    }
    
    private SyntaxElement declaration;
    private TypeDefType type;
    
    public TypeDefinition(@NonNull Formula presenceCondition, File sourceFile, SyntaxElement declaration,
        TypeDefType type) {
        
        super(presenceCondition, sourceFile);
        this.declaration = declaration;
        this.type = type;
    }

    @Override
    protected String elementToString() {
        return type.name() + "-Definition\n"
            + (declaration == null ? "\t\t\t\tnull" : declaration.toString("\t\t\t\t")); // TODO
    }

}
