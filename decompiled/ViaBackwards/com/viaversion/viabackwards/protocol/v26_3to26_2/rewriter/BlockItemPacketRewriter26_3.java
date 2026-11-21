package com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v26_3to26_2.Protocol26_3To26_2;
import com.viaversion.viabackwards.protocol.v26_3to26_2.storage.ProtocolStorables26_3;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.ResolvableFloat;
import com.viaversion.viaversion.api.minecraft.ResolvableInt;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.BrewingFuel;
import com.viaversion.viaversion.api.minecraft.item.data.Compostable;
import com.viaversion.viaversion.api.minecraft.item.data.CookingFuel;
import com.viaversion.viaversion.api.minecraft.item.data.DeathProtection;
import com.viaversion.viaversion.api.minecraft.item.data.MobVisibility;
import com.viaversion.viaversion.api.minecraft.item.data.SignText;
import com.viaversion.viaversion.api.minecraft.item.data.SwingAnimation;
import com.viaversion.viaversion.api.minecraft.item.data.VillagerFood;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.TeleportRandomlyConsumeEffect;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_3 extends BackwardsStructuredItemRewriter<ClientboundPacket26_3, ServerboundPacket26_1, Protocol26_3To26_2> {
   private static final int DEFAULT_RANDOMIZATION = 0;
   private static final int ALTERNATIVE_WITH_SPEED_RANDOMIZATION = 2;
   private static final int BREWING_STAND_MENU_TYPE = 11;
   private static final Holder<SoundEvent> SILENT_SOUND = Holder.of(new SoundEvent("intentionally_empty", null));

   public BlockItemPacketRewriter26_3(Protocol26_3To26_2 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.MAP_ITEM_DATA, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         if ((Boolean)wrapper.passthrough(Types.BOOLEAN)) {
            int icons = (Integer)wrapper.passthrough(Types.VAR_INT);

            for (int i = 0; i < icons; i++) {
               int decorationType = (Integer)wrapper.read(Types.VAR_INT);
               wrapper.write(Types.VAR_INT, Math.min(decorationType, 34));
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG);
            }
         }
      });
      ((Protocol26_3To26_2)this.protocol)
         .replaceClientbound(
            ClientboundPackets26_3.LEVEL_PARTICLES,
            wrapper -> {
               Particle particle = (Particle)wrapper.read(((Protocol26_3To26_2)this.protocol).getParticleRewriter().particleType());
               ((Protocol26_3To26_2)this.protocol).getParticleRewriter().rewriteParticle(wrapper.user(), particle);
               boolean overrideLimiter = (Boolean)wrapper.read(Types.BOOLEAN);
               boolean alwaysShow = (Boolean)wrapper.read(Types.BOOLEAN);
               double x = (Double)wrapper.read(Types.DOUBLE);
               double y = (Double)wrapper.read(Types.DOUBLE);
               double z = (Double)wrapper.read(Types.DOUBLE);
               float offsetX = (Float)wrapper.read(Types.FLOAT);
               float offsetY = (Float)wrapper.read(Types.FLOAT);
               float offsetZ = (Float)wrapper.read(Types.FLOAT);
               float maxSpeedX = (Float)wrapper.read(Types.FLOAT);
               float maxSpeedY = (Float)wrapper.read(Types.FLOAT);
               float maxSpeedZ = (Float)wrapper.read(Types.FLOAT);
               int count = (Integer)wrapper.read(Types.VAR_INT);
               int randomizationType = (Integer)wrapper.read(Types.VAR_INT);
               boolean singleSpeed = maxSpeedX == maxSpeedY && maxSpeedY == maxSpeedZ;
               if (count <= 0) {
                  if (singleSpeed) {
                     this.writeParticle(wrapper, particle, overrideLimiter, alwaysShow, x, y, z, offsetX, offsetY, offsetZ, maxSpeedX, count);
                  } else {
                     this.writeParticle(
                        wrapper,
                        particle,
                        overrideLimiter,
                        alwaysShow,
                        x,
                        y,
                        z,
                        (float)((double)maxSpeedX * offsetX),
                        (float)((double)maxSpeedY * offsetY),
                        (float)((double)maxSpeedZ * offsetZ),
                        1.0F,
                        count
                     );
                  }
               } else if (randomizationType == 0 && singleSpeed) {
                  this.writeParticle(wrapper, particle, overrideLimiter, alwaysShow, x, y, z, offsetX, offsetY, offsetZ, maxSpeedX, count);
               } else if (count > 3) {
                  float maxSpeed = Math.max(maxSpeedX, Math.max(maxSpeedY, maxSpeedZ));
                  if (randomizationType == 0) {
                     this.writeParticle(wrapper, particle, overrideLimiter, alwaysShow, x, y, z, offsetX, offsetY, offsetZ, maxSpeed, count);
                  } else {
                     this.writeParticle(
                        wrapper,
                        particle,
                        overrideLimiter,
                        alwaysShow,
                        x + offsetX / 2.0F,
                        y + offsetY / 2.0F,
                        z + offsetZ / 2.0F,
                        offsetX / 2.0F,
                        offsetY / 2.0F,
                        offsetZ / 2.0F,
                        maxSpeed,
                        count
                     );
                  }
               } else {
                  wrapper.cancel();
                  ThreadLocalRandom random = ThreadLocalRandom.current();

                  for (int i = 0; i < count; i++) {
                     double varianceX;
                     double varianceY;
                     double varianceZ;
                     double speedX;
                     double speedY;
                     double speedZ;
                     if (randomizationType == 0) {
                        varianceX = random.nextGaussian() * offsetX;
                        varianceY = random.nextGaussian() * offsetY;
                        varianceZ = random.nextGaussian() * offsetZ;
                        speedX = random.nextGaussian() * maxSpeedX;
                        speedY = random.nextGaussian() * maxSpeedY;
                        speedZ = random.nextGaussian() * maxSpeedZ;
                     } else {
                        varianceX = random.nextDouble() * offsetX;
                        varianceY = random.nextDouble() * offsetY;
                        varianceZ = random.nextDouble() * offsetZ;
                        boolean randomizeSpeed = randomizationType == 2;
                        speedX = randomizeSpeed ? maxSpeedX * random.nextDouble() : maxSpeedX;
                        speedY = randomizeSpeed ? maxSpeedY * random.nextDouble() : maxSpeedY;
                        speedZ = randomizeSpeed ? maxSpeedZ * random.nextDouble() : maxSpeedZ;
                     }

                     PacketWrapper particlePacket = wrapper.create(ClientboundPackets26_1.LEVEL_PARTICLES);
                     this.writeParticle(
                        particlePacket,
                        particle,
                        overrideLimiter,
                        alwaysShow,
                        x + varianceX,
                        y + varianceY,
                        z + varianceZ,
                        (float)speedX,
                        (float)speedY,
                        (float)speedZ,
                        1.0F,
                        0
                     );
                     particlePacket.send(Protocol26_3To26_2.class);
                  }
               }
            }
         );
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.EXPLODE, wrapper -> {
         if (!(Boolean)wrapper.read(Types.BOOLEAN)) {
            wrapper.set(Types.SOUND_EVENT, 0, SILENT_SOUND);
         }
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.OPEN_SIGN_EDITOR, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         boolean frontText = (Integer)wrapper.read(Types.VAR_INT) == 1;
         wrapper.write(Types.BOOLEAN, frontText);
      });
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.OPEN_SCREEN, wrapper -> {
         wrapper.resetReader();
         int containerId = (Integer)wrapper.passthrough(Types.VAR_INT);
         int menuType = (Integer)wrapper.passthrough(Types.VAR_INT);
         ProtocolStorables26_3 storables = (ProtocolStorables26_3)wrapper.user().storables(this.protocol);
         storables.setBrewingStandContainerId(menuType == 11 ? containerId : -1);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.CONTAINER_SET_DATA, wrapper -> {
         int containerId = (Integer)wrapper.passthrough(Types.VAR_INT);
         short slot = (Short)wrapper.passthrough(Types.SHORT);
         if (slot >= 2) {
            ProtocolStorables26_3 storables = (ProtocolStorables26_3)wrapper.user().storables(this.protocol);
            if (storables.brewingStandContainerId() == containerId) {
               wrapper.cancel();
            }
         }
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.SIGN_UPDATE, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         boolean frontText = (Boolean)wrapper.read(Types.BOOLEAN);

         for (int i = 0; i < 4; i++) {
            wrapper.passthrough(Types.STRING);
         }

         wrapper.write(Types.VAR_INT, frontText ? 1 : 0);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.LIGHT_UPDATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         this.handleLightMasks(wrapper);
      });
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.LEVEL_CHUNK_WITH_LIGHT, this::handleLightMasks);
      ((Protocol26_3To26_2)this.protocol).replaceClientbound(ClientboundPackets26_3.UPDATE_ADVANCEMENTS, wrapper -> {
         int lastPositionIndex = 0;
         wrapper.passthrough(Types.BOOLEAN);
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            boolean hasDisplayData = (Boolean)wrapper.passthrough(Types.BOOLEAN);
            if (hasDisplayData) {
               Tag title = (Tag)wrapper.passthrough(Types.TRUSTED_TAG);
               Tag description = (Tag)wrapper.passthrough(Types.TRUSTED_TAG);
               NBTComponentRewriter<ClientboundPacket26_3> componentRewriter = ((Protocol26_3To26_2)this.protocol).getComponentRewriter();
               componentRewriter.processTag(wrapper.user(), title);
               componentRewriter.processTag(wrapper.user(), description);
               this.passthroughClientboundItemTemplate(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               int flags = (Integer)wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               wrapper.write(Types.FLOAT, 0.0F);
               wrapper.write(Types.FLOAT, 0.0F);
            }

            int requirements = (Integer)wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
            float x = (Float)wrapper.read(Types.FLOAT);
            float y = (Float)wrapper.read(Types.FLOAT);
            if (hasDisplayData) {
               wrapper.set(Types.FLOAT, lastPositionIndex, x);
               wrapper.set(Types.FLOAT, lastPositionIndex + 1, y);
               lastPositionIndex += 2;
            }
         }
      });
   }

   private void writeParticle(
      PacketWrapper wrapper,
      Particle particle,
      boolean overrideLimiter,
      boolean alwaysShow,
      double x,
      double y,
      double z,
      float offsetX,
      float offsetY,
      float offsetZ,
      float maxSpeed,
      int count
   ) {
      wrapper.write(Types.BOOLEAN, overrideLimiter);
      wrapper.write(Types.BOOLEAN, alwaysShow);
      wrapper.write(Types.DOUBLE, x);
      wrapper.write(Types.DOUBLE, y);
      wrapper.write(Types.DOUBLE, z);
      wrapper.write(Types.FLOAT, offsetX);
      wrapper.write(Types.FLOAT, offsetY);
      wrapper.write(Types.FLOAT, offsetZ);
      wrapper.write(Types.FLOAT, maxSpeed);
      wrapper.write(Types.INT, count);
      wrapper.write(((Protocol26_3To26_2)this.protocol).getParticleRewriter().mappedParticleType(), particle);
   }

   private void handleLightMasks(PacketWrapper wrapper) {
      for (int i = 0; i < 4; i++) {
         BitSet mask = (BitSet)wrapper.read(Types.BIT_SET);
         wrapper.write(Types.LONG_ARRAY_PRIMITIVE, mask.toLongArray());
      }
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToClient(connection, item, container);
      com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.BlockItemPacketRewriter26_3.downgradeData(container);
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToServer(connection, item, container);
      com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.BlockItemPacketRewriter26_3.upgradeData(container);
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
      if (customData.remove(this.nbtTagName("backup")) instanceof CompoundTag backupTag) {
         this.restoreIntData(StructuredDataKey.PROVIDES_POTTERY_PATTERN, container, backupTag);
         this.restoreIntData(StructuredDataKey.BLOCK_TRANSFORMER, container, backupTag);
         this.restoreIntData(StructuredDataKey.CUSHION_COLOR, container, backupTag);
         if (backupTag.contains("waxed")) {
            container.set(StructuredDataKey.WAXED);
         }

         IntTag villagerFood = backupTag.getIntTag("villager_food");
         if (villagerFood != null) {
            container.set(StructuredDataKey.VILLAGER_FOOD, new VillagerFood(villagerFood.asInt()));
         }

         Tag compostable = backupTag.get("compostable");
         if (compostable != null) {
            container.set(StructuredDataKey.COMPOSTABLE, new Compostable(this.restoreResolvableInt(compostable)));
         }

         CompoundTag cookingFuel = backupTag.getCompoundTag("cooking_fuel");
         if (cookingFuel != null) {
            container.set(
               StructuredDataKey.COOKING_FUEL,
               new CookingFuel(this.restoreResolvableInt(cookingFuel.get("burn_time")), this.restoreResolvableFloat(cookingFuel.get("speed_multiplier")))
            );
         }

         CompoundTag brewingFuel = backupTag.getCompoundTag("brewing_fuel");
         if (brewingFuel != null) {
            container.set(
               StructuredDataKey.BREWING_FUEL,
               new BrewingFuel(this.restoreResolvableInt(brewingFuel.get("uses")), this.restoreResolvableFloat(brewingFuel.get("speed_multiplier")))
            );
         }

         CompoundTag mobVisibility = backupTag.getCompoundTag("mob_visibility");
         if (mobVisibility != null) {
            container.set(
               StructuredDataKey.MOB_VISIBILITY,
               new MobVisibility(this.restoreHolderSet(mobVisibility, "targeting_entity_types"), mobVisibility.getFloat("visibility"))
            );
         }

         this.restoreSignText(StructuredDataKey.SIGN_TEXT_FRONT, container, backupTag, "sign_text_front");
         this.restoreSignText(StructuredDataKey.SIGN_TEXT_BACK, container, backupTag, "sign_text_back");
         CompoundTag attackAnimation = backupTag.getCompoundTag("attack_animation");
         CompoundTag interactAnimation = backupTag.getCompoundTag("interact_animation");
         if (attackAnimation != null || interactAnimation != null) {
            container.remove(StructuredDataKey.SWING_ANIMATION);
            if (attackAnimation != null) {
               container.set(StructuredDataKey.ATTACK_ANIMATION, this.restoreSwingAnimation(attackAnimation));
            }

            if (interactAnimation != null) {
               container.set(StructuredDataKey.INTERACT_ANIMATION, this.restoreSwingAnimation(interactAnimation));
            }
         }

         this.restoreMapDecorationTypes((CompoundTag)container.get(StructuredDataKey.MAP_DECORATIONS), backupTag);
         Consumable1_21_2 consumable = (Consumable1_21_2)container.get(StructuredDataKey.CONSUMABLE1_21_2);
         if (consumable != null) {
            this.restoreDirectionalParticles(backupTag, "consumable_effects", consumable.consumeEffects());
         }

         DeathProtection deathProtection = (DeathProtection)container.get(StructuredDataKey.DEATH_PROTECTION1_21_2);
         if (deathProtection != null) {
            this.restoreDirectionalParticles(backupTag, "death_protection_effects", deathProtection.deathEffects());
         }
      }
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.PROVIDES_POTTERY_PATTERN, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.BLOCK_TRANSFORMER, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.CUSHION_COLOR, dataContainer, backupTag);
      if (dataContainer.hasValue(StructuredDataKey.WAXED)) {
         backupTag.putBoolean("waxed", true);
      }

      VillagerFood villagerFood = (VillagerFood)dataContainer.get(StructuredDataKey.VILLAGER_FOOD);
      if (villagerFood != null) {
         backupTag.putInt("villager_food", villagerFood.nutrition());
      }

      Compostable compostable = (Compostable)dataContainer.get(StructuredDataKey.COMPOSTABLE);
      if (compostable != null) {
         backupTag.put("compostable", this.resolvableIntToTag(compostable.layers()));
      }

      CookingFuel cookingFuel = (CookingFuel)dataContainer.get(StructuredDataKey.COOKING_FUEL);
      if (cookingFuel != null) {
         CompoundTag fuelTag = new CompoundTag();
         fuelTag.put("burn_time", this.resolvableIntToTag(cookingFuel.burnTime()));
         fuelTag.put("speed_multiplier", this.resolvableFloatToTag(cookingFuel.speedMultiplier()));
         backupTag.put("cooking_fuel", fuelTag);
      }

      BrewingFuel brewingFuel = (BrewingFuel)dataContainer.get(StructuredDataKey.BREWING_FUEL);
      if (brewingFuel != null) {
         CompoundTag fuelTag = new CompoundTag();
         fuelTag.put("uses", this.resolvableIntToTag(brewingFuel.uses()));
         fuelTag.put("speed_multiplier", this.resolvableFloatToTag(brewingFuel.speedMultiplier()));
         backupTag.put("brewing_fuel", fuelTag);
      }

      MobVisibility mobVisibility = (MobVisibility)dataContainer.get(StructuredDataKey.MOB_VISIBILITY);
      if (mobVisibility != null) {
         CompoundTag visibilityTag = new CompoundTag();
         visibilityTag.put("targeting_entity_types", this.holderSetToTag(mobVisibility.targetingEntityTypes()));
         visibilityTag.putFloat("visibility", mobVisibility.visibility());
         backupTag.put("mob_visibility", visibilityTag);
      }

      this.saveSignText(backupTag, "sign_text_front", (SignText)dataContainer.get(StructuredDataKey.SIGN_TEXT_FRONT));
      this.saveSignText(backupTag, "sign_text_back", (SignText)dataContainer.get(StructuredDataKey.SIGN_TEXT_BACK));
      this.saveSwingAnimation(backupTag, "attack_animation", (SwingAnimation)dataContainer.get(StructuredDataKey.ATTACK_ANIMATION));
      this.saveSwingAnimation(backupTag, "interact_animation", (SwingAnimation)dataContainer.get(StructuredDataKey.INTERACT_ANIMATION));
      this.saveMapDecorationTypes(backupTag, (CompoundTag)dataContainer.get(StructuredDataKey.MAP_DECORATIONS));
      Consumable1_21_2 consumable = (Consumable1_21_2)dataContainer.get(StructuredDataKey.CONSUMABLE26_3);
      if (consumable != null) {
         this.saveDirectionalParticles(backupTag, "consumable_effects", consumable.consumeEffects());
      }

      DeathProtection deathProtection = (DeathProtection)dataContainer.get(StructuredDataKey.DEATH_PROTECTION26_3);
      if (deathProtection != null) {
         this.saveDirectionalParticles(backupTag, "death_protection_effects", deathProtection.deathEffects());
      }
   }

   private Tag resolvableIntToTag(ResolvableInt value) {
      return (Tag)(value.isLeft() ? new IntTag((Integer)value.left()) : new StringTag((String)value.right()));
   }

   private ResolvableInt restoreResolvableInt(@Nullable Tag tag) {
      return tag instanceof StringTag stringTag
         ? ResolvableInt.of(stringTag.getValue())
         : ResolvableInt.of(tag instanceof NumberTag numberTag ? numberTag.asInt() : 0);
   }

   private Tag resolvableFloatToTag(ResolvableFloat value) {
      return (Tag)(value.isLeft() ? new FloatTag((Float)value.left()) : new StringTag((String)value.right()));
   }

   private ResolvableFloat restoreResolvableFloat(@Nullable Tag tag) {
      return tag instanceof StringTag stringTag
         ? ResolvableFloat.of(stringTag.getValue())
         : ResolvableFloat.of(tag instanceof NumberTag numberTag ? numberTag.asFloat() : 0.0F);
   }

   private void saveSignText(CompoundTag backupTag, String key, @Nullable SignText signText) {
      if (signText != null) {
         CompoundTag tag = new CompoundTag();
         tag.put("messages", this.messagesToTag(signText.messages()));
         if (signText.filteredMessages() != null) {
            tag.put("filtered_messages", this.messagesToTag(signText.filteredMessages()));
         }

         tag.putInt("color", signText.color());
         tag.putBoolean("has_glowing_text", signText.hasGlowingText());
         backupTag.put(key, tag);
      }
   }

   private void restoreSignText(StructuredDataKey<SignText> key, StructuredDataContainer container, CompoundTag backupTag, String name) {
      CompoundTag tag = backupTag.getCompoundTag(name);
      CompoundTag messages = tag != null ? tag.getCompoundTag("messages") : null;
      if (messages != null) {
         CompoundTag filteredMessages = tag.getCompoundTag("filtered_messages");
         container.set(
            key,
            new SignText(
               this.messagesFromTag(messages),
               filteredMessages != null ? this.messagesFromTag(filteredMessages) : null,
               tag.getInt("color"),
               tag.getBoolean("has_glowing_text")
            )
         );
      }
   }

   private CompoundTag messagesToTag(Tag[] messages) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("length", messages.length);

      for (int i = 0; i < messages.length; i++) {
         if (messages[i] != null) {
            tag.put(Integer.toString(i), messages[i]);
         }
      }

      return tag;
   }

   private Tag[] messagesFromTag(CompoundTag tag) {
      Tag[] messages = new Tag[tag.getInt("length")];

      for (int i = 0; i < messages.length; i++) {
         messages[i] = tag.get(Integer.toString(i));
      }

      return messages;
   }

   private void saveSwingAnimation(CompoundTag backupTag, String key, @Nullable SwingAnimation animation) {
      if (animation != null) {
         CompoundTag tag = new CompoundTag();
         tag.putInt("type", animation.type());
         tag.putInt("duration", animation.duration());
         backupTag.put(key, tag);
      }
   }

   private SwingAnimation restoreSwingAnimation(CompoundTag tag) {
      return new SwingAnimation(tag.getInt("type"), tag.getInt("duration"));
   }

   private void saveMapDecorationTypes(CompoundTag backupTag, @Nullable CompoundTag mapDecorations) {
      if (mapDecorations != null) {
         CompoundTag types = new CompoundTag();

         for (Entry<String, Tag> entry : mapDecorations.entrySet()) {
            types.put(entry.getKey(), ((CompoundTag)entry.getValue()).getStringTag("type"));
         }

         backupTag.put("map_decorations", types);
      }
   }

   private void restoreMapDecorationTypes(@Nullable CompoundTag mapDecorations, CompoundTag backupTag) {
      CompoundTag types = backupTag.getCompoundTag("map_decorations");
      if (types != null && mapDecorations != null) {
         for (Entry<String, Tag> entry : types.entrySet()) {
            CompoundTag decorationTag = mapDecorations.getCompoundTag(entry.getKey());
            if (decorationTag != null) {
               decorationTag.put("type", entry.getValue());
            }
         }
      }
   }

   private void saveDirectionalParticles(CompoundTag backupTag, String key, ConsumeEffect<?>[] effects) {
      int[] indexes = new int[effects.length];
      int size = 0;

      for (int i = 0; i < effects.length; i++) {
         if (effects[i].value() instanceof TeleportRandomlyConsumeEffect effect && effect.directionalParticles()) {
            indexes[size++] = i;
         }
      }

      if (size != 0) {
         backupTag.put(key, new IntArrayTag(Arrays.copyOf(indexes, size)));
      }
   }

   private void restoreDirectionalParticles(CompoundTag backupTag, String key, ConsumeEffect<?>[] effects) {
      IntArrayTag tag = backupTag.getIntArrayTag(key);
      if (tag != null) {
         for (int index : tag.getValue()) {
            if (index >= 0 && index < effects.length && effects[index].value() instanceof Float diameter) {
               effects[index] = new ConsumeEffect(
                  effects[index].id(), ConsumeEffect.TELEPORT_RANDOMLY_TYPE26_3, new TeleportRandomlyConsumeEffect(diameter, true)
               );
            }
         }
      }
   }
}
