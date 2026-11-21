package org.skriptlang.skript.bukkit.entity.interactions.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Interaction Responsive")
@Description({"Makes an interaction either responsive or unresponsive. This determines whether clicking the entity will cause the clicker's arm to swing.\nInteractions default to unresponsive.\n"})
@Example("make last spawned interaction responsive")
@Since({"2.14"})
public class EffMakeResponsive extends Effect {
   private Expression<Entity> interactions;
   private boolean negated;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffMakeResponsive.class)
            .addPatterns("make %entities% responsive", "make %entities% (not |un)responsive")
            .supplier(EffMakeResponsive::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.interactions = (Expression<Entity>)expressions[0];
      this.negated = matchedPattern == 1;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Entity entity : this.interactions.getArray(event)) {
         if (entity instanceof Interaction interaction) {
            interaction.setResponsive(!this.negated);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append("make", this.interactions).appendIf(this.negated, "not").append("responsive").toString();
   }
}
