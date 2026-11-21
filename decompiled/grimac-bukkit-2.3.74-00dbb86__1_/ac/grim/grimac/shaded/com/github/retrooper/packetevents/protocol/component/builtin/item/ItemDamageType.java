package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin.item;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.damagetype.DamageType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemDamageType {
   private MaybeMappedEntity<DamageType> damageType;

   public ItemDamageType(DamageType damageType) {
      this(new MaybeMappedEntity<>(damageType));
   }

   @ApiStatus.Obsolete
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
