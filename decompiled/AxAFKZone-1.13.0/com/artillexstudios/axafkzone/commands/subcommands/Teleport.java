package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import java.util.Collections;
import org.bukkit.entity.Player;

public enum Teleport {
   INSTANCE;

   public void execute(Player sender, Zone zone) {
      if (zone.getRegion() == null) {
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "teleport.fail", Collections.singletonMap("%name%", zone.getName()));
      } else {
         PaperUtils.teleportAsync(sender, zone.getRegion().getCenter());
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "teleport.success", Collections.singletonMap("%name%", zone.getName()));
      }
   }
}
