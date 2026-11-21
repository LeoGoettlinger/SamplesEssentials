package com.viaversion.viaversion.libs.snakeyaml.inspector;

import com.viaversion.viaversion.libs.snakeyaml.nodes.Tag;

public final class UnTrustedTagInspector implements TagInspector {
   @Override
   public boolean isGlobalTagAllowed(Tag tag) {
      return false;
   }
}
