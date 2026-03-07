package net.dehasher.hlib.file.configuration.file;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import net.dehasher.hlib.file.configuration.ConfigurationSection;
import net.dehasher.hlib.file.configuration.serialization.ConfigurationSerializable;
import net.dehasher.hlib.file.configuration.serialization.ConfigurationSerialization;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlRepresenter extends Representer {

    public YamlRepresenter() {
        super(new DumperOptions() {{
            setIndent(4);
            setIndicatorIndent(2);
            setMaxSimpleKeyLength(256);
            setLineBreak(LineBreak.UNIX);
            setAllowUnicode(true);
        }});
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }

    private class RepresentConfigurationSection extends RepresentMap {
        @NotNull
        @Override
        public Node representData(@NotNull Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }

    private class RepresentConfigurationSerializable extends RepresentMap {
        @NotNull
        @Override
        public Node representData(@NotNull Object data) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());
            return super.representData(values);
        }
    }
}