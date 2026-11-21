package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class TransitiveBlockCommand {
   public static HashMap<UUID, TransitiveBlockCommand> activePlayers = new HashMap<>();
   private final Player player;
   private final CustomBossesConfigFields customBossesConfigFields;
   private final TransitiveBlockCommand.TransitiveBlockType transitiveBlockType;
   private final Map<TransitiveBlockCommand.RelativePosition, TransitiveBlock> selection = new LinkedHashMap<>();
   private RegionalBossEntity regionalBossEntity;
   private boolean regionalSelection = false;
   private Location corner1;
   private Location corner2;

   public TransitiveBlockCommand(
      Player player, CustomBossesConfigFields customBossesConfigFields, TransitiveBlockCommand.TransitiveBlockType transitiveBlockType, boolean edit
   ) {
      this.player = player;
      this.customBossesConfigFields = customBossesConfigFields;
      this.transitiveBlockType = transitiveBlockType;
      List<RegionalBossEntity> regionalBossEntities = RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields);
      if (regionalBossEntities.isEmpty()) {
         for (RegionalBossEntity iteratedRegionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet()) {
            if (iteratedRegionalBossEntity.getPhaseBossEntity() != null) {
               for (PhaseBossEntity.BossPhase bossPhase : iteratedRegionalBossEntity.getPhaseBossEntity().getBossPhases()) {
                  if (bossPhase.customBossesConfigFields == null) {
                     String message = "Could not find valid custom boss config fields for phase boss! This is probably a configuration issue. Check why your phase boss isn't valid on console logs on /em reload and make sure to test the phases in-game!";
                     Logger.warn(message);
                     player.sendMessage(message);
                  } else if (bossPhase.customBossesConfigFields.equals(customBossesConfigFields)) {
                     this.regionalBossEntity = iteratedRegionalBossEntity;
                     break;
                  }
               }
            }
         }

         if (this.regionalBossEntity == null) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockRequiresRegionalMessage());
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancellingMessage());
            return;
         }
      }

      if (this.regionalBossEntity == null) {
         if (regionalBossEntities.size() > 1) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockMultipleBossesMessage());
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockMultipleBossesHintMessage());
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancellingMessage());
            return;
         }

         this.regionalBossEntity = regionalBossEntities.get(0);
      }

      if (edit) {
         switch (transitiveBlockType) {
            case ON_SPAWN:
               this.loadSelection(customBossesConfigFields.getOnSpawnBlockStates());
               break;
            case ON_REMOVE:
               this.loadSelection(customBossesConfigFields.getOnRemoveBlockStates());
         }
      }

      activePlayers.put(player.getUniqueId(), this);
      player.sendMessage(CommandMessagesConfig.getTransitiveBlockNowRegisteringMessage().replace("$type", transitiveBlockType.toString()));
      player.sendMessage(CommandMessagesConfig.getTransitiveBlockRunSameCommandMessage());
      player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancelCommandMessage());
   }

   private void loadSelection(List<String> raw) {
      for (TransitiveBlock block : TransitiveBlock.serializeTransitiveBlocks(raw, this.customBossesConfigFields.getFilename())) {
         this.selection.put(TransitiveBlockCommand.RelativePosition.of(block.getRelativeLocation()), block);
      }
   }

   public static void shutdown() {
      activePlayers.clear();
   }

   public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit, boolean regionalSelection) {
      TransitiveBlockCommand transitiveBlockCommand = processCommand(player, filename, transitiveBlockType, edit);
      if (transitiveBlockCommand != null) {
         transitiveBlockCommand.setRegionalSelection(regionalSelection);
      }

      if (transitiveBlockCommand != null) {
         player.sendMessage(CommandMessagesConfig.getTransitiveBlockStartMessage());
      }

      return transitiveBlockCommand;
   }

   public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit) {
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
      if (customBossesConfigFields == null) {
         player.sendMessage(CommandMessagesConfig.getTransitiveBlockInvalidBossMessage());
         return null;
      } else {
         if (!customBossesConfigFields.isRegionalBoss()) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockNotRegionalMessage());
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockIgnoreWarningMessage());
         }

         TransitiveBlockCommand.TransitiveBlockType tbt = null;

         try {
            tbt = TransitiveBlockCommand.TransitiveBlockType.valueOf(transitiveBlockType);
         } catch (Exception var7) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockInvalidTypeMessage());
            return null;
         }

         UUID playerUUID = player.getUniqueId();
         if (activePlayers.containsKey(playerUUID)) {
            activePlayers.get(playerUUID).commitLocations();
            return null;
         } else {
            return new TransitiveBlockCommand(player, customBossesConfigFields, tbt, edit);
         }
      }
   }

   public static void processCommand(Player player) {
      activePlayers.remove(player.getUniqueId());
      player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancelledMessage());
   }

   public void setCorner(boolean leftClick, Location location) {
      if (this.matchesAnchorWorld(location)) {
         if (leftClick) {
            this.corner1 = location.clone();
            this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockCorner1Message());
         } else {
            this.corner2 = location.clone();
            this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockCorner2Message());
         }

         if (this.corner1 != null && this.corner2 != null && this.corner1.getWorld() == this.corner2.getWorld()) {
            long blockCount = this.selectionVolume();
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter()) {
               this.player
                  .sendMessage(
                     CommandMessagesConfig.getTransitiveBlockSelectionCountMessage()
                        .replace("$count", String.valueOf(blockCount))
                        .replace("$limit", String.valueOf(DefaultConfig.getDefaultTransitiveBlockLimiter()))
                  );
            }
         }
      }
   }

   private Vector getRelativeCoordinate(Location location) {
      return new Vector(
         location.getX() - this.regionalBossEntity.getSpawnLocation().getX(),
         location.getY() - this.regionalBossEntity.getSpawnLocation().getY(),
         location.getZ() - this.regionalBossEntity.getSpawnLocation().getZ()
      );
   }

   private boolean matchesAnchorWorld(Location location) {
      Location anchor = this.regionalBossEntity.getSpawnLocation();
      if (location != null && anchor != null && anchor.getWorld() != null && anchor.getWorld().equals(location.getWorld())) {
         return true;
      } else {
         this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockWorldMismatchMessage());
         return false;
      }
   }

   private long selectionVolume() {
      long x = Math.abs((long)this.corner1.getBlockX() - this.corner2.getBlockX()) + 1L;
      long y = Math.abs((long)this.corner1.getBlockY() - this.corner2.getBlockY()) + 1L;
      long z = Math.abs((long)this.corner1.getBlockZ() - this.corner2.getBlockZ()) + 1L;

      try {
         return Math.multiplyExact(Math.multiplyExact(x, y), z);
      } catch (ArithmeticException var8) {
         return Long.MAX_VALUE;
      }
   }

   public void registerBlock(Block block) {
      this.register(block, block.getBlockData());
   }

   public void registerAir(Block block) {
      this.register(block, Material.AIR.createBlockData());
   }

   private void register(Block block, BlockData data) {
      Location location = block.getLocation();
      if (this.matchesAnchorWorld(location)) {
         Vector relative = this.getRelativeCoordinate(location);
         TransitiveBlockCommand.RelativePosition key = TransitiveBlockCommand.RelativePosition.of(relative);
         TransitiveBlock previous = this.selection.get(key);
         if (previous != null && previous.isAir() != data.getMaterial().isAir()) {
            this.selection.remove(key);
            this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockUnregisteredMessage());
         } else {
            this.selection.put(key, new TransitiveBlock(data, relative));
            this.player
               .sendMessage(
                  data.getMaterial().isAir()
                     ? CommandMessagesConfig.getTransitiveBlockRegisteredAirMessage()
                     : CommandMessagesConfig.getTransitiveBlockRegisteredMessage().replace("$type", data.getMaterial().toString())
               );
         }
      }
   }

   public void commitLocations() {
      if (activePlayers.get(this.player.getUniqueId()) == this) {
         if (this.regionalSelection) {
            if (this.corner1 == null || this.corner2 == null) {
               this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockMissingCornersMessage());
               return;
            }

            if (!this.matchesAnchorWorld(this.corner1) || !this.matchesAnchorWorld(this.corner2) || !this.matchesAnchorWorld(this.player.getLocation())) {
               return;
            }
         }

         this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockNowSavingMessage().replace("$type", this.transitiveBlockType.toString()));
         List<String> deserializedData = new ArrayList<>();
         LinkedHashMap<TransitiveBlockCommand.RelativePosition, TransitiveBlock> capturedBlocks = new LinkedHashMap<>(this.selection);
         if (this.regionalSelection) {
            long blockCount = this.selectionVolume();
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter()) {
               this.player
                  .sendMessage(
                     CommandMessagesConfig.getTransitiveBlockTooManyMessage()
                        .replace("$limit", String.valueOf(DefaultConfig.getDefaultTransitiveBlockLimiter()))
                  );
            }

            int lowestX = Math.min(this.corner1.getBlockX(), this.corner2.getBlockX());
            int highestX = Math.max(this.corner1.getBlockX(), this.corner2.getBlockX());
            int lowestY = Math.min(this.corner1.getBlockY(), this.corner2.getBlockY());
            int highestY = Math.max(this.corner1.getBlockY(), this.corner2.getBlockY());
            int lowestZ = Math.min(this.corner1.getBlockZ(), this.corner2.getBlockZ());
            int highestZ = Math.max(this.corner1.getBlockZ(), this.corner2.getBlockZ());

            for (int x = lowestX; x < highestX + 1; x++) {
               for (int y = lowestY; y < highestY + 1; y++) {
                  for (int z = lowestZ; z < highestZ + 1; z++) {
                     Location blockLocation = new Location(this.corner1.getWorld(), x, y, z);
                     Vector relative = this.getRelativeCoordinate(blockLocation);
                     capturedBlocks.put(
                        TransitiveBlockCommand.RelativePosition.of(relative), new TransitiveBlock(blockLocation.getBlock().getBlockData(), relative)
                     );
                  }
               }
            }

            this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockRegisteredCornerMessage().replace("$count", String.valueOf(capturedBlocks.size())));
         }

         for (TransitiveBlock transitiveBlock : capturedBlocks.values()) {
            String deserializedString = transitiveBlock.getRelativeLocation().getX()
               + ","
               + transitiveBlock.getRelativeLocation().getY()
               + ","
               + transitiveBlock.getRelativeLocation().getZ()
               + "/"
               + transitiveBlock.getBlockData().getAsString();
            deserializedData.add(deserializedString);
         }

         try {
            switch (this.transitiveBlockType) {
               case ON_SPAWN:
                  this.customBossesConfigFields.setOnSpawnBlockStates(deserializedData);
                  if (this.regionalBossEntity.getCustomBossesConfigFields().getFilename().equals(this.customBossesConfigFields.getFilename())) {
                     this.regionalBossEntity
                        .setOnSpawnTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, this.customBossesConfigFields.getFilename()));
                  }

                  this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsRegisteredMessage());
                  break;
               case ON_REMOVE:
                  this.customBossesConfigFields.setOnRemoveBlockStates(deserializedData);
                  if (this.regionalBossEntity.getCustomBossesConfigFields().getFilename().equals(this.customBossesConfigFields.getFilename())) {
                     this.regionalBossEntity
                        .setOnRemoveTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, this.customBossesConfigFields.getFilename()));
                  }

                  this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsRegisteredMessage());
                  break;
               default:
                  this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsFailedMessage());
            }
         } catch (UncheckedIOException var16) {
            Logger.warn("Failed to save block capture for " + this.customBossesConfigFields.getFilename() + ": " + var16.getMessage());
            this.player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsFailedMessage());
            return;
         }

         activePlayers.remove(this.player.getUniqueId(), this);
      }
   }

   @Generated
   public boolean isRegionalSelection() {
      return this.regionalSelection;
   }

   @Generated
   public void setRegionalSelection(boolean regionalSelection) {
      this.regionalSelection = regionalSelection;
   }

   private record RelativePosition(double x, double y, double z) {
      private static TransitiveBlockCommand.RelativePosition of(Vector vector) {
         return new TransitiveBlockCommand.RelativePosition(vector.getX(), vector.getY(), vector.getZ());
      }
   }

   public static class TemporaryBossBlockCommandEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGH
      )
      public void onPlayerInteractBlockEvent(PlayerInteractEvent event) {
         UUID playerUUID = event.getPlayer().getUniqueId();
         if (TransitiveBlockCommand.activePlayers.containsKey(playerUUID)) {
            if (event.getHand() == EquipmentSlot.HAND) {
               if (event.getClickedBlock() != null) {
                  if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                     if (event.getClickedBlock().getType().isAir()) {
                        return;
                     }

                     if (TransitiveBlockCommand.activePlayers.get(playerUUID).isRegionalSelection()) {
                        TransitiveBlockCommand.activePlayers.get(playerUUID).setCorner(false, event.getClickedBlock().getLocation());
                     } else {
                        TransitiveBlockCommand.activePlayers.get(playerUUID).registerBlock(event.getClickedBlock());
                     }

                     event.setCancelled(true);
                  } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                     if (TransitiveBlockCommand.activePlayers.get(playerUUID).isRegionalSelection()) {
                        TransitiveBlockCommand.activePlayers.get(playerUUID).setCorner(true, event.getClickedBlock().getLocation());
                     } else {
                        TransitiveBlockCommand.activePlayers.get(playerUUID).registerAir(event.getClickedBlock());
                     }

                     event.setCancelled(true);
                  }
               }
            }
         }
      }

      @EventHandler
      public void onPlayerQuit(PlayerQuitEvent event) {
         TransitiveBlockCommand.activePlayers.remove(event.getPlayer().getUniqueId());
      }
   }

   public static enum TransitiveBlockType {
      ON_SPAWN,
      ON_REMOVE;
   }
}
