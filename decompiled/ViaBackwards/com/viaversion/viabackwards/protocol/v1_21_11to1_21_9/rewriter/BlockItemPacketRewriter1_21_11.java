package com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.Protocol1_21_11To1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.storage.ProtocolStorables1_21_11;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttackRange;
import com.viaversion.viaversion.api.minecraft.item.data.KineticWeapon;
import com.viaversion.viaversion.api.minecraft.item.data.PiercingWeapon;
import com.viaversion.viaversion.api.minecraft.item.data.SwingAnimation;
import com.viaversion.viaversion.api.minecraft.item.data.UseEffects;
import com.viaversion.viaversion.api.minecraft.item.data.KineticWeapon.Condition;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.util.Either;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_21_11
   extends BackwardsStructuredItemRewriter<ClientboundPacket1_21_11, ServerboundPacket1_21_9, Protocol1_21_11To1_21_9> {
   public BlockItemPacketRewriter1_21_11(Protocol1_21_11To1_21_9 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol1_21_11To1_21_9)this.protocol).registerClientbound(ClientboundPackets1_21_11.SET_BORDER_LERP_SIZE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.VAR_LONG, (Long)wrapper.read(Types.VAR_LONG) * 50L);
      });
      ((Protocol1_21_11To1_21_9)this.protocol).registerClientbound(ClientboundPackets1_21_11.INITIALIZE_BORDER, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.VAR_LONG, (Long)wrapper.read(Types.VAR_LONG) * 50L);
      });
      ((Protocol1_21_11To1_21_9)this.protocol).registerClientbound(ClientboundPackets1_21_11.SET_TIME, wrapper -> {
         long gameTime = (Long)wrapper.passthrough(Types.LONG);
         ProtocolStorables1_21_11 storables = (ProtocolStorables1_21_11)wrapper.user().storables(this.protocol);
         storables.gameTimeStorage().setGameTime(gameTime);
      });
      ((Protocol1_21_11To1_21_9)this.protocol).registerServerbound(ServerboundPackets1_21_6.CLIENT_TICK_END, wrapper -> {
         ProtocolStorables1_21_11 storables = (ProtocolStorables1_21_11)wrapper.user().storables(this.protocol);
         storables.gameTimeStorage().incrementGameTime();
      });
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToClient(connection, item, container);
      com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.BlockItemPacketRewriter1_21_11.downgradeData(item, container);
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToServer(connection, item, container);
      com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.BlockItemPacketRewriter1_21_11.upgradeData(item, container);
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
      if (customData.remove(this.nbtTagName("backup")) instanceof CompoundTag backupTag) {
         CompoundTag swingTag = backupTag.getCompoundTag("swing_animation");
         if (swingTag != null) {
            int type = swingTag.getInt("type");
            int duration = swingTag.getInt("duration");
            container.set(StructuredDataKey.SWING_ANIMATION, new SwingAnimation(type, duration));
         }

         CompoundTag kineticTag = backupTag.getCompoundTag("kinetic_weapon");
         if (kineticTag != null) {
            int contactCooldownTicks = kineticTag.getInt("contact_cooldown_ticks");
            int delayTicks = kineticTag.getInt("delay_ticks");
            Condition dismountConditions = this.loadDamageCondition(kineticTag, "dismount_conditions");
            Condition knockbackConditions = this.loadDamageCondition(kineticTag, "knockback_conditions");
            Condition damageConditions = this.loadDamageCondition(kineticTag, "damage_conditions");
            float forwardMovement = kineticTag.getFloat("forward_movement");
            float damageMultiplier = kineticTag.getFloat("damage_multiplier");
            Holder<SoundEvent> sound = kineticTag.contains("sound") ? this.restoreSoundEventHolder(kineticTag, "sound") : null;
            Holder<SoundEvent> hitSound = kineticTag.contains("hit_sound") ? this.restoreSoundEventHolder(kineticTag, "hit_sound") : null;
            container.set(
               StructuredDataKey.KINETIC_WEAPON,
               new KineticWeapon(
                  contactCooldownTicks,
                  delayTicks,
                  dismountConditions,
                  knockbackConditions,
                  damageConditions,
                  forwardMovement,
                  damageMultiplier,
                  sound,
                  hitSound
               )
            );
         }

         CompoundTag piercingTag = backupTag.getCompoundTag("piercing_weapon");
         if (piercingTag != null) {
            boolean dealsKnockback = piercingTag.getBoolean("deals_knockback");
            boolean dismounts = piercingTag.getBoolean("dismounts");
            Holder<SoundEvent> sound = piercingTag.contains("sound") ? this.restoreSoundEventHolder(piercingTag, "sound") : null;
            Holder<SoundEvent> hitSound = piercingTag.contains("hit_sound") ? this.restoreSoundEventHolder(piercingTag, "hit_sound") : null;
            container.set(StructuredDataKey.PIERCING_WEAPON, new PiercingWeapon(dealsKnockback, dismounts, sound, hitSound));
         }

         Tag damageTypeId = backupTag.get("damage_type_id");
         if (damageTypeId != null) {
            if (damageTypeId instanceof StringTag stringTag) {
               container.set(StructuredDataKey.DAMAGE_TYPE1_21_11, Either.right(stringTag.getValue()));
            } else if (damageTypeId instanceof IntTag intTag) {
               container.set(StructuredDataKey.DAMAGE_TYPE1_21_11, Either.left(intTag.asInt()));
            }
         }

         CompoundTag useEffectsTag = backupTag.getCompoundTag("use_effects");
         if (useEffectsTag != null) {
            boolean canSprint = useEffectsTag.getBoolean("can_sprint");
            boolean interactVibrations = useEffectsTag.getBoolean("interact_vibrations");
            float speedMultiplier = useEffectsTag.getFloat("speed_multiplier");
            container.set(StructuredDataKey.USE_EFFECTS, new UseEffects(canSprint, interactVibrations, speedMultiplier));
         }

         CompoundTag attackRangeTag = backupTag.getCompoundTag("attack_range");
         if (attackRangeTag != null) {
            float minRange = attackRangeTag.getFloat("min_range");
            float maxRange = attackRangeTag.getFloat("max_range");
            float minCreativeRange = attackRangeTag.getFloat("min_creative_reach");
            float maxCreativeRange = attackRangeTag.getFloat("max_creative_reach");
            float hitboxMargin = attackRangeTag.getFloat("hitbox_margin");
            float mobFactor = attackRangeTag.getFloat("mob_factor");
            container.set(StructuredDataKey.ATTACK_RANGE, new AttackRange(minRange, maxRange, minCreativeRange, maxCreativeRange, hitboxMargin, mobFactor));
         }

         Tag zombieNautilusVariantTag = backupTag.get("zombie_nautilus_variant");
         if (zombieNautilusVariantTag != null) {
            if (zombieNautilusVariantTag instanceof StringTag stringTag) {
               container.set(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11, Either.right(stringTag.getValue()));
            } else if (zombieNautilusVariantTag instanceof IntTag intTag) {
               container.set(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11, Either.left(intTag.asInt()));
            }
         }

         this.restoreFloatData(StructuredDataKey.MINIMUM_ATTACK_CHARGE, container, backupTag);
      }
   }

   private Condition loadDamageCondition(CompoundTag tag, String key) {
      CompoundTag conditionTag = tag.getCompoundTag(key);
      if (conditionTag == null) {
         return null;
      } else {
         int maxDurationTicks = conditionTag.getInt("max_duration_ticks");
         float minSpeed = conditionTag.getFloat("min_speed");
         float minRelativeSpeed = conditionTag.getFloat("min_relative_speed");
         return new Condition(maxDurationTicks, minSpeed, minRelativeSpeed);
      }
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
      SwingAnimation swingAnimation = (SwingAnimation)dataContainer.get(StructuredDataKey.SWING_ANIMATION);
      if (swingAnimation != null) {
         CompoundTag swingTag = new CompoundTag();
         swingTag.putInt("type", swingAnimation.type());
         swingTag.putInt("duration", swingAnimation.duration());
         backupTag.put("swing_animation", swingTag);
      }

      KineticWeapon kineticWeapon = (KineticWeapon)dataContainer.get(StructuredDataKey.KINETIC_WEAPON);
      if (kineticWeapon != null) {
         CompoundTag kineticTag = new CompoundTag();
         kineticTag.putInt("contact_cooldown_ticks", kineticWeapon.contactCooldownTicks());
         kineticTag.putInt("delay_ticks", kineticWeapon.delayTicks());
         this.saveDamageCondition(kineticTag, "dismount_conditions", kineticWeapon.dismountConditions());
         this.saveDamageCondition(kineticTag, "knockback_conditions", kineticWeapon.knockbackConditions());
         this.saveDamageCondition(kineticTag, "damage_conditions", kineticWeapon.damageConditions());
         kineticTag.putFloat("forward_movement", kineticWeapon.forwardMovement());
         kineticTag.putFloat("damage_multiplier", kineticWeapon.damageMultiplier());
         if (kineticWeapon.sound() != null) {
            kineticTag.put("sound", this.holderToTag(kineticWeapon.sound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }

         if (kineticWeapon.hitSound() != null) {
            kineticTag.put("hit_sound", this.holderToTag(kineticWeapon.hitSound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }

         backupTag.put("kinetic_weapon", kineticTag);
      }

      PiercingWeapon piercingWeapon = (PiercingWeapon)dataContainer.get(StructuredDataKey.PIERCING_WEAPON);
      if (piercingWeapon != null) {
         CompoundTag piercingTag = new CompoundTag();
         piercingTag.putBoolean("deals_knockback", piercingWeapon.dealsKnockback());
         piercingTag.putBoolean("dismounts", piercingWeapon.dismounts());
         if (piercingWeapon.sound() != null) {
            piercingTag.put("sound", this.holderToTag(piercingWeapon.sound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }

         if (piercingWeapon.hitSound() != null) {
            piercingTag.put("hit_sound", this.holderToTag(piercingWeapon.hitSound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }

         backupTag.put("piercing_weapon", piercingTag);
      }

      Either<Integer, String> damageType = (Either<Integer, String>)dataContainer.get(StructuredDataKey.DAMAGE_TYPE1_21_11);
      if (damageType != null) {
         if (damageType.isLeft()) {
            backupTag.putInt("damage_type_id", (Integer)damageType.left());
         } else {
            backupTag.putString("damage_type_id", (String)damageType.right());
         }
      }

      UseEffects useEffects = (UseEffects)dataContainer.get(StructuredDataKey.USE_EFFECTS);
      if (useEffects != null) {
         CompoundTag useEffectsTag = new CompoundTag();
         useEffectsTag.putBoolean("can_sprint", useEffects.canSprint());
         useEffectsTag.putBoolean("interact_vibrations", useEffects.interactVibrations());
         useEffectsTag.putFloat("speed_multiplier", useEffects.speedMultiplier());
         backupTag.put("use_effects", useEffectsTag);
      }

      AttackRange attackRange = (AttackRange)dataContainer.get(StructuredDataKey.ATTACK_RANGE);
      if (attackRange != null) {
         CompoundTag attackRangeTag = new CompoundTag();
         attackRangeTag.putFloat("min_range", attackRange.minRange());
         attackRangeTag.putFloat("max_range", attackRange.maxRange());
         attackRangeTag.putFloat("min_creative_reach", attackRange.minCreativeRange());
         attackRangeTag.putFloat("max_creative_reach", attackRange.maxCreativeRange());
         attackRangeTag.putFloat("hitbox_margin", attackRange.hitboxMargin());
         attackRangeTag.putFloat("mob_factor", attackRange.mobFactor());
         backupTag.put("attack_range", attackRangeTag);
      }

      Either<Integer, String> zombieNautilusVariant = (Either<Integer, String>)dataContainer.get(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11);
      if (zombieNautilusVariant != null) {
         if (zombieNautilusVariant.isLeft()) {
            backupTag.putInt("zombie_nautilus_variant", (Integer)zombieNautilusVariant.left());
         } else {
            backupTag.putString("zombie_nautilus_variant", (String)zombieNautilusVariant.right());
         }
      }

      this.saveFloatData(StructuredDataKey.MINIMUM_ATTACK_CHARGE, dataContainer, backupTag);
   }

   private void saveDamageCondition(CompoundTag tag, String key, @Nullable Condition condition) {
      if (condition != null) {
         CompoundTag conditionTag = new CompoundTag();
         conditionTag.putInt("max_duration_ticks", condition.maxDurationTicks());
         conditionTag.putFloat("min_speed", condition.minSpeed());
         conditionTag.putFloat("min_relative_speed", condition.minRelativeSpeed());
         tag.put(key, conditionTag);
      }
   }
}
