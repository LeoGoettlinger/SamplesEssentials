package com.samplecraft.darkdrop.manager;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.model.BadLootType;
import com.samplecraft.darkdrop.model.DropSettings;
import com.samplecraft.darkdrop.model.DropTarget;
import com.samplecraft.darkdrop.model.PendingChest;
import com.samplecraft.darkdrop.util.MessageUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DropManager {
   private static final String MARKER_KEY = "darkdrop_marker";
   private final DarkDrop plugin;
   private final List<UUID> activeDrops = new ArrayList<>();
   private final List<BukkitTask> tasks = new ArrayList<>();
   private final Map<String, PendingChest> pendingChests = new HashMap<>();
   private static final long DESPAWN_TICKS = 12000L;
   private static final long DESPAWN_AFTER_OPEN_TICKS = 600L;

   public DropManager(DarkDrop plugin) {
      this.plugin = plugin;
   }

   public void startDrop(Player origin, DropSettings settings, DropTarget target) {
      if (origin != null && settings != null && target != null) {
         Location land = target.getLandLocation();
         if (land != null && land.getWorld() != null) {
            World world = land.getWorld();
            int minBuild = world.getMinHeight() + 5;
            int maxBuild = world.getMaxHeight() - 5;
            double clampedY = Math.max((double)minBuild, Math.min((double)maxBuild, land.getY()));
            land.setY(clampedY);
            Location start = land.clone();
            start.setY(Math.min((double)maxBuild, land.getY() + this.plugin.getConfigManager().getSpawnHeight()));
            BlockData visualData = Material.CHEST.createBlockData();
            BlockDisplay display = (BlockDisplay)world.spawn(start, BlockDisplay.class, bd -> {
               bd.setBlock(visualData);
               bd.setGravity(false);
               bd.setPersistent(true);
               bd.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "darkdrop_marker"), PersistentDataType.STRING, "active");
            });
            this.activeDrops.add(display.getUniqueId());
            origin.showTitle(
               Title.title(
                  this.plugin.getConfigManager().rawComponent("drop-start-title"),
                  this.plugin.getConfigManager().rawComponent("drop-start-subtitle"),
                  Times.times(Duration.ofMillis(200L), Duration.ofMillis(2000L), Duration.ofMillis(600L))
               )
            );
            origin.sendMessage(this.plugin.getConfigManager().component("drop-start-chat"));
            world.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.5F, 0.7F);
            world.spawnParticle(Particle.END_ROD, start, 30, 0.5, 0.5, 0.5, 0.1);
            BukkitTask task = new DropManager.SinkTask(display, land, settings, origin.getUniqueId())
               .runTaskTimer(this.plugin, 0L, this.plugin.getConfigManager().getUpdateIntervalTicks());
            this.tasks.add(task);
         } else {
            origin.sendMessage(this.plugin.getConfigManager().component("invalid-coordinates"));
         }
      }
   }

   public synchronized void cancelAll() {
      for (BukkitTask task : new ArrayList<>(this.tasks)) {
         if (!task.isCancelled()) {
            task.cancel();
         }
      }

      this.tasks.clear();

      for (UUID id : new ArrayList<>(this.activeDrops)) {
         Entity e = Bukkit.getEntity(id);
         if (e != null) {
            e.remove();
         }
      }

      this.activeDrops.clear();

      for (PendingChest pc : new ArrayList<>(this.pendingChests.values())) {
         if (pc.getDespawnTask() != null) {
            pc.getDespawnTask().cancel();
         }

         this.despawnChest(pc);
      }

      this.pendingChests.clear();
   }

   public synchronized void onChestOpen(Location loc, Player opener) {
      final PendingChest pc = this.pendingChests.get(blockKey(loc));
      if (pc != null && !pc.isConsumed()) {
         pc.setConsumed(true);
         if (pc.getDespawnTask() != null) {
            pc.getDespawnTask().cancel();
         }

         DropSettings settings = pc.getSettings();
         if (pc.isGoodOutcome()) {
            if (opener != null && opener.isOnline()) {
               opener.sendMessage(this.plugin.getConfigManager().component("loot-good-chat"));
               if (settings.isUseCommandsAsGoodLoot() && !settings.getGoodLootCommands().isEmpty()) {
                  opener.sendMessage(this.plugin.getConfigManager().component("loot-good-commands-info", "count", settings.getGoodLootCommands().size()));
                  this.executeGoodLootCommands(pc.getLocation(), settings, opener);
               }
            }

            World world = loc.getWorld();
            if (world != null) {
               world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
               world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 50, 1.0, 1.0, 1.0, 0.5);
            }
         } else {
            this.triggerBadLoot(pc.getLocation(), settings, opener);
         }

         BukkitTask cleanup = (new BukkitRunnable() {
            {
               Objects.requireNonNull(DropManager.this);
            }

            public void run() {
               DropManager.this.despawnChest(pc);
            }
         }).runTaskLater(this.plugin, 600L);
         pc.setDespawnTask(cleanup);
      }
   }

   public PendingChest getPendingChest(Location loc) {
      return loc != null && loc.getWorld() != null ? this.pendingChests.get(blockKey(loc)) : null;
   }

   private synchronized void placeChest(Location land, DropSettings settings, boolean good, UUID originId) {
      World world = land.getWorld();
      if (world != null) {
         int x = land.getBlockX();
         int y = land.getBlockY();
         int z = land.getBlockZ();
         int targetY = y;

         for (int dy = 0; dy <= 5; dy++) {
            Block b = world.getBlockAt(x, y + dy, z);
            Material m = b.getType();
            if (m.isAir() || m == Material.WATER || m == Material.LAVA || m == Material.SHORT_GRASS || m == Material.TALL_GRASS || m == Material.SNOW) {
               targetY = y + dy;
               break;
            }
         }

         Block chestBlock = world.getBlockAt(x, targetY, z);
         chestBlock.setType(Material.CHEST);

         try {
            if (chestBlock.getBlockData() instanceof Directional dir) {
               Player origin = Bukkit.getPlayer(originId);
               if (origin != null) {
                  BlockFace face = yawToFace(origin.getLocation().getYaw());
                  dir.setFacing(face);
                  chestBlock.setBlockData(dir);
               }
            }
         } catch (ClassCastException var18) {
         }

         if (good && chestBlock.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();
            List<ItemStack> lootItems = settings.getGoodLootItems();
            int amount = Math.min(settings.getGoodAmount(), 64);
            if (!lootItems.isEmpty()) {
               int slot = 0;

               for (int i = 0; i < amount && slot < inv.getSize(); i++) {
                  ItemStack template = lootItems.get((int)(Math.random() * lootItems.size()));
                  if (template != null && !template.getType().isAir()) {
                     inv.setItem(slot++, template.clone());
                  }
               }
            }

            chest.update();
         }

         Location chestLoc = new Location(world, x + 0.5, targetY, z + 0.5);
         final PendingChest pc = new PendingChest(chestLoc, settings, good, originId);
         this.pendingChests.put(blockKey(chestLoc), pc);
         BukkitTask despawn = (new BukkitRunnable() {
            {
               Objects.requireNonNull(DropManager.this);
            }

            public void run() {
               DropManager.this.despawnChest(pc);
            }
         }).runTaskLater(this.plugin, 12000L);
         pc.setDespawnTask(despawn);
         world.playSound(chestLoc, Sound.BLOCK_CHEST_LOCKED, 1.2F, 0.8F);
         world.spawnParticle(Particle.DRAGON_BREATH, chestLoc, 20, 0.4, 0.4, 0.4, 0.02);
      }
   }

   private static BlockFace yawToFace(float yaw) {
      float y = (yaw % 360.0F + 360.0F) % 360.0F;
      if (y < 45.0F || y >= 315.0F) {
         return BlockFace.SOUTH;
      } else if (y < 135.0F) {
         return BlockFace.WEST;
      } else {
         return y < 225.0F ? BlockFace.NORTH : BlockFace.EAST;
      }
   }

   private synchronized void despawnChest(PendingChest pc) {
      if (pc != null) {
         if (pc.getDespawnTask() != null && !pc.getDespawnTask().isCancelled()) {
            pc.getDespawnTask().cancel();
         }

         Location loc = pc.getLocation();
         this.pendingChests.remove(blockKey(loc));
         if (loc.getWorld() != null) {
            Block block = loc.getBlock();
            if (block.getType() == Material.CHEST && block.getState() instanceof Chest chest) {
               for (ItemStack item : chest.getInventory().getContents()) {
                  if (item != null && !item.getType().isAir()) {
                     block.getWorld().dropItemNaturally(block.getLocation(), item);
                  }
               }

               chest.getInventory().clear();
               block.setType(Material.AIR);
               block.getWorld().playSound(block.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.6F, 0.6F);
            }
         }
      }
   }

   private static String blockKey(Location loc) {
      return loc.getWorld().getUID() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
   }

   private void executeGoodLootCommands(Location land, DropSettings settings, Player opener) {
      CommandSender console = Bukkit.getConsoleSender();
      String playerName = opener != null ? opener.getName() : "Server";
      String worldName = land.getWorld() != null ? land.getWorld().getName() : "world";
      int count = 0;

      for (String rawCmd : settings.getGoodLootCommands()) {
         if (rawCmd != null && !rawCmd.isBlank()) {
            String cmd = rawCmd.replace("{player}", playerName)
               .replace("{x}", String.format(Locale.ROOT, "%.2f", land.getX()))
               .replace("{y}", String.format(Locale.ROOT, "%.2f", land.getY()))
               .replace("{z}", String.format(Locale.ROOT, "%.2f", land.getZ()))
               .replace("{world}", worldName)
               .replace("{amount}", String.valueOf(settings.getGoodAmount()));
            String trimmed = cmd.strip();
            if (trimmed.startsWith("/")) {
               trimmed = trimmed.substring(1);
            }

            String finalCmd = trimmed;
            Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(console, finalCmd));
            count++;
         }
      }

      this.plugin.getLogger().info("DarkDrop: " + count + " guten Loot-Commands ausgefuehrt fuer " + playerName);
   }

   public void triggerBadLoot(Location land, DropSettings settings, Player origin) {
      World world = land.getWorld();
      if (world != null) {
         if (origin != null && origin.isOnline()) {
            origin.sendMessage(this.plugin.getConfigManager().component("loot-bad-chat"));
         }

         BadLootType type = settings.getBadLootType();
         int amount = settings.getBadAmount();
         switch (type) {
            case BAD_ITEM:
               ItemStack badItem = new ItemStack(Material.DEAD_BUSH, Math.max(1, amount));
               world.dropItemNaturally(land, badItem);
               break;
            case FIRE:
               int radius = this.plugin.getConfigManager().getFireRadius();
               int duration = this.plugin.getConfigManager().getFireDurationSeconds();

               for (int x = -radius; x <= radius; x++) {
                  for (int z = -radius; z <= radius; z++) {
                     Block b = world.getBlockAt(land.clone().add(x, 0.0, z));
                     if (b.getType().isAir()) {
                        b.setType(Material.FIRE);
                     }
                  }
               }

               for (Player p : world.getNearbyPlayers(land, 4.0, 4.0, 4.0)) {
                  p.setFireTicks(duration * 20);
               }

               world.playSound(land, Sound.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
               break;
            case LIGHTNING:
               double dmg = this.plugin.getConfigManager().getLightningDamage();

               for (int i = 0; i < amount; i++) {
                  Location l = land.clone().add((Math.random() - 0.5) * 2.0, 0.0, (Math.random() - 0.5) * 2.0);
                  world.strikeLightningEffect(l);
                  if (dmg > 0.0) {
                     for (Player p : world.getNearbyPlayers(l, 3.0, 3.0, 3.0)) {
                        p.damage(dmg);
                     }
                  }
               }

               world.playSound(land, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
               break;
            case EXPLOSION:
               double dmg = this.plugin.getConfigManager().getExplosionDamage();
               world.createExplosion(land, 2.0F, false, false);
               if (dmg > 0.0) {
                  for (Player p : world.getNearbyPlayers(land, 5.0, 5.0, 5.0)) {
                     p.damage(dmg);
                  }
               }

               world.playSound(land, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
               break;
            case POISON:
               int dur = this.plugin.getConfigManager().getEffectDurationSeconds() * 20;
               int amp = this.plugin.getConfigManager().getEffectAmplifier();

               for (Player p : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                  p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, amp, false, true, true));
               }

               world.playSound(land, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.0F);
               break;
            case VOID_EFFECT:
               int dur = this.plugin.getConfigManager().getEffectDurationSeconds() * 20;
               int amp = this.plugin.getConfigManager().getEffectAmplifier();

               for (Player p : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                  p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, dur, amp, false, true, true));
                  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, dur, amp, false, true, true));
               }

               world.playSound(land, Sound.ENTITY_ENDERMAN_STARE, 1.0F, 0.5F);
               break;
            case WITHER:
               int dur = this.plugin.getConfigManager().getEffectDurationSeconds() * 20;
               int amp = this.plugin.getConfigManager().getEffectAmplifier();

               for (Player p : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                  p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, dur, amp, false, true, true));
               }

               world.playSound(land, Sound.ENTITY_WITHER_AMBIENT, 1.0F, 1.0F);
               break;
            case NOTHING:
               world.playSound(land, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0F, 1.0F);
         }
      }
   }

   private class SinkTask extends BukkitRunnable {
      private final BlockDisplay display;
      private final Location land;
      private final DropSettings settings;
      private final UUID originId;
      private final double stepPerTick;
      private double currentY;

      SinkTask(BlockDisplay display, Location land, DropSettings settings, UUID originId) {
         Objects.requireNonNull(DropManager.this);
         super();
         this.display = display;
         this.land = land;
         this.settings = settings;
         this.originId = originId;
         this.currentY = display.getLocation().getY();
         double blocksPerSecond = DropManager.this.plugin.getConfigManager().getDescendSpeed();
         this.stepPerTick = blocksPerSecond / 20.0 * DropManager.this.plugin.getConfigManager().getUpdateIntervalTicks();
      }

      public void run() {
         if (this.display.isValid() && !this.display.isDead()) {
            double targetY = this.land.getY();
            double remaining = this.currentY - targetY;
            if (remaining <= this.stepPerTick) {
               this.display.teleport(this.land);
               this.onLand();
               this.finish();
            } else {
               this.currentY = this.currentY - this.stepPerTick;
               Location newLoc = new Location(this.land.getWorld(), this.land.getX(), this.currentY, this.land.getZ());
               this.display.teleport(newLoc);
               Player origin = Bukkit.getPlayer(this.originId);
               if (origin != null && origin.isOnline()) {
                  int remainingTicks = (int)(remaining / DropManager.this.plugin.getConfigManager().getDescendSpeed() * 20.0);
                  int seconds = Math.max(0, remainingTicks / 20);
                  int height = (int)Math.max(0.0, remaining);
                  String actionbar = DropManager.this.plugin.getConfigManager().raw("actionbar-descending", "height", height, "seconds", seconds);
                  origin.sendActionBar(MessageUtil.toComponent(actionbar));
               }

               this.land.getWorld().spawnParticle(Particle.DRAGON_BREATH, newLoc, 5, 0.2, 0.2, 0.2, 0.01);
            }
         } else {
            this.finish();
         }
      }

      private void onLand() {
         Player origin = Bukkit.getPlayer(this.originId);
         if (origin != null && origin.isOnline()) {
            origin.showTitle(
               Title.title(
                  DropManager.this.plugin.getConfigManager().rawComponent("drop-land-title"),
                  DropManager.this.plugin.getConfigManager().rawComponent("drop-land-subtitle"),
                  Times.times(Duration.ofMillis(200L), Duration.ofMillis(1500L), Duration.ofMillis(400L))
               )
            );
            origin.sendMessage(DropManager.this.plugin.getConfigManager().component("drop-land-chat"));
         }

         this.land.getWorld().playSound(this.land, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5F, 0.6F);
         this.land.getWorld().spawnParticle(Particle.EXPLOSION, this.land, 8, 1.0, 1.0, 1.0, 0.0);
         double goodRatio = this.settings.normalizedGoodRatio();
         double roll = Math.random();
         boolean good = roll < goodRatio;
         if (this.display.isValid() && !this.display.isDead()) {
            this.display.remove();
         }

         DropManager.this.placeChest(this.land, this.settings, good, this.originId);
      }

      private void finish() {
         try {
            this.cancel();
         } catch (IllegalStateException var2) {
         }

         DropManager.this.tasks.remove(this);
         DropManager.this.activeDrops.remove(this.display.getUniqueId());
      }
   }
}
