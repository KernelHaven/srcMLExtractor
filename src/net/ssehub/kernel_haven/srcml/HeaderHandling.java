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
package net.ssehub.kernel_haven.srcml;

/**
 * How #include statements should be handled.
 * 
 * @author Adam
 */
public enum HeaderHandling {

    /**
     * Ignore the #include; just leave the preprocessor statement there.
     */
    IGNORE,
    
    /**
     * Includes all headers like {@link #INCLUDE}. Tries to find declarations of functions in the headers and uses
     * their presence condition to expand the presence condition of the function implementation.
     */
    EXPAND_FUNCTION_CONDITION,
    
    /**
     * Parse the headers and include their content instead of the #include directive.
     */
    INCLUDE,
    
}
