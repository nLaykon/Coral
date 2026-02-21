package uk.laykon.coral.autoreg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoRunnable {
    long delayTicks() default 0L;
    long periodTicks() default 20L;
    boolean async() default false;
}
