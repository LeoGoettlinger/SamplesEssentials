package dev.demora.demoralegendaryweapons.weapons.warpick;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class WarPickListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final WarPick warPick;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Set<String> breaking = new HashSet<>();

   public WarPickListener(DemoraLegendaryWeapons plugin, WarPick warPick) {
      this.plugin = plugin;
      this.warPick = warPick;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (this.warPick.isWarPick(tool)) {
         Block origin = event.getBlock();
         String originKey = this.key(origin.getLocation());
         if (!this.breaking.contains(originKey)) {
            if (!this.isOnCooldown(player.getUniqueId())) {
               this.setCooldown(player.getUniqueId());
               Location center = origin.getLocation().add(0.5, 0.5, 0.5);
               origin.getWorld().spawnParticle(Particle.EXPLOSION, center, 1, 0.0, 0.0, 0.0, 0.0);
               origin.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
               this.breakCube3x3(origin, tool);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerHit(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player attacker) {
         if (event.getEntity() instanceof Player victim) {
            ItemStack var6 = attacker.getInventory().getItemInMainHand();
            if (this.warPick.isWarPick(var6)) {
               if (!this.isOnCooldown(attacker.getUniqueId())) {
                  this.setCooldown(attacker.getUniqueId());
                  this.damageArmorDurability(victim);
               }
            }
         }
      }
   }

   private boolean isOnCooldown(UUID playerId) {
      return this.getRemainingCooldownSeconds(playerId) > 0L;
   }

   private void setCooldown(UUID playerId) {
      this.cooldowns.put(playerId, System.currentTimeMillis());
   }

   private void breakCube3x3(Block origin, ItemStack tool) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  Block b = origin.getWorld().getBlockAt(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                  if (!b.getType().isAir()
                     && b.getType() != Material.BEDROCK
                     && b.getType() != Material.BARRIER
                     && !this.plugin.isRegionProtected(b.getLocation())) {
                     String k = this.key(b.getLocation());
                     if (this.breaking.add(k)) {
                        try {
                           b.breakNaturally(tool);
                        } finally {
                           this.breaking.remove(k);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void damageArmorDurability(Player victim) {
      double percent = this.plugin.getConfig().getDouble("weapons.war_pick.armor_durability_damage_percent", 0.05);
      if (!(percent <= 0.0)) {
         ItemStack[] armor = victim.getInventory().getArmorContents();
         boolean changed = false;

         for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && !piece.getType().isAir() && piece.hasItemMeta() && piece.getItemMeta() instanceof Damageable damageable) {
               int maxDamage = damageable.hasMaxDamage() ? damageable.getMaxDamage() : piece.getType().getMaxDurability();
               if (maxDamage > 0) {
                  int currentDamage = damageable.getDamage();
                  int remaining = maxDamage - currentDamage;
                  if (remaining > 0) {
                     int wear = Math.max(1, (int)Math.ceil(remaining * percent));
                     damageable.setDamage(Math.min(maxDamage, currentDamage + wear));
                     piece.setItemMeta(damageable);
                     armor[i] = piece;
                     changed = true;
                  }
               }
            }
         }

         if (changed) {
            victim.getInventory().setArmorContents(armor);
         }
      }
   }

   private String key(Location loc) {
      return loc.getWorld().getUID() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      Long last = this.cooldowns.get(playerId);
      if (last == null) {
         return 0L;
      } else {
         long cooldownMs = this.plugin.getConfig().getLong("weapons.war_pick.cooldown_seconds", 5L) * 1000L;
         long remaining = cooldownMs - (System.currentTimeMillis() - last);
         return Math.max(0L, remaining / 1000L);
      }
   }
}
