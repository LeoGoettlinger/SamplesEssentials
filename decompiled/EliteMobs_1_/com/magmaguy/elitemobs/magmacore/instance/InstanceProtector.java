package com.magmaguy.elitemobs.magmacore.instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.potion.PotionEffectType;

public class InstanceProtector implements Listener {
   private static final Map<UUID, WorldProtectionRules> protectedWorlds = new HashMap<>();
   private static final HashSet<UUID> bypassingPlayers = new HashSet<>();
   private static ContainerAllowlist containerAllowlist;

   public static void addProtectedWorld(World world) {
      addProtectedWorld(world, WorldProtectionRules.strict());
   }

   public static void addProtectedWorld(World world, WorldProtectionRules rules) {
      protectedWorlds.put(world.getUID(), rules);
   }

   public static boolean removeProtectedWorld(World world) {
      return protectedWorlds.remove(world.getUID()) != null;
   }

   public static boolean isProtectedWorld(World world) {
      return world != null && protectedWorlds.containsKey(world.getUID());
   }

   public static WorldProtectionRules getRules(World world) {
      return world == null ? null : protectedWorlds.get(world.getUID());
   }

   public static void setContainerAllowlist(ContainerAllowlist allowlist) {
      containerAllowlist = allowlist;
   }

   public static boolean toggleBypass(UUID playerUUID) {
      if (bypassingPlayers.contains(playerUUID)) {
         bypassingPlayers.remove(playerUUID);
         return false;
      } else {
         bypassingPlayers.add(playerUUID);
         return true;
      }
   }

   public static void shutdown() {
      protectedWorlds.clear();
      bypassingPlayers.clear();
      containerAllowlist = null;
   }

   public static void addPvpPreventedWorld(World world) {
      WorldProtectionRules rules = protectedWorlds.computeIfAbsent(world.getUID(), k -> WorldProtectionRules.strict());
      rules.setPreventFriendlyFire(true);
   }

   public static void removePvpPreventedWorld(World world) {
      WorldProtectionRules rules = protectedWorlds.get(world.getUID());
      if (rules != null) {
         rules.setPreventFriendlyFire(false);
      }
   }

   public static void addRedstonePreventedWorld(World world) {
      WorldProtectionRules rules = protectedWorlds.computeIfAbsent(world.getUID(), k -> WorldProtectionRules.strict());
      rules.setPreventRedstoneInteraction(true);
   }

   public static void removeRedstonePreventedWorld(World world) {
      WorldProtectionRules rules = protectedWorlds.get(world.getUID());
      if (rules != null) {
         rules.setPreventRedstoneInteraction(false);
      }
   }

   private boolean shouldBypass(Player player) {
      return player != null && bypassingPlayers.contains(player.getUniqueId());
   }

