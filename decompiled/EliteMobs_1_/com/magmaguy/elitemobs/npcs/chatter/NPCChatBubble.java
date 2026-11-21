package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NPCChatBubble {
   public NPCChatBubble(String message, final NPCEntity npcEntity, Player player) {
      if (npcEntity.getNPCsConfigFields().isCanTalk()) {
         if (!PatrolService.isActivelyMoving(npcEntity) && !PatrolEditor.isEditing(npcEntity)) {
            if (message != null) {
               if (npcEntity.getVillager() != null && npcEntity.getVillager().isValid()) {
                  if (!npcEntity.getVillager().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                     if (!npcEntity.getIsTalking()) {
                        npcEntity.startTalkingCooldown();
                        int lineCounter = 0;

                        for (String substring : message.split("\\\\n")) {
                           final Location newLocation = npcEntity.getVillager()
                              .getEyeLocation()
                              .clone()
                              .add(player.getLocation().clone().subtract(npcEntity.getVillager().getLocation()).toVector().normalize().multiply(0.5))
                              .add(new Vector(0.0, -0.4 - 0.2 * lineCounter, 0.0));
                           final FakeText fakeText = VisualDisplay.generateFakeText(newLocation, substring, player);
                           if (fakeText == null) {
                              return;
                           }

                           (new BukkitRunnable() {
                                 int counter = 0;
                                 Location currentLocation = newLocation.clone();

                                 public void run() {
                                    if (this.counter <= 100
                                       && npcEntity.getVillager() != null
                                       && npcEntity.getVillager().isValid()
                                       && !PatrolService.isActivelyMoving(npcEntity)
                                       && !PatrolEditor.isEditing(npcEntity)) {
                                       this.currentLocation.add(0.0, 0.005, 0.0);
                                       fakeText.teleport(this.currentLocation);
                                       this.counter++;
                                    } else {
                                       fakeText.remove();
                                       this.cancel();
                                    }
                                 }
                              })
                              .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
                           lineCounter++;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public NPCChatBubble(List<String> message, NPCEntity npcEntity, Player player) {
      String selectedMessage = message.get(ThreadLocalRandom.current().nextInt(message.size()));
      new NPCChatBubble(selectedMessage, npcEntity, player);
   }
}
