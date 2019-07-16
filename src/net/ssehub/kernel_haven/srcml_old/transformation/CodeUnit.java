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

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represent unparsed code fragments, e.g., a function signature or a (partial) expression of a statement.
 * These are the texts between the XML tags in <a href="https://www.srcml.org/doc/c_srcML.html">srcML</a> and will be
 * gathered through by SAX's {@link org.xml.sax.ContentHandler#characters(char[], int, int)}.  
 * @author El-Sharkawy
 *
 */
public class CodeUnit implements ITranslationUnit {
    
    private @NonNull String code;
    private int startLine;
    private int endLine;
    
    /**
     * Sole constructor for this class.
     * @param code A single char sequence/token, which is not intended to be parsed in a later step.
     */
    public CodeUnit(@NonNull String code) {
        this.code = code;
    }
    
    /**
     * Returns the code that is stored in this {@link CodeUnit}.
     * 
     * @return The literal code text.
     */
    public @NonNull String getCode() {
        return code;
    }

    @Override
    public @NonNull String getType() {
        return notNull(this.getClass().getSimpleName());
    }

    @Override
    public void replaceNested(@NonNull ITranslationUnit oldUnit, @NonNull ITranslationUnit newUnit) {
        //
    }

    @Override
    public void removeNested(@NonNull ITranslationUnit oldUnit) {
        //
    }
    
    @Override
    public void removeNested(int index) {
        //
    }
    
    @Override
    public @NonNull String toString() {
        return code;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @NonNull ITranslationUnit getNestedElement(int index) {
        throw new IndexOutOfBoundsException(CodeUnit.class.getSimpleName() + " has no nested elements, "
            + "requested was: " + index);
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
        
    }
}
