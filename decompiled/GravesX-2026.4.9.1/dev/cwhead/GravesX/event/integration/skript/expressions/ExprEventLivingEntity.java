package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEventLivingEntity extends SimpleExpression<LivingEntity> {
   @Nullable
   protected LivingEntity[] get(Event e) {
      return e instanceof GravePlayerEvent ? new LivingEntity[]{((GravePlayerEvent)e).getLivingEntity()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends LivingEntity> getReturnType() {
      return LivingEntity.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event living entity";
   }
}
