package simplexity.simpleplayerfreeze.listeners;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import simplexity.simpleplayerfreeze.SimplePlayerFreeze;
import simplexity.simpleplayerfreeze.Util;
import simplexity.simpleplayerfreeze.configs.ConfigHandler;
import simplexity.simpleplayerfreeze.configs.LocaleHandler;
import simplexity.simpleplayerfreeze.events.PlayerFreezeEvent;
import simplexity.simpleplayerfreeze.freeze.FreezeType;

public class JoinListener implements Listener {
   public static ArrayList<Player> notifyList = new ArrayList<>();
   public static ArrayList<Player> spyList = new ArrayList<>();

   @EventHandler
   public void onJoin(PlayerJoinEvent joinEvent) {
      Player player = joinEvent.getPlayer();
      if (player.hasPermission(Util.freezeNotify)) {
         notifyList.add(player);
      }

      if (player.hasPermission(Util.freezeChatSpy)) {
         spyList.add(player);
      }

      if (!player.hasPermission(Util.freezeBypassPermission)) {
         if (Util.isServerFrozen()) {
            if (ConfigHandler.getInstance().shouldFreezeNewLogins()) {
               this.freezePlayer(joinEvent, FreezeType.SERVER);
               this.sendMessages(player, LocaleHandler.getInstance().getLoginNotifServerFrozen());
            }
         } else {
            Boolean worldFrozen = Util.worldFrozen.get(player.getWorld());
            if (worldFrozen != null && worldFrozen) {
               if (ConfigHandler.getInstance().shouldFreezeNewLogins()) {
                  this.freezePlayer(joinEvent, FreezeType.WORLD);
                  this.sendMessages(player, LocaleHandler.getInstance().getFreezeWorldChange());
               }
            } else if (Util.isFrozen(player) && !ConfigHandler.getInstance().shouldFreezePersist()) {
               this.unfreezePlayer(joinEvent);
               this.sendMessages(player, LocaleHandler.getInstance().getLoginNotifNowUnfrozen());
            } else {
               if (Util.isFrozen(player) && ConfigHandler.getInstance().shouldFreezePersist()) {
                  FreezeType freezeType = Util.getFreezeType(player);
                  if (freezeType != FreezeType.INDIVIDUAL) {
                     this.unfreezePlayer(joinEvent);
                     this.sendMessages(player, LocaleHandler.getInstance().getLoginNotifNowUnfrozen());
                     return;
                  }

                  this.freezePlayer(joinEvent, FreezeType.INDIVIDUAL);
                  this.sendMessages(player, LocaleHandler.getInstance().getLoginNotif());
               }
            }
         }
      }
   }

   private void freezePlayer(PlayerJoinEvent joinEvent, FreezeType freezeType) {
      if (ConfigHandler.getInstance().shouldFreezeNewLogins()) {
         Player player = joinEvent.getPlayer();
         SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, true, freezeType));
      }
   }

   private void unfreezePlayer(PlayerJoinEvent joinEvent) {
      Player player = joinEvent.getPlayer();
      SimplePlayerFreeze.getInstance().getServer().getPluginManager().callEvent(new PlayerFreezeEvent(player, false, FreezeType.NONE));
   }

   private void sendMessages(Player player, String message) {
      for (Player playerNotif : notifyList) {
         Util.sendUserMessageWithPlayer(playerNotif, message, player);
      }

      if (ConfigHandler.getInstance().shouldConsoleBeNotified()) {
         Util.sendUserMessageWithPlayer(SimplePlayerFreeze.getSFConsoleSender(), message, player);
      }
   }
}
