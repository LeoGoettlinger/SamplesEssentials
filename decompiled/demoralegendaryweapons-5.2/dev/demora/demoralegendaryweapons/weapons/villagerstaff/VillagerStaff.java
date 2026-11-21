package dev.demora.demoralegendaryweapons.weapons.villagerstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VillagerStaff {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, Long> cooldowns;
   private final Map<UUID, Long> taggedForReward;

   public VillagerStaff(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.cooldowns = new HashMap<>();
      this.taggedForReward = new ConcurrentHashMap<>();
   }

   public ItemStack createVillagerStaff() {
      ItemStack staff = new ItemStack(Material.IRON_AXE);
      ItemMeta meta = staff.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("villager_staff");
         if (weaponName.isEmpty()) {
            weaponName = "§6§lПосох жителя";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(3);
         meta.setUnbreakable(true);
         this.plugin.applyWeaponLore(meta, "villager_staff");
         staff.setItemMeta(meta);
      }

      return staff;
   }

   public boolean isVillagerStaff(ItemStack item) {
      if (item != null && item.getType() == Material.IRON_AXE) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() == 3 : false;
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.villager_staff.cooldown.time", 10L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   public long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.villager_staff.cooldown.time", 10L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public void useStaff(Player player) {
      Location targetLocation = this.getTargetLocation(player);
      if (targetLocation != null) {
         this.createExplosiveItemDisplay(targetLocation);
         this.setCooldown(player);
      }
   }

   private Location getTargetLocation(Player player) {
      RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 50.0);
      if (result != null && result.getHitBlock() != null) {
         return result.getHitPosition().toLocation(player.getWorld());
      } else {
         Location playerLoc = player.getLocation();
         Vector direction = playerLoc.getDirection().normalize().multiply(10);
         return playerLoc.add(direction);
      }
   }

   private void createExplosiveItemDisplay(Location location) {
      double heightOffset = 1.5;
      Location displayLocation = location.clone().add(0.0, heightOffset, 0.0);
      ItemDisplay itemDisplay = (ItemDisplay)displayLocation.getWorld().spawnEntity(displayLocation, EntityType.ITEM_DISPLAY);
      this.setupItemDisplay(itemDisplay);
      this.scheduleGrowExplodeShrink(itemDisplay);
   }

   private void setupItemDisplay(ItemDisplay itemDisplay) {
      ItemStack slimeBall = this.createSlimeBallHelmet();
      itemDisplay.setItemStack(slimeBall);
      float startX = 1.0F;
      float startY = 50.0F;
      float startZ = 1.0F;
      Vector3f translation = new Vector3f(0.0F, 0.0F, 0.0F);
      AxisAngle4f leftRotation = new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F);
      Vector3f scale = new Vector3f(startX, startY, startZ);
      AxisAngle4f rightRotation = new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F);
      Transformation transformation = new Transformation(translation, new Quaternionf(leftRotation), scale, new Quaternionf(rightRotation));
      itemDisplay.setTransformation(transformation);
      itemDisplay.setGravity(false);
      itemDisplay.setInvulnerable(true);
      itemDisplay.setBrightness(new Brightness(15, 15));
   }

   private ItemStack createSlimeBallHelmet() {
      ItemStack slimeBall = new ItemStack(Material.SLIME_BALL);
      ItemMeta meta = slimeBall.getItemMeta();
      if (meta != null) {
         meta.setCustomModelData(12);
         slimeBall.setItemMeta(meta);
      }

      return slimeBall;
   }

   private void scheduleGrowExplodeShrink(ItemDisplay itemDisplay) {
      final int growTicks = 12;
      final int shrinkTicks = 12;
      final float startX = 1.0F;
      final float startY = 50.0F;
      final float startZ = 1.0F;
      final float endX = 4.0F;
      final float endY = 50.0F;
      final float endZ = 4.0F;
      float rotationDegreesPerTick = 10.0F;
      (new BukkitRunnable() {
         int tick = 0;
         boolean exploded = false;
         float yaw = 0.0F;

         public void run() {
            if (!itemDisplay.isDead() && itemDisplay.isValid()) {
               this.yaw += 10.0F;
               if (this.yaw >= 360.0F) {
                  this.yaw -= 360.0F;
               }

               AxisAngle4f leftRot = new AxisAngle4f((float)Math.toRadians(this.yaw), 0.0F, 1.0F, 0.0F);
               if (!this.exploded) {
                  float t = Math.min(1.0F, growTicks <= 0 ? 1.0F : (float)this.tick / growTicks);
                  float sx = VillagerStaff.this.lerp(startX, endX, t);
                  float sy = VillagerStaff.this.lerp(startY, endY, t);
                  float sz = VillagerStaff.this.lerp(startZ, endZ, t);
                  VillagerStaff.this.applyTransform(itemDisplay, leftRot, sx, sy, sz);
                  this.tick++;
                  if (t >= 1.0F) {
                     VillagerStaff.this.explodeItemDisplay(itemDisplay);
                     this.exploded = true;
                     this.tick = 0;
                  }
               } else {
                  float t = Math.min(1.0F, shrinkTicks <= 0 ? 1.0F : (float)this.tick / shrinkTicks);
                  float sx = VillagerStaff.this.lerp(endX, startX, t);
                  float sy = VillagerStaff.this.lerp(endY, startY, t);
                  float sz = VillagerStaff.this.lerp(endZ, startZ, t);
                  VillagerStaff.this.applyTransform(itemDisplay, leftRot, sx, sy, sz);
                  this.tick++;
                  if (t >= 1.0F) {
                     itemDisplay.remove();
                     this.cancel();
                  }
               }
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void explodeItemDisplay(ItemDisplay itemDisplay) {
      Location location = itemDisplay.getLocation();
      float power = (float)this.plugin.getConfig().getDouble("weapons.villager_staff.explosion.power", 3.0);
      double rewardTagRadius = power;

      for (Entity e : location.getWorld().getNearbyEntities(location, rewardTagRadius, rewardTagRadius, rewardTagRadius)) {
         if (e instanceof LivingEntity living) {
            this.taggedForReward.put(living.getUniqueId(), System.currentTimeMillis());
         }
      }

      location.getWorld().createExplosion(location, power, false, !this.plugin.isRegionProtected(location));
      int particleCount = 50;
      location.getWorld().spawnParticle(Particle.EXPLOSION, location, particleCount, power / 2.0F, power / 2.0F, power / 2.0F, 0.1);
      location.getWorld().spawnParticle(Particle.FLAME, location, particleCount * 2, power / 2.0F, power / 2.0F, power / 2.0F, 0.05);
      location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, particleCount, power / 2.0F, power / 2.0F, power / 2.0F, 0.1);
      location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5F, 1.2F);
   }

   public boolean isTaggedForReward(UUID entityId) {
      Long ts = this.taggedForReward.get(entityId);
      return ts == null ? false : System.currentTimeMillis() - ts <= 3000L;
   }

   public void removeRewardTag(UUID entityId) {
      this.taggedForReward.remove(entityId);
   }

   private void applyTransform(ItemDisplay itemDisplay, AxisAngle4f leftRotation, float x, float y, float z) {
      Transformation current = itemDisplay.getTransformation();
      Transformation updated = new Transformation(current.getTranslation(), new Quaternionf(leftRotation), new Vector3f(x, y, z), current.getRightRotation());
      itemDisplay.setTransformation(updated);
   }

   private float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   public Vector prepareStaffDirection(Location loc) {
      if (loc == null) {
         return null;
      } else {
         Vector v = loc.getDirection().setY(0);
         return v.lengthSquared() < 1.0E-6 ? null : v.normalize();
      }
   }
}
