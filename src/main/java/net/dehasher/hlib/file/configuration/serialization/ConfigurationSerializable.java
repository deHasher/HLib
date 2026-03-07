package net.dehasher.hlib.file.configuration.serialization;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ConfigurationSerializable {
    @NotNull Map<String, Object> serialize();
}