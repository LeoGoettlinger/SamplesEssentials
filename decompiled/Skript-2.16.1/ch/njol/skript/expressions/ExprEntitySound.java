package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Entity Sound")
@Description({"Gets the sound that a given entity will make in a specific scenario."})
@Example.Examples({@Example("play sound (hurt sound of player) at player"), @Example("set {_sounds::*} to death sounds of (all mobs in radius 10 of player)")})
@Since({"2.10"})
@RequiredPlugins({"Spigot 1.19.2+"})
public class ExprEntitySound extends SimpleExpression<String> {
   private static final Patterns<ExprEntitySound.SoundType> patterns = new Patterns<>(
      new Object[][]{
         {"[the] (damage|hurt) sound[s] of %livingentities%", ExprEntitySound.SoundType.DAMAGE},
         {"%livingentities%'[s] (damage|hurt) sound[s]", ExprEntitySound.SoundType.DAMAGE},
         {"[the] death sound[s] of %livingentities%", ExprEntitySound.SoundType.DEATH},
         {"%livingentities%'[s] death sound[s]", ExprEntitySound.SoundType.DEATH},
         {"[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%", ExprEntitySound.SoundType.FALL},
         {"%livingentities%'[s] [high:(tall|high)|low:(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%]", ExprEntitySound.SoundType.FALL},
         {"[the] swim[ming] sound[s] of %livingentities%", ExprEntitySound.SoundType.SWIM},
         {"%livingentities%'[s] swim[ming] sound[s]", ExprEntitySound.SoundType.SWIM},
         {"[the] [fast:(fast|speedy)] splash sound[s] of %livingentities%", ExprEntitySound.SoundType.SPLASH},
         {"%livingentities%'[s] [fast:(fast|speedy)] splash sound[s]", ExprEntitySound.SoundType.SPLASH},
         {"[the] eat[ing] sound[s] of %livingentities% [(with|using|[while] eating [a]) %-itemtype%]", ExprEntitySound.SoundType.EAT},
         {"%livingentities%'[s] eat[ing] sound[s]", ExprEntitySound.SoundType.EAT},
         {"[the] drink[ing] sound[s] of %livingentities% [(with|using|[while] drinking [a]) %-itemtype%]", ExprEntitySound.SoundType.DRINK},
         {"%livingentities%'[s] drink[ing] sound[s]", ExprEntitySound.SoundType.DRINK},
         {"[the] ambient sound[s] of %livingentities%", ExprEntitySound.SoundType.AMBIENT},
         {"%livingentities%'[s] ambient sound[s]", ExprEntitySound.SoundType.AMBIENT}
      }
   );
   private boolean bigOrSpeedy;
   private ExprEntitySound.SoundType soundType;
   private Expression<Number> height;
   private Expression<LivingEntity> entities;
   private Expression<ItemType> item;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.soundType = patterns.getInfo(matchedPattern);
      this.bigOrSpeedy = parseResult.hasTag("high") || parseResult.hasTag("fast");
      if (this.soundType == ExprEntitySound.SoundType.FALL) {
         this.height = (Expression<Number>)exprs[0];
      }

      if (this.soundType == ExprEntitySound.SoundType.EAT || this.soundType == ExprEntitySound.SoundType.DRINK) {
         this.item = (Expression<ItemType>)exprs[1];
      }

      this.entities = (Expression<LivingEntity>)(this.soundType == ExprEntitySound.SoundType.FALL ? exprs[1] : exprs[0]);
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      int height = this.height == null ? -1 : this.height.getOptionalSingle(event).orElse(-1).intValue();
      ItemStack defaultItem = new ItemStack(this.soundType == ExprEntitySound.SoundType.EAT ? Material.COOKED_BEEF : Material.POTION);
      ItemStack item = this.item == null ? defaultItem : this.item.getOptionalSingle(event).map(ItemType::getRandom).orElse(defaultItem);
      return this.entities
         .stream(event)
         .map(entity -> this.soundType.getSound(entity, height, item, this.bigOrSpeedy))
         .filter(Objects::nonNull)
         .distinct()
         .map(sound -> SoundUtils.getKey(sound).getKey())
         .toArray(String[]::new);
   }

   @Override
   public boolean isSingle() {
      return this.entities.isSingle();
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String sound = "unknown";
      switch (this.soundType) {
         case DAMAGE:
         case DEATH:
         case SWIM:
         case AMBIENT:
            sound = this.soundType.name().toLowerCase();
            break;
         case FALL:
            if (this.height == null) {
               sound = this.bigOrSpeedy ? "high fall damage" : "normal fall damage";
            } else {
               sound = "fall damage from a height of " + this.height.toString(event, debug);
            }
            break;
         case SPLASH:
            sound = this.bigOrSpeedy ? "speedy splash" : "splash";
            break;
         case EAT:
         case DRINK:
            String action = this.soundType == ExprEntitySound.SoundType.EAT ? "eating" : "drinking";
            if (this.item == null) {
               sound = action;
            } else {
               sound = action + " " + this.item.toString(event, debug);
            }
      }

      return sound + " sound of " + this.entities.toString(event, debug);
   }

   static {
      if (Skript.methodExists(LivingEntity.class, "getDeathSound")) {
         Skript.registerExpression(ExprEntitySound.class, String.class, ExpressionType.COMBINED, patterns.getPatterns());
      }
   }

   public static enum SoundType {
      DAMAGE {
         @Nullable
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity.getHurtSound();
         }
      },
      DEATH {
         @Nullable
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity.getDeathSound();
         }
      },
      FALL {
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            if (height != -1) {
               return entity.getFallDamageSound(height);
            } else {
               return bigOrSpeedy ? entity.getFallDamageSoundBig() : entity.getFallDamageSoundSmall();
            }
         }
      },
      SWIM {
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity.getSwimSound();
         }
      },
      SPLASH {
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return bigOrSpeedy ? entity.getSwimHighSpeedSplashSound() : entity.getSwimSplashSound();
         }
      },
      EAT {
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity.getEatingSound(item);
         }
      },
      DRINK {
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity.getDrinkingSound(item);
         }
      },
      AMBIENT {
         @Nullable
         @Override
         public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
            return entity instanceof Mob mob ? mob.getAmbientSound() : null;
         }
      };

      @Nullable
      public abstract Sound getSound(LivingEntity var1, int var2, ItemStack var3, boolean var4);
   }
}
