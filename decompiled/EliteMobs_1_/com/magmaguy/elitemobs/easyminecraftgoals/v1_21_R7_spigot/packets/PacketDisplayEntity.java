package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTuning;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.CraftBukkitBridge;
import com.mojang.math.Transformation;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher.c;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.ItemDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class PacketDisplayEntity extends AbstractPacketEntity<ItemDisplay> implements PacketModelEntity {
   private ItemStack carrierItem;
   private net.minecraft.world.item.ItemStack nmsCarrierItem;
   private ItemDisplay itemDisplay;

   public PacketDisplayEntity(Location location) {
      super(location);
   }

   private static Quaternionf eulerToQuaternion(double originalX, double originalY, double originalZ) {
      double yaw = Math.toRadians(originalZ);
      double pitch = Math.toRadians(originalY);
      double roll = Math.toRadians(originalX);
      double cy = Math.cos(yaw * 0.5);
      double sy = Math.sin(yaw * 0.5);
      double cp = Math.cos(pitch * 0.5);
      double sp = Math.sin(pitch * 0.5);
      double cr = Math.cos(roll * 0.5);
      double sr = Math.sin(roll * 0.5);
      double w = cr * cp * cy + sr * sp * sy;
      double x = sr * cp * cy - cr * sp * sy;
      double y = cr * sp * cy + sr * cp * sy;
      double z = cr * cp * sy - sr * sp * cy;
      return new Quaternionf(x, y, z, w);
   }

   protected ItemDisplay createEntity(Location location) {
      return new ItemDisplay(EntityTypes.aw, this.getNMSLevel(location));
   }

   @Override
   public void initializeModel(Location location, String modelID) {
      this.itemDisplay = this.entity;
      this.itemDisplay.b(-1);
      this.itemDisplay.a(0);
      CraftBukkitBridge.setDisplayTeleportDuration(this.itemDisplay, 1);
      this.carrierItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
      ItemMeta meta = this.carrierItem.getItemMeta();
      meta.setItemModel(NamespacedKey.fromString(modelID));
      CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
      cmd.setColors(List.of(Color.WHITE));
      meta.setCustomModelDataComponent(cmd);
      this.carrierItem.setItemMeta(meta);
      this.nmsCarrierItem = CraftBukkitBridge.asNMSCopy(this.carrierItem);
      this.itemDisplay.a(this.nmsCarrierItem);
      this.itemDisplay.x(0.0F);
      this.itemDisplay.y(0.0F);
      this.itemDisplay.b(30.0F);
   }

   @Override
   public void setHorseLeatherArmorColor(Color color) {
      if (this.carrierItem != null) {
         ItemMeta meta = this.carrierItem.getItemMeta();
         CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
         cmd.setColors(List.of(color));
         meta.setCustomModelDataComponent(cmd);
         this.carrierItem.setItemMeta(meta);
         this.nmsCarrierItem = CraftBukkitBridge.asNMSCopy(this.carrierItem);
         this.itemDisplay.a(this.nmsCarrierItem);
      }
   }

   @Override
   public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
      this.move(location);
      Quaternionf quaternionf = eulerToQuaternion(Math.toDegrees(eulerAngle.getX()), Math.toDegrees(eulerAngle.getY()), Math.toDegrees(eulerAngle.getZ()));
      this.rotate(quaternionf);
      this.sendPacketToAll(this.createEntityDataPacket());
   }

   @Override
   public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
      this.generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scale).send();
   }

   @Override
   public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ) {
      this.generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scaleX, scaleY, scaleZ).send();
   }

   @Override
   public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
      AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scale
   ) {
      return this.generateLocationAndRotationAndScalePackets(packetBundle, location, eulerAngle, scale, scale, scale);
   }

   @Override
   public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
      AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ
   ) {
      if (!this.getLocation().getWorld().equals(location.getWorld())) {
         packetBundle.addPacket(this.generateMovePacket(location), this.getViewersAsPlayers());
      } else {
         packetBundle.addPacket(this.generateMovePacket(location), this.getViewersAsPlayers());
      }

      Quaternionf quaternionf = eulerToQuaternion(Math.toDegrees(eulerAngle.getX()), Math.toDegrees(eulerAngle.getY()), Math.toDegrees(eulerAngle.getZ()));
      Transformation transformation = this.getTransformation();
      transformation = new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), quaternionf, new Vector3f(scaleX, scaleY, scaleZ), transformation.h());
      this.entity.a(transformation);
      if (PacketEntityTuning.useDeltaMetadataUpdates) {
         List<c<?>> dirty = this.entity.aD().b();
         if (dirty != null && !dirty.isEmpty()) {
            packetBundle.addPacket(new PacketPlayOutEntityMetadata(this.entity.aA(), dirty), this.getViewersAsPlayers());
         }
      } else {
         packetBundle.addPacket(this.createEntityDataPacket(), this.getViewersAsPlayers());
      }

      return packetBundle;
   }

   @Override
   public void displayTo(Player player) {
      super.displayTo(player);
   }

   @Override
   public void displayTo(UUID player) {
      this.displayTo(Bukkit.getPlayer(player));
   }

   @Override
   public void addViewer(UUID player) {
      this.displayTo(player);
   }

   public Vector3f getScale() {
      Vector3fc scale = this.getTransformation().g();
      return new Vector3f(scale.x(), scale.y(), scale.z());
   }

   @Override
   public void setScale(float scale) {
      this.setScale(new Vector3f(scale, scale, scale));
   }

   public void setScale(Vector3f scale) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.e(), transformation.f(), scale, transformation.h());
      this.setTransformation(newTransformation);
   }

   public Vector3f getTranslation() {
      Vector3fc translation = this.getTransformation().e();
      return new Vector3f(translation.x(), translation.y(), translation.z());
   }

   public void setTranslation(Vector3f translation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(translation, transformation.f(), transformation.g(), transformation.h());
      this.setTransformation(newTransformation);
   }

   public Quaternionf getLeftRotation() {
      Quaternionfc leftRotation = this.getTransformation().f();
      return new Quaternionf(leftRotation.x(), leftRotation.y(), leftRotation.z(), leftRotation.w());
   }

   public void setLeftRotation(Quaternionf rotation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.e(), rotation, transformation.g(), transformation.h());
      this.setTransformation(newTransformation);
   }

   public Quaternionf getRightRotation() {
      Quaternionfc rightRotation = this.getTransformation().h();
      return new Quaternionf(rightRotation.x(), rightRotation.y(), rightRotation.z(), rightRotation.w());
   }

   public void setRightRotation(Quaternionf rotation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.e(), transformation.f(), transformation.g(), rotation);
      this.setTransformation(newTransformation);
   }

   public Transformation getTransformation() {
      Transformation nms = Display.a(this.entity.aD());
      return new Transformation(nms.e(), nms.f(), nms.g(), nms.h());
   }

   private void setTransformation(Transformation transformation) {
      this.entity.a(transformation);
   }

   private void rotate(Quaternionf rotation) {
      if (rotation != null) {
         this.setLeftRotation(rotation);
      }
   }

   @Override
   public void setBillboard(Billboard billboard) {
      this.entity.a(switch (billboard) {
         case FIXED -> BillboardConstraints.a;
         case VERTICAL -> BillboardConstraints.b;
         case HORIZONTAL -> BillboardConstraints.c;
         case CENTER -> BillboardConstraints.d;
         default -> throw new MatchException(null, null);
      });
   }

   @Override
   public void setBrightness(int blockLight, int skyLight) {
      this.entity.a(new Brightness(blockLight, skyLight));
   }

   @Override
   public void setViewRange(float range) {
      this.entity.b(range);
   }

   @Override
   public void setTeleportDuration(int ticks) {
      CraftBukkitBridge.setDisplayTeleportDuration((Display)this.entity, Math.max(0, Math.min(59, ticks)));
   }

   @Override
   public void setTransformation(Vector3f translation, Quaternionf leftRotation, Vector3f scale, int interpolationTicks) {
      this.entity.a(new Transformation(translation, leftRotation, scale, new Quaternionf()));
      this.entity.a(interpolationTicks);
      this.entity.b(-1);
      this.entity.b(0);
   }

   @Override
   public void setItemModel(String modelID, Color tint) {
      if (this.carrierItem != null) {
         ItemMeta meta = this.carrierItem.getItemMeta();
         meta.setItemModel(NamespacedKey.fromString(modelID));
         CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
         cmd.setColors(List.of(tint));
         meta.setCustomModelDataComponent(cmd);
         this.carrierItem.setItemMeta(meta);
         this.nmsCarrierItem = CraftBukkitBridge.asNMSCopy(this.carrierItem);
         this.itemDisplay.a(this.nmsCarrierItem);
      }
   }

   @Override
   public void queueMetadataUpdate(AbstractPacketBundle packetBundle) {
      packetBundle.addPacket(this.createEntityDataPacket(), this.getViewersAsPlayers());
   }
}
