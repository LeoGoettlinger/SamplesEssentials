package vrx.economy.vRXClans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ClanGUI {
   private static void setDynItem(Inventory inv, FileConfiguration gui, String path, LanguageManager lang, String langKey, String... replacements) {
      if (gui.contains(path + ".slot")) {
         int slot = gui.getInt(path + ".slot", -1);
         if (slot >= 0 && slot < inv.getSize()) {
            Material mat = Material.matchMaterial(gui.getString(path + ".material", "STONE").toUpperCase());
            if (mat == null) {
               mat = Material.STONE;
            }

            inv.setItem(slot, createItem(mat, lang.get(langKey + ".name", replacements), lang.getList(langKey + ".lore", replacements)));
         }
      }
   }

   public static void openMainMenu(Player player, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      Clan playerClan = plugin.getClanManager().getClanByPlayer(player);
      boolean hasClan = playerClan != null;
      ClanGUIHolder holder = new ClanGUIHolder(hasClan ? "MAIN_CLAN" : "MAIN_NOCLAN");
      String title = hasClan ? lang.get("gui.title.clan", "%clan%", playerClan.getName()) : lang.get("gui.title.noclan");
      Inventory inv = Bukkit.createInventory(holder, 27, title);
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      if (!hasClan) {
         setDynItem(inv, gui, "menus.main.items.create", lang, "gui.item.create");
         setDynItem(inv, gui, "menus.main.items.list", lang, "gui.item.list");
      } else {
         Clan.ClanRank rank = playerClan.getRank(player.getUniqueId());
         setDynItem(
            inv,
            gui,
            "menus.main.items.info",
            lang,
            "gui.item.info",
            "%members%",
            String.valueOf(playerClan.getMembers().size()),
            "%rank%",
            playerClan.getRankDisplayName(rank),
            "%kills%",
            String.valueOf(playerClan.getKills()),
            "%deaths%",
            String.valueOf(playerClan.getDeaths()),
            "%kdr%",
            playerClan.getKDR()
         );
         setDynItem(
            inv,
            gui,
            "menus.main.items.level",
            lang,
            "gui.item.level",
            "%level%",
            String.valueOf(playerClan.getLevel()),
            "%xp%",
            String.valueOf((int)playerClan.getXp()),
            "%req%",
            String.valueOf((int)playerClan.getRequiredXp(plugin))
         );
         setDynItem(inv, gui, "menus.main.items.bank", lang, "gui.item.bank", "%balance%", String.valueOf(playerClan.getBankBalance()));
         int unlockLvl = plugin.getConfig().getInt("vault-unlock-level", 3);
         if (playerClan.getLevel() >= unlockLvl) {
            setDynItem(inv, gui, "menus.main.items.vault-unlocked", lang, "gui.item.vault-unlocked");
         } else {
            setDynItem(inv, gui, "menus.main.items.vault-locked", lang, "gui.item.vault-locked", "%level%", String.valueOf(unlockLvl));
         }

         setDynItem(inv, gui, "menus.main.items.members", lang, "gui.item.members");
         setDynItem(inv, gui, "menus.main.items.list-clan", lang, "gui.item.list");
         setDynItem(inv, gui, "menus.main.items.setbase", lang, "gui.item.setbase");
         setDynItem(inv, gui, "menus.main.items.shop", lang, "gui.item.shop");
         setDynItem(inv, gui, "menus.main.items.quests", lang, "gui.item.quests");
         if (playerClan.getBaseLocation() != null) {
            int radius = playerClan.getDynamicRadius(plugin);
            double tpCost = plugin.getConfig().getDouble("base-teleport-cost", 10.0);
            String costLine = tpCost > 0.0 ? lang.get("misc.word-cost", "%amount%", String.valueOf(tpCost)) : lang.get("misc.cost-free");
            int delay = plugin.getConfig().getInt("base-teleport-delay", 3);
            String delayLine = delay > 0 ? lang.get("misc.word-delay", "%time%", String.valueOf(delay)) : lang.get("misc.delay-none");
            setDynItem(
               inv, gui, "menus.main.items.basetp", lang, "gui.item.basetp", "%radius%", String.valueOf(radius), "%cost%", costLine, "%delay%", delayLine
            );
         } else {
            setDynItem(inv, gui, "menus.main.items.nobase", lang, "gui.item.nobase");
         }

         setDynItem(inv, gui, "menus.main.items.rename", lang, "gui.item.rename");
         setDynItem(inv, gui, "menus.main.items.invite", lang, "gui.item.invite");
         if (rank == Clan.ClanRank.LEADER) {
            setDynItem(inv, gui, "menus.main.items.disband", lang, "gui.item.disband");
            setDynItem(inv, gui, "menus.main.items.perms", lang, "gui.item.main-perms");
         } else {
            setDynItem(inv, gui, "menus.main.items.leave", lang, "gui.item.leave");
         }
      }

      player.openInventory(inv);
   }

   public static void openPermissionsRankMenu(Player player, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      Clan clan = plugin.getClanManager().getClanByPlayer(player);
      ClanGUIHolder holder = new ClanGUIHolder("PERMS_RANKS");
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.perms-ranks"));
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      setDynItem(
         inv,
         gui,
         "menus.perms_ranks.items.recruit",
         lang,
         "gui.item.perms-rank",
         "%rank%",
         clan != null ? clan.getRankDisplayName(Clan.ClanRank.RECRUIT) : "RECRUIT"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_ranks.items.member",
         lang,
         "gui.item.perms-rank",
         "%rank%",
         clan != null ? clan.getRankDisplayName(Clan.ClanRank.MEMBER) : "MEMBER"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_ranks.items.officer",
         lang,
         "gui.item.perms-rank",
         "%rank%",
         clan != null ? clan.getRankDisplayName(Clan.ClanRank.OFFICER) : "OFFICER"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_ranks.items.coleader",
         lang,
         "gui.item.perms-rank",
         "%rank%",
         clan != null ? clan.getRankDisplayName(Clan.ClanRank.CO_LEADER) : "CO_LEADER"
      );
      setDynItem(inv, gui, "menus.perms_ranks.items.back", lang, "gui.item.back");
      player.openInventory(inv);
   }

   public static void openPermissionsToggleMenu(Player player, Clan clan, Clan.ClanRank rank, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      ClanGUIHolder holder = new ClanGUIHolder("PERMS_TOGGLE", rank.name());
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.perms-toggle", "%rank%", clan.getRankDisplayName(rank)));
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.invite",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "Invite",
         "%status%",
         clan.getRawPermission(rank, "invite") ? "§aYes" : "§cNo"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.vault",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "Vault",
         "%status%",
         clan.getRawPermission(rank, "vault") ? "§aYes" : "§cNo"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.bank",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "Bank Withdraw",
         "%status%",
         clan.getRawPermission(rank, "bank") ? "§aYes" : "§cNo"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.base",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "Set Base",
         "%status%",
         clan.getRawPermission(rank, "base") ? "§aYes" : "§cNo"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.shop",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "Shop",
         "%status%",
         clan.getRawPermission(rank, "shop") ? "§aYes" : "§cNo"
      );
      setDynItem(
         inv,
         gui,
         "menus.perms_toggle.items.war",
         lang,
         "gui.item.perms-toggle",
         "%perm%",
         "War/Diplomacy",
         "%status%",
         clan.getRawPermission(rank, "war") ? "§aYes" : "§cNo"
      );
      setDynItem(inv, gui, "menus.perms_toggle.items.back", lang, "gui.item.back");
      player.openInventory(inv);
   }

   public static void openShopMenu(Player player, Clan clan, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      ClanGUIHolder holder = new ClanGUIHolder("SHOP");
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.shop"));
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      int radCost = plugin.getConfig().getInt("shop.radius-cost");
      int radAdd = plugin.getConfig().getInt("shop.radius-add");
      int xpCost = plugin.getConfig().getInt("shop.xp-cost");
      int hasteCost = plugin.getConfig().getInt("shop.haste-cost");
      int speedCost = plugin.getConfig().getInt("shop.speed-cost");
      int strengthCost = plugin.getConfig().getInt("shop.strength-cost");
      int hours = plugin.getConfig().getInt("shop.booster-hours");
      if (plugin.getConfig().getBoolean("shop.enable-radius", true)) {
         setDynItem(inv, gui, "menus.shop.items.radius", lang, "gui.item.shop-radius", "%add%", String.valueOf(radAdd), "%cost%", String.valueOf(radCost));
      }

      if (plugin.getConfig().getBoolean("shop.enable-xp", true)) {
         setDynItem(
            inv,
            gui,
            "menus.shop.items.xp",
            lang,
            "gui.item.shop-xp",
            "%hours%",
            String.valueOf(hours),
            "%cost%",
            String.valueOf(xpCost),
            "%status%",
            clan.getBoosterTimeLeft(clan.getXpBooster(), plugin)
         );
      }

      if (plugin.getConfig().getBoolean("shop.enable-haste", true)) {
         setDynItem(
            inv,
            gui,
            "menus.shop.items.haste",
            lang,
            "gui.item.shop-haste",
            "%hours%",
            String.valueOf(hours),
            "%cost%",
            String.valueOf(hasteCost),
            "%status%",
            clan.getBoosterTimeLeft(clan.getHasteBooster(), plugin)
         );
      }

      if (plugin.getConfig().getBoolean("shop.enable-speed", true)) {
         setDynItem(
            inv,
            gui,
            "menus.shop.items.speed",
            lang,
            "gui.item.shop-speed",
            "%hours%",
            String.valueOf(hours),
            "%cost%",
            String.valueOf(speedCost),
            "%status%",
            clan.getBoosterTimeLeft(clan.getSpeedBooster(), plugin)
         );
      }

      if (plugin.getConfig().getBoolean("shop.enable-strength", true)) {
         setDynItem(
            inv,
            gui,
            "menus.shop.items.strength",
            lang,
            "gui.item.shop-strength",
            "%hours%",
            String.valueOf(hours),
            "%cost%",
            String.valueOf(strengthCost),
            "%status%",
            clan.getBoosterTimeLeft(clan.getStrengthBooster(), plugin)
         );
      }

      if (plugin.getConfig().getBoolean("shop.enable-base-regen", true)) {
         String stat = clan.isBaseRegen() ? lang.get("misc.status-unlocked") : lang.get("misc.status-locked");
         setDynItem(
            inv,
            gui,
            "menus.shop.items.baseregen",
            lang,
            "gui.item.shop-baseregen",
            "%cost%",
            String.valueOf(plugin.getConfig().getInt("shop.base-regen-cost")),
            "%status%",
            stat
         );
      }

      if (plugin.getConfig().getBoolean("shop.enable-base-saturation", true)) {
         String stat = clan.isBaseSaturation() ? lang.get("misc.status-unlocked") : lang.get("misc.status-locked");
         setDynItem(
            inv,
            gui,
            "menus.shop.items.basesat",
            lang,
            "gui.item.shop-basesat",
            "%cost%",
            String.valueOf(plugin.getConfig().getInt("shop.base-saturation-cost")),
            "%status%",
            stat
         );
      }

      setDynItem(
         inv,
         gui,
         "menus.shop.items.more-members",
         lang,
         "gui.item.shop-more-members",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.members.cost", 10000.0)),
         "%current%",
         String.valueOf(clan.getExtraMemberSlots())
      );
      setDynItem(
         inv,
         gui,
         "menus.shop.items.more-homes",
         lang,
         "gui.item.shop-more-homes",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.homes.cost", 7500.0)),
         "%current%",
         String.valueOf(clan.getExtraHomeSlots())
      );
      setDynItem(
         inv,
         gui,
         "menus.shop.items.more-vault",
         lang,
         "gui.item.shop-more-vault",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.vault.cost", 15000.0)),
         "%current%",
         String.valueOf(clan.getExtraVaultRows())
      );
      setDynItem(
         inv,
         gui,
         "menus.shop.items.more-allies",
         lang,
         "gui.item.shop-more-allies",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.allies.cost", 12000.0)),
         "%current%",
         String.valueOf(clan.getExtraAllySlots())
      );
      setDynItem(
         inv,
         gui,
         "menus.shop.items.more-rivals",
         lang,
         "gui.item.shop-more-rivals",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.rivals.cost", 12000.0)),
         "%current%",
         String.valueOf(clan.getExtraRivalSlots())
      );
      setDynItem(
         inv,
         gui,
         "menus.shop.items.teleport",
         lang,
         "gui.item.shop-teleport",
         "%cost%",
         String.valueOf(plugin.getConfig().getDouble("shop.extended.teleport.cost", 10000.0)),
         "%current%",
         String.valueOf(clan.getTeleportDelayReduction())
      );
      setDynItem(inv, gui, "menus.shop.items.back", lang, "gui.item.back");
      player.openInventory(inv);
   }

   public static void openQuestsMenu(Player player, Clan clan, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      ClanGUIHolder holder = new ClanGUIHolder("QUESTS");
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.quests"));
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      int tZombies = plugin.getConfig().getInt("quests.zombies.target");
      double rZombies = plugin.getConfig().getDouble("quests.zombies.reward-xp");
      String sZombies = clan.isQuestZombiesDone() ? lang.get("misc.quest-done") : lang.get("misc.quest-progress");
      setDynItem(
         inv,
         gui,
         "menus.quests.items.zombies",
         lang,
         "gui.item.quest-zombies",
         "%target%",
         String.valueOf(tZombies),
         "%progress%",
         String.valueOf(clan.getQuestZombies()),
         "%reward%",
         String.valueOf(rZombies),
         "%status%",
         sZombies
      );
      int tDiamonds = plugin.getConfig().getInt("quests.diamonds.target");
      double rDiamonds = plugin.getConfig().getDouble("quests.diamonds.reward-xp");
      String sDiamonds = clan.isQuestDiamondsDone() ? lang.get("misc.quest-done") : lang.get("misc.quest-progress");
      setDynItem(
         inv,
         gui,
         "menus.quests.items.diamonds",
         lang,
         "gui.item.quest-diamonds",
         "%target%",
         String.valueOf(tDiamonds),
         "%progress%",
         String.valueOf(clan.getQuestDiamonds()),
         "%reward%",
         String.valueOf(rDiamonds),
         "%status%",
         sDiamonds
      );
      double tBank = plugin.getConfig().getDouble("quests.bank.target");
      double rBank = plugin.getConfig().getDouble("quests.bank.reward-xp");
      String sBank = clan.isQuestBankDone() ? lang.get("misc.quest-done") : lang.get("misc.quest-progress");
      setDynItem(
         inv,
         gui,
         "menus.quests.items.bank",
         lang,
         "gui.item.quest-bank",
         "%target%",
         String.valueOf(tBank),
         "%progress%",
         String.valueOf((int)clan.getQuestMoney()),
         "%reward%",
         String.valueOf(rBank),
         "%status%",
         sBank
      );
      setDynItem(inv, gui, "menus.quests.items.back", lang, "gui.item.back");
      player.openInventory(inv);
   }

   public static void openClanList(Player player, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      ClanGUIHolder holder = new ClanGUIHolder("CLAN_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, lang.get("gui.title.list"));
      holder.setInventory(inv);
      int slot = 0;

      for (Clan clan : plugin.getClanManager().getClans().values()) {
         if (slot >= 54) {
            break;
         }

         Material icon = Material.matchMaterial(clan.getIconMaterial());
         if (icon == null || !icon.isItem()) {
            icon = Material.SHIELD;
         }

         inv.setItem(
            slot,
            createItem(
               icon,
               lang.get("gui.item.list-format.name", "%clan%", clan.getName()),
               lang.getList(
                  "gui.item.list-format.lore",
                  "%level%",
                  String.valueOf(clan.getLevel()),
                  "%members%",
                  String.valueOf(clan.getMembers().size()),
                  "%kdr%",
                  clan.getKDR(),
                  "%allies%",
                  String.valueOf(clan.getAllies().size())
               )
            )
         );
         slot++;
      }

      player.openInventory(inv);
   }

   public static void openBankMenu(Player player, Clan clan, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      ClanGUIHolder holder = new ClanGUIHolder("BANK");
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.bank"));
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      setDynItem(inv, gui, "menus.bank.items.balance", lang, "gui.item.bank-balance", "%balance%", String.valueOf(clan.getBankBalance()));
      setDynItem(inv, gui, "menus.bank.items.deposit-1", lang, "gui.item.bank-deposit-1");
      setDynItem(inv, gui, "menus.bank.items.deposit-2", lang, "gui.item.bank-deposit-2");
      setDynItem(inv, gui, "menus.bank.items.withdraw-1", lang, "gui.item.bank-withdraw-1");
      setDynItem(inv, gui, "menus.bank.items.withdraw-2", lang, "gui.item.bank-withdraw-2");
      player.openInventory(inv);
   }

   public static void openMemberList(Player player, Clan clan, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      ClanGUIHolder holder = new ClanGUIHolder("MEMBER_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, lang.get("gui.title.members"));
      holder.setInventory(inv);
      int slot = 0;

      for (Entry<UUID, Clan.ClanRank> entry : clan.getMembers().entrySet()) {
         if (slot >= 54) {
            break;
         }

         OfflinePlayer member = Bukkit.getOfflinePlayer(entry.getKey());
         String name = member.getName() != null ? member.getName() : "Unbekannt";
         ItemStack head = new ItemStack(Material.PLAYER_HEAD);
         SkullMeta meta = (SkullMeta)head.getItemMeta();
         if (meta != null) {
            meta.setOwningPlayer(member);
            meta.setDisplayName(lang.get("gui.item.member-head.name", "%player%", name));
            meta.setLore(lang.getList("gui.item.member-head.lore", "%rank%", clan.getRankDisplayName(entry.getValue())));
            head.setItemMeta(meta);
         }

         inv.setItem(slot, head);
         slot++;
      }

      player.openInventory(inv);
   }

   public static void openMemberManageMenu(Player player, UUID targetUUID, String targetName, VRXClans plugin) {
      LanguageManager lang = plugin.getLang();
      FileConfiguration gui = plugin.getGuiConfig();
      ClanGUIHolder holder = new ClanGUIHolder("MANAGE_MEMBER", targetUUID.toString());
      Inventory inv = Bukkit.createInventory(holder, 27, lang.get("gui.title.manage", "%player%", targetName));
      holder.setInventory(inv);
      setDynItem(inv, gui, "menus.manage.items.promote", lang, "gui.item.manage-promote");
      setDynItem(inv, gui, "menus.manage.items.demote", lang, "gui.item.manage-demote");
      setDynItem(inv, gui, "menus.manage.items.kick", lang, "gui.item.manage-kick");
      player.openInventory(inv);
   }

   public static void openAdminInspectList(Player player, VRXClans plugin) {
      ClanGUIHolder holder = new ClanGUIHolder("ADMIN_INSPECT_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, "§cClan Inspect");
      holder.setInventory(inv);
      int slot = 0;

      for (Clan clan : plugin.getClanManager().getClans().values()) {
         if (slot >= 54) {
            break;
         }

         inv.setItem(
            slot++,
            createItem(
               Material.SPYGLASS,
               "§b" + clan.getName(),
               List.of(
                  "§7Level: §e" + clan.getLevel(),
                  "§7Mitglieder: §e" + clan.getMembers().size(),
                  "§7Bank: §a" + clan.getBankBalance() + "$",
                  "§7Kills/Deaths: §e" + clan.getKills() + "§7/§e" + clan.getDeaths(),
                  "§8Linksklick zum Inspizieren"
               )
            )
         );
      }

      player.openInventory(inv);
   }

   public static void openAdminInspectMenu(Player player, Clan clan, VRXClans plugin) {
      ClanGUIHolder holder = new ClanGUIHolder("ADMIN_INSPECT", clan.getName());
      Inventory inv = Bukkit.createInventory(holder, 54, "§cInspect: §b" + clan.getName());
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      inv.setItem(
         10,
         createItem(
            Material.NAME_TAG,
            "§b" + clan.getName(),
            List.of("§7Level: §e" + clan.getLevel(), "§7XP: §e" + (int)clan.getXp(), "§7Belohnungen bis Level: §e" + clan.getLastRewardLevel())
         )
      );
      inv.setItem(12, createItem(Material.EMERALD, "§aBank", List.of("§7Kontostand: §a" + clan.getBankBalance() + "$")));
      inv.setItem(
         14,
         createItem(
            Material.DIAMOND_SWORD,
            "§cPvP",
            List.of(
               "§7Kills: §e" + clan.getKills(),
               "§7Deaths: §e" + clan.getDeaths(),
               "§7KDR: §e" + clan.getKDR(),
               "§7War: §e" + (clan.getActiveWar() == null ? "-" : clan.getActiveWar()),
               "§7War-Kills: §e" + clan.getWarKills()
            )
         )
      );
      inv.setItem(
         16,
         createItem(
            Material.BEACON,
            "§dBase",
            List.of(
               "§7Gesetzt: §e" + (clan.getBaseLocation() != null ? "Ja" : "Nein"),
               "§7Radius: §e" + clan.getDynamicRadius(plugin),
               "§7Regen: §e" + clan.isBaseRegen(),
               "§7Saturation: §e" + clan.isBaseSaturation()
            )
         )
      );
      inv.setItem(28, createItem(Material.PLAYER_HEAD, "§eMitglieder", List.of("§7Anzahl: §e" + clan.getMembers().size(), "§8Siehe Slots rechts")));
      int slot = 29;

      for (Entry<UUID, Clan.ClanRank> entry : clan.getMembers().entrySet()) {
         if (slot >= 44) {
            break;
         }

         OfflinePlayer member = Bukkit.getOfflinePlayer(entry.getKey());
         String name = member.getName() != null ? member.getName() : entry.getKey().toString().substring(0, 8);
         inv.setItem(
            slot++,
            createItem(Material.PLAYER_HEAD, "§f" + name, List.of("§7UUID: §8" + entry.getKey(), "§7Rang: §e" + clan.getRankDisplayName(entry.getValue())))
         );
      }

      inv.setItem(45, createItem(Material.BARRIER, "§cBack", List.of("§7Back to the clan inspect list")));
      inv.setItem(48, createItem(Material.COMMAND_BLOCK, "§cAdmin Editor", List.of("§7Opens the clan editor.")));
      inv.setItem(49, createItem(Material.CHEST, "§6Open Vault", List.of("§7Opens the clan vault if unlocked.")));
      player.openInventory(inv);
   }

   public static void openAdminEditorMenu(Player player, Clan clan, VRXClans plugin) {
      ClanGUIHolder holder = new ClanGUIHolder("ADMIN_EDITOR", clan.getName());
      Inventory inv = Bukkit.createInventory(holder, 54, "§cEditor: §b" + clan.getName());
      holder.setInventory(inv);
      fillBorder(inv, plugin);
      inv.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, "§a+1 Level", List.of("§7Increases the clan level by 1.")));
      inv.setItem(11, createItem(Material.EMERALD, "§a+10,000$ Bank", List.of("§7Adds money to the clan bank.")));
      inv.setItem(12, createItem(Material.BARRIER, "§cBank leeren", List.of("§7Setzt die Clan-Bank auf 0.")));
      inv.setItem(13, createItem(Material.BEACON, "§dDelete Base", List.of("§7Removes the clan base.")));
      inv.setItem(14, createItem(Material.ENDER_CHEST, "§6Open Vault", List.of("§7Opens the clan vault.")));
      inv.setItem(15, createItem(Material.DIAMOND_SWORD, "§cEnd War", List.of("§7Resets the war status.")));
      inv.setItem(16, createItem(Material.WRITABLE_BOOK, "§eClear MOTD", List.of("§7Removes the clan MOTD.")));
      inv.setItem(
         31,
         createItem(
            Material.PAPER,
            "§bInfo",
            List.of(
               "§7Level: §e" + clan.getLevel(),
               "§7Bank: §a" + clan.getBankBalance() + "$",
               "§7Mitglieder: §e" + clan.getMembers().size(),
               "§7Tag: §e" + clan.getTag(),
               "§7Icon: §e" + clan.getIconMaterial()
            )
         )
      );
      inv.setItem(45, createItem(Material.ARROW, "§cBack", List.of("§7Back to inspect view")));
      player.openInventory(inv);
   }

   private static void fillBorder(Inventory inv, VRXClans plugin) {
      String colorString = plugin.getConfig().getString("gui-theme-color", "GRAY_STAINED_GLASS_PANE").toUpperCase();
      Material glassMat = Material.matchMaterial(colorString);
      if (glassMat == null) {
         glassMat = Material.GRAY_STAINED_GLASS_PANE;
      }

      ItemStack glass = createItem(glassMat, " ", new ArrayList<>());

      for (int i = 0; i < inv.getSize(); i++) {
         if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
            inv.setItem(i, glass);
         }
      }
   }

   private static ItemStack createItem(Material mat, String name, List<String> lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }
}
