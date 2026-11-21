package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.inspector;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.nodes.Tag;

public final class UnTrustedTagInspector implements TagInspector {
   @Override
   public boolean isGlobalTagAllowed(Tag tag) {
      return false;
   }
}
