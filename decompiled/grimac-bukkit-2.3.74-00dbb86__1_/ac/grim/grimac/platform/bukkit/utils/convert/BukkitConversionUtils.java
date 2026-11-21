package ac.grim.grimac.platform.bukkit.utils.convert;

import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.platform.bukkit.world.BukkitPlatformWorld;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import org.bukkit.Location;
import org.bukkit.permissions.PermissionDefault;

public class BukkitConversionUtils {
   @Contract("null -> null; !null -> new")
   public static Location toBukkitLocation(ac.grim.grimac.utils.math.Location location) {
      return location == null
         ? null
         : new Location(
            ((BukkitPlatformWorld)location.getWorld()).bukkitWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
         );
   }

   @Contract(
      value = "null -> null; !null -> !null",
      pure = true
   )
   @Nullable
   public static PermissionDefault toBukkitPermissionDefault(@Nullable PermissionDefaultValue permissionDefaultValue) {
      if (permissionDefaultValue == null) {
         return null;
      } else {
         return switch (permissionDefaultValue) {
            case TRUE -> PermissionDefault.TRUE;
            case FALSE -> PermissionDefault.FALSE;
            case OP -> PermissionDefault.OP;
            case NOT_OP -> PermissionDefault.NOT_OP;
         };
      }
   }

   public static BlockFace fromBukkitFace(org.bukkit.block.BlockFace face) {
      return switch (face) {
         case NORTH -> BlockFace.NORTH;
         case SOUTH -> BlockFace.SOUTH;
         case WEST -> BlockFace.WEST;
         case EAST -> BlockFace.EAST;
         case UP -> BlockFace.UP;
         case DOWN -> BlockFace.DOWN;
         default -> BlockFace.OTHER;
      };
   }
}
