package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.Version;

public enum MinecraftArgumentType {
   ENTITY("ArgumentEntity", boolean.class, boolean.class),
   ENTITY_PAPER("EntityArgument", boolean.class, boolean.class),
   GAME_PROFILE("ArgumentProfile"),
   COLOR("ArgumentChatFormat"),
   COMPONENT("ArgumentChatComponent"),
   MESSAGE("ArgumentChat"),
   NBT("ArgumentNBTTag"),
   NBT_TAG("ArgumentNBTBase"),
   NBT_PATH("ArgumentNBTKey"),
   SCOREBOARD_OBJECTIVE("ArgumentScoreboardObjective"),
   OBJECTIVE_CRITERIA("ArgumentScoreboardCriteria"),
   SCOREBOARD_SLOT("ArgumentScoreboardSlot"),
   SCORE_HOLDER("ArgumentScoreholder"),
   TEAM("ArgumentScoreboardTeam"),
   OPERATION("ArgumentMathOperation"),
   PARTICLE("ArgumentParticle"),
   ANGLE("ArgumentAngle"),
   ITEM_SLOT("ArgumentInventorySlot"),
   RESOURCE_LOCATION("ArgumentMinecraftKeyRegistered"),
   POTION_EFFECT("ArgumentMobEffect"),
   ENCHANTMENT("ArgumentEnchantment"),
   ENTITY_SUMMON("ArgumentEntitySummon"),
   DIMENSION("ArgumentDimension"),
   TIME("ArgumentTime"),
   UUID("ArgumentUUID"),
   BLOCK_POS("coordinates.ArgumentPosition"),
   COLUMN_POS("coordinates.ArgumentVec2I"),
   VECTOR_3("coordinates.ArgumentVec3"),
   VECTOR_2("coordinates.ArgumentVec2"),
   ROTATION("coordinates.ArgumentRotation"),
   SWIZZLE("coordinates.ArgumentRotationAxis"),
   BLOCK_STATE("blocks.ArgumentTile"),
   BLOCK_PREDICATE("blocks.ArgumentBlockPredicate"),
   ITEM_STACK("item.ArgumentItemStack"),
   ITEM_PREDICATE("item.ArgumentItemPredicate"),
   FUNCTION("item.ArgumentTag"),
   ENTITY_ANCHOR("ArgumentAnchor"),
   INT_RANGE("ArgumentCriterionValue$b"),
   FLOAT_RANGE("ArgumentCriterionValue$a"),
   TEMPLATE_MIRROR("TemplateMirrorArgument"),
   TEMPLATE_ROTATION("TemplateRotationArgument");

   @Nullable
   private ArgumentType<?> argumentType;
   @Nullable
   private Constructor<? extends ArgumentType> argumentConstructor;
   private final Class<?>[] parameters;

   private MinecraftArgumentType(String name, Class<?>... parameters) {
      Class<?> argumentClass = resolveArgumentClass(name);
      this.parameters = parameters;
      if (argumentClass == null) {
         this.argumentType = null;
         this.argumentConstructor = null;
      } else {
         try {
            this.argumentConstructor = argumentClass.asSubclass(ArgumentType.class).getDeclaredConstructor(parameters);
            if (!this.argumentConstructor.isAccessible()) {
               this.argumentConstructor.setAccessible(true);
            }

            if (parameters.length == 0) {
               this.argumentType = (ArgumentType<?>)this.argumentConstructor.newInstance();
            } else {
               this.argumentType = null;
            }
         } catch (Throwable var7) {
            this.argumentType = null;
            this.argumentConstructor = null;
         }
      }
   }

   public boolean isSupported() {
      return this.argumentConstructor != null;
   }

   public boolean requiresParameters() {
      return this.parameters.length != 0;
   }

   @NotNull
   public <T> ArgumentType<T> get() {
      if (this.argumentConstructor == null) {
         throw new IllegalArgumentException("Argument type '" + this.name().toLowerCase() + "' is not available on this version.");
      } else if (this.argumentType != null) {
         return (ArgumentType<T>)this.argumentType;
      } else {
         throw new IllegalArgumentException(
            "This argument type requires "
               + this.parameters.length
               + " parameter(s) of type(s) "
               + Arrays.stream(this.parameters).map(Class::getName).collect(Collectors.joining(", "))
               + ". Use #create() instead."
         );
      }
   }

   @NotNull
   public <T> ArgumentType<T> create(Object... arguments) {
      try {
         if (this.argumentConstructor == null) {
            throw new IllegalArgumentException("Argument type '" + this.name().toLowerCase() + "' is not available on this version.");
         } else {
            return (ArgumentType<T>)(this.argumentType != null && arguments.length == 0
               ? this.argumentType
               : (ArgumentType)this.argumentConstructor.newInstance(arguments));
         }
      } catch (Throwable var3) {
         throw var3;
      }
   }

   @NotNull
   public <T> Optional<ArgumentType<T>> getIfPresent() {
      if (this.argumentConstructor == null) {
         return Optional.empty();
      } else if (this.argumentType != null) {
         return Optional.of((ArgumentType<T>)this.argumentType);
      } else {
         throw new IllegalArgumentException(
            "This argument type requires "
               + this.parameters.length
               + " parameter(s) of type(s) "
               + Arrays.stream(this.parameters).map(Class::getName).collect(Collectors.joining(", "))
               + ". Use #create() instead."
         );
      }
   }

   @NotNull
   public <T> Optional<ArgumentType<T>> createIfPresent(Object... arguments) {
      try {
         if (this.argumentConstructor == null) {
            return Optional.empty();
         } else {
            return this.argumentType != null && arguments.length == 0
               ? Optional.of((ArgumentType<T>)this.argumentType)
               : Optional.of((ArgumentType<T>)this.argumentConstructor.newInstance(arguments));
         }
      } catch (Throwable var3) {
         throw var3;
      }
   }

   @Nullable
   private static Class<?> resolveArgumentClass(String name) {
      try {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_16_5)) {
            try {
               return BukkitVersion.findNmsClass("commands.arguments." + name);
            } catch (Throwable var2) {
               return Class.forName("net.minecraft.commands.arguments." + name);
            }
         } else {
            String stripped;
            if (name.lastIndexOf(46) != -1) {
               stripped = name.substring(name.lastIndexOf(46) + 1);
            } else {
               stripped = name;
            }

            return BukkitVersion.findNmsClass(stripped);
         }
      } catch (Throwable var3) {
         return null;
      }
   }

   public static void ensureSetup() {
   }
}
