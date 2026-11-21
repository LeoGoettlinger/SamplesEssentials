package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TropicalFishData extends EntityData<TropicalFish> {
   private static final Patterns<Pattern> PATTERNS = new Patterns<>(
      new Object[][]{
         {"tropical fish", null},
         {"kob", Pattern.KOB},
         {"sunstreak", Pattern.SUNSTREAK},
         {"snooper", Pattern.SNOOPER},
         {"dasher", Pattern.DASHER},
         {"brinely", Pattern.BRINELY},
         {"spotty", Pattern.SPOTTY},
         {"flopper", Pattern.FLOPPER},
         {"stripey", Pattern.STRIPEY},
         {"glitter", Pattern.GLITTER},
         {"blockfish", Pattern.BLOCKFISH},
         {"betty", Pattern.BETTY},
         {"clayfish", Pattern.CLAYFISH}
      }
   );
   private static final Pattern[] FISH_PATTERNS = Pattern.values();
   @Nullable
   private DyeColor bodyColor = null;
   @Nullable
   private DyeColor patternColor = null;
   @Nullable
   private Pattern fishPattern = null;

   public TropicalFishData() {
   }

   public TropicalFishData(@Nullable Pattern fishPattern, @Nullable DyeColor bodyColor, @Nullable DyeColor patternColor) {
      this.fishPattern = fishPattern;
      this.bodyColor = bodyColor;
      this.patternColor = patternColor;
      super.codeNameIndex = PATTERNS.getMatchedPattern(fishPattern, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.fishPattern = PATTERNS.getInfo(matchedCodeName);
      if (exprs.length == 0) {
         return true;
      } else {
         if (matchedPattern == 0) {
            if (exprs[0] != null) {
               this.bodyColor = ((Color)exprs[0].getSingle()).asDyeColor();
               if (exprs[1] != null) {
                  this.patternColor = ((Color)exprs[1].getSingle()).asDyeColor();
               }
            }
         } else if (exprs[0] != null) {
            this.bodyColor = ((Color)exprs[0].getSingle()).asDyeColor();
            this.patternColor = this.bodyColor;
         }

         return true;
      }
   }

   protected boolean init(@Nullable Class<? extends TropicalFish> entityClass, @Nullable TropicalFish tropicalFish) {
      if (tropicalFish != null) {
         this.bodyColor = tropicalFish.getBodyColor();
         this.patternColor = tropicalFish.getPatternColor();
         this.fishPattern = tropicalFish.getPattern();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.fishPattern, 0).orElse(0);
      }

      return true;
   }

   public void set(TropicalFish tropicalFish) {
      Pattern fishPattern = this.fishPattern;
      if (fishPattern == null) {
         fishPattern = CollectionUtils.getRandom(FISH_PATTERNS);
      }

      assert fishPattern != null;

      tropicalFish.setPattern(fishPattern);
      if (this.bodyColor != null) {
         tropicalFish.setBodyColor(this.bodyColor);
      }

      if (this.patternColor != null) {
         tropicalFish.setPatternColor(this.patternColor);
      }
   }

   protected boolean match(TropicalFish tropicalFish) {
      if (!this.dataMatch(this.bodyColor, tropicalFish.getBodyColor())) {
         return false;
      } else {
         return !this.dataMatch(this.patternColor, tropicalFish.getPatternColor()) ? false : this.dataMatch(this.fishPattern, tropicalFish.getPattern());
      }
   }

   @Override
   public Class<? extends TropicalFish> getType() {
      return TropicalFish.class;
   }

   @NotNull
   @Override
   public EntityData<TropicalFish> getSuperType() {
      return new TropicalFishData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hash(this.fishPattern, this.bodyColor, this.patternColor);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return !(entityData instanceof TropicalFishData other)
         ? false
         : this.fishPattern == other.fishPattern && this.bodyColor == other.bodyColor && this.patternColor == other.patternColor;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof TropicalFishData other) {
         if (!this.dataMatch(this.bodyColor, other.bodyColor)) {
            return false;
         } else {
            return !this.dataMatch(this.patternColor, other.patternColor) ? false : this.dataMatch(this.fishPattern, other.fishPattern);
         }
      } else {
         return false;
      }
   }

   static {
      register(TropicalFishData.class, "tropical fish", TropicalFish.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Pattern.class, "TropicalFish.Pattern");
   }
}
