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
package net.ssehub.kernel_haven.srcml.transformation;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Marker to indicate the end of a preprocessor if-elif-else structure, which is only needed as long the surrounded
 * elements are not nested inside the {@link PreprocessorBlock}s (flat structure vs hierarchy).
 * @author El-Sharkawy
 *
 */
public class PreprocessorEndIf implements ITranslationUnit {
    
    private int startLine;
    private int endLine;

    @Override
    public @NonNull String getType() {
        return "ENDIF";
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
        return "#" + getType();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @NonNull ITranslationUnit getNestedElement(int index) {
        throw new IndexOutOfBoundsException(PreprocessorEndIf.class.getSimpleName() + " has no nested elements, "
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
