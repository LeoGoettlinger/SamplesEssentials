package com.ranull.graves.listener.integration.playernpc;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.PlayerNPC;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.UUIDUtil;
import dev.sergiferry.playernpc.api.NPC.Global;
import dev.sergiferry.playernpc.api.NPC.Personal;
import dev.sergiferry.playernpc.api.NPC.Events.Interact;
import dev.sergiferry.playernpc.api.NPC.Interact.ClickType;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NPCInteractListener implements Listener {
   private final Graves plugin;
   private final PlayerNPC playerNPC;

   public NPCInteractListener(Graves plugin, PlayerNPC playerNPC) {
      this.plugin = plugin;
      this.playerNPC = playerNPC;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onNPCInteract(Interact event) {
      if (this.isRightClick(event)) {
         this.handleNPCInteraction(event);
      }
   }

   private boolean isRightClick(Interact event) {
      return event.getClickType() == ClickType.RIGHT_CLICK;
   }

   private void handleNPCInteraction(Interact event) {
      Personal npcPersonal = event.getNPC();
      if (npcPersonal.hasGlobal()) {
         Global npcGlobal = npcPersonal.getGlobal();
         if (npcGlobal.hasCustomData(this.plugin, "grave_uuid")) {
            UUID uuid = UUIDUtil.getUUID(npcGlobal.getCustomData(this.plugin, "grave_uuid"));
            if (uuid != null) {
               this.openGraveIfExists(event, npcGlobal, uuid);
            }
         }
      }
   }

   private void openGraveIfExists(Interact event, Global npcGlobal, UUID uuid) {
      Grave grave = this.plugin.getCacheManager().getGraveMap().get(uuid);
      if (grave != null) {
         event.setCancelled(this.plugin.getGraveManager().openGrave(event.getPlayer(), npcGlobal.getLocation(), grave));
      }
   }
}
