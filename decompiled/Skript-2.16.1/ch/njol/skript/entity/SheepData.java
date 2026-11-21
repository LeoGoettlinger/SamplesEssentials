package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import org.bukkit.entity.Sheep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SheepData extends EntityData<Sheep> {
   private static final Patterns<Kleenean> PATTERNS = new Patterns<>(
      new Object[][]{{"sheep", Kleenean.UNKNOWN}, {"sheared sheep", Kleenean.TRUE}, {"unsheared sheep", Kleenean.FALSE}}
   );
   @Nullable
   private Color[] colors = null;
   private Kleenean sheared = Kleenean.UNKNOWN;
   @Nullable
   private Adjective[] adjectives = null;

   public SheepData() {
   }

   public SheepData(@Nullable Kleenean sheared, @Nullable Color[] colors) {
      this.sheared = sheared != null ? sheared : Kleenean.UNKNOWN;
      this.colors = colors;
      super.codeNameIndex = PATTERNS.getMatchedPattern(this.sheared, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.sheared = PATTERNS.getInfo(matchedCodeName);
      if (exprs[0] != null) {
         this.colors = (Color[])exprs[0].getAll();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Sheep> entityClass, @Nullable Sheep sheep) {
      if (sheep != null) {
         this.sheared = Kleenean.get(sheep.isSheared());
         this.colors = CollectionUtils.array(SkriptColor.fromDyeColor(sheep.getColor()));
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.sheared, 0).orElse(0);
      }

      return true;
   }

   public void set(Sheep sheep) {
      if (this.colors != null) {
         Color color = CollectionUtils.getRandom(this.colors);

         assert color != null;

         sheep.setColor(color.asDyeColor());
      }

      sheep.setSheared(this.sheared.isTrue());
   }

   public boolean match(Sheep sheep) {
      return !this.kleeneanMatch(this.sheared, sheep.isSheared())
         ? false
         : this.colors == null || SimpleExpression.check(this.colors, c -> sheep.getColor() == c.asDyeColor(), false, false);
   }

   @Override
   public Class<Sheep> getType() {
      return Sheep.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new SheepData();
   }

   @Override
   protected int hashCode_i() {
      int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode((Object[])this.colors);
      return prime * result + this.sheared.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      if (entityData instanceof SheepData other) {
         return !Arrays.equals((Object[])this.colors, (Object[])other.colors) ? false : this.sheared == other.sheared;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof SheepData other) {
         return !this.kleeneanMatch(this.sheared, other.sheared) ? false : this.colors == null || CollectionUtils.isSubset(this.colors, other.colors);
      } else {
         return false;
      }
   }

   @Override
   public String toString(int flags) {
      Color[] colors = this.colors;
      if (colors == null) {
         return super.toString(flags);
      } else {
         Adjective[] adjectives = this.adjectives;
         if (adjectives == null) {
            this.adjectives = adjectives = new Adjective[colors.length];

            for (int i = 0; i < colors.length; i++) {
               if (colors[i] instanceof SkriptColor skriptColor) {
                  adjectives[i] = skriptColor.getAdjective();
               }
            }
         }

         Noun name = this.getName();
         Adjective age = this.getAgeAdjective();
         return name.getArticleWithSpace(flags)
            + (age == null ? "" : age.toString(name.getGender(), flags) + " ")
            + Adjective.toString(adjectives, name.getGender(), flags, false)
            + " "
            + name.toString(flags & -7);
      }
   }

   static {
      EntityData.register(SheepData.class, "sheep", Sheep.class, 0, PATTERNS.getPatterns());
   }
}
