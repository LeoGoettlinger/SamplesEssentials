package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Salmon.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SalmonData extends EntityData<Salmon> {
   private static final boolean SUPPORT_SALMON_VARIANTS = Skript.classExists("org.bukkit.entity.Salmon$Variant");
   private static final Object[] VARIANTS;
   private static final Patterns<Object> PATTERNS;
   @Nullable
   private Object variant = null;

   public SalmonData() {
   }

   public SalmonData(@Nullable Object variant) {
      this.variant = variant;
      super.codeNameIndex = PATTERNS.getMatchedPattern(variant, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.variant = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Salmon> entityClass, @Nullable Salmon salmon) {
      if (salmon != null && SUPPORT_SALMON_VARIANTS) {
         this.variant = salmon.getVariant();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.variant, 0).orElse(0);
      }

      return true;
   }

   public void set(Salmon entity) {
      if (SUPPORT_SALMON_VARIANTS) {
         Variant variant = (Variant)this.variant;
         if (variant == null) {
            variant = CollectionUtils.getRandom(VARIANTS);
         }

         assert variant != null;

         entity.setVariant(variant);
      }
   }

   protected boolean match(Salmon entity) {
      return this.variant == null || this.variant == entity.getVariant();
   }

   @Override
   public Class<? extends Salmon> getType() {
      return Salmon.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new SalmonData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof SalmonData other ? this.variant == other.variant : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof SalmonData other ? this.dataMatch(this.variant, other.variant) : false;
   }

   static {
      if (SUPPORT_SALMON_VARIANTS) {
         VARIANTS = Variant.values();
         PATTERNS = new Patterns<>(
            new Object[][]{
               {"salmon", null}, {"any salmon", null}, {"small salmon", Variant.SMALL}, {"medium salmon", Variant.MEDIUM}, {"large salmon", Variant.LARGE}
            }
         );
         Variables.yggdrasil.registerSingleClass(Variant.class, "Salmon.Variant");
      } else {
         VARIANTS = null;
         PATTERNS = new Patterns<>(new Object[][]{{"salmon", null}});
      }

      EntityData.register(SalmonData.class, "salmon", Salmon.class, 0, PATTERNS.getPatterns());
   }
}
