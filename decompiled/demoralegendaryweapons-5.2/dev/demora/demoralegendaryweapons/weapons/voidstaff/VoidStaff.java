package dev.demora.demoralegendaryweapons.weapons.voidstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class VoidStaff {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey voidStaffKey;
   private final NamespacedKey staffModeKey;
   private final Map<UUID, Long> portalCooldowns;
   private final Map<UUID, Long> shulkerCooldowns;
   private final Map<UUID, Long> portalSpawnCooldowns;
   private final Map<UUID, VoidStaff.StaffMode> playerModes;
   private final PortalManager portalManager;

   public VoidStaff(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.voidStaffKey = new NamespacedKey(plugin, "void_staff");
      this.staffModeKey = new NamespacedKey(plugin, "staff_mode");
      this.portalCooldowns = new HashMap<>();
      this.shulkerCooldowns = new HashMap<>();
      this.portalSpawnCooldowns = new HashMap<>();
      this.playerModes = new HashMap<>();
      this.portalManager = new PortalManager(plugin);
   }

   public ItemStack createVoidStaff() {
      ItemStack staff = new ItemStack(Material.STONE_SWORD);
      ItemMeta meta = staff.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("void_staff");
         if (weaponName.isEmpty()) {
            weaponName = "§5§lПосох пустоты";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(4);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.voidStaffKey, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.staffModeKey, PersistentDataType.STRING, VoidStaff.StaffMode.PORTAL.name());
         this.plugin.applyWeaponLore(meta, "void_staff");
         staff.setItemMeta(meta);
      }

      return staff;
   }

   public boolean isVoidStaff(ItemStack item) {
      if (item != null && item.getType() == Material.STONE_SWORD && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         Byte mark = (Byte)pdc.get(this.voidStaffKey, PersistentDataType.BYTE);
         return mark != null && mark == 1;
      } else {
         return false;
      }
   }

   public VoidStaff.StaffMode getStaffMode(ItemStack staff) {
      if (!this.isVoidStaff(staff)) {
         return VoidStaff.StaffMode.PORTAL;
      } else {
         ItemMeta meta = staff.getItemMeta();
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         String modeStr = (String)pdc.get(this.staffModeKey, PersistentDataType.STRING);

         try {
            return VoidStaff.StaffMode.valueOf(modeStr);
         } catch (Exception var6) {
            return VoidStaff.StaffMode.PORTAL;
         }
      }
   }

   public void switchMode(Player player, ItemStack staff) {
      if (this.isVoidStaff(staff)) {
         VoidStaff.StaffMode currentMode = this.getStaffMode(staff);
         VoidStaff.StaffMode newMode = currentMode == VoidStaff.StaffMode.PORTAL ? VoidStaff.StaffMode.SHULKER_CHARGE : VoidStaff.StaffMode.PORTAL;
         ItemMeta meta = staff.getItemMeta();
         if (meta != null) {
            meta.setCustomModelData(newMode.getModelData());
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(this.staffModeKey, PersistentDataType.STRING, newMode.name());
            staff.setItemMeta(meta);
         }

         this.playerModes.put(player.getUniqueId(), newMode);
         this.updateActionBar(player, newMode);
         player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 1.5F);
      }
   }

   public void updateActionBar(Player player, VoidStaff.StaffMode mode) {
      String modeText = mode == VoidStaff.StaffMode.PORTAL
         ? this.plugin.getMessage("weapon-messages.void-staff-mode-portal")
         : this.plugin.getMessage("weapon-messages.void-staff-mode-shulker");
      StringBuilder message = new StringBuilder();
      message.append(this.plugin.getMessage("weapon-messages.void-staff-mode")).append(modeText);
      long cooldown = 0L;
      if (mode == VoidStaff.StaffMode.PORTAL && this.isOnPortalCooldown(player)) {
         cooldown = this.getRemainingPortalCooldown(player);
      } else if (mode == VoidStaff.StaffMode.SHULKER_CHARGE && this.isOnShulkerCooldown(player)) {
         cooldown = this.getRemainingShulkerCooldown(player);
      }

      if (cooldown > 0L) {
         message.append(" §f| §c").append(cooldown);
      }

      player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(message.toString()));
   }

   public boolean isOnPortalCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.portalCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.void_staff.portal_cooldown", 40) * 1000L;
         long lastUse = this.portalCooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   public boolean isOnShulkerCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.shulkerCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.void_staff.shulker_cooldown", 30) * 1000L;
         long lastUse = this.shulkerCooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   public long getRemainingPortalCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.portalCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.void_staff.portal_cooldown", 40) * 1000L;
         long lastUse = this.portalCooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public long getRemainingShulkerCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.shulkerCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.void_staff.shulker_cooldown", 30) * 1000L;
         long lastUse = this.shulkerCooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L + 1L);
      }
   }

   public boolean isOnPortalSpawnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.portalSpawnCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.portalSpawnCooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < 1000L;
      }
   }

   public void usePortalMode(Player player) {
      if (!this.isOnPortalSpawnCooldown(player)) {
         Location targetLocation = this.getTargetLocation(player);
         if (targetLocation != null) {
            boolean portalCreated = this.portalManager.createPortal(player, targetLocation);
            if (portalCreated || this.portalManager.hasFirstPortal(player)) {
               if (portalCreated) {
                  this.portalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
               }

               this.portalSpawnCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
               player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
            }
         }
      }
   }

   public void useShulkerMode(Player player) {
      this.shulkerCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
      Location eyeLoc = player.getEyeLocation();
      final ShulkerBullet bullet = (ShulkerBullet)player.getWorld().spawn(eyeLoc, ShulkerBullet.class);
      bullet.setShooter(player);
      bullet.setTarget(null);
      final Vector direction = eyeLoc.getDirection().normalize().multiply(0.7);
      (new BukkitRunnable() {
         private int ticks = 0;

         public void run() {
            if (!bullet.isDead() && bullet.isValid() && this.ticks <= 200) {
               bullet.setVelocity(direction);
               this.ticks++;
            } else {
               final Location explodeLoc = bullet.getLocation();
               bullet.remove();
               float explosionPower = (float)VoidStaff.this.plugin.getConfig().getDouble("weapons.void_staff.shulker_explosion_power", 4.0);
               explodeLoc.getWorld().createExplosion(explodeLoc, explosionPower, false, !VoidStaff.this.plugin.isRegionProtected(explodeLoc));
               final Player nearestPlayer = VoidStaff.this.getNearestPlayer(explodeLoc, player);

               for (int i = 0; i < 3; i++) {
                  (new BukkitRunnable() {
                     public void run() {
                        ShulkerBullet extraBullet = (ShulkerBullet)explodeLoc.getWorld().spawn(explodeLoc, ShulkerBullet.class);
                        extraBullet.setShooter(player);
                        if (nearestPlayer != null && nearestPlayer.isOnline()) {
                           extraBullet.setTarget(nearestPlayer);
                        } else {
                           extraBullet.setTarget(null);
                        }
                     }
                  }).runTaskLater(VoidStaff.this.plugin, i * 5L);
               }

               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0F, 1.0F);
   }

   private Player getNearestPlayer(Location location, Player owner) {
      Player nearest = null;
      double minDistance = Double.MAX_VALUE;

      for (Player p : location.getWorld().getPlayers()) {
         if (!p.equals(owner) && p.isOnline()) {
            double distance = p.getLocation().distance(location);
            if (distance < minDistance && distance < 50.0) {
               minDistance = distance;
               nearest = p;
            }
         }
      }

      return nearest;
   }

   private Location getTargetLocation(Player player) {
      RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 50.0);
      if (result != null && result.getHitBlock() != null) {
         Block hitBlock = result.getHitBlock();
         return hitBlock.getLocation().add(0.0, 1.0, 0.0);
      } else {
         Location playerLoc = player.getLocation();
         Vector direction = playerLoc.getDirection().normalize().multiply(10);
         return playerLoc.add(direction);
      }
   }

   public PortalManager getPortalManager() {
      return this.portalManager;
   }

   public void removePlayerData(UUID playerId) {
      this.portalCooldowns.remove(playerId);
      this.shulkerCooldowns.remove(playerId);
      this.portalSpawnCooldowns.remove(playerId);
      this.playerModes.remove(playerId);
   }

   public static enum StaffMode {
      PORTAL("Портал", 4),
      SHULKER_CHARGE("Заряд шалкера", 5);

      private final String displayName;
      private final int modelData;

      private StaffMode(String displayName, int modelData) {
         this.displayName = displayName;
         this.modelData = modelData;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public int getModelData() {
         return this.modelData;
      }
   }
}
