package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.ArenaMenu;
import com.magmaguy.elitemobs.menus.ArrowShopMenu;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import com.magmaguy.elitemobs.menus.EliteScrollMenu;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.elitemobs.menus.RepairMenu;
import com.magmaguy.elitemobs.menus.ScrapperMenu;
import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.elitemobs.menus.SkillBonusMenu;
import com.magmaguy.elitemobs.menus.UnbindMenu;
import com.magmaguy.elitemobs.menus.gambling.BettingMenu;
import com.magmaguy.elitemobs.npcs.scripts.ScriptableNPC;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.transport.TransportModule;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCInteractions implements Listener {
   private static final Set<UUID> cooldowns = new HashSet<>();

   public static void shutdown() {
      cooldowns.clear();
   }

   public static void handleNPCInteraction(final Player player, final NPCEntity npcEntity) {
      if (!PatrolEditor.isEditing(npcEntity)) {
         UUID playerUUID = player.getUniqueId();
         if (!cooldowns.contains(playerUUID)) {
            cooldowns.add(playerUUID);
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> cooldowns.remove(playerUUID), 1L);
            ClassUnlockObjective.refresh(player);
            if (!DialogObjective.progressAtNPC(player, npcEntity)) {
               npcEntity.runScripts(ScriptableNPC.ON_INTERACT, null, player);
               switch (npcEntity.getNPCsConfigFields().getInteractionType()) {
                  case CLASS_TRAINER:
                     if (player.hasPermission("elitemobs.command") && npcEntity.getNPCsConfigFields().getClassRoot() != null) {
                        Bukkit.getScheduler()
                           .runTask(MetadataHandler.PLUGIN, () -> ClassSelectionMenu.openTrainer(player, npcEntity.getNPCsConfigFields().getClassRoot()));
                     }
                     break;
                  case TRANSPORT:
                     TransportModule transport = TransportModule.get();
                     if (transport != null) {
                        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> transport.openDestinations(player, npcEntity));
                     }
                     break;
                  case GUILD_GREETER:
                     if (player.hasPermission("elitemobs.skill.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              SkillBonusMenu.openWeaponSelectMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case CHAT:
                     npcEntity.sayDialog(player);
                     break;
                  case CUSTOM_SHOP:
                     if (player.hasPermission("elitemobs.shop.custom.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              CustomShopMenu.customShopInitializer(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case PROCEDURALLY_GENERATED_SHOP:
                     if (player.hasPermission("elitemobs.shop.dynamic.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              ProceduralShopMenu.shopInitializer(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case BAR:
                     player.sendMessage(CommandMessagesConfig.getNpcFeatureComingSoonMessage());
                  case ARENA:
                  case NONE:
                  default:
                     break;
                  case QUEST_GIVER:
                     if (player.hasPermission("elitemobs.quest.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              QuestInteractionHandler.processDynamicQuests(player, npcEntity);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case CUSTOM_QUEST_GIVER:
                     QuestInteractionHandler.processNPCQuests(player, npcEntity);
                     break;
                  case SELL:
                     if (player.hasPermission("elitemobs.shop.sell.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              SellMenu sellMenu = new SellMenu();
                              sellMenu.constructSellMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case TELEPORT_BACK:
                     if (player.hasPermission("elitemobs.back.npc")) {
                        Location previousLocation = PlayerData.getBackTeleportLocation(player);
                        if (previousLocation == null) {
                           if (npcEntity.npCsConfigFields.noPreviousLocationMessage != null) {
                              player.sendMessage(ChatColorConverter.convert(npcEntity.npCsConfigFields.noPreviousLocationMessage));
                           }
                        } else {
                           PlayerPreTeleportEvent.teleportPlayer(player, previousLocation);
                        }
                     }
                     break;
                  case SCRAPPER:
                     if (player.hasPermission("elitemobs.scrap.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              ScrapperMenu scrapperMenu = new ScrapperMenu();
                              scrapperMenu.constructScrapMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case SMELTER:
                  case ENHANCER:
                  case REFINER:
                     player.sendMessage(CommandMessagesConfig.getNpcFeatureReplacedMessage());
                     if (player.isOp() || player.hasPermission("elitemobs.*")) {
                        player.sendMessage(CommandMessagesConfig.getNpcRemoveInstructions());
                     }
                     break;
                  case REPAIRMAN:
                     if (player.hasPermission("elitemobs.repair.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              RepairMenu repairMenu = new RepairMenu();
                              repairMenu.constructRepairMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case UNBINDER:
                     if (player.hasPermission("elitemobs.unbind.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              UnbindMenu unbindMenu = new UnbindMenu();
                              unbindMenu.constructUnbinderMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case ARENA_MASTER:
                     (new BukkitRunnable() {
                        public void run() {
                           ArenaMenu arenaMenu = new ArenaMenu();
                           arenaMenu.constructArenaMenu(player, npcEntity.getNPCsConfigFields().getArenaFilename());
                        }
                     }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     break;
                  case COMMAND:
                     if (npcEntity.getNPCsConfigFields().getCommand() == null) {
                        Logger.warn("Failed to run NPC command because none is configured for " + npcEntity.getNPCsConfigFields().getFilename());
                        return;
                     }

                     (new BukkitRunnable() {
                        public void run() {
                           player.performCommand(npcEntity.getNPCsConfigFields().getCommand());
                        }
                     }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     break;
                  case ENCHANTER:
                     if (player.hasPermission("elitemobs.enchant.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              new ItemEnchantmentMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case SCROLL_APPLIER:
                     if (player.hasPermission("elitemobs.scroll.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              new EliteScrollMenu(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case ARROW_SHOP:
                     if (player.hasPermission("elitemobs.shop.arrow.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              ArrowShopMenu.openArrowShop(player);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case GAMBLING_BLACKJACK:
                     if (player.hasPermission("elitemobs.gambling.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              BettingMenu.openBettingMenu(player, BettingMenu.GameType.BLACKJACK);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case GAMBLING_COINFLIP:
                     if (player.hasPermission("elitemobs.gambling.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              BettingMenu.openBettingMenu(player, BettingMenu.GameType.COIN_FLIP);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case GAMBLING_SLOTS:
                     if (player.hasPermission("elitemobs.gambling.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              BettingMenu.openBettingMenu(player, BettingMenu.GameType.SLOTS);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
                     break;
                  case GAMBLING_HIGHERLOWER:
                     if (player.hasPermission("elitemobs.gambling.npc")) {
                        (new BukkitRunnable() {
                           public void run() {
                              BettingMenu.openBettingMenu(player, BettingMenu.GameType.HIGHER_LOWER);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                     }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerNPCInteract(PlayerInteractAtEntityEvent event) {
      this.interactWithNPC(event);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerNPCInteractGeneric(PlayerInteractEntityEvent event) {
      this.interactWithNPC(event);
   }

   private void interactWithNPC(PlayerInteractEntityEvent event) {
      if (!event.isCancelled()) {
         NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
         if (npcEntity != null) {
            if (PatrolEditor.isEditing(npcEntity)) {
               event.setCancelled(true);
            } else {
               Player player = event.getPlayer();
               if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
                  event.setCancelled(true);
                  player.sendMessage(CommandMessagesConfig.getNpcCannotRenameMessage());
               } else {
                  event.setCancelled(true);
                  handleNPCInteraction(player, npcEntity);
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getInventory() instanceof MerchantInventory inventory
         && inventory.getMerchant() instanceof Entity merchant
         && EntityTracker.getNPCEntity(merchant) != null) {
         event.setCancelled(true);
      }
   }

   public static enum NPCInteractionType {
      CLASS_TRAINER,
      TRANSPORT,
      GUILD_GREETER,
      CHAT,
      CUSTOM_SHOP,
      PROCEDURALLY_GENERATED_SHOP,
      BAR,
      ARENA,
      QUEST_GIVER,
      CUSTOM_QUEST_GIVER,
      NONE,
      SELL,
      TELEPORT_BACK,
      SCRAPPER,
      SMELTER,
      REPAIRMAN,
      ENHANCER,
      REFINER,
      UNBINDER,
      ARENA_MASTER,
      COMMAND,
      ENCHANTER,
      SCROLL_APPLIER,
      ARROW_SHOP,
      GAMBLING_BLACKJACK,
      GAMBLING_COINFLIP,
      GAMBLING_SLOTS,
      GAMBLING_HIGHERLOWER;
   }
}
