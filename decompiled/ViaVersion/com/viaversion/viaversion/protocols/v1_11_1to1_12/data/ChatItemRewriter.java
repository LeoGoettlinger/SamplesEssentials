package com.viaversion.viaversion.protocols.v1_11_1to1_12.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public final class ChatItemRewriter {
   public static void toClient(JsonElement element) {
      if (element instanceof JsonObject obj) {
         if (obj.has("hoverEvent")) {
            if (!(obj.get("hoverEvent") instanceof JsonObject hoverEvent)) {
               return;
            }

            if (!hoverEvent.has("action") || !hoverEvent.has("value")) {
               return;
            }

            String type = hoverEvent.get("action").getAsString();
            JsonElement value = hoverEvent.get("value");
            if (type.equals("show_item")) {
               CompoundTag compound;
               try {
                  compound = ComponentUtil.deserializeLegacyShowItem(value, SerializerVersion.V1_8);
               } catch (Exception var8) {
                  if (Via.getConfig().logTextComponentConversionErrors()) {
                     Via.getPlatform().getLogger().log(Level.WARNING, "Error reading NBT in 1.8 show_item: " + StringUtil.forLogging(value), (Throwable)var8);
                  }

                  return;
               }

               hoverEvent.addProperty("value", SerializerVersion.V1_12.toSNBT(compound));
            }
         } else if (obj.has("extra")) {
            toClient(obj.get("extra"));
         } else if (obj.has("translate") && obj.has("with")) {
            toClient(obj.get("with"));
         }
      } else if (element instanceof JsonArray) {
         for (JsonElement value : (JsonArray)element) {
            toClient(value);
         }
      }
   }
}
