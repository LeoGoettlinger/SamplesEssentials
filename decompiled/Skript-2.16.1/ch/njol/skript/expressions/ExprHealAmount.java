package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.Nullable;

@Name("Heal Amount")
@Description({"The amount of health healed in a <a href='/#heal'>heal event</a>."})
@Example("on player healing:\n\tincrease the heal amount by 2\n\tremove 0.5 from the healing amount\n")
@Events({"heal"})
@Since({"2.5.1"})
public class ExprHealAmount extends SimpleExpression<Double> implements EventRestrictedSyntax {
   private Kleenean delay;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.delay = isDelayed;
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EntityRegainHealthEvent.class);
   }

   @Nullable
   protected Double[] get(Event event) {
      return !(event instanceof EntityRegainHealthEvent) ? null : new Double[]{((EntityRegainHealthEvent)event).getAmount()};
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.delay != Kleenean.FALSE) {
         Skript.error("The heal amount cannot be changed after the event has already passed");
         return null;
      } else {
         return mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.RESET ? CollectionUtils.array(Number.class) : null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof EntityRegainHealthEvent healthEvent) {
         double value = delta == null ? 0.0 : ((Number)delta[0]).doubleValue();
         switch (mode) {
            case SET:
            case DELETE:
               healthEvent.setAmount(value);
               break;
            case ADD:
               healthEvent.setAmount(healthEvent.getAmount() + value);
               break;
            case REMOVE:
               healthEvent.setAmount(healthEvent.getAmount() - value);
         }
      }
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, EntityRegainHealthEvent.class);
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Double> getReturnType() {
      return Double.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "heal amount";
   }

   static {
      Skript.registerExpression(ExprHealAmount.class, Double.class, ExpressionType.SIMPLE, "[the] heal[ing] amount");
   }
}
