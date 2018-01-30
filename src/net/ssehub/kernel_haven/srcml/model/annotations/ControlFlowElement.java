package net.ssehub.kernel_haven.srcml.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an element as a complex structure able to take other statements as nested elements.
 * This is intended for control structures and loops, which raise the complexity when containing nested elements.
 * @author El-Sharkawy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ControlFlowElement {

}
