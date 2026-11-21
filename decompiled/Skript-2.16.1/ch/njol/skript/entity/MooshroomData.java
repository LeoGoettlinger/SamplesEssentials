package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MooshroomData extends EntityData<MushroomCow> {
   private static final Patterns<Variant> PATTERNS = new Patterns<>(
      new Object[][]{{"mooshroom", null}, {"red mooshroom", Variant.RED}, {"brown mooshroom", Variant.BROWN}}
   );
   private static final Variant[] VARIANTS = Variant.values();
   @Nullable
   private Variant variant = null;

   public MooshroomData() {
   }

   public MooshroomData(@Nullable Variant variant) {
      this.variant = variant;
      super.codeNameIndex = PATTERNS.getMatchedPattern(variant, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.variant = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends MushroomCow> entityClass, @Nullable MushroomCow mushroomCow) {
      if (mushroomCow != null) {
         this.variant = mushroomCow.getVariant();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.variant, 0).orElse(0);
      }

      return true;
   }

   public void set(MushroomCow mushroomCow) {
      Variant variant = this.variant;
      if (variant == null) {
         variant = CollectionUtils.getRandom(VARIANTS);
      }

      assert variant != null;

      mushroomCow.setVariant(variant);
   }

   protected boolean match(MushroomCow mushroomCow) {
      return this.dataMatch(this.variant, mushroomCow.getVariant());
   }

   @Override
   public Class<? extends MushroomCow> getType() {
      return MushroomCow.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new MooshroomData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof MooshroomData other ? this.variant == other.variant : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof MooshroomData other ? this.dataMatch(this.variant, other.variant) : false;
   }

   static {
      EntityData.register(MooshroomData.class, "mooshroom", MushroomCow.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Variant.class, "MushroomCow.Variant");
   }
}
