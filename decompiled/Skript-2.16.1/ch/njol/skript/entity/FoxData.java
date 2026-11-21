package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoxData extends EntityData<Fox> {
   private static final Patterns<Type> PATTERNS = new Patterns<>(new Object[][]{{"fox", null}, {"red fox", Type.RED}, {"snow fox", Type.SNOW}});
   private static final Type[] TYPES = Type.values();
   @Nullable
   private Type type = null;

   public FoxData() {
   }

   public FoxData(@Nullable Type type) {
      this.type = type;
      super.codeNameIndex = PATTERNS.getMatchedPattern(type, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.type = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Fox> entityClass, @Nullable Fox fox) {
      if (fox != null) {
         this.type = fox.getFoxType();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.type, 0).orElse(0);
      }

      return true;
   }

   public void set(Fox fox) {
      Type type = this.type;
      if (type == null) {
         type = CollectionUtils.getRandom(TYPES);
      }

      assert type != null;

      fox.setFoxType(type);
   }

   protected boolean match(Fox fox) {
      return this.dataMatch(this.type, fox.getFoxType());
   }

   @Override
   public Class<? extends Fox> getType() {
      return Fox.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new FoxData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.type);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof FoxData other ? this.type == other.type : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof FoxData other ? this.dataMatch(this.type, other.type) : false;
   }

   static {
      EntityData.register(FoxData.class, "fox", Fox.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Type.class, "Fox.Type");
   }
}
