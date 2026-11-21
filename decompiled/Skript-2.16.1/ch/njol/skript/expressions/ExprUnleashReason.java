package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.jetbrains.annotations.Nullable;

@Name("Unleash Reason")
@Description({"The unleash reason in an unleash event."})
@Example("if the unleash reason is distance:\n\tbroadcast \"The leash was snapped in half.\"\n")
@Events({"Leash / Unleash"})
@Since({"2.10"})
public class ExprUnleashReason extends EventValueExpression<UnleashReason> implements EventRestrictedSyntax {
   public ExprUnleashReason() {
      super(UnleashReason.class);
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EntityUnleashEvent.class);
   }

   protected UnleashReason[] get(Event event) {
      return event instanceof EntityUnleashEvent unleashEvent ? new UnleashReason[]{unleashEvent.getReason()} : new UnleashReason[0];
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends UnleashReason> getReturnType() {
      return UnleashReason.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the unleash reason";
   }

   static {
      Skript.registerExpression(ExprUnleashReason.class, UnleashReason.class, ExpressionType.SIMPLE, "[the] unleash[ing] reason");
   }
}
