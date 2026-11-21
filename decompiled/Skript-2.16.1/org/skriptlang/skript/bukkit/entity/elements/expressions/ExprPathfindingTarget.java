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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pathfinding Target Entity")
@Description({"The target entity that another entity is pathfinding towards in a pathfinding event."})
@Example("on pathfind:\n\tif the pathfinding target entity is a villager:\n\t    broadcast \"I suspect a zombie is going towards a villager right now..\"\n")
@Since({"2.16"})
@Events({"pathfind"})
public class ExprPathfindingTarget extends SimpleExpression<Entity> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprPathfindingTarget.class, Entity.class
                     )
                     .supplier(ExprPathfindingTarget::new))
                  .priority(SyntaxInfo.SIMPLE))
               .addPattern("[the] path[ ]finding target [entity]"))
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

   protected Entity[] get(Event event) {
      if (event instanceof EntityPathfindEvent pathfindEvent) {
         Entity target = pathfindEvent.getTargetEntity();
         return target != null ? new Entity[]{target} : new Entity[0];
      } else {
         return null;
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the pathfinding target";
   }
}
