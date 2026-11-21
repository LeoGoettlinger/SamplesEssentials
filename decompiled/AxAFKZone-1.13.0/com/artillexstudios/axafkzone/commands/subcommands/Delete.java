package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.utils.FileUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import java.util.Collections;
import org.bukkit.command.CommandSender;

public enum Delete {
   INSTANCE;

   public void execute(CommandSender sender, Zone zone) {
      FileUtils.delete(zone);
      AxAFKZone.MESSAGEUTILS.sendLang(sender, "zone.deleted", Collections.singletonMap("%name%", zone.getName()));
   }
}
