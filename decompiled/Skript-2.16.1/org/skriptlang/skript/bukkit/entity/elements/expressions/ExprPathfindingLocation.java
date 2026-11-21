package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pathfinding Target Location")
@Description({"The location that the entity is pathfinding towards."})
@Example("on pathfind:\n\tif the pathfinding target location is within radius 5 of {mylocation}:\n\t    broadcast \"A mob tried to pathfind near a forbidden location!\"\n\t    cancel event\n")
@Since({"2.16"})
@Events({"pathfind"})
public class ExprPathfindingLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprPathfindingLocation.class, Location.class
                     )
                     .supplier(ExprPathfindingLocation::new))
                  .priority(SyntaxInfo.SIMPLE))
               .addPattern("[the] path[ ]finding target location"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{EntityPathfindEvent.class};
   }

   protected Location[] get(Event event) {
      if (event instanceof EntityPathfindEvent pathfindEvent) {
         Location location = pathfindEvent.getLoc();
         return new Location[]{location};
      } else {
         return null;
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Location> getReturnType() {
      return Location.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the pathfinding location";
   }
}
