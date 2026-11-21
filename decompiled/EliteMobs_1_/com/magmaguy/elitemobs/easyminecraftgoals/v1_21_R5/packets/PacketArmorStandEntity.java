package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketTextEntity;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher.c;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;

public class PacketArmorStandEntity extends AbstractPacketEntity<EntityArmorStand> implements PacketModelEntity, PacketTextEntity {
   private ItemStack nmsLeatherHorseArmor;
   private org.bukkit.inventory.ItemStack leatherHorseArmor;
   private EntityArmorStand armorStand;

   public PacketArmorStandEntity(Location location) {
      super(location);
   }

   protected EntityArmorStand createEntity(Location location) {
      return new EntityArmorStand(EntityTypes.g, this.getNMSLevel(location));
   }

   @Override
   public void initializeModel(Location location, String modelID) {
      this.armorStand = this.entity;
      this.armorStand.l(true);
      this.armorStand.v(true);
      this.leatherHorseArmor = new org.bukkit.inventory.ItemStack(Material.LEATHER_HORSE_ARMOR);
      LeatherArmorMeta itemMeta = (LeatherArmorMeta)this.leatherHorseArmor.getItemMeta();
      itemMeta.setItemModel(NamespacedKey.fromString(modelID));
      itemMeta.setColor(Color.WHITE);
      this.leatherHorseArmor.setItemMeta(itemMeta);
      this.nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(this.leatherHorseArmor);
      this.armorStand.a(EnumItemSlot.f, this.nmsLeatherHorseArmor);
   }

   @Override
   public void initializeText(Location location) {
      this.armorStand = this.entity;
      this.armorStand.l(true);
      this.armorStand.v(true);
   }

   @Override
   public void setScale(float scale) {
   }

   @Override
   public void setHorseLeatherArmorColor(Color color) {
      LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)this.leatherHorseArmor.getItemMeta();
      leatherArmorMeta.setColor(color);
      this.leatherHorseArmor.setItemMeta(leatherArmorMeta);
      this.nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(this.leatherHorseArmor);
      this.armorStand.a(EnumItemSlot.f, this.nmsLeatherHorseArmor);
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
         Vector3f rotations = new Vector3f(
            (float)Math.toDegrees(eulerAngle.getX()), (float)Math.toDegrees(eulerAngle.getY()), (float)Math.toDegrees(eulerAngle.getZ())
         );
         this.entity.a(rotations);
         c<Vector3f> dv = c.a(EntityArmorStand.bT, rotations);
         packetBundle.addPacket(new PacketPlayOutEntityMetadata(this.entity.ar(), List.of(dv)), this.getViewersAsPlayers());
      }

      return packetBundle;
   }

   @Override
   public void displayTo(Player player) {
      if (player != null && !this.hasViewer(player.getUniqueId())) {
         super.displayTo(player);
         if (this.nmsLeatherHorseArmor != null) {
            this.sendPacket(
               player, new Packet[]{new PacketPlayOutEntityEquipment(this.entity.ar(), List.of(Pair.of(EnumItemSlot.f, this.nmsLeatherHorseArmor)))}
            );
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
         Vector3f rotations = new Vector3f(
            (float)Math.toDegrees(eulerAngle.getX()), (float)Math.toDegrees(eulerAngle.getY()), (float)Math.toDegrees(eulerAngle.getZ())
         );
         this.entity.a(rotations);
         c<Vector3f> dv = c.a(EntityArmorStand.bT, rotations);
         this.sendPacket(new PacketPlayOutEntityMetadata(this.entity.ar(), List.of(dv)));
      }
   }

   @Override
   public void setText(String text) {
      this.armorStand.p(true);
      this.armorStand.b(CraftChatMessage.fromStringOrNull(text));
      this.sendPacket(this.createEntityDataPacket());
   }

   @Override
   public void setTextVisible(boolean visible) {
      this.armorStand.p(visible);
      this.sendPacket(this.createEntityDataPacket());
   }
}
