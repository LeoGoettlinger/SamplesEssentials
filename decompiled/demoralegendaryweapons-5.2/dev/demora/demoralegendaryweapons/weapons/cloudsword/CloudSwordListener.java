package dev.demora.demoralegendaryweapons.weapons.cloudsword;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CloudSwordListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final CloudSword cloudSword;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, CloudSwordListener.CloudAbility> activeAbilities = new HashMap<>();
   private final Map<UUID, Long> lastClickTime = new HashMap<>();

   public CloudSwordListener(DemoraLegendaryWeapons plugin, CloudSword cloudSword) {
      this.plugin = plugin;
      this.cloudSword = cloudSword;
   }

   public boolean isOnCooldown(Player player) {
      Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
      return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
   }

   public long getRemainingCooldown(Player player) {
      Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
      return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd ? (cooldownEnd - System.currentTimeMillis()) / 1000L : 0L;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction().isRightClick()) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.cloudSword.isCloudSword(item)) {
            event.setCancelled(true);
            UUID playerId = player.getUniqueId();
            Long lastClick = this.lastClickTime.get(playerId);
            if (lastClick == null || System.currentTimeMillis() - lastClick >= 300L) {
               this.lastClickTime.put(playerId, System.currentTimeMillis());
               CloudSwordListener.CloudAbility ability = this.activeAbilities.get(playerId);
               if (ability != null) {
                  if (ability.isPaused()) {
                     if (!ability.canResume()) {
                        return;
                     }

                     ability.resume();
                     player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.5F);
                  } else {
                     if (!ability.canPause()) {
                        return;
                     }

                     ability.pause();
                     player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 0.8F);
                  }
               } else {
                  Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
                  if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
                     long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
                     player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.cloud-sword-cooldown").replace("{time}", String.valueOf(remaining)));
                  } else {
                     this.startCloudAbility(player);
                  }
               }
            }
         }
      }
   }

   private void startCloudAbility(Player player) {
      int duration = this.plugin.getConfig().getInt("weapons.cloud_sword.ability_duration", 10);
      int cloudLifetimeTicks = this.plugin.getConfig().getInt("weapons.cloud_sword.cloud_lifetime_ticks", 40);
      CloudSwordListener.CloudAbility ability = new CloudSwordListener.CloudAbility(player, duration, cloudLifetimeTicks);
      this.activeAbilities.put(player.getUniqueId(), ability);
      ability.start();
      player.playSound(player.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0F, 1.0F);
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0F, 1.5F);
   }

   @EventHandler
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         if (event.getCause() == DamageCause.FALL) {
            Player player = (Player)event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (this.cloudSword.isCloudSword(item)) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID id = event.getPlayer().getUniqueId();
      CloudSwordListener.CloudAbility ability = this.activeAbilities.remove(id);
      if (ability != null) {
         ability.cancel();
      }

      this.cooldowns.remove(id);
      this.lastClickTime.remove(id);
   }

   private class CloudAbility extends BukkitRunnable {
      private final Player player;
      private final int maxDuration;
      private int ticksElapsed = 0;
      private boolean paused = false;
      private final Map<Location, Material> originalBlocks = new HashMap<>();
      private final int cloudLifetimeTicks;
      private long startTime;
      private long lastPauseToggleTime = 0L;

      public CloudAbility(Player player, int durationSeconds, int cloudLifetimeTicks) {
         this.player = player;
         this.maxDuration = durationSeconds * 20;
         this.cloudLifetimeTicks = cloudLifetimeTicks;
      }

      public void start() {
         this.startTime = System.currentTimeMillis();
         this.runTaskTimer(CloudSwordListener.this.plugin, 0L, 1L);
      }

      public boolean canPause() {
         long currentTime = System.currentTimeMillis();
         return currentTime - this.startTime >= 250L && currentTime - this.lastPauseToggleTime >= 300L;
      }

      public boolean canResume() {
         long currentTime = System.currentTimeMillis();
         return currentTime - this.lastPauseToggleTime >= 300L;
      }

      public void pause() {
         this.paused = true;
         this.lastPauseToggleTime = System.currentTimeMillis();
      }

      public void resume() {
         this.paused = false;
         this.lastPauseToggleTime = System.currentTimeMillis();
      }

      public boolean isPaused() {
         return this.paused;
      }

      public void run() {
         if (this.player.isOnline() && CloudSwordListener.this.cloudSword.isCloudSword(this.player.getInventory().getItemInMainHand())) {
            if (!this.paused) {
               this.ticksElapsed++;
               if (this.ticksElapsed % 2 == 0) {
                  this.spawnClouds();
               }

               if (this.ticksElapsed >= this.maxDuration) {
                  this.finish();
                  return;
               }
            }

            this.updateActionBar();
         } else {
            this.finish();
         }
      }

      private void spawnClouds() {
         Location playerLoc = this.player.getLocation();
         int y = playerLoc.getBlockY() - 1;

         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               Location blockLoc = new Location(this.player.getWorld(), playerLoc.getBlockX() + x, y, playerLoc.getBlockZ() + z);
               Block block = blockLoc.getBlock();
               if (!CloudSwordListener.this.plugin.isRegionProtected(blockLoc) && (block.getType() == Material.AIR || block.getType() == Material.SNOW_BLOCK)) {
                  if (!this.originalBlocks.containsKey(blockLoc)) {
                     this.originalBlocks.put(blockLoc.clone(), block.getType());
                  }

                  block.setType(Material.SNOW_BLOCK);
                  Location particleLoc = blockLoc.clone().add(0.5, 0.8, 0.5);
                  Bukkit.getScheduler().runTaskLater(CloudSwordListener.this.plugin, () -> {
                     if (block.getType() == Material.SNOW_BLOCK) {
                        Material original = this.originalBlocks.getOrDefault(blockLoc, Material.AIR);
                        block.setType(original);
                        this.player.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 12, 0.2, 0.2, 0.2, 0.0);
                     }
                  }, this.cloudLifetimeTicks);
               }
            }
         }
      }

      private void updateActionBar() {
         int totalSegments = 6;
         double progress = (double)this.ticksElapsed / this.maxDuration;
         int filled = (int)Math.round(totalSegments * Math.max(0.0, Math.min(1.0, progress)));
         int brightCount = totalSegments - filled;
         StringBuilder bar = new StringBuilder("");

         for (int i = 0; i < totalSegments; i++) {
            if (i < brightCount) {
               bar.append("§f⍣");
            } else {
               bar.append("§7⍣");
            }

            if (i < totalSegments - 1) {
               bar.append(" ");
            }
         }

         bar.append("");
         if (this.paused) {
            bar.append(" ").append(CloudSwordListener.this.plugin.getMessage("weapon-messages.cloud-sword-pause"));
         }

         this.player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(bar.toString()));
      }

      private void finish() {
         this.cancel();
         CloudSwordListener.this.activeAbilities.remove(this.player.getUniqueId());
         int cooldownSeconds = CloudSwordListener.this.plugin.getConfig().getInt("weapons.cloud_sword.cooldown", 25);
         CloudSwordListener.this.cooldowns.put(this.player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
         this.player.sendActionBar(Component.text(""));
         this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7F, 1.2F);
      }
   }
}
