package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.CustomEventStartEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ActionEvent extends CustomEvent {
   private static final List<ActionEvent> blueprintEvents = new ArrayList<>();
   private static final Set<UUID> playerCooldowns = new HashSet<>();
   private final double chance;
   private final List<Material> breakableMaterials;
   private Player player;
   private BukkitTask playerCooldownTask;

   public ActionEvent(CustomEventsConfigFields customEventsConfigFields) {
      super(customEventsConfigFields);
      this.chance = customEventsConfigFields.getChance();
      this.breakableMaterials = customEventsConfigFields.getBreakableMaterials();
      this.setPrimaryCustomBossFilenames(this.primaryCustomBossFilenames);
   }

   public static void shutdown() {
      blueprintEvents.clear();
      playerCooldowns.clear();
   }

   public static void initializeBlueprintEvents() {
      if (EventsConfig.isActionEventsEnabled()) {
         for (CustomEventsConfigFields customEventsConfigFields : CustomEventsConfig.getCustomEvents().values()) {
            if (customEventsConfigFields.isEnabled()) {
               switch (customEventsConfigFields.getEventType()) {
                  case BREAK_BLOCK:
                  case FISH:
                  case TILL_SOIL:
                     blueprintEvents.add(new ActionEvent(customEventsConfigFields));
               }
            }
         }
      }
   }

   public boolean checkBlockBreakStartConditions(Material material) {
      return ThreadLocalRandom.current().nextDouble() >= this.chance ? false : this.breakableMaterials.contains(material);
   }

   public boolean checkFishStartConditions() {
      return ThreadLocalRandom.current().nextDouble() < this.chance;
   }

   public boolean checkTillSoilStartConditions() {
      return ThreadLocalRandom.current().nextDouble() < this.chance;
   }

   public void instantiateEvent(Location location, Player player) {
      if (!PeaceBannerConfig.isSuppressEvents() || !PeaceBannerManager.isProtected(location)) {
         UUID playerId = player.getUniqueId();
         if (playerCooldowns.add(playerId)) {
            ActionEvent actionEvent = null;
            boolean started = false;

            try {
               actionEvent = new ActionEvent(this.customEventsConfigFields);
               actionEvent.player = player;
               actionEvent.setEventStartLocation(location);
               CustomEventStartEvent customEventStartEvent = new CustomEventStartEvent(actionEvent);
               new EventCaller(customEventStartEvent);
               if (customEventStartEvent.isCancelled() || !actionEvent.startConditions.areValid()) {
                  return;
               }

               List<CustomBossesConfigFields> definitions = new ArrayList<>();

               for (String filename : this.primaryCustomBossFilenames) {
                  CustomBossesConfigFields definition = CustomBossesConfig.getCustomBoss(filename);
                  if (definition == null) {
                     Logger.warn("Missing custom boss " + filename + "; cancelled action event " + this.customEventsConfigFields.getFilename());
                     return;
                  }

                  definitions.add(definition);
               }

               if (definitions.isEmpty()) {
                  Logger.warn("No bosses configured; cancelled action event " + this.customEventsConfigFields.getFilename());
                  return;
               }

               for (CustomBossesConfigFields definition : definitions) {
                  actionEvent.primaryEliteMobs.add(new CustomBossEntity(definition));
               }

               Iterator var15 = actionEvent.primaryEliteMobs.iterator();

               CustomBossEntity customBossEntity;
               do {
                  if (!var15.hasNext()) {
                     actionEvent.start();
                     started = true;
                     return;
                  }

                  customBossEntity = (CustomBossEntity)var15.next();
                  customBossEntity.spawn(actionEvent.getEventStartLocation(), false);
               } while (customBossEntity.exists());

               Logger.warn("Boss spawn was refused; cancelled action event " + this.customEventsConfigFields.getFilename());
            } finally {
               if (!started) {
                  playerCooldowns.remove(playerId);
                  if (actionEvent != null) {
                     actionEvent.rollbackStartup();
                  }
               }
            }
         }
      }
   }

   private void rollbackStartup() {
      if (this.playerCooldownTask != null) {
         this.playerCooldownTask.cancel();
      }

      if (this.eventWatchdog != null) {
         this.eventWatchdog.cancel();
      }

      for (CustomBossEntity boss : this.primaryEliteMobs) {
         try {
            boss.remove(RemovalReason.OTHER);
         } catch (RuntimeException var4) {
            Logger.warn("Failed to remove rejected action-event boss " + boss.getCustomBossesConfigFields().getFilename() + ": " + var4.getMessage());
         }
      }
   }

   @Override
   public void startModifiers() {
      UUID playerUUID = this.player.getUniqueId();
      playerCooldowns.add(playerUUID);
      this.playerCooldownTask = Bukkit.getScheduler()
         .runTaskLater(MetadataHandler.PLUGIN, () -> playerCooldowns.remove(playerUUID), 1200L * EventsConfig.getActionEventMinimumCooldown());
   }

   @Override
   public void eventWatchdog() {
   }

   @Override
   public void endModifiers() {
   }

   public static class ActionEventEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGH
      )
      public void onBlockBreakEvent(BlockBreakEvent event) {
         if (!ActionEvent.playerCooldowns.contains(event.getPlayer().getUniqueId())) {
            if (CustomEvent.isLocationValid(event.getBlock().getLocation())) {
               for (ActionEvent actionEvent : ActionEvent.blueprintEvents) {
                  if (actionEvent.eventType.equals(CustomEvent.EventType.BREAK_BLOCK) && actionEvent.checkBlockBreakStartConditions(event.getBlock().getType())
                     )
                   {
                     actionEvent.instantiateEvent(event.getBlock().getLocation().clone().add(new Vector(0.5, 0.0, 0.5)), event.getPlayer());
                  }
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGH
      )
      public void onFishEvent(PlayerFishEvent event) {
         if (event.getCaught() != null) {
            if (!ActionEvent.playerCooldowns.contains(event.getPlayer().getUniqueId())) {
               if (CustomEvent.isLocationValid(event.getCaught().getLocation())) {
                  for (ActionEvent actionEvent : ActionEvent.blueprintEvents) {
                     if (actionEvent.eventType.equals(CustomEvent.EventType.FISH) && actionEvent.checkFishStartConditions()) {
                        actionEvent.instantiateEvent(event.getCaught().getLocation(), event.getPlayer());
                     }
                  }
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGH
      )
      public void onTillSoil(PlayerInteractEvent event) {
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock() != null) {
               if (!ActionEvent.playerCooldowns.contains(event.getPlayer().getUniqueId())) {
                  Location location = event.getClickedBlock().getLocation().clone().add(new Vector(0.5, 1.0, 0.5));
                  if (CustomEvent.isLocationValid(location)) {
                     if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_HOE)
                        || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_HOE)
                        || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_HOE)
                        || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.STONE_HOE)
                        || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.WOODEN_HOE)
                        || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NETHERITE_HOE)) {
                        if (event.getClickedBlock().getType().equals(Material.DIRT) || event.getClickedBlock().getType().equals(Material.GRASS_BLOCK)) {
                           for (ActionEvent actionEvent : ActionEvent.blueprintEvents) {
                              if (actionEvent.eventType.equals(CustomEvent.EventType.TILL_SOIL) && actionEvent.checkTillSoilStartConditions()) {
                                 actionEvent.instantiateEvent(location, event.getPlayer());
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
