package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import com.github.jikoo.openinv.internal.spigot26_2.player.OpenPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public class ContentOffHand extends ContentEquipment {
   private ServerPlayer holder;

   public ContentOffHand(ServerPlayer holder) {
      super(holder, EquipmentSlot.OFFHAND);
   }

   @Override
   public void setHolder(@NotNull ServerPlayer holder) {
      super.setHolder(holder);
      this.holder = holder;
   }

   @Override
   public SlotType getSlotType() {
      return SlotType.QUICKBAR;
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new ContentEquipment.SlotEquipment(container, slot, x, y) {
         public void setChanged() {
            if (OpenPlayer.isConnected(ContentOffHand.this.holder.connection)
               && ContentOffHand.this.holder.containerMenu != ContentOffHand.this.holder.inventoryMenu) {
               ContentOffHand.this.holder
                  .connection
                  .send(
                     new ClientboundContainerSetSlotPacket(
                        ContentOffHand.this.holder.inventoryMenu.containerId,
                        ContentOffHand.this.holder.inventoryMenu.incrementStateId(),
                        45,
                        ContentOffHand.this.holder.getOffhandItem()
                     )
                  );
            }
         }
      };
   }
}