   private static WorldProtectionRules rulesFor(World world) {
      return world == null ? null : protectedWorlds.get(world.getUID());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void removeProtectedInstance(WorldUnloadEvent event) {
      protectedWorlds.remove(event.getWorld().getUID());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventPlayerBlockDamage(BlockDamageEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventPlayerBlockBreak(BlockBreakEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBlockBurnDamage(BlockBurnEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventPlayerBlockPlace(BlockCanBuildEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (event.getPlayer() == null || !this.shouldBypass(event.getPlayer())) {
            event.setBuildable(false);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBlockExplosionEvent(BlockExplodeEvent event) {
      WorldProtectionRules rules = rulesFor(event.getBlock().getWorld());
      if (rules != null) {
         if (!rules.isAllowExplosions()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventEntityExplosionEvent(EntityExplodeEvent event) {
      WorldProtectionRules rules = rulesFor(event.getLocation().getWorld());
      if (rules != null) {
         if (!rules.isAllowExplosions()) {
            event.blockList().clear();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventTntPrimeEvent(TNTPrimeEvent event) {
      WorldProtectionRules rules = rulesFor(event.getBlock().getWorld());
      if (rules != null) {
         if (!rules.isAllowExplosions()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBlockFadeEvent(BlockFadeEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (!event.getBlock().getType().equals(Material.FROSTED_ICE)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBonemeal(BlockFertilizeEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (event.getPlayer() == null || !this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventLiquidFlow(BlockFromToEvent event) {
      WorldProtectionRules rules = rulesFor(event.getBlock().getWorld());
      if (rules != null) {
         if (!rules.isAllowLiquidFlow()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBlockFire(BlockIgniteEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventBlockPlace(BlockPlaceEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventLiquidPlace(PlayerBucketEmptyEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventLeafDecay(LeavesDecayEvent event) {
      if (isProtectedWorld(event.getBlock().getWorld())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventVanillaMobSpawning(CreatureSpawnEvent event) {
      if (!event.getSpawnReason().equals(SpawnReason.CUSTOM)) {
         WorldProtectionRules rules = rulesFor(event.getLocation().getWorld());
         if (rules != null) {
            if (rules.isPreventVanillaMobSpawning()) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventFriendlyFireInDungeon(EntityDamageByEntityEvent event) {
      WorldProtectionRules rules = rulesFor(event.getEntity().getWorld());
      if (rules != null && rules.isPreventFriendlyFire()) {
         if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventDoorOpening(PlayerInteractEvent event) {
      WorldProtectionRules rules = rulesFor(event.getPlayer().getWorld());
      if (rules != null) {
         if (event.getClickedBlock() != null) {
            if (!this.shouldBypass(event.getPlayer())) {
               Material material = event.getClickedBlock().getType();
               String name = material.toString().toLowerCase(Locale.ROOT);
               if (!name.endsWith("_door") && !name.endsWith("_trapdoor") && !name.endsWith("_fence_gate")) {
                  if (event.getAction() == Action.PHYSICAL && rules.isPreventRedstoneInteraction()) {
                     event.setCancelled(true);
                  }
               } else {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventEntityChangeBlockEvent(EntityChangeBlockEvent event) {
      if (isProtectedWorld(event.getEntity().getWorld())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventCobwebPotions(LingeringPotionSplashEvent event) {
      if (isProtectedWorld(event.getEntity().getWorld())) {
         if (event.getEntity().getShooter() != null) {
            if (event.getEntity().getShooter() instanceof Player) {
               event.getEntity().getEffects().forEach(potionEffect -> {
                  if (potionEffect.getType().equals(PotionEffectType.WEAVING)) {
                     event.setCancelled(true);
                  }
               });
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventCobwebPotions(PotionSplashEvent event) {
      if (isProtectedWorld(event.getEntity().getWorld())) {
         if (event.getEntity().getShooter() != null) {
            if (event.getEntity().getShooter() instanceof Player) {
               event.getEntity().getEffects().forEach(potionEffect -> {
                  if (potionEffect.getType().equals(PotionEffectType.WEAVING)) {
                     event.setCancelled(true);
                  }
               });
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventSignEdits(SignChangeEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventContainerAccess(PlayerInteractEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            Block block = event.getClickedBlock();
            if (block != null) {
               Material type = block.getType();
               if (type == Material.CHEST
                  || type == Material.TRAPPED_CHEST
                  || type == Material.BARREL
                  || type == Material.DECORATED_POT
                  || type == Material.SHULKER_BOX
                  || type.name().endsWith("_SHULKER_BOX")) {
                  if (containerAllowlist != null && containerAllowlist.isAllowed(block)) {
                     return;
                  }

                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventDragonEggInteraction(PlayerInteractEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.DRAGON_EGG) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventFlowerpotInteraction(PlayerInteractEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            Block block = event.getClickedBlock();
            if (block != null) {
               String name = block.getType().name();
               if (block.getType() == Material.FLOWER_POT || name.startsWith("POTTED_")) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventElytraGlideStart(EntityToggleGlideEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (event.isGliding()) {
            WorldProtectionRules rules = rulesFor(player.getWorld());
            if (rules != null && !rules.isAllowElytra()) {
               if (!this.shouldBypass(player)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventElytraOnWorldEnter(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      if (player.isGliding()) {
         WorldProtectionRules rules = rulesFor(player.getWorld());
         if (rules != null && !rules.isAllowElytra()) {
            if (!this.shouldBypass(player)) {
               player.setGliding(false);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void enforceNoElytraInProtectedWorld(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (player.isGliding()) {
         WorldProtectionRules rules = rulesFor(player.getWorld());
         if (rules != null && !rules.isAllowElytra()) {
            if (!this.shouldBypass(player)) {
               player.setGliding(false);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventFlyToggle(PlayerToggleFlightEvent event) {
      WorldProtectionRules rules = rulesFor(event.getPlayer().getWorld());
      if (rules != null && rules.isPreventFlyToggle()) {
         if (!event.getPlayer().isOp()) {
            if (!this.shouldBypass(event.getPlayer())) {
               if (event.isFlying()) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventItemFrameBreak(HangingBreakByEntityEvent event) {
      if (isProtectedWorld(event.getEntity().getWorld())) {
         if (event.getRemover() instanceof Player player) {
            if (this.shouldBypass(player)) {
               return;
            }
         } else if (event.getRemover() instanceof Projectile projectile && projectile.getShooter() instanceof Player playerx && this.shouldBypass(playerx)) {
            return;
         }

         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventItemFrameDamage(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof ItemFrame) {
         if (isProtectedWorld(event.getEntity().getWorld())) {
            if (event.getDamager() instanceof Player player) {
               if (this.shouldBypass(player)) {
                  return;
               }
            } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player playerx && this.shouldBypass(playerx)) {
               return;
            }

            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventItemFrameInteract(PlayerInteractEntityEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            if (event.getRightClicked() instanceof ItemFrame) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
      if (isProtectedWorld(event.getPlayer().getWorld())) {
         if (!this.shouldBypass(event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void preventArmorStandDamage(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof ArmorStand) {
         if (isProtectedWorld(event.getEntity().getWorld())) {
            if (event.getDamager() instanceof Player player) {
               if (this.shouldBypass(player)) {
                  return;
               }
            } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player playerx && this.shouldBypass(playerx)) {
               return;
            }

            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void applyEnvironmentalDamageMultipliers(EntityDamageEvent event) {
      WorldProtectionRules rules = rulesFor(event.getEntity().getWorld());
      if (rules != null) {
         switch (event.getCause()) {
            case POISON:
               event.setDamage(event.getDamage() * rules.getPoisonDamageMultiplier());
               break;
            case WITHER:
               event.setDamage(event.getDamage() * rules.getWitherDamageMultiplier());
               break;
            case FIRE_TICK:
               event.setDamage(event.getDamage() * rules.getFireDamageMultiplier());
         }
      }
   }
}
