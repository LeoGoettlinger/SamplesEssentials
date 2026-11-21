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

public class UnfreezeAll implements TabExecutor {
   private int moduleRegistry;
   ArrayList<String> tabComplete = new ArrayList<>();

   public UnfreezeAll() {
      this.moduleRegistry = this.prepareModuleRegistry();
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
      if (this.unfreezeWorld(sender, args)) {
         return true;
      } else {
         this.unfreezeServer(sender);
         return true;
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
      this.tabComplete.clear();
      if (!sender.hasPermission(Util.unfreezeWorldPermission)) {
         return List.of("");
      } else {
         for (World world : Util.worldFrozen.keySet()) {
            this.tabComplete.add(world.getName());
         }

         return this.tabComplete;
      }
   }

   public boolean unfreezeWorld(CommandSender sender, String[] args) {
      if (args.length == 0) {
         return false;
      } else if (!sender.hasPermission(Util.unfreezeWorldPermission)) {
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
               if (Util.isFrozen(player) && Util.getFreezeType(player) != FreezeType.INDIVIDUAL) {
                  SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, false, FreezeType.NONE));
               }
            }

            Util.worldFrozen.remove(requestedWorld);
            Util.sendUserMessageWithWorld(sender, LocaleHandler.getInstance().getUnfreezeWorldMessage(), requestedWorld);
            return true;
         }
      }
   }

   public void unfreezeServer(CommandSender sender) {
      if (!sender.hasPermission(Util.unfreezeServerPermission)) {
         Util.sendErrorMessage(sender, LocaleHandler.getInstance().getNoPermission());
      } else {
         for (Player player : SimplePlayerFreeze.server.getOnlinePlayers()) {
            if (Util.isFrozen(player) && Util.getFreezeType(player) != FreezeType.INDIVIDUAL) {
               SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, false, FreezeType.NONE));
            }
         }

         Util.setServerFrozen(false);
         Util.sendUserMessage(sender, LocaleHandler.getInstance().getUnfreezeServerMessage());
      }
   }
}
