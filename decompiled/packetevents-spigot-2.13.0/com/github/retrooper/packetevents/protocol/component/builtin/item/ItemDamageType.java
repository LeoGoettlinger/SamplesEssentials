package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageType;
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemDamageType {
   private MaybeMappedEntity<DamageType> damageType;

   public ItemDamageType(DamageType damageType) {
      this(new MaybeMappedEntity<>(damageType));
   }

   @Obsolete
   public ItemDamageType(MaybeMappedEntity<DamageType> damageType) {
      this.damageType = damageType;
   }

   public static ItemDamageType read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         return new ItemDamageType(DamageType.read(wrapper));
      } else {
         MaybeMappedEntity<DamageType> damageType = MaybeMappedEntity.read(wrapper, DamageTypes.getRegistry(), DamageType::read);
         return new ItemDamageType(damageType);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ItemDamageType component) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         DamageType.write(wrapper, component.damageType.getValueOrThrow());
      } else {
         MaybeMappedEntity.write(wrapper, component.damageType, DamageType::write);
      }
   }

   public MaybeMappedEntity<DamageType> getDamageType() {
      return this.damageType;
   }

   public void setDamageType(DamageType damageType) {
      this.setDamageType(new MaybeMappedEntity<>(damageType));
   }

   public void setDamageType(MaybeMappedEntity<DamageType> damageType) {
      this.damageType = damageType;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         ItemDamageType that = (ItemDamageType)obj;
         return this.damageType.equals(that.damageType);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.damageType);
   }
}
