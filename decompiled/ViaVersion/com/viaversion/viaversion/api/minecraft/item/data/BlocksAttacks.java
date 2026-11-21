package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record BlocksAttacks(
   float blockDelaySeconds,
   float disableCooldownScale,
   BlocksAttacks.DamageReduction[] damageReductions,
   BlocksAttacks.ItemDamageFunction itemDamage,
   @Nullable HolderSet bypassedBy,
   @Nullable Holder<SoundEvent> blockSound,
   @Nullable Holder<SoundEvent> disableSound
) implements Rewritable {
   public static final Type<BlocksAttacks> TYPE1_21_5 = new Type<BlocksAttacks>(BlocksAttacks.class) {
      public BlocksAttacks read(ByteBuf buffer) {
         float blockDelaySeconds = buffer.readFloat();
         float disableCooldownScale = buffer.readFloat();
         BlocksAttacks.DamageReduction[] damageReductions = BlocksAttacks.DamageReduction.ARRAY_TYPE.read(buffer);
         BlocksAttacks.ItemDamageFunction itemDamage = BlocksAttacks.ItemDamageFunction.TYPE.read(buffer);
         String bypassedByTag = Types.OPTIONAL_STRING.read(buffer);
         Holder<SoundEvent> blockSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         Holder<SoundEvent> disableSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         return new BlocksAttacks(
            blockDelaySeconds,
            disableCooldownScale,
            damageReductions,
            itemDamage,
            bypassedByTag != null ? HolderSet.of(bypassedByTag) : null,
            blockSound,
            disableSound
         );
      }

      public void write(ByteBuf buffer, BlocksAttacks value) {
         buffer.writeFloat(value.blockDelaySeconds());
         buffer.writeFloat(value.disableCooldownScale());
         BlocksAttacks.DamageReduction.ARRAY_TYPE.write(buffer, value.damageReductions());
         BlocksAttacks.ItemDamageFunction.TYPE.write(buffer, value.itemDamage());
         Types.OPTIONAL_STRING.write(buffer, value.bypassedBy() != null ? value.bypassedBy().tagKey() : null);
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.blockSound());
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.disableSound());
      }

      public void write(Ops ops, BlocksAttacks value) {
         BlocksAttacks.DamageReduction[] defaultDamageReductions = new BlocksAttacks.DamageReduction[]{
            new BlocksAttacks.DamageReduction(90.0F, null, 0.0F, 1.0F)
         };
         BlocksAttacks.ItemDamageFunction defaultItemDamage = new BlocksAttacks.ItemDamageFunction(1.0F, 0.0F, 1.0F);
         ops.writeMap(
            map -> map.writeOptional("block_delay_seconds", Types.FLOAT, value.blockDelaySeconds(), 0.0F)
               .writeOptional("disable_cooldown_scale", Types.FLOAT, value.disableCooldownScale(), 1.0F)
               .writeOptional("damage_reductions", BlocksAttacks.DamageReduction.ARRAY_TYPE, value.damageReductions(), defaultDamageReductions)
               .writeOptional("item_damage", BlocksAttacks.ItemDamageFunction.TYPE, value.itemDamage(), defaultItemDamage)
               .writeOptional("bypassed_by", Types.TAG_KEY, value.bypassedBy() != null ? Key.of(value.bypassedBy().tagKey()) : null)
               .writeOptional("block_sound", Types.SOUND_EVENT, value.blockSound())
               .writeOptional("disabled_sound", Types.SOUND_EVENT, value.disableSound())
         );
      }
   };
   public static final Type<BlocksAttacks> TYPE26_1 = new Type<BlocksAttacks>(BlocksAttacks.class) {
      public BlocksAttacks read(ByteBuf buffer) {
         float blockDelaySeconds = buffer.readFloat();
         float disableCooldownScale = buffer.readFloat();
         BlocksAttacks.DamageReduction[] damageReductions = BlocksAttacks.DamageReduction.ARRAY_TYPE.read(buffer);
         BlocksAttacks.ItemDamageFunction itemDamage = BlocksAttacks.ItemDamageFunction.TYPE.read(buffer);
         HolderSet bypassedByTag = Types.OPTIONAL_HOLDER_SET.read(buffer);
         Holder<SoundEvent> blockSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         Holder<SoundEvent> disableSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         return new BlocksAttacks(blockDelaySeconds, disableCooldownScale, damageReductions, itemDamage, bypassedByTag, blockSound, disableSound);
      }

      public void write(ByteBuf buffer, BlocksAttacks value) {
         buffer.writeFloat(value.blockDelaySeconds());
         buffer.writeFloat(value.disableCooldownScale());
         BlocksAttacks.DamageReduction.ARRAY_TYPE.write(buffer, value.damageReductions());
         BlocksAttacks.ItemDamageFunction.TYPE.write(buffer, value.itemDamage());
         Types.OPTIONAL_HOLDER_SET.write(buffer, value.bypassedBy());
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.blockSound());
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.disableSound());
      }

      public void write(Ops ops, BlocksAttacks value) {
         BlocksAttacks.DamageReduction[] defaultDamageReductions = new BlocksAttacks.DamageReduction[]{
            new BlocksAttacks.DamageReduction(90.0F, null, 0.0F, 1.0F)
         };
         BlocksAttacks.ItemDamageFunction defaultItemDamage = new BlocksAttacks.ItemDamageFunction(1.0F, 0.0F, 1.0F);
         ops.writeMap(
            map -> map.writeOptional("block_delay_seconds", Types.FLOAT, value.blockDelaySeconds(), 0.0F)
               .writeOptional("disable_cooldown_scale", Types.FLOAT, value.disableCooldownScale(), 1.0F)
               .writeOptional("damage_reductions", BlocksAttacks.DamageReduction.ARRAY_TYPE, value.damageReductions(), defaultDamageReductions)
               .writeOptional("item_damage", BlocksAttacks.ItemDamageFunction.TYPE, value.itemDamage(), defaultItemDamage)
               .writeOptional("bypassed_by", new HolderSetType(RegistryKey.of("damage_type")), value.bypassedBy())
               .writeOptional("block_sound", Types.SOUND_EVENT, value.blockSound())
               .writeOptional("disabled_sound", Types.SOUND_EVENT, value.disableSound())
         );
      }
   };

   public BlocksAttacks rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<SoundEvent> blockSound = SoundEvent.rewriteHolder(this.blockSound, Rewritable.soundRewriteFunction(protocol, clientbound));
      Holder<SoundEvent> disableSound = SoundEvent.rewriteHolder(this.disableSound, Rewritable.soundRewriteFunction(protocol, clientbound));
      return new BlocksAttacks(
         this.blockDelaySeconds, this.disableCooldownScale, this.damageReductions, this.itemDamage, this.bypassedBy, blockSound, disableSound
      );
   }

   public record DamageReduction(float horizontalBlockingAngle, @Nullable HolderSet type, float base, float factor) {
      public static final Type<BlocksAttacks.DamageReduction> TYPE = new Type<BlocksAttacks.DamageReduction>(BlocksAttacks.DamageReduction.class) {
         public BlocksAttacks.DamageReduction read(ByteBuf buffer) {
            float horizontalBlockingAngle = buffer.readFloat();
            HolderSet type = Types.OPTIONAL_HOLDER_SET.read(buffer);
            float base = buffer.readFloat();
            float factor = buffer.readFloat();
            return new BlocksAttacks.DamageReduction(horizontalBlockingAngle, type, base, factor);
         }

         public void write(ByteBuf buffer, BlocksAttacks.DamageReduction value) {
            buffer.writeFloat(value.horizontalBlockingAngle());
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.type());
            buffer.writeFloat(value.base());
            buffer.writeFloat(value.factor());
         }

         public void write(Ops ops, BlocksAttacks.DamageReduction value) {
            ops.writeMap(
               map -> map.writeOptional("horizontal_blocking_angle", Types.FLOAT, value.horizontalBlockingAngle(), 90.0F)
                  .writeOptional("type", new HolderSetType(RegistryKey.of("damage_type")), value.type())
                  .write("base", Types.FLOAT, value.base())
                  .write("factor", Types.FLOAT, value.factor())
            );
         }
      };
      public static final ArrayType<BlocksAttacks.DamageReduction> ARRAY_TYPE = new ArrayType<>(TYPE);
   }

   public record ItemDamageFunction(float threshold, float base, float factor) {
      public static final Type<BlocksAttacks.ItemDamageFunction> TYPE = new Type<BlocksAttacks.ItemDamageFunction>(BlocksAttacks.ItemDamageFunction.class) {
         public BlocksAttacks.ItemDamageFunction read(ByteBuf buffer) {
            float threshold = buffer.readFloat();
            float base = buffer.readFloat();
            float factor = buffer.readFloat();
            return new BlocksAttacks.ItemDamageFunction(threshold, base, factor);
         }

         public void write(ByteBuf buffer, BlocksAttacks.ItemDamageFunction value) {
            buffer.writeFloat(value.threshold());
            buffer.writeFloat(value.base());
            buffer.writeFloat(value.factor());
         }

         public void write(Ops ops, BlocksAttacks.ItemDamageFunction value) {
            ops.writeMap(
               map -> map.write("threshold", Types.FLOAT, value.threshold())
                  .write("base", Types.FLOAT, value.base())
                  .write("factor", Types.FLOAT, value.factor())
            );
         }
      };
   }
}
