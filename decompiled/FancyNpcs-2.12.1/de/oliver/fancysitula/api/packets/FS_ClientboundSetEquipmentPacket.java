package de.oliver.fancysitula.api.packets;

import de.oliver.fancysitula.api.utils.FS_EquipmentSlot;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public abstract class FS_ClientboundSetEquipmentPacket extends FS_ClientboundPacket {
   protected int entityId;
   protected Map<FS_EquipmentSlot, ItemStack> equipment;

   public FS_ClientboundSetEquipmentPacket(int entityId, Map<FS_EquipmentSlot, ItemStack> equipment) {
      this.entityId = entityId;
      this.equipment = equipment;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public Map<FS_EquipmentSlot, ItemStack> getEquipment() {
      return this.equipment;
   }

   public void setEquipment(Map<FS_EquipmentSlot, ItemStack> equipment) {
      this.equipment = equipment;
   }
}
