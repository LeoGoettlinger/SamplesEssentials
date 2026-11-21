package com.artillexstudios.axrankmenu.libs.axapi.packetentity;

import com.artillexstudios.axrankmenu.libs.axapi.events.PacketEntityInteractEvent;
import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axrankmenu.libs.axapi.libs.caffeine.caffeine.cache.Caffeine;
import com.artillexstudios.axrankmenu.libs.axapi.libs.caffeine.caffeine.cache.LoadingCache;
import com.artillexstudios.axrankmenu.libs.axapi.libs.caffeine.caffeine.cache.Scheduler;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axrankmenu.libs.axapi.utils.EquipmentSlot;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import java.time.Duration;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PacketEntity {
   LoadingCache<String, Object> placeholderFormatCache = Caffeine.newBuilder()
      .maximumSize(FeatureFlags.HOLOGRAM_PARSED_LINE_CACHE.get().intValue())
      .expireAfterAccess(Duration.ofSeconds(20L))
      .scheduler(Scheduler.systemScheduler())
      .build(parsed -> ComponentSerializer.INSTANCE.toVanilla(StringUtils.format(parsed)));

   void teleport(Location location);

   Location location();

   EntityMeta meta();

   int id();

   int viewDistanceSquared();

   void viewDistance(int blocks);

   void spawn();

   void hide(Player player);

   void show(Player player);

   void setVisibleByDefault(boolean visible);

   void setItem(EquipmentSlot slot, WrappedItemStack item);

   WrappedItemStack getItem(EquipmentSlot slot);

   void sendChanges();

   void removePairing(Player player);

   void addPairing(Player player);

   boolean canSee(Player player);

   void remove();

   void onInteract(Consumer<PacketEntityInteractEvent> event);

   void callInteract(PacketEntityInteractEvent event);

   void ride(int entityId);

   void unRide(int entityId);

   int riddenEntity();

   void rotate(float yaw, float pitch);

   void rotateHead(float yaw);

   void update();
}
