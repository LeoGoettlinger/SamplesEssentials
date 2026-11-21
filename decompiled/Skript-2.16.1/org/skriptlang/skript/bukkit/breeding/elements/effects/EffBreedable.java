package org.skriptlang.skript.bukkit.breeding.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Breedable")
@Description({"Sets whether or not entities will be able to breed. Only works on animals."})
@Example("on spawn of animal:\n\tmake entity unbreedable\n")
@Since({"2.10"})
public class EffBreedable extends Effect {
   private boolean sterilize;
   private Expression<LivingEntity> entities;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffBreedable.class)
            .addPatterns(
               "make %livingentities% breedable",
               "unsterilize %livingentities%",
               "make %livingentities% (not |non(-| )|un)breedable",
               "sterilize %livingentities%"
            )
            .supplier(EffBreedable::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.sterilize = matchedPattern > 1;
      this.entities = (Expression<LivingEntity>)expressions[0];
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Breedable breedable) {
            breedable.setBreed(!this.sterilize);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "make " + this.entities.toString(event, debug) + (this.sterilize ? " non-" : " ") + "breedable";
   }
}
