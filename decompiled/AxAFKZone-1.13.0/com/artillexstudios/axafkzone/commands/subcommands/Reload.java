package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import com.artillexstudios.axafkzone.utils.FileUtils;
import com.artillexstudios.axafkzone.utils.NumberUtils;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public enum Reload {
   INSTANCE;

   public void execute(CommandSender sender) {
      Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055[AxAFKZone] &#FF8855Reloading configuration..."));
      if (!AxAFKZone.CONFIG.reload()) {
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "config.yml"));
      } else {
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╠ &#FF8855Reloaded &fconfig.yml&#FF8855!"));
         if (!AxAFKZone.LANG.reload()) {
            AxAFKZone.MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "lang.yml"));
         } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╠ &#FF8855Reloaded &flang.yml&#FF8855!"));
            NumberUtils.reload();
            FileUtils.loadAll();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╚ &#FF8855Successful reload!"));
            AxAFKZone.MESSAGEUTILS.sendLang(sender, "reload.success");
         }
      }
   }
}
