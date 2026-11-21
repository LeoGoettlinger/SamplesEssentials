package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.ConstructorException;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.NodeType;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.ScalarResolver;

public class ConstructOptionalClass extends ConstructScalar {
   private final ScalarResolver scalarResolver;

   public ConstructOptionalClass(ScalarResolver scalarResolver) {
      this.scalarResolver = scalarResolver;
   }

   @Override
   public Object construct(Node node) {
      if (node.getNodeType() != NodeType.SCALAR) {
         throw new ConstructorException("while constructing Optional", Optional.empty(), "found non scalar node", node.getStartMark());
      } else {
         String value = this.constructScalar(node);
         Tag implicitTag = this.scalarResolver.resolve(value, true);
         return implicitTag.equals(Tag.NULL) ? Optional.empty() : Optional.of(value);
      }
   }
}
