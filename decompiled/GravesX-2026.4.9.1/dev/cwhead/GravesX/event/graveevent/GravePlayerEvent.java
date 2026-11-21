package dev.cwhead.GravesX.event.graveevent;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.interfaces.Addon;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePlayerEvent extends GraveEntityEvent implements Cancellable, Addon {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final InventoryView inventoryView;
   @NotNull
   private final Player player;

   public GravePlayerEvent(
      @NotNull Grave grave,
      @NotNull Player player,
      @Nullable Location location,
      @Nullable BlockData.BlockType blockType,
      @Nullable Block block,
      @Nullable InventoryView inventoryView,
      @Nullable LivingEntity livingEntity,
      @Nullable LivingEntity targetEntity
   ) {
      super(grave, player, location, blockType, block, livingEntity, targetEntity);
      this.player = Objects.requireNonNull(player, "player");
      this.inventoryView = inventoryView;
   }

   public boolean hasInventoryView() {
      return this.inventoryView != null;
   }

   @NotNull
   public InventoryView getInventoryView() {
      return this.inventoryView;
   }

   @Override
   public boolean hasPlayer() {
      return true;
   }

   @NotNull
   @Override
   public Player getPlayer() {
      return this.player;
   }

   @NotNull
   public String getPlayerName() {
      return this.player.getName();
   }

   @NotNull
   public UUID getPlayerUniqueId() {
      return this.player.getUniqueId();
   }

   @NotNull
   public String getPlayerDisplayName() {
      return this.player.getDisplayName();
   }

   @Override
   public boolean isEntityActuallyPlayer() {
      return true;
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
