package com.fastasyncworldedit.core.configuration.file;

import com.fastasyncworldedit.core.configuration.serialization.ConfigurationSerialization;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.constructor.SafeConstructor.ConstructYamlMap;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class YamlConstructor extends SafeConstructor {
   public YamlConstructor() {
      super(new LoaderOptions());
      this.yamlConstructors.put(Tag.MAP, new YamlConstructor.ConstructCustomObject());
   }

   private class ConstructCustomObject extends ConstructYamlMap {
      private ConstructCustomObject() {
         super(YamlConstructor.this);
      }

      public Object construct(final Node node) {
         if (node.isTwoStepsConstruction()) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
         } else {
            Map<?, ?> raw = (Map<?, ?>)super.construct(node);
            if (!raw.containsKey("==")) {
               return raw;
            } else {
               Map<String, Object> typed = new LinkedHashMap<>(raw.size());

               for (Entry<?, ?> entry : raw.entrySet()) {
                  typed.put(entry.getKey().toString(), entry.getValue());
               }

               try {
                  return ConfigurationSerialization.deserializeObject(typed);
               } catch (IllegalArgumentException var6) {
                  throw new YAMLException("Could not deserialize object", var6);
               }
            }
         }
      }

      public void construct2ndStep(final Node node, final Object object) {
         throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
      }
   }
}
