package uk.laykon.coral.autoreg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoCommand {
    String[] value();
    String description() default "";
    String usage() default "";
    String permission() default "";
    String permissionMessage() default "";
    String[] aliases() default {};
}
