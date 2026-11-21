package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.Protocol1_21_6To1_21_5;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21.Display;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21.OverrideText;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;

public final class BlockItemPacketRewriter1_21_6
   extends BackwardsStructuredItemRewriter<ClientboundPacket1_21_6, ServerboundPacket1_21_5, Protocol1_21_6To1_21_5> {
   public BlockItemPacketRewriter1_21_6(Protocol1_21_6To1_21_5 protocol) {
      super(protocol);
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter.BlockItemPacketRewriter1_21_6.downgradeItemData(item);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter.BlockItemPacketRewriter1_21_6.upgradeItemData(item);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
      AttributeModifiers1_21 attributeModifiers = (AttributeModifiers1_21)dataContainer.get(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6);
      if (attributeModifiers != null) {
         ListTag<CompoundTag> modifiersBackup = new ListTag(CompoundTag.class);
         boolean needsBackup = false;

         for (AttributeModifier modifier : attributeModifiers.modifiers()) {
            if (modifier.display().id() != 0) {
               needsBackup = true;
            }

            CompoundTag modifierBackup = new CompoundTag();
            modifiersBackup.add(modifierBackup);
            modifierBackup.putInt("id", modifier.display().id());
            if (modifier.display() instanceof OverrideText overrideText) {
               modifierBackup.put("text", overrideText.component());
            }
         }

         if (needsBackup) {
            backupTag.put("attribute_modifiers_displays", modifiersBackup);
         }
      }

      Equippable equippable = (Equippable)dataContainer.get(StructuredDataKey.EQUIPPABLE1_21_6);
      if (equippable != null && equippable.canBeSheared()) {
         CompoundTag equippableTag = new CompoundTag();
         equippableTag.putBoolean("can_be_sheared", true);
         this.saveSoundEventHolder(equippableTag, equippable.shearingSound());
         backupTag.put("equippable", equippableTag);
      }
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
      if (customData.remove(this.nbtTagName("backup")) instanceof CompoundTag backupTag) {
         ListTag<CompoundTag> attributeModifiersDisplays = backupTag.getListTag("attribute_modifiers_displays", CompoundTag.class);
         if (attributeModifiersDisplays != null) {
            container.replace(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6, modifiers -> {
               AttributeModifier[] updatedModifiers = new AttributeModifier[modifiers.modifiers().length];

               for (int i = 0; i < modifiers.modifiers().length; i++) {
                  CompoundTag modifierBackup = (CompoundTag)attributeModifiersDisplays.get(i);
                  int id = modifierBackup.getInt("id");
                  Display display = (Display)(id == 2 ? new OverrideText(modifierBackup.get("text")) : new Display(id));
                  AttributeModifier modifier = modifiers.modifiers()[i];
                  updatedModifiers[i] = new AttributeModifier(modifier.attribute(), modifier.modifier(), modifier.slotType(), display);
               }

               return new AttributeModifiers1_21(updatedModifiers);
            });
         }

         CompoundTag equippableTag = backupTag.getCompoundTag("equippable");
         if (equippableTag != null) {
            container.replace(
               StructuredDataKey.EQUIPPABLE1_21_5,
               StructuredDataKey.EQUIPPABLE1_21_6,
               equippable -> new Equippable(
                  equippable.equipmentSlot(),
                  equippable.soundEvent(),
                  equippable.model(),
                  equippable.cameraOverlay(),
                  equippable.allowedEntities(),
                  equippable.dispensable(),
                  equippable.swappable(),
                  equippable.damageOnHurt(),
                  equippable.equipOnInteract(),
                  equippableTag.getBoolean("can_be_sheared"),
                  this.restoreSoundEventHolder(equippableTag)
               )
            );
         }
      }
   }
}
