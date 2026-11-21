package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface PacketEntityInterface {
   void addRemoveCallback(Runnable var1);

   void displayTo(UUID var1);

   void hideFrom(UUID var1);

   void remove();

   void setVisible(boolean var1);

   Location getLocation();

   UUID getUniqueId();

   void teleport(Location var1);

   default void teleport(Location location, AbstractPacketBundle bundle) {
      this.teleport(location);
   }

   default void mountTo(int vehicleEntityId) {
   }

   default void dismount() {
   }

   default int getEntityId() {
      return -1;
   }

   void addViewer(UUID var1);

   void removeViewer(UUID var1);

   boolean hasViewers();

   AbstractPacketBundle createPacketBundle();

   default void syncMetadata() {
   }

   default <B extends Entity> B getBukkitEntity() {
      throw new UnsupportedOperationException("getBukkitEntity is only supported in Minecraft 1.21.11+");
   }

   default void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
   }

   default void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
   }

   default void setContextualRightClickCallback(PacketInteractionCallback callback) {
   }

   default void setContextualLeftClickCallback(PacketInteractionCallback callback) {
   }

   default void handleInteraction(Player player, boolean isAttack) {
   }

   default void handleInteraction(Player player, PacketInteractionContext context) {
      this.handleInteraction(player, context.isAttack());
   }
}
