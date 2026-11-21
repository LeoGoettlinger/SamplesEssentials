package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StriderData extends EntityData<Strider> {
   private static final Patterns<Kleenean> PATTERNS = new Patterns<>(
      new Object[][]{{"strider", Kleenean.UNKNOWN}, {"warm strider", Kleenean.FALSE}, {"shivering strider", Kleenean.TRUE}}
   );
   private Kleenean shivering = Kleenean.UNKNOWN;

   public StriderData() {
   }

   public StriderData(@Nullable Kleenean shivering) {
      this.shivering = shivering != null ? shivering : Kleenean.UNKNOWN;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.shivering, 0).orElseThrow();
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.shivering = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider strider) {
      if (strider != null) {
         this.shivering = Kleenean.get(strider.isShivering());
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.shivering, 0).orElseThrow();
      }

      return true;
   }

   public void set(Strider entity) {
      entity.setShivering(this.shivering.isTrue());
   }

   protected boolean match(Strider strider) {
      return this.kleeneanMatch(this.shivering, strider.isShivering());
   }

   @Override
   public Class<? extends Strider> getType() {
      return Strider.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new StriderData();
   }

   @Override
   protected int hashCode_i() {
      return this.shivering.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof StriderData other ? this.shivering == other.shivering : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof StriderData other ? this.kleeneanMatch(this.shivering, other.shivering) : false;
   }

   static {
      register(StriderData.class, "strider", Strider.class, 0, PATTERNS.getPatterns());
   }
}
