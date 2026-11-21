package me.jay.GravesX.listener.integration.fancynpcs;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.UUIDUtil;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NpcInteractListener implements Listener {
   private final Graves plugin;

   public NpcInteractListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onNpcInteraction(NpcInteractEvent event) {
      if (event.getInteractionType() != ActionTrigger.RIGHT_CLICK) {
         this.plugin.debugMessage("Interaction for FancyNpcs npc was not a right-click.", 2);
      } else {
         Npc npc = event.getNpc();
         if (npc.getData() == null) {
            this.plugin.debugMessage("NPC Data for FancyNpcs npc returned null.", 2);
         } else {
            String rawId = npc.getData().getName();
            if (rawId != null && !rawId.isEmpty()) {
               UUID graveUuid = UUIDUtil.getUUID(rawId);
               if (graveUuid == null) {
                  this.plugin.debugMessage("NPC UUID Data for FancyNpcs npc returned null.", 2);
               } else {
                  Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUuid);
                  if (grave != null) {
                     if (grave.getUUID().equals(graveUuid)) {
                        this.plugin.getGraveManager().openGrave(event.getPlayer(), npc.getData().getLocation(), grave);
                        this.plugin.debugMessage("FancyNpcs npc is a grave. Opening GUI for " + event.getPlayer().getDisplayName() + ".", 2);
                        event.setCancelled(true);
                     } else {
                        this.plugin.debugMessage("FancyNpcs npc UUID did not match Grave UUID.", 2);
                     }
                  } else {
                     this.plugin.debugMessage("FancyNpcs npc is not a grave. Ignoring...", 2);
                  }
               }
            } else {
               this.plugin.debugMessage("NPC name/id for FancyNpcs npc returned null or empty.", 2);
            }
         }
      }
   }
}
