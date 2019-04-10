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

import net.ssehub.kernel_haven.util.FormatException;

/**
 * Utility class for creating exceptions.
 * 
 * @author Adam
 */
public class ExceptionUtil {

    /**
     * No intstances.
     */
    private ExceptionUtil() {
    }

    /**
     * Creates an exception with the given message.
     * 
     * @param message The message describing the exception.
     * @param unit The unit where the exception occurred.
     * 
     * @return An exception with the given message.
     */
    public static FormatException makeException(String message, ITranslationUnit unit) {
        return new FormatException("Line " + unit.getStartLine() + ": " + message);
    }
    
    /**
     * Creates an exception with the given message and cause.
     * 
     * @param message The message describing the exception.
     * @param cause The exception that caused this exception.
     * @param unit The unit where the exception occurred.
     * 
     * @return An exception with the given message.
     */
    public static FormatException makeException(String message, Throwable cause, ITranslationUnit unit) {
        return (FormatException) new FormatException("Line " + unit.getStartLine() + ": " + message).initCause(cause);
    }
    
}
