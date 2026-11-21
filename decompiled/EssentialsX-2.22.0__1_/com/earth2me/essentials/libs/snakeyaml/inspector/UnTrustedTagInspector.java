package com.earth2me.essentials.libs.snakeyaml.inspector;

import com.earth2me.essentials.libs.snakeyaml.nodes.Tag;

public final class UnTrustedTagInspector implements TagInspector {
   @Override
   public boolean isGlobalTagAllowed(Tag tag) {
      return false;
   }
}
