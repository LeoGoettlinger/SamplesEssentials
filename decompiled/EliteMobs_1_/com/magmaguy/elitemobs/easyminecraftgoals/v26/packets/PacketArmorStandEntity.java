package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketTextEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;

public class PacketArmorStandEntity extends AbstractPacketEntity<ArmorStand> implements PacketModelEntity, PacketTextEntity {
   private ItemStack nmsLeatherHorseArmor;
   private org.bukkit.inventory.ItemStack leatherHorseArmor;
   private ArmorStand armorStand;

   public PacketArmorStandEntity(Location location) {
      super(location);
   }

   protected ArmorStand createEntity(Location location) {
      return new ArmorStand(PacketEntityTypes.ARMOR_STAND, this.getNMSLevel(location));
   }

   @Override
   public void initializeModel(Location location, String modelID) {
      this.armorStand = this.entity;
      this.armorStand.setInvisible(true);
      this.armorStand.setMarker(true);
      this.leatherHorseArmor = new org.bukkit.inventory.ItemStack(Material.LEATHER_HORSE_ARMOR);
      LeatherArmorMeta itemMeta = (LeatherArmorMeta)this.leatherHorseArmor.getItemMeta();
      itemMeta.setItemModel(NamespacedKey.fromString(modelID));
      itemMeta.setColor(Color.WHITE);
      this.leatherHorseArmor.setItemMeta(itemMeta);
      this.nmsLeatherHorseArmor = CraftBukkitBridge.asNMSCopy(this.leatherHorseArmor);
      this.armorStand.setItemSlot(EquipmentSlot.HEAD, this.nmsLeatherHorseArmor);
   }

   @Override
   public void initializeText(Location location) {
      this.armorStand = this.entity;
      this.armorStand.setInvisible(true);
      this.armorStand.setMarker(true);
   }

   @Override
   public void setScale(float scale) {
   }

   @Override
   public void setHorseLeatherArmorColor(Color color) {
      LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)this.leatherHorseArmor.getItemMeta();
      leatherArmorMeta.setColor(color);
      this.leatherHorseArmor.setItemMeta(leatherArmorMeta);
      this.nmsLeatherHorseArmor = CraftBukkitBridge.asNMSCopy(this.leatherHorseArmor);
      this.armorStand.setItemSlot(EquipmentSlot.HEAD, this.nmsLeatherHorseArmor);
   }

   @Override
   public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
      this.move(location);
      this.rotate(eulerAngle);
   }

   @Override
   public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
      this.sendLocationAndRotationPacket(location, eulerAngle);
   }

   @Override
   public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
      AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scale
   ) {
      packetBundle.addPacket(this.generateMovePacket(location), this.getViewersAsPlayers());
      if (eulerAngle != null) {
         Rotations rotations = new Rotations(
            (float)Math.toDegrees(eulerAngle.getX()), (float)Math.toDegrees(eulerAngle.getY()), (float)Math.toDegrees(eulerAngle.getZ())
         );
         this.entity.setHeadPose(rotations);
         DataValue<Rotations> dv = DataValue.create(ArmorStand.DATA_HEAD_POSE, rotations);
         packetBundle.addPacket(new ClientboundSetEntityDataPacket(this.entity.getId(), List.of(dv)), this.getViewersAsPlayers());
      }

      return packetBundle;
   }

   @Override
   public void displayTo(Player player) {
      if (player != null && !this.hasViewer(player.getUniqueId())) {
         super.displayTo(player);
         if (this.nmsLeatherHorseArmor != null) {
            this.sendPacket(
               player, new Packet[]{new ClientboundSetEquipmentPacket(this.entity.getId(), List.of(Pair.of(EquipmentSlot.HEAD, this.nmsLeatherHorseArmor)))}
            );
         }
      }
   }

   @Override
   public void displayTo(Player player, AbstractPacketBundle packetBundle) {
      if (player != null && !this.hasViewer(player.getUniqueId())) {
         super.displayTo(player, packetBundle);
         if (player != null && packetBundle != null) {
            if (this.nmsLeatherHorseArmor != null) {
               packetBundle.addPacket(
                  new ClientboundSetEquipmentPacket(this.entity.getId(), List.of(Pair.of(EquipmentSlot.HEAD, this.nmsLeatherHorseArmor))), List.of(player)
               );
            }
         }
      }
   }

   @Override
   public void displayTo(UUID player) {
      this.displayTo(Bukkit.getPlayer(player));
   }

   @Override
   public void addViewer(UUID player) {
      this.displayTo(player);
   }

   private void rotate(EulerAngle eulerAngle) {
      if (eulerAngle != null) {
         Rotations rotations = new Rotations(
            (float)Math.toDegrees(eulerAngle.getX()), (float)Math.toDegrees(eulerAngle.getY()), (float)Math.toDegrees(eulerAngle.getZ())
         );
         this.entity.setHeadPose(rotations);
         DataValue<Rotations> dv = DataValue.create(ArmorStand.DATA_HEAD_POSE, rotations);
         this.sendPacket(new ClientboundSetEntityDataPacket(this.entity.getId(), List.of(dv)));
      }
   }

   @Override
   public void setText(String text) {
      this.armorStand.setCustomNameVisible(true);
      this.armorStand.setCustomName(CraftBukkitBridge.fromLegacyText(text));
      this.sendPacket(this.createEntityDataPacket());
   }

   @Override
   public void setTextVisible(boolean visible) {
      this.armorStand.setCustomNameVisible(visible);
      this.sendPacket(this.createEntityDataPacket());
   }
}
