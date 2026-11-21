package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePistonExtendEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final Block pistonBlock;
   @NotNull
   private final BlockFace direction;
   @NotNull
   private final List<Block> movedBlocks;

   public GravePistonExtendEvent(
      @NotNull Grave grave, @Nullable Location location, @NotNull Block pistonBlock, @NotNull BlockFace direction, @NotNull List<Block> movedBlocks
   ) {
      super(Objects.requireNonNull(grave, "grave"), location, null, null);
      this.pistonBlock = Objects.requireNonNull(pistonBlock, "pistonBlock");
      this.direction = Objects.requireNonNull(direction, "direction");
      this.movedBlocks = List.copyOf(Objects.requireNonNull(movedBlocks, "movedBlocks"));
   }

   @NotNull
   public Block getPistonBlock() {
      return this.pistonBlock;
   }

   @NotNull
   public BlockFace getDirection() {
      return this.direction;
   }

   @NotNull
   public List<Block> getMovedBlocks() {
      return this.movedBlocks;
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
