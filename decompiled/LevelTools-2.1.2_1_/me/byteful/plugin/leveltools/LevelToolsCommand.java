package me.byteful.plugin.leveltools;

import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Command;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Default;
import me.byteful.plugin.leveltools.libs.lamp.annotation.DefaultFor;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Dependency;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Description;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Subcommand;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Switch;
import me.byteful.plugin.leveltools.libs.lamp.help.CommandHelp;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Command({"leveltools"})
public class LevelToolsCommand {
   @Dependency
   private LevelToolsPlugin plugin;

   @DefaultFor({"leveltools"})
   @Subcommand({"help"})
   @Description("Shows the list of LevelTools commands.")
   public void onHelp(CommandSender sender, CommandHelp<String> help, @Default({"1"}) int page) {
      sender.sendMessage(Text.colorize("&6&lLevelTools Command Help:"));

      for (String entry : help.paginate(page, 7)) {
         sender.sendMessage(Text.colorize(entry));
      }
   }

   @Subcommand({"reload"})
   @Description("Reloads LevelTools' plugin configuration.")
   public void onReload(CommandSender sender) {
      if (this.checkPerm(sender)) {
         this.plugin.reloadPlugin();
         sender.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.successful_reload"))));
      }
   }

   @Subcommand({"reset"})
   @Description("Resets all XP/Levels for all the items in the target player.")
   public void onReset(CommandSender sender, Player target, @Switch("all") boolean all) {
      if (this.checkPerm(sender)) {
         ItemStack hand = LevelToolsUtil.getHand(target);
         if (!all) {
            if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
               sender.sendMessage(Text.colorize(this.plugin.getConfig().getString("messages.item_not_tool")));
            } else {
               LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(hand);
               tool.setLevel(0);
               tool.setXp(0.0);
               LevelToolsUtil.setHand(target, tool.getItemStack());
               sender.sendMessage(
                  Text.colorize(
                     this.plugin
                        .getConfig()
                        .getString("messages.successfully_reset_hand_tool", "&aSuccessfully reset tool in hand's XP/Levels for {player}.")
                        .replace("{player}", target.getName())
                  )
               );
            }
         } else {
            PlayerInventory inv = target.getInventory();
            ItemStack[] contents = inv.getContents();

            for (int i = 0; i < contents.length; i++) {
               ItemStack item = contents[i];
               if (item != null && LevelToolsUtil.isSupportedTool(item.getType())) {
                  LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
                  tool.setLevel(0);
                  tool.setXp(0.0);
                  inv.setItem(i, tool.getItemStack());
               }
            }

            sender.sendMessage(
               Text.colorize(
                  Objects.requireNonNull(this.plugin.getConfig().getString("messages.successfully_reset_tools")).replace("{player}", target.getName())
               )
            );
         }
      }
   }

   @Subcommand({"xp"})
   @Description("Sets the item in hand's XP to provided XP.")
   public void onXP(Player player, double xp) {
      if (this.checkPerm(player)) {
         ItemStack item = LevelToolsUtil.getHand(player);
         if (LevelToolsUtil.isSupportedTool(item.getType())) {
            LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
            tool.setXp(xp);
            LevelToolsUtil.setHand(player, tool.getItemStack());
            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.successfully_executed_action"))));
         } else {
            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.item_not_tool"))));
         }
      }
   }

   @Subcommand({"level"})
   @Description("Sets the item in hand's level to provided level.")
   public void onLevel(Player player, int level) {
      if (this.checkPerm(player)) {
         ItemStack item = LevelToolsUtil.getHand(player);
         if (LevelToolsUtil.isSupportedTool(item.getType())) {
            LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
            int initial = tool.getLevel();
            tool.setLevel(level);
            LevelToolsUtil.setHand(player, tool.getItemStack());
            if (initial != tool.getLevel()) {
               LevelToolsUtil.handleReward(tool, player);
            }

            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.successfully_executed_action"))));
         } else {
            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.item_not_tool"))));
         }
      }
   }

   @Subcommand({"levelup"})
   @Description("Increases the item in hand's level to next level.")
   public void onLevelUp(Player player) {
      if (this.checkPerm(player)) {
         ItemStack item = LevelToolsUtil.getHand(player);
         if (LevelToolsUtil.isSupportedTool(item.getType())) {
            LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
            tool.setLevel(tool.getLevel() + 1);
            LevelToolsUtil.setHand(player, tool.getItemStack());
            LevelToolsUtil.handleReward(tool, player);
            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.successfully_executed_action"))));
         } else {
            player.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.item_not_tool"))));
         }
      }
   }

   @Subcommand({"debug"})
   @Description("Shows debug information about the server and plugin.")
   public void onDebug(CommandSender sender) {
      if (this.checkPerm(sender)) {
         this.plugin.getUpdateChecker().check();
         sender.sendMessage("LevelTools Debug Information:");
         sender.sendMessage("- Server Version: " + Bukkit.getVersion());
         sender.sendMessage("- Server Type: " + Bukkit.getBukkitVersion());
         sender.sendMessage("- Plugin Version: " + this.plugin.getDescription().getVersion());
         sender.sendMessage("- Latest Version: " + this.plugin.getUpdateChecker().getLastCheckedVersion());
         sender.sendMessage(
            "{!} Please include your configuration with this when asking for help. Please COPY AND PASTE configuration into discord server. {!}"
         );
      }
   }

   private boolean checkPerm(CommandSender sender) {
      if (!sender.hasPermission("leveltools.admin")) {
         sender.sendMessage(Text.colorize(Objects.requireNonNull(this.plugin.getConfig().getString("messages.no_permission"))));
         return false;
      } else {
         return true;
      }
   }
}
