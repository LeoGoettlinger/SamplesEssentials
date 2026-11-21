package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.serializer;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.nodes.Node;
import java.text.NumberFormat;

public class NumberAnchorGenerator implements AnchorGenerator {
   private int lastAnchorId = 0;

   public NumberAnchorGenerator(int lastAnchorId) {
      this.lastAnchorId = lastAnchorId;
   }

   @Override
   public String nextAnchor(Node node) {
      if (node.getAnchor() != null) {
         return node.getAnchor();
      } else {
         this.lastAnchorId++;
         NumberFormat format = NumberFormat.getNumberInstance();
         format.setMinimumIntegerDigits(3);
         format.setMaximumFractionDigits(0);
         format.setGroupingUsed(false);
         String anchorId = format.format((long)this.lastAnchorId);
         return "id" + anchorId;
      }
   }
}
