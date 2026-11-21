package dev.cwhead.GravesX.api.grave;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.manager.CacheManager;
import com.ranull.graves.manager.DataManager;
import com.ranull.graves.manager.EntityManager;
import com.ranull.graves.manager.GraveManager;
import com.ranull.graves.manager.IntegrationManager;
import com.ranull.graves.manager.LocationManager;
import com.ranull.graves.manager.VersionManager;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.util.UtilAPI;
import dev.cwhead.GravesX.api.world.LocationAPI;
import dev.cwhead.GravesX.event.GraveBlockPlaceEvent;
import dev.cwhead.GravesX.event.GraveCreateEvent;
import dev.cwhead.GravesX.event.GraveProtectionCreateEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveCreationAPI {
   private final Graves plugin;
   private final LocationAPI world;
   private final UtilAPI util;
   private final GraveManagementAPI manage;

   public GraveCreationAPI(Graves plugin, LocationAPI world, UtilAPI util, GraveManagementAPI manage) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.world = Objects.requireNonNull(world, "world");
      this.util = Objects.requireNonNull(util, "util");
      this.manage = Objects.requireNonNull(manage, "manage");
   }

   public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, long timeAliveRemaining) {
      this.createGrave(victim, null, killerEntityType, null, null, null, 0, timeAliveRemaining, null, false, 0L);
   }

   public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, int experience, long timeAliveRemaining) {
      this.createGrave(victim, null, killerEntityType, null, null, null, experience, timeAliveRemaining, null, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining
   ) {
      this.createGrave(victim, null, killerEntityType, null, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      boolean graveProtection,
      long graveProtectionTime
   ) {
      this.createGrave(
         victim, null, killerEntityType, null, equipmentMap, itemStackList, experience, timeAliveRemaining, null, graveProtection, graveProtectionTime
      );
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause
   ) {
      this.createGrave(victim, null, killerEntityType, null, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause,
      boolean graveProtection,
      long graveProtectionTime
   ) {
      this.createGrave(
         victim, null, killerEntityType, null, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, graveProtection, graveProtectionTime
      );
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause
   ) {
      this.createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause,
      boolean graveProtection,
      long graveProtectionTime
   ) {
      this.createGrave(
         victim,
         null,
         killerEntityType,
         locationDeath,
         equipmentMap,
         itemStackList,
         experience,
         timeAliveRemaining,
         damageCause,
         graveProtection,
         graveProtectionTime
      );
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining
   ) {
      this.createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable Entity killer,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining
   ) {
      this.createGrave(victim, killer, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0L);
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable Entity killer,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      boolean graveProtection,
      long graveProtectionTime
   ) {
      this.createGrave(
         victim,
         killer,
         killerEntityType,
         locationDeath,
         equipmentMap,
         itemStackList,
         experience,
         timeAliveRemaining,
         null,
         graveProtection,
         graveProtectionTime
      );
   }

   public void createGrave(
      @NotNull Entity victim,
      @Nullable Entity killer,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause,
      boolean graveProtection,
      long graveProtectionTime
   ) {
      GraveManager graveManager = this.plugin.getGraveManager();
      DataManager dataManager = this.plugin.getDataManager();
      IntegrationManager integrationManager = this.plugin.getIntegrationManager();
      VersionManager versionManager = this.plugin.getVersionManager();
      LocationManager locationManager = this.plugin.getLocationManager();
      EntityManager entityManager = this.plugin.getEntityManager();
      CacheManager cacheManager = this.plugin.getCacheManager();
      Map<Location, BlockData.BlockType> locationMap = new HashMap<>();
      Grave grave = graveManager.createGrave(victim, itemStackList);
      long defaultTimeAlive = this.plugin.getConfigManager().getConfigSection("grave.time", grave).getLong("grave.time");
      long trueTimeAliveRemaining = timeAliveRemaining > 0L ? timeAliveRemaining : defaultTimeAlive;
      Location defaultLocationDeath = locationDeath != null
         ? locationDeath
         : locationManager.getSafeGraveLocation((LivingEntity)victim, victim.getLocation(), grave);
      GraveCreateEvent createGrave = new GraveCreateEvent(victim, grave, itemStackList, null, null);
      createGrave.setOwnerType(victim.getType());
      createGrave.setOwnerName(victim.getName());
      createGrave.setOwnerNameDisplay(victim instanceof Player p ? p.getDisplayName() : victim.getName());
      createGrave.setOwnerUUID(victim.getUniqueId());
      createGrave.setOwnerTexture(this.util.skinTexture(victim));
      createGrave.setOwnerTextureSignature(this.util.skinSignature(victim));
      createGrave.setPermissionList(null);
      createGrave.setYaw(victim.getLocation().getYaw());
      createGrave.setPitch(victim.getLocation().getPitch());
      createGrave.setExperience(experience);
      createGrave.setTimeAlive(trueTimeAliveRemaining);
      createGrave.setDeathLocation(defaultLocationDeath);
      if (killer != null) {
         createGrave.setKillerType(killerEntityType != null ? killerEntityType : killer.getType());
         createGrave.setKillerName(killer.getName());
         createGrave.setKillerNameDisplay(killer.getCustomName());
         createGrave.setKillerUUID(killer.getUniqueId());
         createGrave.setDeathCause(damageCause);
      } else {
         createGrave.setKillerUUID(victim.getUniqueId());
         createGrave.setKillerType(EntityType.PLAYER);
         createGrave.setDeathCause(damageCause);
         String killerName = graveManager.getDamageReason(grave);
         createGrave.setKillerName(killerName);
         createGrave.setKillerNameDisplay(killerName);
      }

      if (graveProtection && this.plugin.getConfigManager().getConfigSection("protection.enabled", grave).getBoolean("protection.enabled")) {
         GraveProtectionCreateEvent gp = new GraveProtectionCreateEvent(victim, grave);
         this.plugin.getServer().getPluginManager().callEvent(gp);
         if (!gp.isCancelled() && !gp.isAddon()) {
            createGrave.setProtection(true);
            long protTime = graveProtectionTime > 0L
               ? graveProtectionTime
               : this.plugin.getConfigManager().getConfigSection("protection.time", grave).getInt("protection.time") * 1000L;
            createGrave.setTimeProtection(protTime);
         }
      } else {
         createGrave.setProtection(false);
         createGrave.setTimeProtection(0L);
      }

      Bukkit.getPluginManager().callEvent(createGrave);
      if (!createGrave.isCancelled()) {
         try {
            grave.setOwnerType(createGrave.getOwnerType());
            grave.setOwnerName(createGrave.getOwnerName());
            grave.setOwnerNameDisplay(createGrave.getOwnerNameDisplay());
            grave.setOwnerUUID(createGrave.getOwnerUUID());
            grave.setOwnerTexture(createGrave.getOwnerTexture());
            grave.setOwnerTextureSignature(createGrave.getOwnerTextureSignature());
            grave.setPermissionList(createGrave.getPermissionList());
            grave.setYaw(createGrave.getYaw());
            grave.setPitch(createGrave.getPitch());
            grave.setExperience(createGrave.getExperience());
            grave.setTimeCreation(System.currentTimeMillis());
            long finalTimeAlive = createGrave.getTimeAlive() > 0L ? createGrave.getTimeAlive() : trueTimeAliveRemaining;
            grave.setTimeAlive(finalTimeAlive);
            grave.setTimeAliveRemaining(finalTimeAlive);
            grave.setKillerType(createGrave.getKillerType());
            grave.setKillerName(createGrave.getKillerName());
            grave.setKillerNameDisplay(createGrave.getKillerNameDisplay());
            grave.setKillerUUID(createGrave.getKillerUUID());
            grave.setProtection(createGrave.getProtection());
            grave.setTimeProtection(createGrave.getTimeProtection());
            Location finalLocationDeath = createGrave.getLocationDeath() != null ? createGrave.getLocationDeath() : defaultLocationDeath;
            locationMap.put(finalLocationDeath, BlockData.BlockType.DEATH);
            cacheManager.getGraveMap().put(grave.getUUID(), grave);
            grave.setLocationDeath(finalLocationDeath);
            grave.setInventory(
               graveManager.getGraveInventory(grave, (LivingEntity)victim, itemStackList, this.getRemovedItemStacks((LivingEntity)victim), null)
            );
            grave.setEquipmentMap(
               (Map<EquipmentSlot, ItemStack>)(equipmentMap != null
                  ? equipmentMap
                  : (!versionManager.is_v1_7() ? entityManager.getEquipmentMap((LivingEntity)victim, grave) : new HashMap<>()))
            );
            dataManager.addGrave(grave);
            if (victim instanceof Player player) {
               if (this.plugin.getConfigManager().getConfigSection("noteblockapi.enabled", grave).getBoolean("noteblockapi.enabled")
                  && this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
                  String deathReason = victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause().name() : "UNKNOWN";
                  String nbsSound = null;

                  for (String cause : this.plugin
                     .getConfigManager()
                     .getConfigSection("noteblockapi.death-causes", grave)
                     .getStringList("noteblockapi.death-causes")) {
                     String[] parts = cause.split(": ");
                     if (parts.length == 2 && parts[0].equalsIgnoreCase(deathReason)) {
                        nbsSound = parts[1].trim();
                        break;
                     }
                  }

                  if (nbsSound == null) {
                     nbsSound = this.plugin.getConfigManager().getConfigSection("noteblockapi.nbs-sound", grave).getString("noteblockapi.nbs-sound");
                  }

                  if (this.plugin.getConfigManager().getConfigSection("noteblockapi.play-locally", grave).getBoolean("noteblockapi.play-locally")) {
                     this.plugin.getIntegrationManager().getNoteBlockAPI().playSongForPlayer(player, nbsSound);
                  } else {
                     this.plugin.getIntegrationManager().getNoteBlockAPI().playSongForAllPlayers(nbsSound);
                  }
               } else {
                  this.plugin.getEntityManager().playPlayerSound("sound.grave-create", player, grave);
               }
            }

            if (integrationManager.hasMultiPaper()) {
               integrationManager.getMultiPaper().notifyGraveCreation(grave);
            }

            this.placeGraveBlocks(grave, locationMap, (LivingEntity)victim);
            this.plugin.debugMessage("Creating grave " + grave.getUUID() + " for entity " + victim + " via GravesX API", 1);
         } catch (Exception var38) {
            this.plugin.getLogger().severe("An error occurred while creating grave " + grave.getUUID() + " for entity " + victim + ".");
            this.plugin.logStackTrace(var38);
         }
      }
   }

   private List<ItemStack> getRemovedItemStacks(@NotNull LivingEntity livingEntity) {
      List<ItemStack> out = new ArrayList<>();
      Map<UUID, List<ItemStack>> map = this.plugin.getCacheManager().getRemovedItemStackMap();
      if (map.containsKey(livingEntity.getUniqueId())) {
         out.addAll(map.get(livingEntity.getUniqueId()));
         map.remove(livingEntity.getUniqueId());
      }

      return out;
   }

   private void placeGraveBlocks(@NotNull Grave grave, @NotNull Map<Location, BlockData.BlockType> locationMap, @NotNull LivingEntity livingEntity) {
      for (Entry<Location, BlockData.BlockType> entry : locationMap.entrySet()) {
         Location loc = entry.getKey().clone();
         int offsetX = 0;
         int offsetY = 0;
         int offsetZ = 0;
         switch ((BlockData.BlockType)entry.getValue()) {
            case NORMAL:
               offsetX = this.plugin.getConfigManager().getConfigSection("placement.offset.x", grave).getInt("placement.offset.x");
               offsetY = this.plugin.getConfigManager().getConfigSection("placement.offset.y", grave).getInt("placement.offset.y");
               offsetZ = this.plugin.getConfigManager().getConfigSection("placement.offset.z", grave).getInt("placement.offset.z");
            case DEATH:
         }

         loc.add(offsetX, offsetY, offsetZ);
         GraveBlockPlaceEvent evt = new GraveBlockPlaceEvent(grave, loc, entry.getValue(), entry.getKey().getBlock(), livingEntity);
         this.plugin.getServer().getPluginManager().callEvent(evt);
         if (!evt.isCancelled()) {
            this.plugin.getGraveManager().placeGrave(evt.getLocation(), grave);
            this.plugin.getEntityManager().sendMessage("message.block", livingEntity, loc, grave);
            this.plugin.getEntityManager().runCommands("event.command.block", livingEntity, evt.getLocation(), grave);
         }
      }
   }
}
