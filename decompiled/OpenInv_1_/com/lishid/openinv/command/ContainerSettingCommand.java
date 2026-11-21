package com.lishid.openinv.command;

import com.lishid.openinv.event.OpenEvents;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import com.lishid.openinv.util.setting.PlayerToggle;
import com.lishid.openinv.util.setting.PlayerToggles;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ContainerSettingCommand implements TabExecutor {
   @NotNull
   private final LanguageManager lang;

   public ContainerSettingCommand(@NotNull LanguageManager lang) {
      this.lang = lang;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (sender instanceof Player player) {
         PlayerToggle toggle = PlayerToggles.get(command.getName());
         if (toggle == null) {
            JavaPlugin.getProvidingPlugin(this.getClass()).getLogger().warning("Command /" + command.getName() + " registered with no corresponding toggle!");
            return false;
         } else {
            UUID playerId = player.getUniqueId();
            if (args.length > 0) {
               args[0] = args[0].toLowerCase(Locale.ENGLISH);
               if (args[0].equals("on")) {
                  this.set(toggle, playerId, true);
               } else if (args[0].equals("off")) {
                  this.set(toggle, playerId, false);
               } else if (!args[0].equals("check")) {
                  return false;
               }
            } else {
               this.set(toggle, playerId, !toggle.is(playerId));
            }

            String onOff = this.lang.getLocalizedMessage(player, toggle.is(playerId) ? "messages.info.on" : "messages.info.off");
            if (onOff == null) {
               onOff = String.valueOf(toggle.is(playerId));
            }

            this.lang.sendMessage(sender, "messages.info.settingState", new Replacement("%setting%", toggle.getName()), new Replacement("%state%", onOff));
            return true;
         }
      } else {
         this.lang.sendMessage(sender, "messages.error.consoleUnsupported");
         return true;
      }
   }

   private void set(@NotNull PlayerToggle toggle, @NotNull UUID uuid, boolean state) {
      if (toggle.set(uuid, state)) {
         OpenEvents.notifyPlayerToggle(toggle, uuid, state);
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      return command.testPermissionSilent(sender) && args.length == 1
         ? TabCompleter.completeString(args[0], new String[]{"check", "on", "off"})
         : Collections.emptyList();
   }
}
