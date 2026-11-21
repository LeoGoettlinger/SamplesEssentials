package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.schema;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.ConstructNode;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.FailsafeScalarResolver;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.ScalarResolver;
import java.util.HashMap;
import java.util.Map;

public class FailsafeSchema implements Schema {
   @Override
   public ScalarResolver getScalarResolver() {
      return new FailsafeScalarResolver();
   }

   @Override
   public Map<Tag, ConstructNode> getSchemaTagConstructors() {
      return new HashMap<>();
   }
}
