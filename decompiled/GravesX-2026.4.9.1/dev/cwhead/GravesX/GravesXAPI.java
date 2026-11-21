package dev.cwhead.GravesX;

import com.mojang.authlib.GameProfile;
import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.data.LocationData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.skin.SkinAPI;
import dev.cwhead.GravesX.util.PluginDownloadUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated(
   forRemoval = true,
   since = "4.9.9.1"
)
@ScheduledForRemoval(
   inVersion = "2027.4.9.1"
)
public class GravesXAPI {
   private final Graves plugin;
   private final dev.cwhead.GravesX.api.GravesXAPI api;

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GravesXAPI(Graves plugin) {
      this.plugin = plugin;
      this.api = new dev.cwhead.GravesX.api.GravesXAPI(plugin);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, long timeAliveRemaining) {
      this.api.gravesCreate.createGrave(victim, killerEntityType, timeAliveRemaining);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, int experience, long timeAliveRemaining) {
      this.api.gravesCreate.createGrave(victim, killerEntityType, experience, timeAliveRemaining);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining
   ) {
      this.api.gravesCreate.createGrave(victim, killerEntityType, equipmentMap, itemStackList, experience, timeAliveRemaining);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api
         .gravesCreate
         .createGrave(victim, killerEntityType, equipmentMap, itemStackList, experience, timeAliveRemaining, graveProtection, graveProtectionTime);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining,
      @Nullable DamageCause damageCause
   ) {
      this.api.gravesCreate.createGrave(victim, killerEntityType, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api
         .gravesCreate
         .createGrave(victim, killerEntityType, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, graveProtection, graveProtectionTime);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api.gravesCreate.createGrave(victim, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api
         .gravesCreate
         .createGrave(
            victim,
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

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void createGrave(
      @NotNull Entity victim,
      @Nullable EntityType killerEntityType,
      @Nullable Location locationDeath,
      @Nullable Map<EquipmentSlot, ItemStack> equipmentMap,
      @Nullable List<ItemStack> itemStackList,
      int experience,
      long timeAliveRemaining
   ) {
      this.api.gravesCreate.createGrave(victim, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api.gravesCreate.createGrave(victim, killer, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api
         .gravesCreate
         .createGrave(
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

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
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
      this.api
         .gravesCreate
         .createGrave(
            victim,
            killer,
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

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void removeGrave(@NotNull Grave grave) {
      this.api.gravesManage.removeGrave(grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void breakGrave(@NotNull Grave grave) {
      this.api.gravesManage.breakGrave(grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void breakGrave(@NotNull Location location, @NotNull Grave grave) {
      this.api.gravesManage.breakGrave(location, grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void autoLootGrave(@NotNull Entity entity, @NotNull Location location, @NotNull Grave grave) {
      this.api.gravesManage.autoLootGrave(entity, location, grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void abandonGrave(@NotNull Grave grave) {
      this.api.gravesManage.abandonGrave(grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void dropGraveItems(@NotNull Location location, @NotNull Grave grave) {
      this.api.gravesManage.dropGraveItems(location, grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void removeOldestGrave(@NotNull LivingEntity livingEntity) {
      this.api.gravesManage.removeOldestGrave(livingEntity);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isNearGrave(@NotNull Location location, @Nullable Player player, @Nullable Block block) {
      return this.api.gravesManage.isNearGrave(location, player, block);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isNearGrave(@NotNull Location location) {
      return this.api.gravesManage.isNearGrave(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isNearGrave(@NotNull Location location, @NotNull Player player) {
      return this.api.gravesManage.isNearGrave(location, player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isNearGrave(@NotNull Location location, @NotNull Block block) {
      return this.api.gravesManage.isNearGrave(location, block);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Grave getGrave(@NotNull UUID uuid) {
      return this.api.gravesManage.getGrave(uuid);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public BlockData getBlockData(@NotNull Location location, @NotNull UUID graveUUID, @NotNull String replaceMaterial, @NotNull String replaceData) {
      return new BlockData(location, graveUUID, replaceMaterial, replaceData);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public ChunkData getChunkData(@NotNull Location location) {
      return new ChunkData(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public EntityData getEntityData(@NotNull Location location, @NotNull UUID uuidEntity, @NotNull UUID uuidGrave, @NotNull EntityData.Type type) {
      return new EntityData(location, uuidEntity, uuidGrave, type);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public HologramData getHologramData(@NotNull Location location, @NotNull UUID uuidEntity, @NotNull UUID uuidGrave, int line) {
      return new HologramData(location, uuidEntity, uuidGrave, line);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public LocationData getLocationData(@NotNull Location location) {
      return new LocationData(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public BlockFace simplifyBlockFace(@NotNull BlockFace face) {
      return this.api.world.simplifyBlockFace(face);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Rotation getRotationFromBlockFace(@NotNull BlockFace face) {
      return this.api.world.getRotationFromBlockFace(face);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String encodeObjectToBase64(@NotNull Object object) {
      return this.api.util.objectToBase64(object);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Object decodeBase64ToObject(@NotNull String base64String) {
      return this.api.util.base64ToObject(base64String);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void loadClass(@NotNull String className) {
      this.api.util.loadClass(className);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Color getColor(@NotNull String colorName) {
      return this.api.util.getColor(colorName);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Color getColorFromHex(@NotNull String hex) {
      return this.api.util.getColorFromHex(hex);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public DustOptions createDustOptionsFromHex(@NotNull String hexColor, float size) {
      return this.api.util.dustFromHex(hexColor, size);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean hasPermission(@NotNull Entity entity, @NotNull String permission) {
      return this.api.util.hasPermission(entity, permission);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getPlayerExperience(@NotNull Player player) {
      return this.api.util.playerTotalXp(player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getExperienceAtLevel(int level) {
      return this.api.util.xpAtLevel(level);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public long getLevelFromExperience(long experience) {
      return this.api.util.levelFromXp(experience);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getDropPercent(int experience, float percent) {
      return this.api.util.dropPercent(experience, percent);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getPlayerDropExperience(@NotNull Player player, float expStorePercent) {
      return this.api.util.playerDropXp(player, expStorePercent);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void moveFile(@NotNull File file, @NotNull String name) {
      this.api.util.moveFile(file, name);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void copyFile(@NotNull File file, @NotNull String name) {
      this.api.util.copyFile(file, name);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getInventorySize(int size) {
      return this.api.inventory.getInventorySize(size);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void equipArmor(@NotNull Inventory inventory, @NotNull Player player) {
      this.api.inventory.equipArmor(inventory, player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void equipItems(@NotNull Inventory inventory, @NotNull Player player) {
      this.api.inventory.equipItems(inventory, player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String inventoryToString(@NotNull Inventory inventory) {
      return this.api.inventory.inventoryToString(inventory);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Inventory stringToInventory(@NotNull InventoryHolder inventoryHolder, @NotNull String string, @NotNull String title) {
      return this.api.inventory.stringToInventory(inventoryHolder, string, title);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Location roundLocation(@NotNull Location location) {
      return this.api.world.roundLocation(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String locationToString(@NotNull Location location) {
      return this.api.world.locationToString(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String chunkToString(@NotNull Location location) {
      return this.api.world.chunkToString(location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Location chunkStringToLocation(@NotNull String string) {
      return this.api.world.chunkStringToLocation(string);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Location stringToLocation(@NotNull String string) {
      return this.api.world.stringToLocation(string);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Location getClosestLocation(@NotNull Location locationBase, @NotNull List<Location> locationList) {
      return this.api.world.getClosestLocation(locationBase, locationList);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isAir(@NotNull Material material) {
      return this.api.util.isAir(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isLava(@NotNull Material material) {
      return this.api.util.isLava(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isSafeNotSolid(@NotNull Material material) {
      return this.api.util.isSafeNotSolid(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isSafeSolid(@NotNull Material material) {
      return this.api.util.isSafeSolid(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isWater(@NotNull Material material) {
      return this.api.util.isWater(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isPlayerHead(@NotNull Material material) {
      return this.api.util.isPlayerHead(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isPlayerHead(@NotNull String material) {
      return this.api.util.isPlayerHead(material);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String postLog(@NotNull String content) {
      return this.api.util.postLog(content);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int getHighestInt(@NotNull Player player, @Nullable String permission) {
      return this.api.util.highestInt(player, permission);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public double getHighestDouble(@NotNull Player player, String permission) {
      return this.api.util.highestDouble(player, permission);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void swingMainHand(@NotNull Player player) {
      this.api.util.swingMainHand(player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void copyResources(@NotNull String inputPath, @NotNull String outputPath) {
      this.api.util.copyResources(inputPath, outputPath);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void copyResources(@NotNull String inputPath, @NotNull String outputPath, boolean overwrite) {
      this.api.util.copyResources(inputPath, outputPath, overwrite);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String getSkinSignature(@NotNull Entity entity) {
      return SkinAPI.getSkinSignature(entity);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void setSkullTexture(@NotNull Skull skull, @NotNull String name, @NotNull String base64) {
      SkinAPI.setSkullTexture(skull, name, base64);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void setSkullTexture(@NotNull SkullMeta skullMeta, @NotNull String name, @NotNull String base64) {
      SkinAPI.setSkullTexture(skullMeta, name, base64);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String getTexture(@NotNull Entity entity) {
      return SkinAPI.getTexture(entity);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GameProfile getPlayerGameProfile(@NotNull Player player) {
      return SkinAPI.getPlayerGameProfile(player);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public UUID getUUID(@NotNull String string) {
      return this.api.util.uuidOf(string);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public String getLatestVersion(int resourceId) {
      return this.api.util.latestSpigotVersion(resourceId);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isValidYAML(@NotNull File file) {
      return this.api.util.isValidYaml(file);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isGrave(@NotNull Grave grave) {
      return this.api.gravesManage.isGrave(grave);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public boolean isGrave(@NotNull Grave grave, @NotNull Location location) {
      return this.api.gravesManage.isGrave(grave, location);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public long getGraveAmount() {
      return this.api.gravesManage.getGraveAmount();
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public long getGraveAmount(@Nullable Player targetPlayer) {
      return this.api.gravesManage.getGraveAmount(targetPlayer);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public static void downloadAndReplacePlugin(long pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
      PluginDownloadUtil.downloadAndReplacePlugin(pluginId, pluginName, pluginsFolder, commandSender);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public static void downloadAndReplacePlugin(String pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
      PluginDownloadUtil.downloadAndReplacePlugin(pluginId, pluginName, pluginsFolder, commandSender);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void ensureGravesXAddonFolder(Plugin addon) {
      this.api.addon.ensureAddonFolder(addon);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int exportAddonConfigs(Plugin addon) {
      return this.api.addon.exportAddonConfigs(addon);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public int exportAddonConfigs(Plugin addon, boolean replaceIfExists) {
      return this.api.addon.exportAddonConfigs(addon, replaceIfExists);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @Experimental
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public Graves getGravesX() {
      return this.plugin;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GravesXAPI getInstance() {
      return this;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.9.1"
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public void register() {
      this.plugin.getLogger().warning("[GravesX] GravesXAPI#register() is deprecated and now a no-op.");
   }
}
