package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreeperData extends EntityData<Creeper> {
   private static final Patterns<Kleenean> PATTERNS = new Patterns<>(
      new Object[][]{{"creeper", Kleenean.UNKNOWN}, {"powered creeper", Kleenean.TRUE}, {"unpowered creeper", Kleenean.FALSE}}
   );
   private Kleenean powered = Kleenean.UNKNOWN;

   public CreeperData() {
   }

   public CreeperData(@Nullable Kleenean powered) {
      this.powered = powered != null ? powered : Kleenean.UNKNOWN;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.powered, 0).orElseThrow();
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.powered = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Creeper> entityClass, @Nullable Creeper creeper) {
      if (creeper != null) {
         this.powered = Kleenean.get(creeper.isPowered());
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.powered, 0).orElseThrow();
      }

      return true;
   }

   public void set(Creeper creeper) {
      creeper.setPowered(this.powered.isTrue());
   }

   public boolean match(Creeper creeper) {
      return this.kleeneanMatch(this.powered, creeper.isPowered());
   }

   @Override
   public Class<Creeper> getType() {
      return Creeper.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new CreeperData();
   }

   @Override
   protected int hashCode_i() {
      return this.powered.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof CreeperData other ? this.powered == other.powered : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof CreeperData other ? this.kleeneanMatch(this.powered, other.powered) : false;
   }

   static {
      EntityData.register(CreeperData.class, "creeper", Creeper.class, 0, PATTERNS.getPatterns());
   }
}
