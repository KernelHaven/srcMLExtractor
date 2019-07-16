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

import net.ssehub.kernel_haven.srcml.SrcMlConditionGrammar;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Representation of an <tt>&#35;else</tt> or <tt>&#35;elif</tt> block.
 * @author El-Sharkawy
 *
 */
public class PreprocessorElse extends PreprocessorBlock {

    private @NonNull PreprocessorIf startElement;
    
    /**
     * Sole constructor for this class.
     * @param type Denotes which kind of <i>else</i>
     *     this object represents (one of {@link Type#ELSEIF}, or {@link Type#ELSE})
     * @param condition The condition of the if, should be in form that the {@link SrcMlConditionGrammar} can handle it,
     *     or <tt>null</tt> in case of an <tt>&#35;else</tt>-block.
     * @param startElement The starting block, must not be <tt>null</tt>.
     */
    public PreprocessorElse(@NonNull Type type, @Nullable String condition, @NonNull PreprocessorIf startElement) {
        super(type, condition);
        this.startElement = startElement;
    }

    /**
     * Returns the first block of the if-elif-else structure, the <tt>&#35;if</tt> block.
     * @return The <tt>&#35;if</tt> block to which this else block belongs to.
     */
    @Override
    public @NonNull PreprocessorIf getStartingIf() {
        return startElement;
    }
}
