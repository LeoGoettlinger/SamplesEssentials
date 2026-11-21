package org.skriptlang.skript.bukkit.brewing.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Brewing Will Consume Fuel")
@Description({"Checks if the 'brewing fuel' event will consume fuel.\nPreventing the fuel from being consumed will keep the fuel item and still add to the fuel level of the brewing stand.\n"})
@Example("on brewing fuel:\n\tif the brewing stand will consume the fuel:\n\t\tprevent the brewing stand from consuming the fuel\n")
@Since({"2.13"})
@Events({"Brewing Fuel"})
public class CondBrewingConsume extends Condition implements EventRestrictedSyntax {
   private boolean willConsume;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         SyntaxInfo.builder(CondBrewingConsume.class)
            .addPatterns("[the] brewing stand will consume [the] fuel", "[the] brewing stand (will not|won't) consume [the] fuel")
            .supplier(CondBrewingConsume::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.willConsume = matchedPattern == 0;
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(BrewingStandFuelEvent.class);
   }

   @Override
   public boolean check(Event event) {
      return event instanceof BrewingStandFuelEvent brewingStandFuelEvent ? brewingStandFuelEvent.isConsuming() == this.willConsume : false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the brewing stand will" + (this.willConsume ? "" : " not") + " consume the fuel";
   }
}
