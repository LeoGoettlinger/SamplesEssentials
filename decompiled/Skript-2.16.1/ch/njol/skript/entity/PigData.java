package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import java.util.Objects;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Pig.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigData extends EntityData<Pig> {
   private static final boolean VARIANTS_ENABLED;
   private static final Object[] VARIANTS;
   private static final Patterns<Kleenean> PATTERNS = new Patterns<>(
      new Object[][]{{"pig", Kleenean.UNKNOWN}, {"saddled pig", Kleenean.TRUE}, {"unsaddled pig", Kleenean.FALSE}}
   );
   private Kleenean saddled = Kleenean.UNKNOWN;
   @Nullable
   private Object variant = null;

   public PigData() {
   }

   public PigData(@Nullable Kleenean saddled, @Nullable Object variant) {
      this.saddled = saddled != null ? saddled : Kleenean.UNKNOWN;
      this.variant = variant;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.saddled, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.saddled = PATTERNS.getInfo(matchedCodeName);
      if (VARIANTS_ENABLED && exprs[0] != null) {
         this.variant = exprs[0].getSingle();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
      if (pig != null) {
         this.saddled = Kleenean.get(pig.hasSaddle());
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.saddled, 0).orElse(0);
         if (VARIANTS_ENABLED) {
            this.variant = pig.getVariant();
         }
      }

      return true;
   }

   public void set(Pig pig) {
      pig.setSaddle(this.saddled.isTrue());
      if (VARIANTS_ENABLED) {
         Object finalVariant = this.variant != null ? this.variant : CollectionUtils.getRandom(VARIANTS);

         assert finalVariant != null;

         pig.setVariant((Variant)finalVariant);
      }
   }

   protected boolean match(Pig pig) {
      return !this.kleeneanMatch(this.saddled, pig.hasSaddle()) ? false : this.variant == null || this.variant == pig.getVariant();
   }

   @Override
   public Class<? extends Pig> getType() {
      return Pig.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new PigData();
   }

   @Override
   protected int hashCode_i() {
      return this.saddled.ordinal() + Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      if (entityData instanceof PigData other) {
         return this.saddled != other.saddled ? false : this.variant == other.variant;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof PigData other) {
         return !this.kleeneanMatch(this.saddled, other.saddled) ? false : this.variant == null || this.variant == other.variant;
      } else {
         return false;
      }
   }

   static {
      ClassInfo<?> pigVariantClassInfo = BukkitUtils.getRegistryClassInfo("org.bukkit.entity.Pig$Variant", "PIG_VARIANT", "pigvariant", "pig variants");
      if (pigVariantClassInfo == null) {
         pigVariantClassInfo = new ClassInfo<>(PigData.PigVariantDummy.class, "pigvariant");
      }

      Classes.registerClass(
         pigVariantClassInfo.user("pig ?variants?")
            .name("Pig Variant")
            .description("Represents the variant of a pig entity.", "NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
            .since("2.12")
            .requiredPlugins("Minecraft 1.21.5+")
            .documentationId("PigVariant")
      );
      register(PigData.class, "pig", Pig.class, 0, PATTERNS.getPatterns());
      if (Skript.classExists("org.bukkit.entity.Pig$Variant")) {
         VARIANTS_ENABLED = true;
         VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Variant.class).getSupplier().get(), Variant.class);
      } else {
         VARIANTS_ENABLED = false;
         VARIANTS = null;
      }
   }

   public static class PigVariantDummy {
   }
}
