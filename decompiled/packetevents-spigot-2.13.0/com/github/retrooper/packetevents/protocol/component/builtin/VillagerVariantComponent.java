package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerType;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class VillagerVariantComponent {
   private VillagerType villagerType;

   public VillagerVariantComponent(VillagerType villagerType) {
      this.villagerType = villagerType;
   }

   public static VillagerVariantComponent read(PacketWrapper<?> wrapper) {
      VillagerType type = wrapper.readMappedEntity(VillagerTypes.getRegistry());
      return new VillagerVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, VillagerVariantComponent component) {
      wrapper.writeMappedEntity(component.villagerType);
   }

   public VillagerType getVillagerType() {
      return this.villagerType;
   }

   public void setVillagerType(VillagerType villagerType) {
      this.villagerType = villagerType;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof VillagerVariantComponent)) {
         return false;
      } else {
         VillagerVariantComponent that = (VillagerVariantComponent)obj;
         return this.villagerType.equals(that.villagerType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.villagerType);
   }
}
