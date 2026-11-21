package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Glowing")
@Description({"Indicates if targeted entity is glowing (new 1.9 effect) or not. Glowing entities can be seen through walls."})
@Example("set glowing of player to true")
@Since({"2.2-dev18"})
public class ExprGlowing extends SimplePropertyExpression<Entity, Boolean> {
   public Boolean convert(Entity e) {
      return e.isGlowing();
   }

   @Override
   protected String getPropertyName() {
      return "glowing";
   }

   @Override
   public Class<Boolean> getReturnType() {
      return Boolean.class;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : new Class[]{Boolean.class};
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      for (Entity entity : this.getExpr().getArray(e)) {
         entity.setGlowing(delta == null ? false : (Boolean)delta[0]);
      }
   }

   static {
      register(ExprGlowing.class, Boolean.class, "glowing", "entities");
   }
}
