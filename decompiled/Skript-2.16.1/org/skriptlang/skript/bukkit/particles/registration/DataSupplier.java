package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DataSupplier<D> {
   @Nullable
   D getData(@Nullable Event var1, Expression<?>[] var2, SkriptParser.ParseResult var3);

   @Nullable
   static Material getMaterialData(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      return expressions[0].getSingle(event) instanceof ItemType itemType ? itemType.getMaterial() : null;
   }

   @Nullable
   static BlockFace getBlockFaceData(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      return expressions[0].getSingle(event) instanceof Direction direction ? Direction.toNearestBlockFace(direction.getDirection()) : null;
   }

   @Nullable
   static BlockFace getCartesianBlockFaceData(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      return expressions[0].getSingle(event) instanceof Direction direction ? Direction.toNearestCartesianBlockFace(direction.getDirection()) : null;
   }

   @Nullable
   static BlockData getBlockData(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      Object input = expressions[0].getSingle(event);
      if (input instanceof ItemType itemType) {
         return itemType.getMaterial().createBlockData();
      } else {
         return input instanceof BlockData blockData ? blockData : null;
      }
   }

   @Nullable
   static Color getColorData(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      return expressions[0].getSingle(event) instanceof ch.njol.skript.util.Color color ? color.asBukkitColor() : null;
   }

   static boolean isOminous(Event event, Expression<?>[] expressions, @NotNull SkriptParser.ParseResult parseResult) {
      return parseResult.hasTag("ominous");
   }

   static int getNumberDefault10(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      if (expressions[0] == null) {
         return 10;
      } else {
         return expressions[0].getSingle(event) instanceof Number number ? number.intValue() : 10;
      }
   }

   static int getNumberDefault1(Event event, @NotNull Expression<?>[] expressions, SkriptParser.ParseResult parseResult) {
      if (expressions[0] == null) {
         return 1;
      } else {
         return expressions[0].getSingle(event) instanceof Number number ? number.intValue() : 1;
      }
   }
}
