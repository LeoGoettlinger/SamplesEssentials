package vrx.economy.vRXClans;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

public class ClanListener implements Listener {
   private final VRXClans plugin;

   public ClanListener(VRXClans plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         Player player = event.getPlayer();
         this.plugin.getClanManager().updatePlayerClanVisuals(player);
         Clan clan = this.plugin.getClanManager().getClanByPlayer(player);
         if (clan != null) {
            if (!clan.getMotd().isBlank()) {
               player.sendMessage("§8[§bClan-MOTD§8] §f" + clan.getMotd());
            }

            if (!clan.getMailMessages().isEmpty()) {
               player.sendMessage("§8[§bClan Mail§8] §7You have §e" + clan.getMailMessages().size() + " §7clan mail messages. Use §e/clan mail read§7.");
            }
         }
      }, 20L);
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player p = event.getPlayer();
      if (this.plugin.getClanManager().isTeleporting(p)) {
         if (event.getFrom().getBlockX() != event.getTo().getBlockX()
            || event.getFrom().getBlockY() != event.getTo().getBlockY()
            || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            this.plugin.getClanManager().cancelTeleport(p);
            p.sendMessage(this.plugin.getLang().get("msg.base-tp-cancel"));
         }
      }
   }

   @EventHandler
   public void onPlayerDamageAny(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player p = (Player)event.getEntity();
         if (this.plugin.getClanManager().isTeleporting(p)) {
            this.plugin.getClanManager().cancelTeleport(p);
            p.sendMessage(this.plugin.getLang().get("msg.base-tp-cancel"));
         }
      }
   }

   private boolean isProtected(Location loc, Player player) {
      return this.plugin.getClanManager().isProtectedBaseLocation(loc, player);
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (this.isProtected(event.getBlock().getLocation(), event.getPlayer())) {
         event.setCancelled(true);
      } else {
         if (event.getBlock().getType() == Material.DIAMOND_ORE || event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            event.getBlock().setMetadata("vrx_player_placed", new FixedMetadataValue(this.plugin, true));
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getClickedBlock() != null) {
         Material type = event.getClickedBlock().getType();
         if ((type.name().contains("CHEST") || type.name().contains("DOOR") || type.name().contains("FURNACE") || type == Material.HOPPER)
            && this.isProtected(event.getClickedBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (this.isProtected(event.getBlock().getLocation(), player)) {
         event.setCancelled(true);
      } else {
         Clan clan = this.plugin.getClanManager().getClanByPlayer(player);
         if (clan != null) {
            if (event.getBlock().hasMetadata("vrx_player_placed")) {
               return;
            }

            double xpAmount = this.plugin.getConfig().getDouble("xp-per-blockbreak", 1.0);
            if (clan.hasXpBooster()) {
               xpAmount *= 2.0;
            }

            if (clan.addXp(xpAmount, this.plugin)) {
               this.plugin.handleClanLevelUps(clan);
            }

            Material type = event.getBlock().getType();
            if ((type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) && !clan.isQuestDiamondsDone()) {
               clan.setQuestDiamonds(clan.getQuestDiamonds() + 1);
               int target = this.plugin.getConfig().getInt("quests.diamonds.target", 1000);
               if (clan.getQuestDiamonds() >= target) {
                  clan.setQuestDiamondsDone(true);
                  double reward = this.plugin.getConfig().getDouble("quests.diamonds.reward-xp", 1500.0);
                  if (clan.addXp(reward, this.plugin)) {
                     this.plugin.handleClanLevelUps(clan);
                  }

                  this.plugin
                     .clanBroadcast(
                        clan,
                        this.plugin
                           .getLang()
                           .get("msg.quest-completed", "%quest%", this.plugin.getLang().get("quests.name-diamonds"), "%reward%", String.valueOf(reward))
                     );
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (event.getEntity() instanceof Player) {
         Player victim = (Player)event.getEntity();
         Clan victimClan = this.plugin.getClanManager().getClanByPlayer(victim);
         if (victimClan != null) {
            victimClan.setDeaths(victimClan.getDeaths() + 1);
         }

         if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            Clan killerClan = this.plugin.getClanManager().getClanByPlayer(killer);
            if (killerClan != null) {
               killerClan.setKills(killerClan.getKills() + 1);
               this.plugin.getClanManager().evaluateAchievements(killerClan);
               double xpAmount = this.plugin.getConfig().getDouble("xp-per-playerkill", 10.0);
               if (xpAmount > 0.0) {
                  if (killerClan.hasXpBooster()) {
                     xpAmount *= 2.0;
                  }

                  if (killerClan.addXp(xpAmount, this.plugin)) {
                     this.plugin.handleClanLevelUps(killerClan);
                  }
               }

               if (victimClan != null && killerClan.getActiveWar() != null && killerClan.getActiveWar().equals(victimClan.getName())) {
                  if (!this.plugin.getClanManager().canCountWarKill(killer.getUniqueId(), victim.getUniqueId())) {
                     killer.sendMessage("§cThis war kill does not count because you killed the same player again too quickly.");
                     return;
                  }

                  killerClan.setWarKills(killerClan.getWarKills() + 1);
                  this.plugin
                     .getClanManager()
                     .logClanEvent(killerClan, ClanLogEntry.LogType.WAR_KILL, killer.getName(), victim.getName(), 0.0, "War kill counted");
                  int target = this.plugin.getConfig().getInt("war-kill-target", 50);
                  if (killerClan.getWarKills() >= target) {
                     double pot = killerClan.getWarWager() * 2.0;
                     killerClan.deposit(pot);
                     this.plugin.getClanManager().finishWar(killerClan, victimClan, killerClan.getWarKills(), victimClan.getWarKills(), pot);
                     this.plugin.getClanManager().evaluateAchievements(killerClan);
                     this.plugin
                        .getClanManager()
                        .logClanEvent(killerClan, ClanLogEntry.LogType.WAR_END, killer.getName(), victimClan.getName(), pot, "Krieg gewonnen");
                     this.plugin
                        .getClanManager()
                        .logClanEvent(victimClan, ClanLogEntry.LogType.WAR_END, victim.getName(), killerClan.getName(), pot, "Krieg verloren");
                     Bukkit.broadcastMessage(
                        this.plugin
                           .getLang()
                           .get("msg.war-ended", "%winner%", killerClan.getName(), "%loser%", victimClan.getName(), "%pot%", String.valueOf(pot))
                     );
                     if (this.plugin.getConfig().getBoolean("discord.enable-war-win", true)) {
                        this.plugin.sendDiscordWebhook("war", "%winner%", killerClan.getName(), "%loser%", victimClan.getName(), "%pot%", String.valueOf(pot));
                     }

                     killerClan.resetWar();
                     victimClan.resetWar();
                  } else {
                     Bukkit.broadcastMessage(
                        this.plugin
                           .getLang()
                           .get(
                              "msg.war-kill",
                              "%killer%",
                              killer.getName(),
                              "%victim%",
                              victim.getName(),
                              "%kills%",
                              String.valueOf(killerClan.getWarKills()),
                              "%target%",
                              String.valueOf(target)
                           )
                     );
                  }
               }
            }
         }
      } else if (event.getEntity().getKiller() != null && event.getEntity() instanceof Monster) {
         Player killer = event.getEntity().getKiller();
         Clan clan = this.plugin.getClanManager().getClanByPlayer(killer);
         if (clan != null) {
            double xpAmountx = this.plugin.getConfig().getDouble("xp-per-monsterkill", 5.0);
            if (xpAmountx > 0.0) {
               if (clan.hasXpBooster()) {
                  xpAmountx *= 2.0;
               }

               if (clan.addXp(xpAmountx, this.plugin)) {
                  this.plugin.handleClanLevelUps(clan);
               }
            }

            if (event.getEntity().getType() == EntityType.ZOMBIE && !clan.isQuestZombiesDone()) {
               clan.setQuestZombies(clan.getQuestZombies() + 1);
               int target = this.plugin.getConfig().getInt("quests.zombies.target", 5000);
               if (clan.getQuestZombies() >= target) {
                  clan.setQuestZombiesDone(true);
                  double reward = this.plugin.getConfig().getDouble("quests.zombies.reward-xp", 1000.0);
                  if (clan.addXp(reward, this.plugin)) {
                     this.plugin.handleClanLevelUps(clan);
                  }

                  this.plugin
                     .clanBroadcast(
                        clan,
                        this.plugin
                           .getLang()
                           .get("msg.quest-completed", "%quest%", this.plugin.getLang().get("quests.name-zombies"), "%reward%", String.valueOf(reward))
                     );
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerDamage(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
         Player victim = (Player)event.getEntity();
         Player attacker = (Player)event.getDamager();
         Clan victimClan = this.plugin.getClanManager().getClanByPlayer(victim);
         Clan attackerClan = this.plugin.getClanManager().getClanByPlayer(attacker);
         if (victimClan != null && attackerClan != null) {
            if (victimClan.getName().equals(attackerClan.getName())) {
               if (this.plugin.getConfig().getBoolean("disable-friendly-fire")) {
                  event.setCancelled(true);
                  attacker.sendMessage(this.plugin.getLang().get("msg.ff-friendly"));
               }
            } else if (attackerClan.isAlliedWith(victimClan.getName()) && this.plugin.getConfig().getBoolean("disable-ally-fire")) {
               event.setCancelled(true);
               attacker.sendMessage(this.plugin.getLang().get("msg.ff-ally"));
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onClanChat(AsyncChatEvent event) {
      String message = PlainTextComponentSerializer.plainText().serialize(event.message());
      Player player = event.getPlayer();
      if (this.plugin.getClanManager().hasPendingRankRename(player)) {
         event.setCancelled(true);
         Bukkit.getScheduler().runTask(this.plugin, () -> this.handleRankRename(player, message));
      } else if (message.startsWith("@c ")) {
         event.setCancelled(true);
         String clanMessage = message.substring(3);
         Bukkit.getScheduler().runTask(this.plugin, () -> this.sendClanChat(player, clanMessage));
      } else {
         Clan playerClan = this.plugin.getClanManager().getClanByPlayer(player);
         if (playerClan != null && this.plugin.getConfig().getBoolean("prefix-in-global-chat")) {
            String prefixFormat = this.plugin.getConfig().getString("global-chat-prefix-format", "&8[%tagcolor%%tag%&8] ");
            Component prefix = LegacyComponentSerializer.legacyAmpersand()
               .deserialize(
                  prefixFormat.replace("%clan%", playerClan.getName()).replace("%tag%", playerClan.getTag()).replace("%tagcolor%", playerClan.getTagColor())
               );
            event.renderer(
               (source, sourceDisplayName, messageComponent, viewer) -> prefix.append(sourceDisplayName).append(Component.text(" » ")).append(messageComponent)
            );
         }
      }
   }

   private void handleRankRename(Player player, String newName) {
      Clan.ClanRank rank = this.plugin.getClanManager().consumeRankRename(player);
      Clan clan = this.plugin.getClanManager().getClanByPlayer(player);
      if (rank != null && clan != null) {
         if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
            player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
         } else if (!newName.equalsIgnoreCase("cancel") && !newName.equalsIgnoreCase("abbrechen")) {
            String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', newName)).trim();
            if (stripped.length() >= 2 && stripped.length() <= 24) {
               clan.setRankDisplayName(rank, stripped);
               this.plugin
                  .getClanManager()
                  .logClanEvent(clan, ClanLogEntry.LogType.RANK_RENAME, player.getName(), rank.name(), 0.0, "Rolle umbenannt zu " + stripped);
               player.sendMessage("§aRole §e" + rank.name() + " §awas renamed to §b" + stripped + "§a.");
               ClanGUI.openPermissionsRankMenu(player, this.plugin);
            } else {
               player.sendMessage("§cDer Rollenname muss zwischen 2 und 24 Zeichen lang sein.");
            }
         } else {
            player.sendMessage("§cUmbenennen abgebrochen.");
         }
      }
   }

   private void sendClanChat(Player player, String clanMessage) {
      Clan playerClan = this.plugin.getClanManager().getClanByPlayer(player);
      if (playerClan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         String rawFormat = this.plugin.getConfig().getString("clan-chat-format", "&8[&b%clan%&8] &7%player% &8» &f%message%");
         String finalMessage = rawFormat.replace("%clan%", playerClan.getName()).replace("%player%", player.getName()).replace("%message%", clanMessage);
         finalMessage = ChatColor.translateAlternateColorCodes('&', finalMessage);

         for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            Clan onlinePlayerClan = this.plugin.getClanManager().getClanByPlayer(onlinePlayer);
            if (onlinePlayerClan != null && onlinePlayerClan.getName().equals(playerClan.getName())) {
               onlinePlayer.sendMessage(finalMessage);
            }
         }

         for (UUID spyUuid : this.plugin.getClanManager().getSpies()) {
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.isOnline()) {
               Clan spyClan = this.plugin.getClanManager().getClanByPlayer(spy);
               if (spyClan == null || !spyClan.getName().equals(playerClan.getName())) {
                  spy.sendMessage("§8[§cSPY§8] " + finalMessage);
               }
            }
         }
      }
   }

   private void handleRankSlotClick(Player player, Clan clan, Clan.ClanRank rank, boolean rename) {
      if (rename) {
         player.closeInventory();
         this.plugin.getClanManager().startRankRename(player, rank);
         player.sendMessage("§7Type the new name for §e" + clan.getRankDisplayName(rank) + " §7in chat. §8(type cancel to abort)");
      } else {
         ClanGUI.openPermissionsToggleMenu(player, clan, rank, this.plugin);
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof ClanGUIHolder) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Clan clan = this.plugin.getClanManager().getClanByVaultInventory(event.getInventory());
      if (clan != null) {
         clan.markVaultDirty();
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Clan vaultClan = this.plugin.getClanManager().getClanByVaultInventory(event.getView().getTopInventory());
      if (vaultClan != null) {
         vaultClan.markVaultDirty();
      }

      if (event.getView().getTopInventory().getHolder() instanceof ClanGUIHolder) {
         event.setCancelled(true);
         String clickName = event.getClick().name();
         if (!clickName.contains("SHIFT")
            && !clickName.contains("NUMBER")
            && !clickName.contains("DROP")
            && !clickName.contains("DOUBLE")
            && !clickName.contains("SWAP")) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
               if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                  Player player = (Player)event.getWhoClicked();
                  ClanGUIHolder holder = (ClanGUIHolder)event.getView().getTopInventory().getHolder();
                  String menuType = holder.getMenuType();
                  int slot = event.getSlot();
                  FileConfiguration gui = this.plugin.getGuiConfig();
                  if (menuType.equals("MAIN_NOCLAN")) {
                     if (slot == gui.getInt("menus.main.items.create.slot", -1)) {
                        player.closeInventory();
                        player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan create <Name>"));
                     } else if (slot == gui.getInt("menus.main.items.list.slot", -1)) {
                        ClanGUI.openClanList(player, this.plugin);
                     }
                  } else if (menuType.equals("MAIN_CLAN")) {
                     Clan clan = this.plugin.getClanManager().getClanByPlayer(player);
                     if (clan == null) {
                        player.closeInventory();
                        return;
                     }

                     if (slot == gui.getInt("menus.main.items.bank.slot", -1)) {
                        ClanGUI.openBankMenu(player, clan, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.vault-unlocked.slot", -1) || slot == gui.getInt("menus.main.items.vault-locked.slot", -1)) {
                        if (clan.hasPermission(player.getUniqueId(), "vault")) {
                           int unlockLvl = this.plugin.getConfig().getInt("vault-unlock-level", 3);
                           if (clan.getLevel() >= unlockLvl) {
                              player.openInventory(clan.getVault(this.plugin));
                           } else {
                              player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
                           }
                        } else {
                           player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
                        }
                     } else if (slot == gui.getInt("menus.main.items.members.slot", -1)) {
                        ClanGUI.openMemberList(player, clan, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.list-clan.slot", -1)) {
                        ClanGUI.openClanList(player, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.setbase.slot", -1)) {
                        player.closeInventory();
                        player.performCommand("clan setbase");
                     } else if (slot == gui.getInt("menus.main.items.shop.slot", -1)) {
                        ClanGUI.openShopMenu(player, clan, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.quests.slot", -1)) {
                        ClanGUI.openQuestsMenu(player, clan, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.basetp.slot", -1) || slot == gui.getInt("menus.main.items.nobase.slot", -1)) {
                        player.closeInventory();
                        player.performCommand("clan base");
                     } else if (slot == gui.getInt("menus.main.items.rename.slot", -1)) {
                        player.closeInventory();
                        player.sendMessage("§8[§bVRX Clans§8] §7/clan rename <Name>");
                     } else if (slot == gui.getInt("menus.main.items.invite.slot", -1)) {
                        player.closeInventory();
                        player.sendMessage("§8[§bVRX Clans§8] §7/clan invite <Player>");
                     } else if (slot == gui.getInt("menus.main.items.perms.slot", -1)) {
                        ClanGUI.openPermissionsRankMenu(player, this.plugin);
                     } else if (slot == gui.getInt("menus.main.items.disband.slot", -1) || slot == gui.getInt("menus.main.items.leave.slot", -1)) {
                        player.closeInventory();
                        if (clan.getRank(player.getUniqueId()) == Clan.ClanRank.LEADER) {
                           player.performCommand("clan disband");
                        } else {
                           player.performCommand("clan leave");
                        }
                     }
                  } else if (menuType.equals("PERMS_RANKS")) {
                     Clan clanx = this.plugin.getClanManager().getClanByPlayer(player);
                     if (clanx == null || clanx.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
                        player.closeInventory();
                        return;
                     }

                     if (slot == gui.getInt("menus.perms_ranks.items.recruit.slot", -1)) {
                        this.handleRankSlotClick(player, clanx, Clan.ClanRank.RECRUIT, event.getClick().isRightClick());
                     } else if (slot == gui.getInt("menus.perms_ranks.items.member.slot", -1)) {
                        this.handleRankSlotClick(player, clanx, Clan.ClanRank.MEMBER, event.getClick().isRightClick());
                     } else if (slot == gui.getInt("menus.perms_ranks.items.officer.slot", -1)) {
                        this.handleRankSlotClick(player, clanx, Clan.ClanRank.OFFICER, event.getClick().isRightClick());
                     } else if (slot == gui.getInt("menus.perms_ranks.items.coleader.slot", -1)) {
                        this.handleRankSlotClick(player, clanx, Clan.ClanRank.CO_LEADER, event.getClick().isRightClick());
                     } else if (slot == gui.getInt("menus.perms_ranks.items.back.slot", -1)) {
                        ClanGUI.openMainMenu(player, this.plugin);
                     }
                  } else if (menuType.equals("PERMS_TOGGLE")) {
                     Clan clanxx = this.plugin.getClanManager().getClanByPlayer(player);
                     if (clanxx == null || clanxx.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
                        player.closeInventory();
                        return;
                     }

                     Clan.ClanRank targetRank = Clan.ClanRank.valueOf(holder.getTargetData());
                     if (slot == gui.getInt("menus.perms_toggle.items.invite.slot", -1)) {
                        clanxx.togglePermission(targetRank, "invite");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.vault.slot", -1)) {
                        clanxx.togglePermission(targetRank, "vault");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.bank.slot", -1)) {
                        clanxx.togglePermission(targetRank, "bank");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.base.slot", -1)) {
                        clanxx.togglePermission(targetRank, "base");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.shop.slot", -1)) {
                        clanxx.togglePermission(targetRank, "shop");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.war.slot", -1)) {
                        clanxx.togglePermission(targetRank, "war");
                     } else if (slot == gui.getInt("menus.perms_toggle.items.back.slot", -1)) {
                        ClanGUI.openPermissionsRankMenu(player, this.plugin);
                        return;
                     }

                     ClanGUI.openPermissionsToggleMenu(player, clanxx, targetRank, this.plugin);
                  } else if (menuType.equals("QUESTS")) {
                     if (slot == gui.getInt("menus.quests.items.back.slot", -1)) {
                        ClanGUI.openMainMenu(player, this.plugin);
                     }
                  } else if (menuType.equals("BANK")) {
                     if (slot == gui.getInt("menus.bank.items.deposit-1.slot", -1)) {
                        player.performCommand("clan deposit 100");
                     } else if (slot == gui.getInt("menus.bank.items.deposit-2.slot", -1)) {
                        player.performCommand("clan deposit 1000");
                     } else if (slot == gui.getInt("menus.bank.items.withdraw-1.slot", -1)) {
                        player.performCommand("clan withdraw 100");
                     } else if (slot == gui.getInt("menus.bank.items.withdraw-2.slot", -1)) {
                        player.performCommand("clan withdraw 1000");
                     }

                     Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        Clan c = this.plugin.getClanManager().getClanByPlayer(player);
                        if (c != null && player.getOpenInventory().getTitle().contains(this.plugin.getLang().get("gui.title.bank").replace("§", ""))) {
                           ClanGUI.openBankMenu(player, c, this.plugin);
                        }
                     }, 2L);
                  } else if (menuType.equals("MEMBER_LIST")) {
                     if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                        String targetName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                        if (target != null) {
                           ClanGUI.openMemberManageMenu(player, target.getUniqueId(), targetName, this.plugin);
                        }
                     }
                  } else if (menuType.equals("MANAGE_MEMBER")) {
                     String targetUUIDStr = holder.getTargetData();
                     OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(targetUUIDStr));
                     player.closeInventory();
                     if (slot == gui.getInt("menus.manage.items.promote.slot", -1)) {
                        player.performCommand("clan promote " + target.getName());
                     } else if (slot == gui.getInt("menus.manage.items.demote.slot", -1)) {
                        player.performCommand("clan demote " + target.getName());
                     } else if (slot == gui.getInt("menus.manage.items.kick.slot", -1)) {
                        player.performCommand("clan kick " + target.getName());
                     }
                  } else if (menuType.equals("ADMIN_INSPECT_LIST")) {
                     if (!player.hasPermission("vrxclans.admin")) {
                        player.closeInventory();
                        return;
                     }

                     String clanName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                     Clan targetClan = this.plugin.getClanManager().getClanByName(clanName);
                     if (targetClan != null) {
                        ClanGUI.openAdminInspectMenu(player, targetClan, this.plugin);
                     }
                  } else if (menuType.equals("ADMIN_INSPECT")) {
                     if (!player.hasPermission("vrxclans.admin")) {
                        player.closeInventory();
                        return;
                     }

                     Clan targetClan = this.plugin.getClanManager().getClanByName(holder.getTargetData());
                     if (targetClan == null) {
                        player.closeInventory();
                        return;
                     }

                     if (slot == 45) {
                        ClanGUI.openAdminInspectList(player, this.plugin);
                     } else if (slot == 48) {
                        ClanGUI.openAdminEditorMenu(player, targetClan, this.plugin);
                     } else if (slot == 49) {
                        Inventory vault = targetClan.getVault(this.plugin);
                        if (vault == null) {
                           player.sendMessage("§cThe vault is not unlocked for this clan yet.");
                        } else {
                           player.openInventory(vault);
                        }
                     }
                  } else if (menuType.equals("ADMIN_EDITOR")) {
                     if (!player.hasPermission("vrxclans.admin")) {
                        player.closeInventory();
                        return;
                     }

                     Clan targetClanx = this.plugin.getClanManager().getClanByName(holder.getTargetData());
                     if (targetClanx == null) {
                        player.closeInventory();
                        return;
                     }

                     if (slot == 45) {
                        ClanGUI.openAdminInspectMenu(player, targetClanx, this.plugin);
                        return;
                     }

                     if (slot == 10) {
                        targetClanx.setLevel(targetClanx.getLevel() + 1);
                        this.plugin.handleClanLevelUps(targetClanx);
                        player.sendMessage("§aClan level increased.");
                     } else if (slot == 11) {
                        targetClanx.deposit(10000.0);
                        player.sendMessage("§a10,000$ added.");
                     } else if (slot == 12) {
                        targetClanx.setBankBalance(0.0);
                        player.sendMessage("§cClan bank cleared.");
                     } else if (slot == 13) {
                        targetClanx.setBaseLocation(null);
                        this.plugin.getClanManager().updateBaseIndex(targetClanx);
                        player.sendMessage("§cClan base deleted.");
                     } else {
                        if (slot == 14) {
                           Inventory vault = targetClanx.getVault(this.plugin);
                           if (vault != null) {
                              player.openInventory(vault);
                           } else {
                              player.sendMessage("§cVault is not unlocked.");
                           }

                           return;
                        }

                        if (slot == 15) {
                           targetClanx.resetWar();
                           player.sendMessage("§cWar status reset.");
                        } else if (slot == 16) {
                           targetClanx.setMotd("");
                           player.sendMessage("§eMOTD cleared.");
                        }
                     }

                     this.plugin
                        .getClanManager()
                        .logClanEvent(targetClanx, ClanLogEntry.LogType.ADMIN_EDIT, player.getName(), "Editor", 0.0, "Admin Editor Aktion Slot " + slot);
                     ClanGUI.openAdminEditorMenu(player, targetClanx, this.plugin);
                  } else if (menuType.equals("SHOP")) {
                     Clan clanxxx = this.plugin.getClanManager().getClanByPlayer(player);
                     if (clanxxx == null) {
                        player.closeInventory();
                        return;
                     }

                     if (slot == gui.getInt("menus.shop.items.back.slot", -1)) {
                        ClanGUI.openMainMenu(player, this.plugin);
                        return;
                     }

                     if (!clanxxx.hasPermission(player.getUniqueId(), "shop")) {
                        player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
                        return;
                     }

                     int hours = this.plugin.getConfig().getInt("shop.booster-hours", 1);
                     long millisToAdd = hours * 60L * 60L * 1000L;
                     LanguageManager l = this.plugin.getLang();
                     if (slot == gui.getInt("menus.shop.items.radius.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-radius", true)) {
                        int cost = this.plugin.getConfig().getInt("shop.radius-cost");
                        int add = this.plugin.getConfig().getInt("shop.radius-add");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.addPurchasedRadiusBonus(add);
                           this.plugin.getClanManager().updateBaseIndex(clanxxx);
                           player.sendMessage(l.get("msg.shop-radius-bought", "%amount%", String.valueOf(add)));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.xp.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-xp", true)) {
                        int cost = this.plugin.getConfig().getInt("shop.xp-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.addXpBooster(millisToAdd);
                           player.sendMessage(l.get("msg.shop-booster-bought", "%type%", "XP"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.haste.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-haste", true)) {
                        int cost = this.plugin.getConfig().getInt("shop.haste-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.addHasteBooster(millisToAdd);
                           player.sendMessage(l.get("msg.shop-booster-bought", "%type%", "Haste"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.speed.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-speed", true)) {
                        int cost = this.plugin.getConfig().getInt("shop.speed-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.addSpeedBooster(millisToAdd);
                           player.sendMessage(l.get("msg.shop-booster-bought", "%type%", "Speed"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.strength.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-strength", true)) {
                        int cost = this.plugin.getConfig().getInt("shop.strength-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.addStrengthBooster(millisToAdd);
                           player.sendMessage(l.get("msg.shop-booster-bought", "%type%", "Strength"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.baseregen.slot", -1) && this.plugin.getConfig().getBoolean("shop.enable-base-regen", true)
                        )
                      {
                        if (clanxxx.isBaseRegen()) {
                           return;
                        }

                        int cost = this.plugin.getConfig().getInt("shop.base-regen-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.setBaseRegen(true);
                           player.sendMessage(l.get("msg.shop-upgrade-bought", "%type%", "Regeneration"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.basesat.slot", -1)
                        && this.plugin.getConfig().getBoolean("shop.enable-base-saturation", true)) {
                        if (clanxxx.isBaseSaturation()) {
                           return;
                        }

                        int cost = this.plugin.getConfig().getInt("shop.base-saturation-cost");
                        if (clanxxx.withdraw(cost)) {
                           clanxxx.setBaseSaturation(true);
                           player.sendMessage(l.get("msg.shop-upgrade-bought", "%type%", "Saturation"));
                           ClanGUI.openShopMenu(player, clanxxx, this.plugin);
                        } else {
                           player.sendMessage(l.get("msg.eco-not-enough"));
                        }
                     } else if (slot == gui.getInt("menus.shop.items.more-members.slot", -1)) {
                        player.performCommand("clan shopupgrade members");
                     } else if (slot == gui.getInt("menus.shop.items.more-homes.slot", -1)) {
                        player.performCommand("clan shopupgrade homes");
                     } else if (slot == gui.getInt("menus.shop.items.more-vault.slot", -1)) {
                        player.performCommand("clan shopupgrade vault");
                     } else if (slot == gui.getInt("menus.shop.items.more-allies.slot", -1)) {
                        player.performCommand("clan shopupgrade allies");
                     } else if (slot == gui.getInt("menus.shop.items.more-rivals.slot", -1)) {
                        player.performCommand("clan shopupgrade rivals");
                     } else if (slot == gui.getInt("menus.shop.items.teleport.slot", -1)) {
                        player.performCommand("clan shopupgrade teleport");
                     }
                  }
               }
            }
         }
      }
   }
}
