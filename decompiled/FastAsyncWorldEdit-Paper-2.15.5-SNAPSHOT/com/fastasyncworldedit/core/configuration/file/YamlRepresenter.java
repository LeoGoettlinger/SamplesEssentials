package com.fastasyncworldedit.core.configuration.file;

import com.fastasyncworldedit.core.configuration.ConfigurationSection;
import com.fastasyncworldedit.core.configuration.serialization.ConfigurationSerializable;
import com.fastasyncworldedit.core.configuration.serialization.ConfigurationSerialization;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.representer.SafeRepresenter.RepresentMap;

public class YamlRepresenter extends Representer {
   public YamlRepresenter() {
      super(new DumperOptions());
      this.multiRepresenters.put(ConfigurationSection.class, new YamlRepresenter.RepresentConfigurationSection());
      this.multiRepresenters.put(ConfigurationSerializable.class, new YamlRepresenter.RepresentConfigurationSerializable());
   }

   private class RepresentConfigurationSection extends RepresentMap {
      private RepresentConfigurationSection() {
         super(YamlRepresenter.this);
      }

      public Node representData(Object data) {
         return super.representData(((ConfigurationSection)data).getValues(false));
      }
   }

   private class RepresentConfigurationSerializable extends RepresentMap {
      private RepresentConfigurationSerializable() {
         super(YamlRepresenter.this);
      }

      public Node representData(Object data) {
         ConfigurationSerializable serializable = (ConfigurationSerializable)data;
         Map<String, Object> values = new LinkedHashMap<>();
         values.put("==", ConfigurationSerialization.getAlias((Class<? extends ConfigurationSerializable>)serializable.getClass()));
         values.putAll(serializable.serialize());
         return super.representData(values);
      }
   }
}
