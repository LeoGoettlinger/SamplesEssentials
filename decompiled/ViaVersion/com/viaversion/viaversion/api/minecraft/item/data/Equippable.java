package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record Equippable(
   int equipmentSlot,
   Holder<SoundEvent> soundEvent,
   @Nullable String model,
   @Nullable String cameraOverlay,
   @Nullable HolderSet allowedEntities,
   boolean dispensable,
   boolean swappable,
   boolean damageOnHurt,
   boolean equipOnInteract,
   boolean canBeSheared,
   Holder<SoundEvent> shearingSound
) implements Rewritable {
   public static final Type<Equippable> TYPE1_21_2 = new Type<Equippable>(Equippable.class) {
      public Equippable read(ByteBuf buffer) {
         int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
         Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
         String model = Types.OPTIONAL_STRING.read(buffer);
         String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
         HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
         boolean dispensable = buffer.readBoolean();
         boolean swappable = buffer.readBoolean();
         boolean damageOnHurt = buffer.readBoolean();
         return new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
      }

      public void write(ByteBuf buffer, Equippable value) {
         Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
         Types.SOUND_EVENT.write(buffer, value.soundEvent());
         Types.OPTIONAL_STRING.write(buffer, value.model());
         Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
         Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
         buffer.writeBoolean(value.dispensable());
         buffer.writeBoolean(value.swappable());
         buffer.writeBoolean(value.damageOnHurt());
      }
   };
   public static final Type<Equippable> TYPE1_21_5 = new Type<Equippable>(Equippable.class) {
      public Equippable read(ByteBuf buffer) {
         int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
         Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
         String model = Types.OPTIONAL_STRING.read(buffer);
         String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
         HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
         boolean dispensable = buffer.readBoolean();
         boolean swappable = buffer.readBoolean();
         boolean damageOnHurt = buffer.readBoolean();
         boolean equipOnInteract = buffer.readBoolean();
         return new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract);
      }

      public void write(ByteBuf buffer, Equippable value) {
         Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
         Types.SOUND_EVENT.write(buffer, value.soundEvent());
         Types.OPTIONAL_STRING.write(buffer, value.model());
         Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
         Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
         buffer.writeBoolean(value.dispensable());
         buffer.writeBoolean(value.swappable());
         buffer.writeBoolean(value.damageOnHurt());
         buffer.writeBoolean(value.equipOnInteract());
      }

      public void write(Ops ops, Equippable value) {
         Holder<SoundEvent> defaultEquipSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "item.armor.equip_generic"));
         ops.writeMap(
            map -> map.write("slot", EnumTypes.EQUIPMENT_SLOT, value.equipmentSlot)
               .writeOptional("equip_sound", Types.SOUND_EVENT, value.soundEvent, defaultEquipSound)
               .writeOptional("asset_id", Types.IDENTIFIER, value.model != null ? Key.of(value.model) : null)
               .writeOptional("camera_overlay", Types.IDENTIFIER, value.cameraOverlay != null ? Key.of(value.cameraOverlay) : null)
               .writeOptional("allowed_entities", new HolderSetType(MappingData.MappingType.ENTITY_TYPE), value.allowedEntities)
               .writeOptional("dispensable", Types.BOOLEAN, value.dispensable, true)
               .writeOptional("swappable", Types.BOOLEAN, value.swappable, true)
               .writeOptional("damage_on_hurt", Types.BOOLEAN, value.damageOnHurt, true)
               .writeOptional("equip_on_interact", Types.BOOLEAN, value.equipOnInteract, false)
         );
      }
   };
   public static final Type<Equippable> TYPE1_21_6 = new Type<Equippable>(Equippable.class) {
      public Equippable read(ByteBuf buffer) {
         int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
         Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
         String model = Types.OPTIONAL_STRING.read(buffer);
         String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
         HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
         boolean dispensable = buffer.readBoolean();
         boolean swappable = buffer.readBoolean();
         boolean damageOnHurt = buffer.readBoolean();
         boolean equipOnInteract = buffer.readBoolean();
         boolean canBeSheared = buffer.readBoolean();
         Holder<SoundEvent> shearingSound = Types.SOUND_EVENT.read(buffer);
         return new Equippable(
            equipmentSlot,
            soundEvent,
            model,
            cameraOverlay,
            allowedEntities,
            dispensable,
            swappable,
            damageOnHurt,
            equipOnInteract,
            canBeSheared,
            shearingSound
         );
      }

      public void write(ByteBuf buffer, Equippable value) {
         Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
         Types.SOUND_EVENT.write(buffer, value.soundEvent());
         Types.OPTIONAL_STRING.write(buffer, value.model());
         Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
         Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
         buffer.writeBoolean(value.dispensable());
         buffer.writeBoolean(value.swappable());
         buffer.writeBoolean(value.damageOnHurt());
         buffer.writeBoolean(value.equipOnInteract());
         buffer.writeBoolean(value.canBeSheared());
         Types.SOUND_EVENT.write(buffer, value.shearingSound());
      }

      public void write(Ops ops, Equippable value) {
         Holder<SoundEvent> defaultSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "item.armor.equip_generic"));
         Holder<SoundEvent> defaultShearingSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "item.shears.snip"));
         ops.writeMap(
            map -> map.write("slot", EnumTypes.EQUIPMENT_SLOT, value.equipmentSlot)
               .writeOptional("equip_sound", Types.SOUND_EVENT, value.soundEvent, defaultSound)
               .writeOptional("asset_id", Types.IDENTIFIER, value.model != null ? Key.of(value.model) : null)
               .writeOptional("camera_overlay", Types.IDENTIFIER, value.cameraOverlay != null ? Key.of(value.cameraOverlay) : null)
               .writeOptional("allowed_entities", new HolderSetType(MappingData.MappingType.ENTITY_TYPE), value.allowedEntities)
               .writeOptional("dispensable", Types.BOOLEAN, value.dispensable, true)
               .writeOptional("swappable", Types.BOOLEAN, value.swappable, true)
               .writeOptional("damage_on_hurt", Types.BOOLEAN, value.damageOnHurt, true)
               .writeOptional("equip_on_interact", Types.BOOLEAN, value.equipOnInteract, false)
               .writeOptional("can_be_sheared", Types.BOOLEAN, value.canBeSheared, false)
               .writeOptional("shearing_sound", Types.SOUND_EVENT, value.shearingSound, defaultShearingSound)
         );
      }
   };

   public Equippable(
      int equipmentSlot,
      Holder<SoundEvent> soundEvent,
      @Nullable String model,
      @Nullable String cameraOverlay,
      @Nullable HolderSet allowedEntities,
      boolean dispensable,
      boolean swappable,
      boolean damageOnHurt
   ) {
      this(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, true);
   }

   public Equippable(
      int equipmentSlot,
      Holder<SoundEvent> soundEvent,
      @Nullable String model,
      @Nullable String cameraOverlay,
      @Nullable HolderSet allowedEntities,
      boolean dispensable,
      boolean swappable,
      boolean damageOnHurt,
      boolean equipOnInteract
   ) {
      this(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, false, Holder.of(0));
   }

   public Equippable rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<SoundEvent> soundEvent = SoundEvent.rewriteHolder(this.soundEvent, Rewritable.soundRewriteFunction(protocol, clientbound));
      Holder<SoundEvent> shearingSound = SoundEvent.rewriteHolder(this.shearingSound, Rewritable.soundRewriteFunction(protocol, clientbound));
      HolderSet allowedEntities = this.allowedEntities != null ? this.allowedEntities.rewrite(Rewritable.entityRewriteFunction(protocol, clientbound)) : null;
      return soundEvent == this.soundEvent && shearingSound == this.shearingSound && allowedEntities == this.allowedEntities
         ? this
         : new Equippable(
            this.equipmentSlot,
            soundEvent,
            this.model,
            this.cameraOverlay,
            allowedEntities,
            this.dispensable,
            this.swappable,
            this.damageOnHurt,
            this.equipOnInteract,
            this.canBeSheared,
            shearingSound
         );
   }
}
