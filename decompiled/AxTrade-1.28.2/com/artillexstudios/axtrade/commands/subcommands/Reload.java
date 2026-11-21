package com.artillexstudios.axtrade.commands.subcommands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.hooks.HookManager;
import com.artillexstudios.axtrade.lang.LanguageManager;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.utils.NumberUtils;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public enum Reload {
   INSTANCE;

   public void execute(CommandSender sender) {
      Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD[AxTrade] &#AAFFDDReloading configuration..."));
      if (!AxTrade.CONFIG.reload()) {
         AxTrade.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "config.yml"));
      } else {
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fconfig.yml&#AAFFDD!"));
         if (!AxTrade.LANG.reload()) {
            AxTrade.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "lang.yml"));
         } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &flang.yml&#AAFFDD!"));
            if (!AxTrade.GUIS.reload()) {
               AxTrade.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "guis.yml"));
            } else {
               Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fguis.yml&#AAFFDD!"));
               if (!AxTrade.HOOKS.reload()) {
                  AxTrade.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "currencies.yml"));
               } else {
                  Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fcurrencies.yml&#AAFFDD!"));
                  LanguageManager.reload();
                  HookManager.updateHooks();
                  NumberUtils.reload();
                  Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╚ &#AAFFDDSuccessful reload!"));
                  AxTrade.MESSAGEUTILS.sendLang(sender, "reload.success");
               }
            }
         }
      }
   }
}
