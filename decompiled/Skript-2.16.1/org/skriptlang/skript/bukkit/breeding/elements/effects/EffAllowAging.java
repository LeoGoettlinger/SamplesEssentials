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

@Name("Allow Aging")
@Description({"Sets whether or not living entities will be able to age."})
@Example("on spawn of animal:\n\tallow aging of entity\n")
@Since({"2.10"})
public class EffAllowAging extends Effect {
   private boolean unlock;
   private Expression<LivingEntity> entities;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffAllowAging.class)
            .addPatterns(
               "lock age of %livingentities%",
               "prevent aging of %livingentities%",
               "prevent %livingentities% from aging",
               "unlock age of %livingentities%",
               "allow aging of %livingentities%",
               "allow %livingentities% to age"
            )
            .supplier(EffAllowAging::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)expressions[0];
      this.unlock = matchedPattern > 2;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity livingEntity : this.entities.getArray(event)) {
         if (livingEntity instanceof Breedable breedable) {
            breedable.setAgeLock(!this.unlock);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.unlock ? "allow" : "prevent") + " aging of " + this.entities.toString(event, debug);
   }
}
