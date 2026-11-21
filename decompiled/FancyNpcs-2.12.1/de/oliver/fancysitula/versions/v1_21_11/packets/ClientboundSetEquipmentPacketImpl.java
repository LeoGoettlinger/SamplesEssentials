package de.oliver.fancysitula.versions.v1_21_11.packets;

import com.mojang.datafixers.util.Pair;
import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetEquipmentPacket;
import de.oliver.fancysitula.api.utils.FS_EquipmentSlot;
import de.oliver.fancysitula.versions.v1_21_11.utils.VanillaPlayerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ClientboundSetEquipmentPacketImpl extends FS_ClientboundSetEquipmentPacket {
   public ClientboundSetEquipmentPacketImpl(int entityId, Map<FS_EquipmentSlot, ItemStack> equipment) {
      super(entityId, equipment);
   }

   @Override
   public Object createPacket() {
      List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> slots = new ArrayList<>();

      for (Entry<FS_EquipmentSlot, ItemStack> entry : this.equipment.entrySet()) {
         EquipmentSlot equipmentSlot = EquipmentSlot.byName(entry.getKey().name().toLowerCase());
         net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(entry.getValue());
         slots.add(Pair.of(equipmentSlot, itemStack));
      }

      return new ClientboundSetEquipmentPacket(this.entityId, slots);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundSetEquipmentPacket packet = (ClientboundSetEquipmentPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
