package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Apply Potion Effect")
@Description({"Applies a potion effect to an entity."})
@Example.Examples({@Example("apply swiftness 2 to the player"), @Example("command /strengthboost:\n\ttrigger:\n\t\tapply strength 10 to the player for 5 minutes\n"), @Example("apply the potion effects of the player's tool to the player")})
@Since({"2.0", "2.14 (syntax rework)"})
public class EffApplyPotionEffect extends Effect {
   private Expression<SkriptPotionEffect> potions;
   private Expression<LivingEntity> entities;
   @Nullable
   private Expression<Timespan> duration;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffApplyPotionEffect.class)
            .supplier(EffApplyPotionEffect::new)
            .addPatterns(
               "(apply|grant) %skriptpotioneffects% to %livingentities% [for %-timespan%]",
               "(affect|afflict) %livingentities% with %skriptpotioneffects% [for %-timespan%]"
            )
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      boolean first = matchedPattern == 0;
      this.potions = (Expression<SkriptPotionEffect>)exprs[first ? 0 : 1];
      this.entities = (Expression<LivingEntity>)exprs[first ? 1 : 0];
      this.duration = (Expression<Timespan>)exprs[2];
      return true;
   }

   @Override
   protected void execute(Event event) {
      SkriptPotionEffect[] potions = this.potions.getArray(event);
      if (this.duration != null) {
         Timespan timespan = this.duration.getSingle(event);
         if (timespan != null) {
            if (timespan.isInfinite()) {
               for (int i = 0; i < potions.length; i++) {
                  potions[i] = potions[i].clone().infinite(true);
               }
            } else {
               int ticks = (int)timespan.getAs(Timespan.TimePeriod.TICK);

               for (int i = 0; i < potions.length; i++) {
                  potions[i] = potions[i].clone().duration(ticks);
               }
            }
         }
      }

      for (SkriptPotionEffect skriptPotionEffect : potions) {
         PotionEffect potionEffect = skriptPotionEffect.asBukkitPotionEffect();

         for (LivingEntity livingEntity : this.entities.getArray(event)) {
            livingEntity.addPotionEffect(potionEffect);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "apply " + this.potions.toString(event, debug) + " to " + this.entities.toString(event, debug);
   }
}
