package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public final class ComponentRewriter1_14<C extends ClientboundPacketType> extends JsonNBTComponentRewriter<C> {
   public ComponentRewriter1_14(Protocol<C, ?, ?, ?> protocol) {
      super(protocol, ComponentRewriterBase.ReadType.JSON);
   }

   @Override
   protected void handleHoverEvent(UserConnection connection, JsonObject hoverEvent) {
      super.handleHoverEvent(connection, hoverEvent);
      String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
      if (action.equals("show_item")) {
         JsonElement value = hoverEvent.get("value");
         if (value != null) {
            CompoundTag tag;
            try {
               tag = ComponentUtil.deserializeLegacyShowItem(value, SerializerVersion.V1_13);
            } catch (Exception var11) {
               if (Via.getConfig().logTextComponentConversionErrors()) {
                  Protocol1_13_2To1_14.LOGGER.log(Level.WARNING, "Error reading NBT in show_item: " + StringUtil.forLogging(value), var11);
               }

               return;
            }

            CompoundTag itemTag = tag.getCompoundTag("tag");
            Item item = new DataItem();
            item.setTag(itemTag);
            this.protocol.getItemRewriter().handleItemToClient(null, item);
            if (itemTag != null) {
               tag.put("tag", itemTag);
            }

            JsonArray newValue = new JsonArray();
            JsonObject showItem = new JsonObject();
            newValue.add(showItem);

            try {
               showItem.addProperty("text", SerializerVersion.V1_14.toSNBT(tag));
               hoverEvent.add("value", newValue);
            } catch (Exception var12) {
               if (Via.getConfig().logTextComponentConversionErrors()) {
                  Protocol1_13_2To1_14.LOGGER.log(Level.WARNING, "Error writing NBT in show_item: " + StringUtil.forLogging(value), var12);
               }
            }
         }
      }
   }
}
