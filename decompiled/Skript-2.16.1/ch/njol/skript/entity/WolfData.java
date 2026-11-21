package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import java.util.Objects;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WolfData extends EntityData<Wolf> {
   private static final Patterns<WolfData.WolfStates> PATTERNS = new Patterns<>(
      new Object[][]{
         {"wolf", new WolfData.WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN)},
         {"wild wolf", new WolfData.WolfStates(Kleenean.UNKNOWN, Kleenean.FALSE)},
         {"tamed wolf", new WolfData.WolfStates(Kleenean.UNKNOWN, Kleenean.TRUE)},
         {"angry wolf", new WolfData.WolfStates(Kleenean.TRUE, Kleenean.UNKNOWN)},
         {"peaceful wolf", new WolfData.WolfStates(Kleenean.FALSE, Kleenean.UNKNOWN)}
      }
   );
   private static final boolean VARIANTS_ENABLED;
   private static final Object[] VARIANTS;
   @Nullable
   private Object variant = null;
   @Nullable
   private DyeColor collarColor = null;
   private Kleenean isAngry = Kleenean.UNKNOWN;
   private Kleenean isTamed = Kleenean.UNKNOWN;

   public WolfData() {
   }

   public WolfData(@Nullable Kleenean isAngry, @Nullable Kleenean isTamed) {
      this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
      this.isTamed = isTamed != null ? isTamed : Kleenean.UNKNOWN;
      super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfData.WolfStates(this.isAngry, this.isTamed), 0).orElseThrow();
   }

   public WolfData(@Nullable WolfData.WolfStates wolfState) {
      if (wolfState != null) {
         this.isAngry = wolfState.angry;
         this.isTamed = wolfState.tamed;
         super.codeNameIndex = PATTERNS.getMatchedPattern(wolfState, 0).orElse(0);
      } else {
         this.isAngry = Kleenean.UNKNOWN;
         this.isTamed = Kleenean.UNKNOWN;
         super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfData.WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN), 0).orElse(0);
      }
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      WolfData.WolfStates state = PATTERNS.getInfo(matchedCodeName);

      assert state != null;

      this.isAngry = state.angry;
      this.isTamed = state.tamed;
      if (exprs[0] != null && VARIANTS_ENABLED) {
         this.variant = exprs[0].getSingle();
      }

      if (exprs[1] != null) {
         this.collarColor = ((Color)exprs[1].getSingle()).asDyeColor();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Wolf> entityClass, @Nullable Wolf wolf) {
      if (wolf != null) {
         this.isAngry = Kleenean.get(wolf.isAngry());
         this.isTamed = Kleenean.get(wolf.isTamed());
         this.collarColor = wolf.getCollarColor();
         if (VARIANTS_ENABLED) {
            this.variant = wolf.getVariant();
         }

         super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfData.WolfStates(this.isAngry, this.isTamed), 0).orElse(0);
      }

      return true;
   }

   public void set(Wolf wolf) {
      wolf.setAngry(this.isAngry.isTrue());
      wolf.setTamed(this.isTamed.isTrue());
      if (this.collarColor != null) {
         wolf.setCollarColor(this.collarColor);
      }

      if (VARIANTS_ENABLED) {
         Object variantSet = this.variant != null ? this.variant : CollectionUtils.getRandom(VARIANTS);

         assert variantSet != null;

         wolf.setVariant((Variant)variantSet);
      }
   }

   public boolean match(Wolf wolf) {
      if (!this.kleeneanMatch(this.isAngry, wolf.isAngry())) {
         return false;
      } else if (!this.kleeneanMatch(this.isTamed, wolf.isTamed())) {
         return false;
      } else {
         return !this.dataMatch(this.collarColor, wolf.getCollarColor()) ? false : this.variant == null || this.variant == wolf.getVariant();
      }
   }

   @Override
   public Class<Wolf> getType() {
      return Wolf.class;
   }

   @NotNull
   @Override
   public EntityData<Wolf> getSuperType() {
      return new WolfData();
   }

   @Override
   protected int hashCode_i() {
      int prime = 31;
      int result = 1;
      result = prime * result + this.isAngry.hashCode();
      result = prime * result + this.isTamed.hashCode();
      result = prime * result + Objects.hashCode(this.collarColor);
      if (VARIANTS_ENABLED) {
         result = prime * result + Objects.hashCode(this.variant);
      }

      return result;
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      if (entityData instanceof WolfData other) {
         if (this.isAngry != other.isAngry) {
            return false;
         } else if (this.isTamed != other.isTamed) {
            return false;
         } else {
            return this.collarColor != other.collarColor ? false : this.variant == other.variant;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof WolfData other) {
         if (!this.kleeneanMatch(this.isAngry, other.isAngry)) {
            return false;
         } else if (!this.kleeneanMatch(this.isTamed, other.isTamed)) {
            return false;
         } else {
            return !this.dataMatch(this.collarColor, other.collarColor) ? false : this.dataMatch(this.variant, other.variant);
         }
      } else {
         return false;
      }
   }

   static {
      ClassInfo<?> wolfVariantClassInfo = BukkitUtils.getRegistryClassInfo("org.bukkit.entity.Wolf$Variant", "WOLF_VARIANT", "wolfvariant", "wolf variants");
      if (wolfVariantClassInfo == null) {
         wolfVariantClassInfo = new ClassInfo<>(WolfData.WolfVariantDummy.class, "wolfvariant");
      }

      Classes.registerClass(
         wolfVariantClassInfo.user("wolf ?variants?")
            .name("Wolf Variant")
            .description("Represents the variant of a wolf entity.", "NOTE: Minecraft namespaces are supported, ex: 'minecraft:ashen'.")
            .since("2.10")
            .requiredPlugins("Minecraft 1.21+")
            .documentationId("WolfVariant")
      );
      EntityData.register(WolfData.class, "wolf", Wolf.class, 0, PATTERNS.getPatterns());
      if (Skript.classExists("org.bukkit.entity.Wolf$Variant")) {
         VARIANTS_ENABLED = true;
         VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Variant.class).getSupplier().get(), Variant.class);
      } else {
         VARIANTS_ENABLED = false;
         VARIANTS = null;
      }
   }

   public record WolfStates(Kleenean angry, Kleenean tamed) {
   }

   public static class WolfVariantDummy {
   }
}
