package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Fire Burn Duration")
@Description({"How much time an entity will be burning for."})
@Example.Examples({@Example("send \"You will stop burning in %fire time of player%\""), @Example("send the max burn time of target")})
@Since({"2.7, 2.10 (maximum)"})
public class ExprFireTicks extends SimplePropertyExpression<Entity, Timespan> {
   private boolean max;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.max = parseResult.hasTag("max");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Timespan convert(Entity entity) {
      return new Timespan(Timespan.TimePeriod.TICK, this.max ? entity.getMaxFireTicks() : Math.max(entity.getFireTicks(), 0));
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.max) {
         return null;
      } else {
         return switch (mode) {
            case ADD, SET, RESET, DELETE, REMOVE -> (Class[])CollectionUtils.array(Timespan.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Entity[] entities = this.getExpr().getArray(event);
      int change = delta == null ? 0 : (int)((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
      switch (mode) {
         case SET:
         case RESET:
         case DELETE:
            for (Entity entity : entities) {
               entity.setFireTicks(change);
            }
            break;
         case REMOVE:
            change = -change;
         case ADD:
            for (Entity entity : entities) {
               entity.setFireTicks(entity.getFireTicks() + change);
            }
            break;
         default:
            assert false;
      }
   }

   @Override
   public Class<Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "fire time";
   }

   static {
      register(ExprFireTicks.class, Timespan.class, "[:max[imum]] (burn[ing]|fire) (time|duration)", "entities");
   }
}
