package org.skriptlang.skript.bukkit.breeding.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Breeding Family")
@Description({"Represents family members within a breeding event."})
@Example("on breeding:\n\tsend \"When a %breeding mother% and %breeding father% love each other very much, they make a %bred offspring%\" to breeder\n")
@Since({"2.10"})
public class ExprBreedingFamily extends SimpleExpression<LivingEntity> {
   private int pattern;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprBreedingFamily.class, LivingEntity.class
                  )
                  .addPatterns(new String[]{"[the] breeding mother", "[the] breeding father", "[the] [bred] (offspring|child)", "[the] breeder"}))
               .supplier(ExprBreedingFamily::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(EntityBreedEvent.class)) {
         Skript.error("The 'breeding family' expression can only be used in an breed event.");
         return false;
      } else {
         this.pattern = matchedPattern;
         return true;
      }
   }

   @Nullable
   protected LivingEntity[] get(Event event) {
      if (event instanceof EntityBreedEvent breedEvent) {
         return switch (this.pattern) {
            case 0 -> new LivingEntity[]{breedEvent.getMother()};
            case 1 -> new LivingEntity[]{breedEvent.getFather()};
            case 2 -> new LivingEntity[]{breedEvent.getEntity()};
            case 3 -> new LivingEntity[]{breedEvent.getBreeder()};
            default -> new LivingEntity[0];
         };
      } else {
         return new LivingEntity[0];
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends LivingEntity> getReturnType() {
      return LivingEntity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "breeding family";
   }
}
