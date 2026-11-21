package com.oheers.fish.fishing.processors;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.messages.ConfigMessage;
import java.time.LocalDateTime;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishingProcessor extends Processor<PlayerFishEvent> implements Listener {
   private final EvenMoreFish plugin = EvenMoreFish.getInstance();

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void process(@NonNull PlayerFishEvent event) {
      if (event.isCancelled()) {
         this.plugin.debug("Fishing event was cancelled. Skipping handling.");
      } else {
         ItemStack rod = this.getRod(event);
         if (rod == null) {
            this.plugin.debug("Custom fishing blocked: could not find rod.");
         } else if (!this.isCustomFishAllowed(event.getPlayer())) {
            this.plugin.debug("Custom fishing blocked: custom fish not allowed for player %s.".formatted(event.getPlayer().getName()));
         } else if (!Checks.canUseRod(rod)) {
            this.plugin.debug("Custom fishing blocked: rod unusable (%s).".formatted(rod));
         } else if (MainConfig.getInstance().requireFishingPermission() && !event.getPlayer().hasPermission("emf.use_rod")) {
            this.plugin.debug("Custom fishing blocked: permission required and player lacks it.");
            if (event.getState() == State.FISHING) {
               ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(event.getPlayer());
            }
         } else if (event.getState() != State.CAUGHT_FISH) {
            this.plugin.debug("Ignoring event state: %s".formatted(event.getState()));
         } else if (event.getCaught() instanceof Item nonCustom) {
            if (MainConfig.getInstance().isFishCatchOverrideOnlyFish() && !Tag.ITEMS_FISHES.isTagged(nonCustom.getItemStack().getType())) {
               this.plugin.debug("Caught item is not a vanilla fish, and we have been told to skip non-fish. Skipping.");
            } else {
               ItemStack fish = this.getCaughtItem(event.getPlayer(), event.getHook().getLocation(), rod);
               if (fish == null) {
                  this.plugin.debug("Could not obtain fish.");
               } else if (MainConfig.getInstance().isGiveStraightToInventory() && FishUtils.inventoryHasSpace(event.getPlayer().getInventory())) {
                  FishUtils.giveItem(fish, event.getPlayer());
                  nonCustom.remove();
               } else if (fish.isEmpty()) {
                  nonCustom.remove();
               } else {
                  nonCustom.setItemStack(fish);
               }
            }
         } else {
            this.plugin.debug("Caught entity is not an Item.");
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
   private ItemStack getRod(@NonNull PlayerFishEvent event) {
      Player player = event.getPlayer();
      if (event.getState() == State.FISHING) {
         EquipmentSlot hand = event.getHand();
         return hand != null ? player.getInventory().getItem(hand) : null;
      } else {
         ItemStack mainHand = player.getInventory().getItem(EquipmentSlot.HAND);
         if (mainHand.getType() == Material.FISHING_ROD) {
            return mainHand;
         } else {
            ItemStack offHand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
            return offHand.getType() == Material.FISHING_ROD ? offHand : null;
         }
      }
   }
}
