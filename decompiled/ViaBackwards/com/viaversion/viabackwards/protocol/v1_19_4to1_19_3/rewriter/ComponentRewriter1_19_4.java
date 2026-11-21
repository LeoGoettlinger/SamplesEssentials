package com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.rewriter;

import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase.ReadType;

public final class ComponentRewriter1_19_4 extends JsonNBTComponentRewriter<ClientboundPackets1_19_4> {
   public ComponentRewriter1_19_4(Protocol1_19_4To1_19_3 protocol) {
      super(protocol, ReadType.JSON);
   }

   @Override
   protected void handleTranslate(JsonObject root, String translate) {
      super.handleTranslate(root, translate);
      if ((translate.startsWith("vb.item.") || translate.startsWith("vb.entity.")) && root.has("fallback")) {
         JsonElement fallback = root.remove("fallback");
         root.add("translate", fallback);
      }
   }
}
