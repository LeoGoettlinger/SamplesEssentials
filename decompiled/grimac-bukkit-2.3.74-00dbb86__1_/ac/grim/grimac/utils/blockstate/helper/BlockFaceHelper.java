package ac.grim.grimac.utils.blockstate.helper;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import lombok.Generated;

public final class BlockFaceHelper {
   @Contract(
      pure = true
   )
   public static boolean isFaceVertical(@Nullable BlockFace face) {
      return face == BlockFace.UP || face == BlockFace.DOWN;
   }

   @Contract(
      pure = true
   )
   public static boolean isFaceHorizontal(@Nullable BlockFace face) {
      return face == BlockFace.NORTH || face == BlockFace.EAST || face == BlockFace.SOUTH || face == BlockFace.WEST;
   }

   @Generated
   private BlockFaceHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
