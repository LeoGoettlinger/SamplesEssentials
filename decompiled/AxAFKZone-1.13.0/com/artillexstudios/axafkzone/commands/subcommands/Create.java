package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.selection.Selection;
import com.artillexstudios.axafkzone.utils.FileUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.entity.Player;

public enum Create {
   INSTANCE;

   public void execute(Player sender, String name) {
      Zone zone = Zones.getZoneByName(name);
      if (zone != null) {
         AxAFKZone.MESSAGEUTILS.sendLang(sender, "zone.already-exists", Collections.singletonMap("%name%", name));
      } else {
         Selection sel = WandListeners.getSelections().remove(sender);
         if (sel == null) {
            AxAFKZone.MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", name));
         } else if (sel.getPosition1() != null && sel.getPosition2() != null && Objects.equals(sel.getPosition1().getWorld(), sel.getPosition2().getWorld())) {
            FileUtils.create(name, sel);
            AxAFKZone.MESSAGEUTILS.sendLang(sender, "zone.created", Collections.singletonMap("%name%", name));
         } else {
            AxAFKZone.MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", name));
         }
      }
   }
}
