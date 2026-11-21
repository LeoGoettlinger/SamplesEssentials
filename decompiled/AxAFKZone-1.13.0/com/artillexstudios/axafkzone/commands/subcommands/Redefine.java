package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.selection.Selection;
import com.artillexstudios.axafkzone.zones.Zone;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.entity.Player;

public enum Redefine {
   INSTANCE;

   public void execute(Player sender, Zone zone) {
      Selection sel = WandListeners.getSelections().remove(sender);
      if (sel == null) {
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", zone.getName()));
      } else if (sel.getPosition1() != null && sel.getPosition2() != null && Objects.equals(sel.getPosition1().getWorld(), sel.getPosition2().getWorld())) {
         zone.setRegion(new Region(sel.getPosition1(), sel.getPosition2(), zone));
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "zone.redefined", Collections.singletonMap("%name%", zone.getName()));
      } else {
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", zone.getName()));
      }
   }
}
