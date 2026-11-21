package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
   public static final Type<Weapon> TYPE = new Type<Weapon>(Weapon.class) {
      public Weapon read(ByteBuf buffer) {
         int damagePerAttack = Types.VAR_INT.readPrimitive(buffer);
         float disableBlockingForSeconds = buffer.readFloat();
         return new Weapon(damagePerAttack, disableBlockingForSeconds);
      }

      public void write(ByteBuf buffer, Weapon value) {
         Types.VAR_INT.writePrimitive(buffer, value.itemDamagePerAttack());
         buffer.writeFloat(value.disableBlockingForSeconds());
      }

      public void write(Ops ops, Weapon weapon) {
         ops.writeMap(
            map -> map.writeOptional("item_damage_per_attack", Types.INT, weapon.itemDamagePerAttack, 1)
               .writeOptional("disable_blocking_for_seconds", Types.FLOAT, weapon.disableBlockingForSeconds, 0.0F)
         );
      }
   };
}
