package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;

public class FailsafeScalarResolver extends BaseScalarResolver {
   @Override
   protected void addImplicitResolvers() {
      this.addImplicitResolver(Tag.NULL, EMPTY, null);
   }
}
