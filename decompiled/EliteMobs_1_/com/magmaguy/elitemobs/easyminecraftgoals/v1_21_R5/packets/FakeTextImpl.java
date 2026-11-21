package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.TrackedPacketEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class FakeTextImpl implements FakeText, TrackedPacketEntity {
   private final FakeTextSettings settings;
   private Location location;
   private String currentText;
   private boolean visible = true;
   private final UUID uniqueId = UUID.randomUUID();
   private final Map<UUID, PacketEntityInterface> playerEntities = new ConcurrentHashMap<>();
   private final Set<UUID> bedrockPlayers = ConcurrentHashMap.newKeySet();
   private final List<Runnable> removeCallbacks = new ArrayList<>();
   private int currentVehicleId = -1;
   private Entity vehicleEntity = null;
   private boolean autoTracked = false;
   private boolean valid = true;

   public FakeTextImpl(Location location, FakeTextSettings settings) {
      this.location = location.clone();
      this.settings = new FakeTextSettings(settings);
      this.currentText = settings.getText();
   }

   @Override
   public void setText(String text) {
      this.currentText = text;

      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         UUID uuid = entry.getKey();
         PacketEntityInterface entity = entry.getValue();
         if (this.bedrockPlayers.contains(uuid)) {
            EntityArmorStand as = (EntityArmorStand)((AbstractPacketEntity)entity).getNMSEntity();
            as.p(true);
            as.b(CraftChatMessage.fromStringOrNull(text));
         } else {
            TextDisplay td = entity.getBukkitEntity();
            td.setText(text);
         }

         entity.syncMetadata();
      }
   }

   @Override
   public String getText() {
      return this.currentText;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;

      for (PacketEntityInterface e : this.playerEntities.values()) {
         e.setVisible(visible);
      }
   }

   @Override
   public void displayTo(Player player) {
      if (player != null) {
         UUID uuid = player.getUniqueId();
         if (!this.playerEntities.containsKey(uuid)) {
            boolean isBedrock = BedrockChecker.isBedrock(player);
            PacketEntityInterface entity = isBedrock ? this.createArmorStandEntity(this.location) : this.createTextDisplayEntity(this.location);
            if (isBedrock) {
               this.bedrockPlayers.add(uuid);
            }

            entity.teleport(this.location);
            this.playerEntities.put(uuid, entity);
            entity.displayTo(uuid);
            if (this.currentVehicleId != -1) {
               entity.mountTo(this.currentVehicleId);
            }
         }
      }
   }

   @Override
   public void displayTo(UUID uuid) {
      Player p = Bukkit.getPlayer(uuid);
      if (p != null) {
         this.displayTo(p);
      }
   }

   @Override
   public void hideFrom(Player player) {
      if (player != null) {
         this.hideFrom(player.getUniqueId());
      }
   }

   @Override
   public void hideFrom(UUID uuid) {
      PacketEntityInterface e = this.playerEntities.remove(uuid);
      if (e != null) {
         e.remove();
      }

      this.bedrockPlayers.remove(uuid);
   }

   @Override
   public void teleport(Location loc) {
      this.location = loc.clone();

      for (PacketEntityInterface e : this.playerEntities.values()) {
         e.teleport(loc);
      }
   }

   @Override
   public Location getLocation() {
      return this.location.clone();
   }

   @Override
   public void remove() {
      for (PacketEntityInterface e : this.playerEntities.values()) {
         e.remove();
      }

      this.playerEntities.clear();
      this.bedrockPlayers.clear();
      this.removeCallbacks.forEach(Runnable::run);
   }

   @Override
   public boolean hasViewers() {
      return !this.playerEntities.isEmpty();
   }

   @Override
   public void setTextOpacity(byte opacity) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            td.setTextOpacity(opacity);
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void setScale(float scale) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            Transformation t = td.getTransformation();
            td.setTransformation(new Transformation(t.getTranslation(), t.getLeftRotation(), new Vector3f(scale, scale, scale), t.getRightRotation()));
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void setBackgroundColor(Color color) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            td.setBackgroundColor(color);
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void setBackgroundColor(int argb) {
      this.setBackgroundColor(Color.fromARGB(argb));
   }

   @Override
   public void setShadowed(boolean shadow) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            td.setShadowed(shadow);
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void setSeeThrough(boolean seeThrough) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            td.setSeeThrough(seeThrough);
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void setBillboard(Billboard billboard) {
      for (Entry<UUID, PacketEntityInterface> entry : this.playerEntities.entrySet()) {
         if (!this.bedrockPlayers.contains(entry.getKey())) {
            TextDisplay td = entry.getValue().getBukkitEntity();
            td.setBillboard(billboard);
            entry.getValue().syncMetadata();
         }
      }
   }

   @Override
   public void mountTo(Entity vehicle) {
      if (vehicle != null) {
         this.currentVehicleId = vehicle.getEntityId();

         for (PacketEntityInterface entity : this.playerEntities.values()) {
            entity.mountTo(this.currentVehicleId);
         }
      }
   }

   @Override
   public void dismount() {
      if (this.currentVehicleId != -1) {
         for (PacketEntityInterface entity : this.playerEntities.values()) {
            entity.dismount();
         }

         this.currentVehicleId = -1;
         this.vehicleEntity = null;
      }
   }

   @Override
   public void attachTo(Entity vehicle) {
      if (vehicle != null) {
         this.vehicleEntity = vehicle;
         this.currentVehicleId = vehicle.getEntityId();
         this.autoTracked = true;
         this.valid = true;
         PacketEntityTracker.getInstance().register(this);
      }
   }

   @Override
   public void detach() {
      if (this.autoTracked) {
         PacketEntityTracker.getInstance().unregister(this);

         for (UUID viewerUUID : new HashSet<>(this.playerEntities.keySet())) {
            Player player = Bukkit.getPlayer(viewerUUID);
            if (player != null) {
               this.hideFrom(player);
            }
         }

         this.dismount();
         this.autoTracked = false;
         this.valid = false;
      }
   }

   @Override
   public Entity getVehicle() {
      return this.vehicleEntity;
   }

   @Override
   public boolean isAutoTracked() {
      return this.autoTracked;
   }

   @Override
   public Location getTrackingLocation() {
      return this.vehicleEntity != null && this.vehicleEntity.isValid() ? this.vehicleEntity.getLocation() : this.location;
   }

   @Override
   public World getWorld() {
      return this.vehicleEntity != null && this.vehicleEntity.isValid()
         ? this.vehicleEntity.getWorld()
         : (this.location != null ? this.location.getWorld() : null);
   }

   @Override
   public void showToPlayer(Player player) {
      this.displayTo(player);
   }

   @Override
   public void hideFromPlayer(Player player) {
      this.hideFrom(player);
   }

   @Override
   public boolean isVisibleTo(Player player) {
      return player != null && this.playerEntities.containsKey(player.getUniqueId());
   }

   @Override
   public boolean canBeSeenBy(Player player) {
      return this.settings.canBeSeenBy(player);
   }

   @Override
   public Set<UUID> getCurrentViewers() {
      return new HashSet<>(this.playerEntities.keySet());
   }

   @Override
   public boolean isValid() {
      return this.valid && (this.vehicleEntity == null || this.vehicleEntity.isValid());
   }

   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Override
   public void remount() {
      if (this.vehicleEntity != null && this.vehicleEntity.isValid()) {
         this.currentVehicleId = this.vehicleEntity.getEntityId();

         for (PacketEntityInterface entity : this.playerEntities.values()) {
            entity.mountTo(this.currentVehicleId);
         }
      }
   }

   public void addRemoveCallback(Runnable callback) {
      this.removeCallbacks.add(callback);
   }

   private PacketEntityInterface createTextDisplayEntity(Location location) {
      FakeTextImpl.TextDisplayPacketEntity entity = new FakeTextImpl.TextDisplayPacketEntity(location);
      TextDisplay td = entity.getBukkitEntity();
      td.setText(this.currentText);
      td.setBillboard(this.settings.getBillboard());
      if (this.settings.hasBackgroundColor()) {
         td.setBackgroundColor(
            this.settings.getBackgroundColor() != null ? this.settings.getBackgroundColor() : Color.fromARGB(this.settings.getBackgroundArgb())
         );
      }

      if (this.settings.getTextOpacity() != -1) {
         td.setTextOpacity(this.settings.getTextOpacity());
      }

      td.setShadowed(this.settings.hasShadow());
      td.setSeeThrough(this.settings.isSeeThrough());
      td.setLineWidth(this.settings.getLineWidth());
      td.setViewRange(this.settings.getViewRange());
      td.setTeleportDuration(this.settings.getTeleportDuration());
      switch (this.settings.getAlignment()) {
         case LEFT:
            td.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.LEFT);
            break;
         case RIGHT:
            td.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.RIGHT);
            break;
         default:
            td.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER);
      }

      if (this.settings.getScale() != 1.0F || this.settings.hasTranslation()) {
         Transformation t = td.getTransformation();
         Vector3f translation = this.settings.hasTranslation()
            ? new Vector3f(this.settings.getTranslationX(), this.settings.getTranslationY(), this.settings.getTranslationZ())
            : t.getTranslation();
         Vector3f scale = this.settings.getScale() != 1.0F
            ? new Vector3f(this.settings.getScale(), this.settings.getScale(), this.settings.getScale())
            : t.getScale();
         td.setTransformation(new Transformation(translation, t.getLeftRotation(), scale, t.getRightRotation()));
      }

      entity.syncMetadata();
      return entity;
   }

   private PacketEntityInterface createArmorStandEntity(Location location) {
      FakeTextImpl.ArmorStandTextEntity entity = new FakeTextImpl.ArmorStandTextEntity(location);
      EntityArmorStand as = entity.getNMSEntity();
      as.l(true);
      as.v(true);
      as.p(true);
      as.b(CraftChatMessage.fromStringOrNull(this.currentText));
      entity.syncMetadata();
      return entity;
   }

   private static class ArmorStandTextEntity extends AbstractPacketEntity<EntityArmorStand> {
      public ArmorStandTextEntity(Location location) {
         super(location);
      }

      protected EntityArmorStand createEntity(Location location) {
         return new EntityArmorStand(EntityTypes.g, ((CraftWorld)location.getWorld()).getHandle());
      }
   }

   private static class TextDisplayPacketEntity extends AbstractPacketEntity<net.minecraft.world.entity.Display.TextDisplay> {
      public TextDisplayPacketEntity(Location location) {
         super(location);
      }

      protected net.minecraft.world.entity.Display.TextDisplay createEntity(Location location) {
         return new net.minecraft.world.entity.Display.TextDisplay(EntityTypes.bx, ((CraftWorld)location.getWorld()).getHandle());
      }
   }
}
