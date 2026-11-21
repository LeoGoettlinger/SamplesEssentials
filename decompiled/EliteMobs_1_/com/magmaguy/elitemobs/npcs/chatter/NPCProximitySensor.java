package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.NPCEntityRemoveEvent;
import com.magmaguy.elitemobs.api.NPCProximityEnterEvent;
import com.magmaguy.elitemobs.api.NPCProximityLeaveEvent;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.scripts.ScriptableNPC;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class NPCProximitySensor implements Listener {
   private static final int BOUNCE_PERIOD_TICKS = 40;
   private static final double BOUNCE_HEIGHT = 0.16;
   private static final double QUEST_MARKER_RADIUS = 30.0;
   private static final NPCProximityState proximityState = new NPCProximityState();
   private static BukkitTask proximityScanTask = null;
   private static BukkitTask questIndicatorTask;
   private static int indicatorAnimationTick;
   private static double indicatorBounceOffset;
   private static final Map<NPCProximityKey, NPCProximitySensor.QuestIndicator> questIndicators = new HashMap<>();

   public NPCProximitySensor() {
      proximityScanTask = (new BukkitRunnable() {
            public void run() {
               Collection<NPCEntity> npcEntities = EntityTracker.getNpcEntities().values();
               if (!npcEntities.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
                  Map<NPCProximityKey, NPCProximitySensor.ProximityDetection> detections = new HashMap<>(Math.max(16, npcEntities.size()));
                  Map<UUID, NPCProximitySensor.PlayerQuestFacts> questFacts = new HashMap<>();

                  for (NPCEntity npcEntity : npcEntities) {
                     LivingEntity villager = npcEntity.getVillager();
                     if (villager != null && villager.isValid() && !PatrolService.isActivelyMoving(npcEntity) && !PatrolEditor.isEditing(npcEntity)) {
                        double activationRadius = npcEntity.getNPCsConfigFields().getActivationRadius();
                        NPCInteractions.NPCInteractionType interaction = npcEntity.getNPCsConfigFields().getInteractionType();
                        boolean questGiver = interaction == NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER
                           || interaction == NPCInteractions.NPCInteractionType.QUEST_GIVER;
                        double scanRadius = Math.max(activationRadius, questGiver ? 30.0 : 0.0);
                        if (!(scanRadius <= 0.0)) {
                           double activationRadiusSquared = activationRadius * activationRadius;
                           Location npcLocation = villager.getLocation();
                           boolean patrolOwnsFacing = npcEntity.getNPCsConfigFields().isPatrolFaceNearbyPlayers()
                              && PatrolService.hasConfiguredPatrol(npcEntity);
                           Vector facingDirection = null;

                           for (Entity entity : villager.getNearbyEntities(scanRadius, scanRadius, scanRadius)) {
                              if (entity instanceof Player player && player.isValid()) {
                                 Location playerLocation = player.getLocation();
                                 if (playerLocation.getWorld() != null
                                    && npcLocation.getWorld() != null
                                    && playerLocation.getWorld().getUID().equals(npcLocation.getWorld().getUID())) {
                                    double distanceSquared = playerLocation.distanceSquared(npcLocation);
                                    if (questGiver && distanceSquared <= 900.0) {
                                       NPCProximitySensor.updateQuestIndicator(npcEntity, player, questFacts);
                                    }

                                    if (!(activationRadius <= 0.0) && !(distanceSquared > activationRadiusSquared)) {
                                       Vector direction = playerLocation.toVector().subtract(npcLocation.toVector());
                                       if (!patrolOwnsFacing && direction.lengthSquared() > 0.0) {
                                          facingDirection = direction;
                                       }

                                       detections.put(
                                          new NPCProximityKey(npcEntity.getUuid(), player.getUniqueId()),
                                          new NPCProximitySensor.ProximityDetection(npcEntity, player)
                                       );
                                    }
                                 }
                              }
                           }

                           if (facingDirection != null && villager.isValid()) {
                              Location facing = npcLocation.clone().setDirection(facingDirection);
                              if (facing.getYaw() != npcLocation.getYaw() || facing.getPitch() != npcLocation.getPitch()) {
                                 villager.setRotation(facing.getYaw(), facing.getPitch());
                              }
                           }
                        }
                     }
                  }

                  NPCProximityState.ProximityChanges changes = NPCProximitySensor.proximityState.update(detections.keySet());

                  for (Entry<NPCProximityKey, NPCProximitySensor.ProximityDetection> entry : detections.entrySet()) {
                     NPCProximitySensor.ProximityDetection detection = entry.getValue();
                     if (changes.entered().contains(entry.getKey())) {
                        NPCProximitySensor.this.handleEnter(detection.npcEntity(), detection.player());
                     } else if (!detection.npcEntity().getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CHAT)) {
                        detection.npcEntity().sayDialog(detection.player());
                     }
                  }

                  for (NPCProximityKey leftKey : changes.left()) {
                     NPCEntity npcEntityx = EntityTracker.getNpcEntities().get(leftKey.npcUuid());
                     Player playerx = Bukkit.getPlayer(leftKey.playerUuid());
                     if (npcEntityx != null && playerx != null) {
                        NPCProximitySensor.this.handleLeave(npcEntityx, playerx);
                     }
                  }
               } else {
                  NPCProximitySensor.proximityState.clear();
                  NPCProximitySensor.removeIndicators(key -> true);
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 100L);
      questIndicatorTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, NPCProximitySensor::refreshQuestIndicators, 1L, 1L);
   }

   public static void shutdown() {
      if (proximityScanTask != null && !proximityScanTask.isCancelled()) {
         proximityScanTask.cancel();
         proximityScanTask = null;
      }

      proximityState.clear();
      if (questIndicatorTask != null) {
         questIndicatorTask.cancel();
      }

      questIndicatorTask = null;
      indicatorAnimationTick = 0;
      indicatorBounceOffset = 0.0;
      removeIndicators(key -> true);
   }

   private void handleEnter(NPCEntity npcEntity, Player player) {
      NPCProximityEnterEvent event = new NPCProximityEnterEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
      new EventCaller(event);
      npcEntity.runScripts(ScriptableNPC.ON_PROXIMITY_ENTER, event, player);
      npcEntity.sayGreeting(player);
   }

   private void handleLeave(NPCEntity npcEntity, Player player) {
      NPCProximityLeaveEvent event = new NPCProximityLeaveEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
      new EventCaller(event);
      npcEntity.runScripts(ScriptableNPC.ON_PROXIMITY_LEAVE, event, player);
   }

   private static void updateQuestIndicator(NPCEntity npcEntity, Player player, Map<UUID, NPCProximitySensor.PlayerQuestFacts> questFacts) {
      NPCInteractions.NPCInteractionType type = npcEntity.getNPCsConfigFields().getInteractionType();
      if (type == NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER || type == NPCInteractions.NPCInteractionType.QUEST_GIVER) {
         NPCProximityKey key = new NPCProximityKey(npcEntity.getUuid(), player.getUniqueId());
         questIndicators.computeIfAbsent(key, ignored -> new NPCProximitySensor.QuestIndicator()).update(npcEntity, player, true, questFacts);
      }
   }

   private static void refreshQuestIndicators() {
      indicatorAnimationTick = (indicatorAnimationTick + 1) % 40;
      indicatorBounceOffset = 0.08 * (1.0 + Math.sin((Math.PI * 2) * indicatorAnimationTick / 40.0 - (Math.PI / 2)));
      boolean refreshQuestState = indicatorAnimationTick % 10 == 0;
      Map<UUID, NPCProximitySensor.PlayerQuestFacts> questFacts = refreshQuestState ? new HashMap<>() : null;
      Iterator<Entry<NPCProximityKey, NPCProximitySensor.QuestIndicator>> iterator = questIndicators.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<NPCProximityKey, NPCProximitySensor.QuestIndicator> entry = iterator.next();
         NPCEntity npc = EntityTracker.getNpcEntities().get(entry.getKey().npcUuid());
         Player player = Bukkit.getPlayer(entry.getKey().playerUuid());
         if (npc != null
            && player != null
            && player.isValid()
            && npc.getVillager() != null
            && npc.getVillager().isValid()
            && !PatrolService.isActivelyMoving(npc)
            && !PatrolEditor.isEditing(npc)
            && npc.getVillager().getWorld().equals(player.getWorld())
            && !(npc.getVillager().getLocation().distanceSquared(player.getLocation()) > 900.0)) {
            entry.getValue().update(npc, player, refreshQuestState, questFacts);
         } else {
            entry.getValue().remove();
            iterator.remove();
         }
      }
   }

   private static String findQuestState(NPCEntity npcEntity, Player player, Map<UUID, NPCProximitySensor.PlayerQuestFacts> questFacts) {
      if (!PlayerData.isInMemory(player)) {
         return null;
      } else {
         NPCProximitySensor.PlayerQuestFacts facts = questFacts.computeIfAbsent(
            player.getUniqueId(), ignored -> new NPCProximitySensor.PlayerQuestFacts(player)
         );
         NPCInteractions.NPCInteractionType type = npcEntity.getNPCsConfigFields().getInteractionType();
         boolean dynamic = type == NPCInteractions.NPCInteractionType.QUEST_GIVER;
         if (!dynamic && type != NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER) {
            return null;
         } else if (dynamic && !player.hasPermission("elitemobs.quest.npc")) {
            return null;
         } else if (dynamic ? !facts.dynamicTurnIn : !facts.customTurnIns.contains(npcEntity.getNPCsConfigFields().getFilename())) {
            if (dynamic) {
               return facts.dynamicAvailable(player) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "!" : null;
            } else {
               if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null) {
                  for (String filename : npcEntity.getNPCsConfigFields().getQuestFilenames()) {
                     if (!facts.activeCustomQuests.contains(filename)
                        && CustomQuest.hasPermissionForQuest(player, CustomQuestsConfig.getCustomQuests().get(filename))) {
                        return "" + ChatColor.YELLOW + ChatColor.BOLD + "!";
                     }
                  }
               }

               return null;
            }
         } else {
            return "" + ChatColor.YELLOW + ChatColor.BOLD + "?";
         }
      }
   }

   private static void removeIndicators(Predicate<NPCProximityKey> matches) {
      Iterator<Entry<NPCProximityKey, NPCProximitySensor.QuestIndicator>> iterator = questIndicators.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<NPCProximityKey, NPCProximitySensor.QuestIndicator> entry = iterator.next();
         if (matches.test(entry.getKey())) {
            entry.getValue().remove();
            iterator.remove();
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      removeIndicators(key -> key.playerUuid().equals(event.getPlayer().getUniqueId()));
   }

   @EventHandler
   public void onWorldChanged(PlayerChangedWorldEvent event) {
      removeIndicators(key -> key.playerUuid().equals(event.getPlayer().getUniqueId()));
   }

   @EventHandler
   public void onNPCRemoved(NPCEntityRemoveEvent event) {
      removeIndicators(key -> key.npcUuid().equals(event.getNPCEntity().getUuid()));
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getView().getTitle().length() > 0) {
         for (Entity entity : event.getPlayer().getNearbyEntities(5.0, 5.0, 5.0)) {
            if (EntityTracker.isNPCEntity(entity)) {
               EntityTracker.getNPCEntity(entity).sayFarewell((Player)event.getPlayer());
            }
         }
      }
   }

   private static final class PlayerQuestFacts {
      private final List<Quest> quests;
      private final Set<String> activeCustomQuests = new HashSet<>();
      private final Set<String> customTurnIns = new HashSet<>();
      private boolean dynamicTurnIn;
      private Boolean dynamicAvailable;

      private PlayerQuestFacts(Player player) {
         this.quests = PlayerData.getQuests(player.getUniqueId());

         for (Quest quest : this.quests) {
            if (quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn()) {
               if (quest instanceof CustomQuest custom) {
                  this.activeCustomQuests.add(custom.getConfigurationFilename());
               }

               if (quest.getQuestObjectives().isOver()) {
                  if (quest instanceof DynamicQuest) {
                     this.dynamicTurnIn = true;
                  } else if (quest instanceof CustomQuest) {
                     this.customTurnIns.add(quest.getQuestTaker());
                  }
               }
            }
         }
      }

      private boolean dynamicAvailable(Player player) {
         if (this.dynamicAvailable == null) {
            this.dynamicAvailable = DynamicQuest.hasAvailableQuests(player, this.quests);
         }

         return this.dynamicAvailable;
      }
   }

   private record ProximityDetection(NPCEntity npcEntity, Player player) {
   }

   private static final class QuestIndicator {
      private FakeText display;

      private void update(NPCEntity npc, Player player, boolean refreshQuestState, Map<UUID, NPCProximitySensor.PlayerQuestFacts> questFacts) {
         String text = refreshQuestState ? NPCProximitySensor.findQuestState(npc, player, questFacts) : (this.display == null ? null : this.display.getText());
         if (text == null) {
            this.remove();
         } else {
            Location location = npc.getQuestIndicatorLocation().add(0.0, NPCProximitySensor.indicatorBounceOffset, 0.0);
            if (this.display == null) {
               this.display = VisualDisplay.createStyledFakeText(location, text, Color.fromARGB(0), true, 3.0F);
               if (this.display != null) {
                  this.display.displayTo(player);
               }
            } else {
               if (!text.equals(this.display.getText())) {
                  this.display.setText(text);
               }

               Location previous = this.display.getLocation();
               if (!location.getWorld().equals(previous.getWorld()) || location.distanceSquared(previous) > 1.0E-8) {
                  this.display.teleport(location);
               }
            }
         }
      }

      private void remove() {
         if (this.display != null) {
            this.display.remove();
         }

         this.display = null;
      }
   }
}
