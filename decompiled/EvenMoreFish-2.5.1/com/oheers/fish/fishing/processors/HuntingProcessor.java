package com.oheers.fish.fishing.processors;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.events.EMFFishHuntEvent;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.messages.ConfigMessage;
import java.time.LocalDateTime;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class HuntingProcessor extends Processor<EntityDeathEvent> implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   protected void process(@NonNull EntityDeathEvent event) {
      if (event.getEntity() instanceof Fish fishEntity) {
         if (!MainConfig.getInstance().isFishHuntIgnoreSpawnerFish() || !fishEntity.fromMobSpawner()) {
            Player player = event.getEntity().getKiller();
            if (player != null) {
               if (this.isCustomFishAllowed(player)) {
                  if (MainConfig.getInstance().requireFishingPermission() && !player.hasPermission("emf.use_rod")) {
                     ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(player);
                  } else {
                     ItemStack fish = this.getCaughtItem(player, fishEntity.getLocation(), player.getInventory().getItemInMainHand());
                     if (fish != null && !fish.isEmpty()) {
                        event.getDrops().clear();
                        if (MainConfig.getInstance().isGiveStraightToInventory() && FishUtils.inventoryHasSpace(player.getInventory())) {
                           FishUtils.giveItem(fish, player);
                        } else {
                           event.getDrops().add(fish);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean isEnabled() {
      return MainConfig.getInstance().isHuntEnabled();
   }

   @Override
   protected boolean competitionOnlyCheck() {
      Competition active = CompetitionManager.getInstance().getActiveCompetition();
      return active != null ? active.getCompetitionFile().isAllowHunting() : !MainConfig.getInstance().isFishHuntOnlyInCompetition();
   }

   @Override
   protected boolean fireEvent(@NonNull IFish fish, @NonNull Player player) {
      return new EMFFishHuntEvent(fish, player, LocalDateTime.now()).callEvent();
   }

   @Override
   protected ConfigMessage getCaughtMessage() {
      return ConfigMessage.FISH_HUNTED;
   }

   @Override
   protected ConfigMessage getLengthlessCaughtMessage() {
      return ConfigMessage.FISH_LENGTHLESS_HUNTED;
   }

   @Override
   protected boolean shouldCatchBait() {
      return false;
   }

   @Override
   public boolean canUseFish(@NonNull IFish fish) {
      return fish.getCatchType() == CatchType.HUNT || fish.getCatchType() == CatchType.BOTH;
   }
}
