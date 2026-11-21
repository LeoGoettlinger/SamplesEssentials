package com.oheers.fish.events;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.Toggle;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent.Cause;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AuraSkillsFishingEvent implements Listener {
   private final Set<Cause> causes = Set.of(Cause.FISHING_LUCK, Cause.TREASURE_HUNTER, Cause.FISHING_OTHER_LOOT, Cause.EPIC_CATCH);

   @EventHandler
   public void fishCatch(LootDropEvent event) {
      Toggle toggle = EvenMoreFish.getInstance().getToggle();
      if (this.causes.contains(event.getCause()) && MainConfig.getInstance().disableAuraSkills() && !toggle.isCustomFishingDisabled(event.getPlayer())) {
         PlayerInventory inventory = event.getPlayer().getInventory();
         ItemStack main = inventory.getItemInMainHand();
         ItemStack off = inventory.getItemInOffHand();
         if (Checks.canUseRod(main) || Checks.canUseRod(off)) {
            if (!MainConfig.getInstance().isFishCatchOnlyInCompetition()) {
               event.setCancelled(true);
            } else {
               if (CompetitionManager.getInstance().isCompetitionActive()) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
