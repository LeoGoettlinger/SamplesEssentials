package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import java.util.Random;
import org.bukkit.entity.Bee;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeeData extends EntityData<Bee> {
   private static final Patterns<BeeData.BeeState> PATTERNS = new Patterns<>(
      new Object[][]{
         {"bee", new BeeData.BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN)},
         {"no nectar bee", new BeeData.BeeState(Kleenean.UNKNOWN, Kleenean.FALSE)},
         {"nectar bee", new BeeData.BeeState(Kleenean.UNKNOWN, Kleenean.TRUE)},
         {"happy bee", new BeeData.BeeState(Kleenean.FALSE, Kleenean.UNKNOWN)},
         {"happy nectar bee", new BeeData.BeeState(Kleenean.FALSE, Kleenean.TRUE)},
         {"happy no nectar bee", new BeeData.BeeState(Kleenean.FALSE, Kleenean.FALSE)},
         {"angry bee", new BeeData.BeeState(Kleenean.TRUE, Kleenean.UNKNOWN)},
         {"angry no nectar bee", new BeeData.BeeState(Kleenean.TRUE, Kleenean.FALSE)},
         {"angry nectar bee", new BeeData.BeeState(Kleenean.TRUE, Kleenean.TRUE)}
      }
   );
   private Kleenean hasNectar = Kleenean.UNKNOWN;
   private Kleenean isAngry = Kleenean.UNKNOWN;

   public BeeData() {
   }

   public BeeData(@Nullable Kleenean isAngry, @Nullable Kleenean hasNectar) {
      this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
      this.hasNectar = hasNectar != null ? hasNectar : Kleenean.UNKNOWN;
      super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeData.BeeState(this.isAngry, this.hasNectar), 0).orElseThrow();
   }

   public BeeData(@Nullable BeeData.BeeState beeState) {
      if (beeState != null) {
         this.isAngry = beeState.angry;
         this.hasNectar = beeState.nectar;
         super.codeNameIndex = PATTERNS.getMatchedPattern(beeState, 0).orElse(0);
      } else {
         this.isAngry = Kleenean.UNKNOWN;
         this.hasNectar = Kleenean.UNKNOWN;
         super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeData.BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN), 0).orElseThrow();
      }
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      BeeData.BeeState state = PATTERNS.getInfo(matchedCodeName);

      assert state != null;

      this.hasNectar = state.nectar;
      this.isAngry = state.angry;
      return true;
   }

   protected boolean init(@Nullable Class<? extends Bee> entityClass, @Nullable Bee bee) {
      if (bee != null) {
         this.isAngry = Kleenean.get(bee.getAnger() > 0);
         this.hasNectar = Kleenean.get(bee.hasNectar());
         super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeData.BeeState(this.isAngry, this.hasNectar), 0).orElse(0);
      }

      return true;
   }

   public void set(Bee bee) {
      int anger = 0;
      if (this.isAngry.isTrue()) {
         anger = new Random().nextInt(400) + 400;
      }

      bee.setAnger(anger);
      bee.setHasNectar(this.hasNectar.isTrue());
   }

   protected boolean match(Bee bee) {
      return !this.kleeneanMatch(this.isAngry, bee.getAnger() > 0) ? false : this.kleeneanMatch(this.hasNectar, bee.hasNectar());
   }

   @Override
   public Class<? extends Bee> getType() {
      return Bee.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new BeeData();
   }

   @Override
   protected int hashCode_i() {
      int prime = 31;
      int result = 1;
      result = prime * result + this.isAngry.hashCode();
      return prime * result + this.hasNectar.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return !(entityData instanceof BeeData other) ? false : this.isAngry == other.isAngry && this.hasNectar == other.hasNectar;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof BeeData other) {
         return !this.kleeneanMatch(this.isAngry, other.isAngry) ? false : this.kleeneanMatch(this.hasNectar, other.hasNectar);
      } else {
         return false;
      }
   }

   static {
      EntityData.register(BeeData.class, "bee", Bee.class, 0, PATTERNS.getPatterns());
   }

   public record BeeState(Kleenean angry, Kleenean nectar) {
   }
}
