package org.sethomegui.SetHomeGUI.libs.block.implementation;

import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI.libs.block.Block;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public class TerminatedBlock extends Block<Object> {
   public TerminatedBlock(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable Object value) {
      super(keyNode, valueNode, value);
   }

   public TerminatedBlock(@Nullable Block<?> previous, @Nullable Object value) {
      super(previous, value);
   }

   @Override
   public boolean isSection() {
      return false;
   }
}
