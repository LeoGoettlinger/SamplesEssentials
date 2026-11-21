package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fall Distance")
@Description({"The distance an entity has fallen for."})
@Example.Examples({@Example("set all entities' fall distance to 10"), @Example("on damage:\n\tsend \"%victim's fall distance%\" to victim\n")})
@Since({"2.5"})
public class ExprFallDistance extends SimplePropertyExpression<Entity, Number> {
   @Nullable
   public Number convert(Entity entity) {
      return entity.getFallDistance();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.RESET && mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.DELETE
         ? CollectionUtils.array(Number.class)
         : null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         Entity[] entities = this.getExpr().getArray(e);
         if (entities.length < 1) {
            return;
         }

         Float number = ((Number)delta[0]).floatValue();

         for (Entity entity : entities) {
            Float fallDistance = entity.getFallDistance();
            switch (mode) {
               case ADD:
                  entity.setFallDistance(fallDistance + number);
                  break;
               case SET:
                  entity.setFallDistance(number);
                  break;
               case REMOVE:
                  entity.setFallDistance(fallDistance - number);
                  break;
               default:
                  assert false;
            }
         }
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return "fall distance";
   }

   static {
      register(ExprFallDistance.class, Number.class, "fall[en] (distance|height)", "entities");
   }
}
