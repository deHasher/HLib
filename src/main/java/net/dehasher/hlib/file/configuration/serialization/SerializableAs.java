package net.dehasher.hlib.file.configuration.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializableAs {
    @NotNull String value();
}