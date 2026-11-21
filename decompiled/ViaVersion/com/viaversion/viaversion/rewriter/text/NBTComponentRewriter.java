package com.viaversion.viaversion.rewriter.text;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NBTComponentRewriter<C extends ClientboundPacketType> extends ComponentRewriterBase<C> {
   public NBTComponentRewriter(Protocol<C, ?, ?, ?> protocol) {
      super(protocol, ComponentRewriterBase.ReadType.NBT);
   }

   @Override
   protected void processCompoundTag(UserConnection connection, CompoundTag tag) {
      super.processCompoundTag(connection, tag);
      CompoundTag clickEvent = tag.getCompoundTag("click_event");
      if (clickEvent != null) {
         this.handleClickEvent(connection, clickEvent);
      }
   }

   @Override
   protected void handleHoverEvent(UserConnection connection, JsonObject hoverEvent) {
      throw new UnsupportedOperationException("Not implemented");
   }

   @Override
   protected void handleHoverEvent(UserConnection connection, CompoundTag hoverEventTag) {
      StringTag actionTag = hoverEventTag.getStringTag("action");
      if (actionTag != null) {
         String action = actionTag.getValue();
         if (action.equals("show_text")) {
            this.processTag(connection, hoverEventTag.get("value"));
         } else if (action.equals("show_entity")) {
            this.processTag(connection, hoverEventTag.get("name"));
            StringTag idTag = hoverEventTag.getStringTag("id");
            if (idTag != null && this.protocol.getEntityRewriter() != null) {
               idTag.setValue(this.protocol.getEntityRewriter().mappedEntityIdentifier(idTag.getValue()));
            }
         } else if (action.equals("show_item")) {
            CompoundTag componentsTag = hoverEventTag.getCompoundTag("components");
            this.handleShowItem(connection, hoverEventTag, componentsTag);
         }
      }
   }

   protected void handleClickEvent(UserConnection connection, CompoundTag clickEventTag) {
      StringTag actionTag = clickEventTag.getStringTag("action");
      if (actionTag != null) {
         String action = actionTag.getValue();
         if (action.equals("show_dialog")) {
            CompoundTag dialogTag = clickEventTag.getCompoundTag("dialog");
            if (dialogTag != null) {
               RegistryDataRewriter registryRewriter = this.protocol.getRegistryDataRewriter();
               if (registryRewriter != null) {
                  registryRewriter.updateDialog(connection, dialogTag);
               }
            }
         }
      }
   }

   @Override
   protected void handleNestedComponent(UserConnection connection, @Nullable Tag tag) {
      this.processTag(connection, tag);
   }

   @Override
   protected String hoverEventKey() {
      return "hover_event";
   }

   public void registerPlayerChat1_21_5(C packetType) {
      this.registerPlayerChat(packetType, Types.LONG_ARRAY_PRIMITIVE);
   }

   public void registerPlayerChat26_3(C packetType) {
      this.registerPlayerChat(packetType, Types.BIT_SET);
   }

   private void registerPlayerChat(C packetType, Type<?> maskType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         int lastSeen = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < lastSeen; i++) {
            int index = wrapper.passthrough(Types.VAR_INT);
            if (index == 0) {
               wrapper.passthrough(Types.SIGNATURE_BYTES);
            }
         }

         this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
         int filterMaskType = wrapper.passthrough(Types.VAR_INT);
         if (filterMaskType == 2) {
            wrapper.passthrough(maskType);
         }

         wrapper.passthrough(ChatType.TYPE);
         this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG));
         this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      });
   }
}
