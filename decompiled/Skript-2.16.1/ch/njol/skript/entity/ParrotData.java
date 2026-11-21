package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParrotData extends EntityData<Parrot> {
   private static final Variant[] VARIANTS = Variant.values();
   private static final Patterns<Variant> PATTERNS = new Patterns<>(
      new Object[][]{
         {"parrot", null},
         {"red parrot", Variant.RED},
         {"blue parrot", Variant.BLUE},
         {"green parrot", Variant.GREEN},
         {"cyan parrot", Variant.CYAN},
         {"gray parrot", Variant.GRAY}
      }
   );
   @Nullable
   private Variant variant = null;

   public ParrotData() {
   }

   public ParrotData(@Nullable Variant variant) {
      this.variant = variant;
      super.codeNameIndex = PATTERNS.getMatchedPattern(variant, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.variant = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Parrot> entityClass, @Nullable Parrot parrot) {
      if (parrot != null) {
         this.variant = parrot.getVariant();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.variant, 0).orElse(0);
      }

      return true;
   }

   public void set(Parrot parrot) {
      Variant variant = this.variant;
      if (variant == null) {
         variant = CollectionUtils.getRandom(VARIANTS);
      }

      assert variant != null;

      parrot.setVariant(variant);
   }

   protected boolean match(Parrot parrot) {
      return this.dataMatch(this.variant, parrot.getVariant());
   }

   @Override
   public Class<? extends Parrot> getType() {
      return Parrot.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new ParrotData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof ParrotData other ? this.variant == other.variant : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof ParrotData other ? this.dataMatch(this.variant, other.variant) : false;
   }

   static {
      EntityData.register(ParrotData.class, "parrot", Parrot.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Variant.class, "Parrot.Variant");
   }
}
