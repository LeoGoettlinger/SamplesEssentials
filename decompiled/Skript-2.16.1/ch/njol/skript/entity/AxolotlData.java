package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Axolotl.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxolotlData extends EntityData<Axolotl> {
   private static final Patterns<Variant> PATTERNS = new Patterns<>(
      new Object[][]{
         {"axolotl", null},
         {"lucy axolotl", Variant.LUCY},
         {"wild axolotl", Variant.WILD},
         {"gold axolotl", Variant.GOLD},
         {"cyan axolotl", Variant.CYAN},
         {"blue axolotl", Variant.BLUE}
      }
   );
   private static final Variant[] VARIANTS = Variant.values();
   @Nullable
   private Variant variant = null;

   public AxolotlData() {
   }

   public AxolotlData(@Nullable Variant variant) {
      this.variant = variant;
      super.codeNameIndex = PATTERNS.getMatchedPattern(variant, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.variant = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Axolotl> entityClass, @Nullable Axolotl axolotl) {
      if (axolotl != null) {
         this.variant = axolotl.getVariant();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.variant, 0).orElse(0);
      }

      return true;
   }

   public void set(Axolotl axolotl) {
      Variant variant = this.variant;
      if (variant == null) {
         variant = CollectionUtils.getRandom(VARIANTS);
      }

      assert variant != null;

      axolotl.setVariant(variant);
   }

   protected boolean match(Axolotl axolotl) {
      return this.dataMatch(this.variant, axolotl.getVariant());
   }

   @Override
   public Class<? extends Axolotl> getType() {
      return Axolotl.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new AxolotlData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof AxolotlData other ? this.variant == other.variant : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof AxolotlData other ? this.dataMatch(this.variant, other.variant) : false;
   }

   static {
      EntityData.register(AxolotlData.class, "axolotl", Axolotl.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Variant.class, "Axolotl.Variant");
   }
}
