package simplexity.simpleplayerfreeze.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simpleplayerfreeze.SimplePlayerFreeze;
import simplexity.simpleplayerfreeze.Util;
import simplexity.simpleplayerfreeze.configs.LocaleHandler;
import simplexity.simpleplayerfreeze.events.PlayerFreezeEvent;
import simplexity.simpleplayerfreeze.freeze.FreezeType;

public class FreezeAll implements TabExecutor {
   ArrayList<String> tabComplete = new ArrayList<>();

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
      if (this.freezeWorld(sender, args)) {
         return true;
      } else {
         this.freezeServer(sender);
         return true;
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
      this.tabComplete.clear();
      if (!sender.hasPermission(Util.freezeWorldPermission)) {
         return List.of("");
      } else {
         for (World world : SimplePlayerFreeze.server.getWorlds()) {
            this.tabComplete.add(world.getName());
         }

         return this.tabComplete;
      }
   }

   public boolean freezeWorld(CommandSender sender, String[] args) {
      if (args.length == 0) {
         return false;
      } else if (!sender.hasPermission(Util.freezeWorldPermission)) {
         Util.sendErrorMessage(sender, LocaleHandler.getInstance().getNoPermission());
         return false;
      } else {
         String worldName = args[0];
         World requestedWorld = SimplePlayerFreeze.server.getWorld(worldName);
         if (requestedWorld == null) {
            Util.sendErrorMessage(sender, LocaleHandler.getInstance().getWorldNotFound());
            return false;
         } else {
            for (Player player : requestedWorld.getPlayers()) {
               if (!Util.isFrozen(player)) {
                  SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, true, FreezeType.WORLD));
               }
            }

            Util.worldFrozen.put(requestedWorld, true);
            Util.sendUserMessageWithWorld(sender, LocaleHandler.getInstance().getFreezeWorldMessage(), requestedWorld);
            return true;
         }
      }
   }

   public void freezeServer(CommandSender sender) {
      if (!sender.hasPermission(Util.freezeServerPermission)) {
         Util.sendErrorMessage(sender, LocaleHandler.getInstance().getNoPermission());
      } else {
         for (Player player : SimplePlayerFreeze.server.getOnlinePlayers()) {
            if (!Util.isFrozen(player)) {
               SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, true, FreezeType.SERVER));
            }
         }

         Util.setServerFrozen(true);
         Util.sendUserMessage(sender, LocaleHandler.getInstance().getFreezeServerMessage());
      }
   }
}
