package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.schema;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.ConstructNode;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreBool;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreFloat;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreInt;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.CoreScalarResolver;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.ScalarResolver;
import java.util.HashMap;
import java.util.Map;

public class CoreSchema extends JsonSchema {
   private final Map<Tag, ConstructNode> tagConstructors = new HashMap<>();

   public CoreSchema() {
      this.tagConstructors.put(Tag.BOOL, new ConstructYamlCoreBool());
      this.tagConstructors.put(Tag.INT, new ConstructYamlCoreInt());
      this.tagConstructors.put(Tag.FLOAT, new ConstructYamlCoreFloat());
   }

   @Override
   public ScalarResolver getScalarResolver() {
      return new CoreScalarResolver();
   }

   @Override
   public Map<Tag, ConstructNode> getSchemaTagConstructors() {
      Map<Tag, ConstructNode> json = super.getSchemaTagConstructors();
      json.putAll(this.tagConstructors);
      return json;
   }
}
