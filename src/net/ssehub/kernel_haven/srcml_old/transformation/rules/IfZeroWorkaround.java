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
package net.ssehub.kernel_haven.srcml_old.transformation.rules;

import net.ssehub.kernel_haven.srcml_old.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml_old.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Workaround to avoid parsing exceptions: Will remove all elements surrounded by if 0.
 * @author El-Sharkawy
 *
 */
public class IfZeroWorkaround implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        if (unit instanceof PreprocessorBlock) {
            String condition = ((PreprocessorBlock) unit).getCondition();
        
            if (condition != null && condition.equals("0")) {
                for (int i = unit.size() - 1; i >= 0; i--) {
                    unit.removeNested(i);
                }
            }
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

}
