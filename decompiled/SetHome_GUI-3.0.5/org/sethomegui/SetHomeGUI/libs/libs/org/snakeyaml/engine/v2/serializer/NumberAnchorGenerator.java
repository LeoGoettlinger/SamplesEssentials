package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.serializer;

import java.text.NumberFormat;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.Anchor;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public class NumberAnchorGenerator implements AnchorGenerator {
   private int lastAnchorId = 0;

   public NumberAnchorGenerator(int lastAnchorId) {
      this.lastAnchorId = lastAnchorId;
   }

   @Override
   public Anchor nextAnchor(Node node) {
      this.lastAnchorId++;
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMinimumIntegerDigits(3);
      format.setMaximumFractionDigits(0);
      format.setGroupingUsed(false);
      String anchorId = format.format((long)this.lastAnchorId);
      return new Anchor("id" + anchorId);
   }
}
