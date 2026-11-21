package org.skriptlang.skript.bukkit.fishing.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Hook")
@Description({"The <a href='#entity'>fishing hook</a> in a fishing event."})
@Example("on fish line cast:\n\twait a second\n\tteleport player to fishing hook\n")
@Events({"Fishing"})
@Since({"2.10"})
public class ExprFishingHook extends EventValueExpression<Entity> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprFishingHook.class, Entity.class, new String[]{"fish[ing] (hook|bobber)"})
               .supplier(ExprFishingHook::new))
            .build()
      );
   }

   public ExprFishingHook() {
      super(FishHook.class);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the fishing hook";
   }
}
