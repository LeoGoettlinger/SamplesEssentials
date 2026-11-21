package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.schema;

import java.util.HashMap;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api.ConstructNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.resolver.FailsafeScalarResolver;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.resolver.ScalarResolver;

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
