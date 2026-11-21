package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import org.bukkit.command.CommandSender;

public enum Help {
   INSTANCE;

   public void execute(CommandSender sender) {
      for (String m : AxAFKZone.LANG.getStringList("help")) {
         sender.sendMessage(StringUtils.formatToString(m));
      }
   }
}
