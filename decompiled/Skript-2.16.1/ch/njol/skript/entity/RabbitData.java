package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RabbitData extends EntityData<Rabbit> {
   private static final Type[] TYPES = Type.values();
   private static final Patterns<Type> PATTERNS = new Patterns<>(
      new Object[][]{
         {"rabbit", null},
         {"white rabbit", Type.WHITE},
         {"black rabbit", Type.BLACK},
         {"black and white rabbit", Type.BLACK_AND_WHITE},
         {"brown rabbit", Type.BROWN},
         {"gold rabbit", Type.GOLD},
         {"salt and pepper rabbit", Type.SALT_AND_PEPPER},
         {"killer rabbit", Type.THE_KILLER_BUNNY}
      }
   );
   @Nullable
   private Type type = null;

   public RabbitData() {
   }

   public RabbitData(@Nullable Type type) {
      this.type = type;
      super.codeNameIndex = PATTERNS.getMatchedPattern(type, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.type = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Rabbit> entityClass, @Nullable Rabbit rabbit) {
      if (rabbit != null) {
         this.type = rabbit.getRabbitType();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.type, 0).orElse(0);
      }

      return true;
   }

   public void set(Rabbit rabbit) {
      Type type = this.type;
      if (type == null) {
         type = CollectionUtils.getRandom(TYPES);
      }

      assert type != null;

      rabbit.setRabbitType(type);
   }

   protected boolean match(Rabbit rabbit) {
      return this.dataMatch(this.type, rabbit.getRabbitType());
   }

   @Override
   public Class<? extends Rabbit> getType() {
      return Rabbit.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new RabbitData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.type);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof RabbitData other ? this.type == other.type : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof RabbitData other ? this.dataMatch(this.type, other.type) : false;
   }

   static {
      EntityData.register(RabbitData.class, "rabbit", Rabbit.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Type.class, "Rabbit.Type");
   }
}
