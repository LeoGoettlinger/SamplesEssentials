package de.oliver.fancynpcs.listeners;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.events.NpcPreInteractEvent;
import de.oliver.fancysitula.api.utils.ServerVersion;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerUseUnknownEntityListener implements Listener {
   @EventHandler
   public void onPlayerUseUnknownEntity(PlayerUseUnknownEntityEvent event) {
      Player player = event.getPlayer();
      Npc npc = FancyNpcs.getInstance().getNpcManagerImpl().getNpc(event.getEntityId());
      if (npc != null) {
         if (event.getHand() == EquipmentSlot.HAND) {
            ActionTrigger actionTrigger = event.isAttack() ? ActionTrigger.LEFT_CLICK : ActionTrigger.RIGHT_CLICK;
            if (ServerVersion.getCurrentVersion().getProtocolVersion() < ServerVersion.v26_1_2.getProtocolVersion()) {
               if (event.isAttack() || event.getClickedRelativePosition() == null || npc.getData().getType() == EntityType.ARMOR_STAND) {
                  if (!new NpcPreInteractEvent(npc, player, actionTrigger).callEvent()) {
                     return;
                  }

                  npc.interact(player, actionTrigger);
               }
            } else {
               if (!new NpcPreInteractEvent(npc, player, actionTrigger).callEvent()) {
                  return;
               }

               npc.interact(player, actionTrigger);
            }
         }
      }
   }
}
