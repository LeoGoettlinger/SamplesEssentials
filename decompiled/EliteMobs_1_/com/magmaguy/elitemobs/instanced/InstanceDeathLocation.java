package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.magmacore.match.MatchPlayer;
import com.magmaguy.elitemobs.magmacore.match.ReviveMarker;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class InstanceDeathLocation {
   private final MatchInstance matchInstance;
   private final Block bannerBlock;
   private final Player deadPlayer;
   private FakeText nameTag;
   private FakeText livesLeft;
   private FakeText instructions;

   private InstanceDeathLocation(MatchInstance matchInstance, Player deadPlayer, Block bannerBlock) {
      this.matchInstance = matchInstance;
      this.deadPlayer = deadPlayer;
      this.bannerBlock = bannerBlock;
   }

   static ReviveMarker marker(MatchPlayer participant) {
      final MatchInstance instance = (MatchInstance)participant.getMatch();
      return new ReviveMarker() {
         private InstanceDeathLocation shown;

         @Override
         public void show(Block banner, MatchPlayer dead, int lives) {
            this.hide();
            this.shown = new InstanceDeathLocation(instance, dead.getPlayer(), banner);
            this.shown.createDisplays(lives);
            instance.deathBanners.put(banner, this.shown);
         }

         @Override
         public void hide() {
            if (this.shown != null) {
               instance.deathBanners.remove(this.shown.bannerBlock, this.shown);
               this.shown.removeDisplays();
               this.shown = null;
            }
         }
      };
   }

   private void createDisplays(int lives) {
      Location deathLocation = this.bannerBlock.getLocation();
      this.instructions = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0.0, 2.2, 0.0)), DungeonsConfig.getInstancePunchToRez(), 30.0);
      this.nameTag = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 2, 0)), this.deadPlayer.getDisplayName(), 30.0);
      this.livesLeft = VisualDisplay.generateFakeText(
         deathLocation.clone().add(new Vector(0.0, 1.8, 0.0)), DungeonsConfig.getInstanceLivesLeft().replace("$amount", String.valueOf(lives)), 30.0
      );
   }

   private void removeDisplays() {
      if (this.nameTag != null) {
         this.nameTag.remove();
      }

      if (this.instructions != null) {
         this.instructions.remove();
      }

      if (this.livesLeft != null) {
         this.livesLeft.remove();
      }

      this.nameTag = null;
      this.instructions = null;
      this.livesLeft = null;
   }

   public void clear(boolean resurrect) {
      this.matchInstance.releaseBanner(this.deadPlayer, resurrect);
   }

   public Location getRespawnLocation() {
      return this.bannerBlock.getLocation().clone().add(0.5, 0.0, 0.5);
   }

   public void bannerWatchdog() {
   }

   @Generated
   public Block getBannerBlock() {
      return this.bannerBlock;
   }

   @Generated
   public Player getDeadPlayer() {
      return this.deadPlayer;
   }
}
