package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Basic {@link ITranslationUnit}, which does not require any special treatment.<br/>
 * <b>Note:</b> All elements are initially parsed either as {@link TranslationUnit} or as {@link CodeUnit} and may be
 * refined in an alter step (or kept if no further treatment is required).
 * @author El-Sharkawy
 *
 */
public class TranslationUnit implements ITranslationUnit {
    
    private @NonNull String type;
    private @NonNull List<@NonNull ITranslationUnit> nestedElements = new ArrayList<>();
    private int startLine;
    private int endLine;
    
    /**
     * Special handling for type="function": the name of the function is stored in here.
     */
    private @Nullable String functionName;
    
    /**
     * Sole constructor for this type.
     * @param type Denotes what kind of element is represented by this {@link TranslationUnit}, should be one of the
     *     top elements from the <a href="https://www.srcml.org/doc/c_srcML.html">srcML XML snytax</a>.
     */
    public TranslationUnit(@NonNull String type) {
        this.type = type;
    }
    
    /**
     * Adds a nested element.
     * @param unit The nested element to add.
     */
    @Override
    public void add(@NonNull ITranslationUnit unit) {
        nestedElements.add(unit);
    }
    
    @Override
    public @NonNull String getType() {
        return type;
    }
    
    /**
     * Special handling for type="function": return the name of the function.
     * 
     * @return The name of the function; <code>null</code> if not set (e.g. if this is not a function).
     */
    public @Nullable String getFunctionName() {
        return functionName;
    }
    
    /**
     * Special handling for type="function": set the name of the function.
     * 
     * @param functionName The name of the function.
     */
    public void setFunctionName(@NonNull String functionName) {
        this.functionName = functionName;
    }
    
    @Override
    public @NonNull String toString() {
        StringBuilder result = new StringBuilder();
        result.append(type.toUpperCase());
        result.append(":");
        boolean lastWasUnit = false;
        for (Object elem : nestedElements) {
            if (elem instanceof CodeUnit) {
                if (lastWasUnit) {
                    result.append("\n");
                } else {
                    result.append(" ");
                }
                result.append(elem.toString().replace("\n", "\n    "));
            } else {
                lastWasUnit = true;
                result.append("\n    ");
                result.append(elem.toString().replace("\n", "\n    "));
            }
        }
        return notNull(result.toString());
    }

    @Override
    public void replaceNested(@NonNull ITranslationUnit oldUnit, @NonNull ITranslationUnit newUnit) {
        int index = nestedElements.indexOf(oldUnit);
        if (index != -1) {
            nestedElements.set(index, newUnit);
        }
    }
    
    @Override
    public void removeNested(@NonNull ITranslationUnit oldUnit) {
        nestedElements.remove(oldUnit);
    }
    
    @Override
    public void removeNested(int index) {
        nestedElements.remove(index);
    }

    @Override
    public int size() {
        return nestedElements.size();
    }

    @Override
    public @NonNull ITranslationUnit getNestedElement(int index) {
        return notNull(nestedElements.get(index));
    }

    @Override
    public void setStartLine(int rowIndex) {
        this.startLine = rowIndex;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }
    
    @Override
    public void setEndLine(int rowIndex) {
        this.endLine = rowIndex;
    }
    
    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public void insertNested(int index, @NonNull ITranslationUnit newUnit) {
        nestedElements.add(index, newUnit);
        
    }
}
