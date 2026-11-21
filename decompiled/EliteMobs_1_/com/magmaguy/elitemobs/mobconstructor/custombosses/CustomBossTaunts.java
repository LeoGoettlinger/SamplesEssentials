package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.powers.specialpowers.NameTagMessageSupport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CustomBossTaunts implements Listener {
   @EventHandler
   public void onDamagedMessages(EliteMobDamagedEvent eliteMobDamagedEvent) {
      if (eliteMobDamagedEvent.getEliteEntity() instanceof CustomBossEntity) {
         if (((CustomBossEntity)eliteMobDamagedEvent.getEliteEntity()).getCustomBossesConfigFields().getOnDamagedMessages() != null
            && !((CustomBossEntity)eliteMobDamagedEvent.getEliteEntity()).getCustomBossesConfigFields().getOnDamagedMessages().isEmpty()) {
            NameTagMessageSupport.nameTagProcessor(
               eliteMobDamagedEvent.getEliteEntity(),
               eliteMobDamagedEvent.getEliteEntity().getLivingEntity(),
               ((CustomBossEntity)eliteMobDamagedEvent.getEliteEntity()).getCustomBossesConfigFields().getOnDamagedMessages()
            );
         }
      }
   }

   @EventHandler
   public void onDamageMessages(PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
      if (playerDamagedByEliteMobEvent.getEliteMobEntity() instanceof CustomBossEntity) {
         if (((CustomBossEntity)playerDamagedByEliteMobEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamageMessages() != null
            && !((CustomBossEntity)playerDamagedByEliteMobEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamageMessages().isEmpty()) {
            NameTagMessageSupport.nameTagProcessor(
               playerDamagedByEliteMobEvent.getEliteMobEntity(),
               playerDamagedByEliteMobEvent.getEliteMobEntity().getLivingEntity(),
               ((CustomBossEntity)playerDamagedByEliteMobEvent.getEliteMobEntity()).getCustomBossesConfigFields().getOnDamageMessages()
            );
         }
      }
   }
}
