package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.Version;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Primitives;

public final class DefaultArgTypeResolvers {
   public static final ArgumentTypeResolver STRING = parameter -> parameter.consumesAllString()
      ? StringArgumentType.greedyString()
      : StringArgumentType.string();
   public static final ArgumentTypeResolver BOOLEAN = parameter -> BoolArgumentType.bool();
   public static final ArgumentTypeResolver NUMBER = parameter -> {
      Range range = parameter.getAnnotation(Range.class);
      Class<?> type = Primitives.wrap(parameter.getType());
      if (type == Integer.class) {
         return range == null ? IntegerArgumentType.integer() : IntegerArgumentType.integer((int)range.min(), (int)range.max());
      } else if (type == Double.class) {
         return range == null ? DoubleArgumentType.doubleArg() : DoubleArgumentType.doubleArg(range.min(), range.max());
      } else if (type == Float.class) {
         return range == null ? FloatArgumentType.floatArg() : FloatArgumentType.floatArg((float)range.min(), (float)range.max());
      } else if (type == Long.class) {
         return range == null ? LongArgumentType.longArg() : LongArgumentType.longArg((long)range.min(), (long)range.max());
      } else if (type == Short.class) {
         return range == null
            ? IntegerArgumentType.integer(-32768, 32767)
            : IntegerArgumentType.integer(Preconditions.coerceAtLeast((int)range.min(), -32768), Preconditions.coerceAtMost((int)range.max(), 32767));
      } else if (type == Byte.class) {
         return range == null
            ? IntegerArgumentType.integer(-128, 127)
            : IntegerArgumentType.integer(Preconditions.coerceAtLeast((int)range.min(), -128), Preconditions.coerceAtMost((int)range.max(), 127));
      } else {
         return null;
      }
   };
   private static final ArgumentType<?> SINGLE_PLAYER = entity(true, true);
   private static final ArgumentType<?> MULTI_PLAYER = entity(false, true);
   private static final ArgumentType<?> MULTI_ENTITY = entity(false, false);
   public static final ArgumentTypeResolver PLAYER = parameter -> SINGLE_PLAYER;
   public static final ArgumentTypeResolver ENTITY_SELECTOR = parameter -> {
      Class<? extends Entity> type = BukkitHandler.getSelectedEntity(parameter.getFullType());
      return Player.class.isAssignableFrom(type) ? MULTI_PLAYER : MULTI_ENTITY;
   };

   private DefaultArgTypeResolvers() {
   }

   private static ArgumentType<?> entity(boolean single, boolean playerOnly) {
      return Version.getServerVersion() != Version.v1_21_8_PAPER && !Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)
         ? MinecraftArgumentType.ENTITY.create(single, playerOnly)
         : MinecraftArgumentType.ENTITY_PAPER.create(single, playerOnly);
   }
}
