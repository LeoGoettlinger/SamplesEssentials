package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoatData extends EntityData<Goat> {
   private static final Patterns<Kleenean> PATTERNS = new Patterns<>(
      new Object[][]{{"goat", Kleenean.UNKNOWN}, {"screaming goat", Kleenean.TRUE}, {"quiet goat", Kleenean.FALSE}}
   );
   private Kleenean screaming = Kleenean.UNKNOWN;

   public GoatData() {
   }

   public GoatData(@Nullable Kleenean screaming) {
      this.screaming = screaming != null ? screaming : Kleenean.UNKNOWN;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.screaming, 0).orElseThrow();
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.screaming = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Goat> entityClass, @Nullable Goat goat) {
      if (goat != null) {
         this.screaming = Kleenean.get(goat.isScreaming());
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.screaming, 0).orElseThrow();
      }

      return true;
   }

   public void set(Goat goat) {
      goat.setScreaming(this.screaming.isTrue());
   }

   protected boolean match(Goat goat) {
      return this.kleeneanMatch(this.screaming, goat.isScreaming());
   }

   @Override
   public Class<? extends Goat> getType() {
      return Goat.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new GoatData();
   }

   @Override
   protected int hashCode_i() {
      return this.screaming.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof GoatData other ? this.screaming == other.screaming : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof GoatData other ? this.kleeneanMatch(this.screaming, other.screaming) : false;
   }

   static {
      EntityData.register(GoatData.class, "goat", Goat.class, 0, PATTERNS.getPatterns());
   }
}
