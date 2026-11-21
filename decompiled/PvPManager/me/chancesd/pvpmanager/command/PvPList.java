package me.chancesd.pvpmanager.command;

import java.util.List;
import java.util.Map;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatMenu;
import me.chancesd.pvpmanager.sdutils.display.chat.NavigationButtons;
import me.chancesd.pvpmanager.sdutils.display.chat.content.StaticContentProvider;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.storage.Storage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PvPList extends BaseCommand {
   private static final int LINES_PER_PAGE = 16;
   private final PlayerManager ph;

   public PvPList(PluginCommand pluginCommand, PlayerManager ph) {
      super(pluginCommand);
      this.ph = ph;
      this.description("List the PvP state of all players")
         .usage("/pvplist [offline]")
         .permission(Permissions.COMMAND_PVPLIST.getPermission())
         .argument("mode", ArgumentType.STRING)
         .tabComplete("offline")
         .endArgument()
         .argument("page", ArgumentType.INTEGER)
         .defaultValue("1")
         .dependsOn("mode")
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      boolean isPlayer = sender instanceof Player;
      boolean offlineMode = false;
      int page = 1;
      if (!args.isEmpty()) {
         CommandArgument firstArg = this.getArgument(args, "mode");
         if (firstArg.getValue().equalsIgnoreCase("offline")) {
            if (!Permissions.COMMAND_PVPLIST_OFFLINE.hasPerm(sender)) {
               return;
            }

            offlineMode = true;
            if (args.size() > 1) {
               page = this.getArgument(args, "page").getAsInt();
            }
         } else {
            try {
               page = Integer.parseInt(firstArg.getValue());
            } catch (NumberFormatException var9) {
               sender.sendMessage(
                  ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/pvplist [page]" + ChatColor.GRAY + " or " + ChatColor.YELLOW + "/pvplist offline [page]"
               );
               return;
            }
         }
      }

      if (offlineMode) {
         this.showOfflineList(sender, page);
      } else {
         this.showOnlineList(sender, isPlayer, page);
      }
   }

   private void showOnlineList(CommandSender sender, boolean isPlayer, int page) {
      StaticContentProvider contentProvider = new StaticContentProvider();
      if (Permissions.COMMAND_PVPLIST_OFFLINE.hasPerm(sender)) {
         contentProvider.addLine(
            ChatColor.DARK_GRAY + "Tip: Use " + ChatColor.YELLOW + "/pvplist offline" + ChatColor.DARK_GRAY + " to see all offline players.", null, null
         );
         contentProvider.addLine("");
      }

      contentProvider.addLine(Lang.PVP_LIST_ENABLED.msg(), null, null);
      this.addPlayerList(sender, contentProvider, true, !isPlayer);
      contentProvider.addLine("");
      contentProvider.addLine(Lang.PVP_LIST_DISABLED.msg(), null, null);
      this.addPlayerList(sender, contentProvider, false, !isPlayer);
      ChatMenu menu = ChatMenu.builder()
         .header(Lang.PVP_LIST_TITLE.msg() + " #9E9E9E(Page {page}/{total})")
         .linesPerPage(16)
         .contentProvider(contentProvider)
         .navigation(NavigationButtons.builder().navigationPrefix("/pvplist").build())
         .build();
      menu.show(sender, page);
   }

   private void addPlayerList(CommandSender sender, StaticContentProvider contentProvider, boolean enabled, boolean console) {
      int count = 0;

      for (CombatPlayer p : this.ph.getPlayers().values()) {
         Player player = p.getPlayer();
         if (enabled == p.hasPvPEnabled() && (console || ((Player)sender).canSee(player))) {
            contentProvider.addLine(ChatColor.GRAY + "  • " + ChatColor.GRAY + player.getDisplayName());
            count++;
         }
      }

      if (count == 0) {
         contentProvider.addLine(ChatColor.GRAY + "  " + Lang.PVP_LIST_NO_RESULTS.msg());
      }
   }

   private void showOfflineList(CommandSender sender, int page) {
      sender.sendMessage(ChatColor.DARK_GRAY + "Gathering all offline players with PvP disabled...");
      ScheduleUtils.runAsync(
         () -> {
            StaticContentProvider contentProvider = new StaticContentProvider();
            Storage storage = this.ph.getPlugin().getStorageManager().getStorage();
            contentProvider.addLine(Lang.PVP_LIST_DISABLED.msg(), null, null);
            int count = 0;

            for (Map<String, Object> userData : storage.getAllUserData()) {
               String displayName = (String)userData.get("displayname");
               Object pvpstatus = userData.get("pvpstatus");
               boolean pvpState = true;
               if (pvpstatus instanceof Integer) {
                  pvpState = (Integer)pvpstatus != 0;
               } else if (pvpstatus instanceof Boolean) {
                  pvpState = (Boolean)pvpstatus;
               }

               if (!pvpState && displayName != null) {
                  contentProvider.addLine(ChatColor.GRAY + "  • " + ChatColor.GRAY + displayName);
                  count++;
               }
            }

            if (count == 0) {
               contentProvider.addLine(ChatColor.GRAY + "  " + Lang.PVP_LIST_NO_RESULTS.msg(), null, null);
            }

            ChatMenu menu = ChatMenu.builder()
               .header(Lang.PVP_LIST_TITLE.msg() + " #FF5722(Offline) #9E9E9E(Page {page}/{total})")
               .linesPerPage(16)
               .contentProvider(contentProvider)
               .navigation(NavigationButtons.builder().navigationPrefix("/pvplist offline").build())
               .build();
            menu.show(sender, page);
         }
      );
   }
}
