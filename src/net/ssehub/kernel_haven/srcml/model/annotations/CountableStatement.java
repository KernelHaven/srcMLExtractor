package net.ssehub.kernel_haven.srcml.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an element as a statement, which shall be counted as single statement by LoC-metrics.
 * @author El-Sharkawy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CountableStatement {

}
