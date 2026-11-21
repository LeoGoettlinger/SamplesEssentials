package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTuning;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import com.mojang.math.Transformation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
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
   private static final EntityDataAccessor<Quaternionfc> RIGHT_ROTATION = resolveRightRotationAccessor();
   private ItemStack carrierItem;
   private net.minecraft.world.item.ItemStack nmsCarrierItem;
   private ItemDisplay itemDisplay;

   private static EntityDataAccessor<Quaternionfc> resolveRightRotationAccessor() {
      try {
         Field field = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
         field.setAccessible(true);
         return (EntityDataAccessor<Quaternionfc>)field.get(null);
      } catch (ReflectiveOperationException var1) {
         throw new IllegalStateException("Unable to access the display right rotation", var1);
      }
   }

   public PacketDisplayEntity(Location location) {
      super(location);
   }

   private static Quaternionf eulerToQuaternion(double rotationX, double rotationY, double rotationZ) {
      double cy = Math.cos(rotationZ * 0.5);
      double sy = Math.sin(rotationZ * 0.5);
      double cp = Math.cos(rotationY * 0.5);
      double sp = Math.sin(rotationY * 0.5);
      double cr = Math.cos(rotationX * 0.5);
      double sr = Math.sin(rotationX * 0.5);
      double w = cr * cp * cy + sr * sp * sy;
      double x = sr * cp * cy - cr * sp * sy;
      double y = cr * sp * cy + sr * cp * sy;
      double z = cr * cp * sy - sr * sp * cy;
      return new Quaternionf(x, y, z, w);
   }

   protected ItemDisplay createEntity(Location location) {
      return new ItemDisplay(PacketEntityTypes.ITEM_DISPLAY, this.getNMSLevel(location));
   }

   @Override
   public void initializeModel(Location location, String modelID) {
      this.itemDisplay = this.entity;
      this.itemDisplay.setTransformationInterpolationDelay(-1);
      this.itemDisplay.setTransformationInterpolationDuration(0);
      CraftBukkitBridge.setDisplayTeleportDuration(this.itemDisplay, 1);
      this.carrierItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
      ItemMeta meta = this.carrierItem.getItemMeta();
      meta.setItemModel(NamespacedKey.fromString(modelID));
      CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
      cmd.setColors(List.of(Color.WHITE));
      meta.setCustomModelDataComponent(cmd);
      this.carrierItem.setItemMeta(meta);
      this.nmsCarrierItem = CraftBukkitBridge.asNMSCopy(this.carrierItem);
      this.itemDisplay.setItemStack(this.nmsCarrierItem);
      this.itemDisplay.setWidth(0.0F);
      this.itemDisplay.setHeight(0.0F);
      this.itemDisplay.setViewRange(30.0F);
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
         this.itemDisplay.setItemStack(this.nmsCarrierItem);
      }
   }

   @Override
   public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
      this.move(location);
      Quaternionf quaternionf = eulerToQuaternion(eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
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
      List<Player> updateViewers = this.getViewersAsPlayers();
      packetBundle.addPacket(this.generateMovePacket(location), updateViewers);
      Quaternionf quaternionf = eulerToQuaternion(eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
      Transformation transformation = new Transformation(
         new Vector3f(0.0F, 0.0F, 0.0F), quaternionf, new Vector3f(scaleX, scaleY, scaleZ), (Quaternionfc)this.entity.getEntityData().get(RIGHT_ROTATION)
      );
      this.entity.setTransformation(transformation);
      if (PacketEntityTuning.useDeltaMetadataUpdates) {
         Packet<?> dirty = this.createDirtyEntityDataPacket();
         if (dirty != null) {
            packetBundle.addPacket(dirty, updateViewers);
         }
      } else {
         packetBundle.addPacket(this.createEntityDataPacket(), updateViewers);
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
      Vector3fc scale = this.getTransformation().scale();
      return new Vector3f(scale.x(), scale.y(), scale.z());
   }

   @Override
   public void setScale(float scale) {
      this.setScale(new Vector3f(scale, scale, scale));
   }

   public void setScale(Vector3f scale) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.translation(), transformation.leftRotation(), scale, transformation.rightRotation());
      this.setTransformation(newTransformation);
   }

   public Vector3f getTranslation() {
      Vector3fc translation = this.getTransformation().translation();
      return new Vector3f(translation.x(), translation.y(), translation.z());
   }

   public void setTranslation(Vector3f translation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(translation, transformation.leftRotation(), transformation.scale(), transformation.rightRotation());
      this.setTransformation(newTransformation);
   }

   public Quaternionf getLeftRotation() {
      Quaternionfc leftRotation = this.getTransformation().leftRotation();
      return new Quaternionf(leftRotation.x(), leftRotation.y(), leftRotation.z(), leftRotation.w());
   }

   public void setLeftRotation(Quaternionf rotation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.translation(), rotation, transformation.scale(), transformation.rightRotation());
      this.setTransformation(newTransformation);
   }

   public Quaternionf getRightRotation() {
      Quaternionfc rightRotation = (Quaternionfc)this.entity.getEntityData().get(RIGHT_ROTATION);
      return new Quaternionf(rightRotation.x(), rightRotation.y(), rightRotation.z(), rightRotation.w());
   }

   public void setRightRotation(Quaternionf rotation) {
      Transformation transformation = this.getTransformation();
      Transformation newTransformation = new Transformation(transformation.translation(), transformation.leftRotation(), transformation.scale(), rotation);
      this.setTransformation(newTransformation);
   }

   public Transformation getTransformation() {
      return Display.createTransformation(this.entity.getEntityData());
   }

   private void setTransformation(Transformation transformation) {
      this.entity.setTransformation(transformation);
   }

   private void rotate(Quaternionf rotation) {
      if (rotation != null) {
         this.setLeftRotation(rotation);
      }
   }

   @Override
   public void setBillboard(Billboard billboard) {
      this.entity.setBillboardConstraints(switch (billboard) {
         case FIXED -> BillboardConstraints.FIXED;
         case VERTICAL -> BillboardConstraints.VERTICAL;
         case HORIZONTAL -> BillboardConstraints.HORIZONTAL;
         case CENTER -> BillboardConstraints.CENTER;
         default -> throw new MatchException(null, null);
      });
   }

   @Override
   public void setBrightness(int blockLight, int skyLight) {
      this.entity.setBrightnessOverride(new Brightness(blockLight, skyLight));
   }

   @Override
   public void setViewRange(float range) {
      this.entity.setViewRange(range);
   }

   @Override
   public void setTeleportDuration(int ticks) {
      CraftBukkitBridge.setDisplayTeleportDuration((Display)this.entity, Math.max(0, Math.min(59, ticks)));
   }

   @Override
   public void setTransformation(Vector3f translation, Quaternionf leftRotation, Vector3f scale, int interpolationTicks) {
      this.entity.setTransformation(new Transformation(translation, leftRotation, scale, new Quaternionf()));
      this.entity.setTransformationInterpolationDuration(interpolationTicks);
      this.entity.setTransformationInterpolationDelay(-1);
      this.entity.setTransformationInterpolationDelay(0);
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
         this.itemDisplay.setItemStack(this.nmsCarrierItem);
      }
   }

   @Override
   public void queueMetadataUpdate(AbstractPacketBundle packetBundle) {
      Packet<?> dirty = this.createDirtyEntityDataPacket();
      if (dirty != null) {
         packetBundle.addPacket(dirty, this.getViewersAsPlayers());
      }
   }
}
