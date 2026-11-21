package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinecartData extends EntityData<Minecart> {
   private static final MinecartData.MinecartType[] TYPES = MinecartData.MinecartType.values();
   private static final Patterns<MinecartData.MinecartType> PATTERNS = new Patterns<>(
      new Object[][]{
         {"minecart", MinecartData.MinecartType.ANY},
         {"regular minecart", MinecartData.MinecartType.NORMAL},
         {"storage minecart", MinecartData.MinecartType.STORAGE},
         {"powered minecart", MinecartData.MinecartType.POWERED},
         {"hopper minecart", MinecartData.MinecartType.HOPPER},
         {"explosive minecart", MinecartData.MinecartType.EXPLOSIVE},
         {"spawner minecart", MinecartData.MinecartType.SPAWNER},
         {"command minecart", MinecartData.MinecartType.COMMAND}
      }
   );
   private MinecartData.MinecartType type = MinecartData.MinecartType.ANY;

   public MinecartData() {
   }

   public MinecartData(@Nullable MinecartData.MinecartType type) {
      this.type = type != null ? type : MinecartData.MinecartType.ANY;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.type, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.type = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Minecart> entityClass, @Nullable Minecart minecart) {
      for (MinecartData.MinecartType type : TYPES) {
         if (type != MinecartData.MinecartType.ANY) {
            Class<?> typeClass = type.entityClass;
            if (minecart == null ? typeClass.isAssignableFrom(entityClass) : typeClass.isInstance(minecart)) {
               this.type = type;
               break;
            }
         }
      }

      if (this.type == null) {
         this.type = MinecartData.MinecartType.ANY;
      }

      super.codeNameIndex = PATTERNS.getMatchedPattern(this.type, 0).orElse(0);
      return true;
   }

   public void set(Minecart minecart) {
   }

   public boolean match(Minecart minecart) {
      return this.type == MinecartData.MinecartType.ANY ? true : this.type.entityClass.isInstance(minecart);
   }

   @Override
   public Class<? extends Minecart> getType() {
      return this.type.entityClass;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new MinecartData();
   }

   @Override
   protected int hashCode_i() {
      return this.type.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof MinecartData other ? this.type == other.type : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return !(entityData instanceof MinecartData other) ? false : this.type == MinecartData.MinecartType.ANY || this.type == other.type;
   }

   static {
      EntityData.register(MinecartData.class, "minecart", Minecart.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(MinecartData.MinecartType.class, "MinecartType");
   }

   public static enum MinecartType {
      ANY(Minecart.class),
      NORMAL(RideableMinecart.class),
      STORAGE(StorageMinecart.class),
      POWERED(PoweredMinecart.class),
      HOPPER(HopperMinecart.class),
      EXPLOSIVE(ExplosiveMinecart.class),
      SPAWNER(SpawnerMinecart.class),
      COMMAND(CommandMinecart.class);

      private final Class<? extends Minecart> entityClass;

      private MinecartType(Class<? extends Minecart> entityClass) {
         this.entityClass = entityClass;
      }
   }
}
