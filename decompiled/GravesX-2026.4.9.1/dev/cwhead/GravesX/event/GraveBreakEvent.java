package dev.cwhead.GravesX.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveBreakEvent extends GravePlayerEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveBreakEvent(@NotNull Block block, @NotNull Player player, @NotNull Grave grave) {
      this(block, player, grave, null);
   }

   public GraveBreakEvent(@NotNull Block block, @NotNull Player player, @NotNull Grave grave, @Nullable BlockData.BlockType blockType) {
      super(
         Objects.requireNonNull(grave, "grave"),
         Objects.requireNonNull(player, "player"),
         safeLocation(Objects.requireNonNull(block, "block"), grave),
         resolveBlockType(blockType, block),
         block,
         null,
         player,
         null
      );
   }

   @NotNull
   private static Location safeLocation(@NotNull Block block, @NotNull Grave grave) {
      return block.getLocation();
   }

   @Nullable
   private static BlockData.BlockType resolveBlockType(@Nullable BlockData.BlockType given, @NotNull Block block) {
      if (given != null) {
         return given;
      } else {
         Material mat = block.getType();

         try {
            return BlockData.BlockType.valueOf(mat.name());
         } catch (IllegalArgumentException var4) {
            return null;
         }
      }
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
