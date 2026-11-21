package com.oheers.fish.fishing.processors;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.messages.ConfigMessage;
import java.time.LocalDateTime;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.events.LavaFishCaughtEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LavaFishingProcessor extends Processor<LavaFishCaughtEvent> implements Listener {
   private final EvenMoreFish plugin = EvenMoreFish.getInstance();

   public LavaFishingProcessor() {
      super(FishingType.LAVA);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void process(@NonNull LavaFishCaughtEvent event) {
      TrackedHook hook = event.getTrackedHook();
      Player player = hook.getPlayer();
      ItemStack rod = this.getRod(player);
      if (rod == null) {
         this.plugin.debug("Fishing blocked: could not find rod.");
      } else if (!this.isCustomFishAllowed(player)) {
         this.plugin.debug("Fishing blocked: custom fish not allowed for player %s.".formatted(player.getName()));
      } else if (!Checks.canUseRod(rod)) {
         this.plugin.debug("Fishing blocked: rod unusable (%s).".formatted(rod));
      } else {
         ItemStack fish = this.getCaughtItem(player, hook.getFishHook().getLocation(), rod);
         if (fish == null) {
            this.plugin.debug("Could not obtain fish.");
         } else {
            FishUtils.giveItem(fish, player);
         }
      }
   }

   @Override
   protected boolean isEnabled() {
      return MainConfig.getInstance().isCatchEnabled();
   }

   @Override
   protected boolean competitionOnlyCheck() {
      Competition active = CompetitionManager.getInstance().getActiveCompetition();
      return active != null ? active.getCompetitionFile().isAllowFishing() : !MainConfig.getInstance().isFishCatchOnlyInCompetition();
   }

   @Override
   protected boolean fireEvent(@NonNull IFish fish, @NonNull Player player) {
      return new EMFFishCaughtEvent(fish, player, LocalDateTime.now()).callEvent();
   }

   @Override
   protected ConfigMessage getCaughtMessage() {
      return ConfigMessage.FISH_CAUGHT;
   }

   @Override
   protected ConfigMessage getLengthlessCaughtMessage() {
      return ConfigMessage.FISH_LENGTHLESS_CAUGHT;
   }

   @Override
   protected boolean shouldCatchBait() {
      return true;
   }

   @Override
   public boolean canUseFish(@NonNull IFish fish) {
      return fish.getCatchType().equals(CatchType.CATCH) || fish.getCatchType().equals(CatchType.BOTH);
   }

   @Nullable
   private ItemStack getRod(@NonNull Player player) {
      ItemStack mainHand = player.getInventory().getItem(EquipmentSlot.HAND);
      if (mainHand.getType() == Material.FISHING_ROD) {
         return mainHand;
      } else {
         ItemStack offHand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
         return offHand.getType() == Material.FISHING_ROD ? offHand : null;
      }
   }
}
