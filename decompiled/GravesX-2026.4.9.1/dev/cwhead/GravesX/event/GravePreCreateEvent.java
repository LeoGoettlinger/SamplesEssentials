package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePreCreateEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private UUID graveUUID;
   @Nullable
   private Location deathLocation;
   @Nullable
   private EntityType ownerType;
   @Nullable
   private String ownerName;
   @Nullable
   private String ownerNameDisplay;
   @Nullable
   private UUID ownerUUID;
   @Nullable
   private List<String> permissionList;
   private float yaw;
   private float pitch;
   private long timeAlive;
   @Nullable
   private String ownerTexture;
   @Nullable
   private String ownerTextureSignature;
   private int experience;
   @Nullable
   private EntityType killerType;
   @Nullable
   private String killerName;
   @Nullable
   private String killerNameDisplay;
   @Nullable
   private UUID killerUUID;
   private boolean protectionEnabled;
   private long protectionTime;
   @Nullable
   private Collection<ItemStack> graveItemStackList;
   @Nullable
   private Collection<ItemStack> ignoredItems;
   @Nullable
   private Collection<Block> ignoredBlocks;
   @Nullable
   private String deathReason;

   public GravePreCreateEvent(@NotNull Entity entity, @NotNull Grave grave) {
      this(entity, grave, null, null, null);
   }

   public GravePreCreateEvent(
      @NotNull Entity entity,
      @NotNull Grave grave,
      @Nullable Collection<ItemStack> graveItemStackList,
      @Nullable Collection<ItemStack> ignoredItems,
      @Nullable Collection<Block> ignoredBlocks
   ) {
      super(
         grave,
         entity,
         grave.getLocationDeath() != null ? grave.getLocationDeath() : entity.getLocation(),
         null,
         null,
         entity instanceof LivingEntity ? (LivingEntity)entity : null,
         null
      );
      this.graveItemStackList = graveItemStackList == null ? null : new ArrayList<>(graveItemStackList);
      this.ignoredItems = ignoredItems == null ? null : new ArrayList<>(ignoredItems);
      this.ignoredBlocks = ignoredBlocks == null ? null : new ArrayList<>(ignoredBlocks);
   }

   public GravePreCreateEvent(
      @NotNull Entity entity, @NotNull Grave grave, @Nullable Collection<ItemStack> ignoredItems, @Nullable Collection<Block> ignoredBlocks
   ) {
      this(entity, grave, null, ignoredItems, ignoredBlocks);
   }

   @NotNull
   @Override
   public Grave getGrave() {
      return super.getGrave();
   }

   @Nullable
   @Override
   public UUID getGraveUUID() {
      return this.graveUUID;
   }

   public void setGraveUUID(@Nullable UUID uuid) {
      this.graveUUID = uuid;
   }

   @Nullable
   public Location getLocationDeath() {
      return this.deathLocation;
   }

   public void setDeathLocation(@Nullable Location location) {
      this.deathLocation = location;
   }

   @Nullable
   public EntityType getOwnerType() {
      return this.ownerType;
   }

   public void setOwnerType(@Nullable EntityType type) {
      this.ownerType = type;
   }

   @Nullable
   public String getOwnerName() {
      return this.ownerName;
   }

   public void setOwnerName(@Nullable String name) {
      this.ownerName = name;
   }

   @Nullable
   public String getOwnerNameDisplay() {
      return this.ownerNameDisplay;
   }

   public void setOwnerNameDisplay(@Nullable String displayName) {
      this.ownerNameDisplay = displayName;
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.ownerUUID = uuid;
   }

   @NotNull
   public List<String> getPermissionList() {
      return this.permissionList == null ? Collections.emptyList() : Collections.unmodifiableList(this.permissionList);
   }

   public void setPermissionList(@Nullable List<String> permissionList) {
      this.permissionList = permissionList == null ? null : new ArrayList<>(permissionList);
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public long getTimeAlive() {
      return this.timeAlive;
   }

   public void setTimeAlive(long millis) {
      this.timeAlive = millis;
   }

   @Nullable
   public String getOwnerTexture() {
      return this.ownerTexture;
   }

   public void setOwnerTexture(@Nullable String texture) {
      this.ownerTexture = texture;
   }

   @Nullable
   public String getOwnerTextureSignature() {
      return this.ownerTextureSignature;
   }

   public void setOwnerTextureSignature(@Nullable String signature) {
      this.ownerTextureSignature = signature;
   }

   public int getExperience() {
      return this.experience;
   }

   public void setExperience(int experience) {
      this.experience = experience;
   }

   @Nullable
   public EntityType getKillerType() {
      return this.killerType;
   }

   public void setKillerType(@Nullable EntityType type) {
      this.killerType = type;
   }

   @Nullable
   public String getKillerName() {
      return this.killerName;
   }

   public void setKillerName(@Nullable String name) {
      this.killerName = name;
   }

   @Nullable
   public String getKillerNameDisplay() {
      return this.killerNameDisplay;
   }

   public void setKillerNameDisplay(@Nullable String displayName) {
      this.killerNameDisplay = displayName;
   }

   @Nullable
   public UUID getKillerUUID() {
      return this.killerUUID;
   }

   public void setKillerUUID(@Nullable UUID uuid) {
      this.killerUUID = uuid;
   }

   public boolean getProtection() {
      return this.protectionEnabled;
   }

   public void setProtection(boolean enabled) {
      this.protectionEnabled = enabled;
   }

   public long getTimeProtection() {
      return this.protectionTime;
   }

   public void setTimeProtection(long millis) {
      this.protectionTime = millis;
   }

   @NotNull
   public Collection<ItemStack> getGraveItemStackList() {
      return (Collection<ItemStack>)(this.graveItemStackList == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.graveItemStackList));
   }

   public void setGraveItemStackList(@Nullable Collection<ItemStack> graveItemStackList) {
      this.graveItemStackList = graveItemStackList == null ? null : new ArrayList<>(graveItemStackList);
   }

   @NotNull
   public Collection<ItemStack> getIgnoredItems() {
      return (Collection<ItemStack>)(this.ignoredItems == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.ignoredItems));
   }

   public void setIgnoredItems(@Nullable Collection<ItemStack> ignoredItems) {
      this.ignoredItems = ignoredItems == null ? null : new ArrayList<>(ignoredItems);
   }

   public void addIgnoredItem(@Nullable ItemStack item) {
      if (item != null) {
         if (this.ignoredItems == null) {
            this.ignoredItems = new ArrayList<>();
         }

         this.ignoredItems.add(item);
      }
   }

   public boolean removeIgnoredItem(@Nullable ItemStack item) {
      return item != null && this.ignoredItems != null ? this.ignoredItems.remove(item) : false;
   }

   @NotNull
   public Collection<Block> getIgnoredBlocks() {
      return (Collection<Block>)(this.ignoredBlocks == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.ignoredBlocks));
   }

   public void setIgnoredBlocks(@Nullable Collection<Block> ignoredBlocks) {
      this.ignoredBlocks = ignoredBlocks == null ? null : new ArrayList<>(ignoredBlocks);
   }

   public void addIgnoredBlock(@Nullable Block block) {
      if (block != null) {
         if (this.ignoredBlocks == null) {
            this.ignoredBlocks = new ArrayList<>();
         }

         this.ignoredBlocks.add(block);
      }
   }

   public boolean removeIgnoredBlock(@Nullable Block block) {
      return block != null && this.ignoredBlocks != null ? this.ignoredBlocks.remove(block) : false;
   }

   @Nullable
   public DamageCause getDeathCause() {
      if (this.deathReason != null && !this.deathReason.isBlank()) {
         try {
            String key = this.normalizeDeathCauseKey(this.deathReason);
            return key == null ? null : DamageCause.valueOf(key);
         } catch (Exception var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void setDeathReason(@Nullable String reason) {
      this.deathReason = reason != null && !reason.isBlank() ? reason : null;
   }

   public void setDeathCause(@Nullable String causeName) {
      this.deathReason = this.normalizeDeathCauseKey(causeName);
   }

   public void setDeathCause(@Nullable EntityDamageEvent damageEvent) {
      if (damageEvent != null && damageEvent.getCause() != null) {
         this.deathReason = damageEvent.getCause().name();
      } else {
         this.deathReason = null;
      }
   }

   public void setDeathCause(@Nullable DamageCause cause) {
      this.deathReason = cause != null ? cause.name() : null;
   }

   @Nullable
   private String normalizeDeathCauseKey(@Nullable String raw) {
      if (raw == null) {
         return null;
      } else {
         String s = raw.trim();
         if (s.isEmpty()) {
            return null;
         } else {
            s = s.replace('-', '_').replace(' ', '_').replace('.', '_').replace('/', '_').replace('\\', '_').toUpperCase().replaceAll("_+", "_");

            while (s.startsWith("_")) {
               s = s.substring(1);
            }

            while (s.endsWith("_")) {
               s = s.substring(0, s.length() - 1);
            }

            if (s.isEmpty()) {
               return null;
            } else {
               try {
                  DamageCause.valueOf(s);
                  return s;
               } catch (IllegalArgumentException var4) {
                  return null;
               }
            }
         }
      }
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
