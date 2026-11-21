package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record Bee(EntityData entityData, int ticksInHive, int minTicksInHive) implements Copyable {
   public static final Type<Bee> TYPE1_20_5 = new Type<Bee>(Bee.class) {
      public Bee read(ByteBuf buffer) {
         CompoundTag entityData = Types.COMPOUND_TAG.read(buffer);
         int ticksInHive = Types.VAR_INT.readPrimitive(buffer);
         int minTicksInHive = Types.VAR_INT.readPrimitive(buffer);
         return new Bee(new EntityData(-1, entityData), ticksInHive, minTicksInHive);
      }

      public void write(ByteBuf buffer, Bee value) {
         Types.COMPOUND_TAG.write(buffer, value.entityData.tag());
         Types.VAR_INT.writePrimitive(buffer, value.ticksInHive);
         Types.VAR_INT.writePrimitive(buffer, value.minTicksInHive);
      }

      public void write(Ops ops, Bee value) {
         ops.writeMap(
            map -> map.writeOptional("entity_data", Types.COMPOUND_TAG, value.entityData.tag(), new CompoundTag())
               .write("ticks_in_hive", Types.INT, value.ticksInHive)
               .write("min_ticks_in_hive", Types.INT, value.minTicksInHive)
         );
      }
   };
   public static final Type<Bee[]> ARRAY_TYPE1_20_5 = new ArrayType<>(TYPE1_20_5);
   public static final Type<Bee> TYPE1_21_9 = new Type<Bee>(Bee.class) {
      public Bee read(ByteBuf buffer) {
         EntityData entityData = EntityData.TYPE.read(buffer);
         int ticksInHive = Types.VAR_INT.readPrimitive(buffer);
         int minTicksInHive = Types.VAR_INT.readPrimitive(buffer);
         return new Bee(entityData, ticksInHive, minTicksInHive);
      }

      public void write(ByteBuf buffer, Bee value) {
         EntityData.TYPE.write(buffer, value.entityData);
         Types.VAR_INT.writePrimitive(buffer, value.ticksInHive);
         Types.VAR_INT.writePrimitive(buffer, value.minTicksInHive);
      }

      public void write(Ops ops, Bee value) {
         ops.writeMap(
            map -> map.write("entity_data", EntityData.TYPE, value.entityData)
               .write("ticks_in_hive", Types.INT, value.ticksInHive)
               .write("min_ticks_in_hive", Types.INT, value.minTicksInHive)
         );
      }
   };
   public static final Type<Bee[]> ARRAY_TYPE1_21_9 = new ArrayType<>(TYPE1_21_9);

   public Bee(CompoundTag entityData, int ticksInHive, int minTicksInHive) {
      this(new EntityData(-1, entityData), ticksInHive, minTicksInHive);
   }

   public Bee copy() {
      return new Bee(this.entityData.copy(), this.ticksInHive, this.minTicksInHive);
   }
}
