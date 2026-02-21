package uk.laykon.coral.autoreg;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoEvent {
    EventPriority priority() default EventPriority.NORMAL;
    boolean ignoreCancelled() default false;
}
