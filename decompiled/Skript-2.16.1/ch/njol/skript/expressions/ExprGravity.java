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

@Name("Gravity")
@Description({"If entity is affected by gravity or not, i.e. if it has Minecraft 1.10+ NoGravity flag."})
@Example("set gravity of player off")
@Since({"2.2-dev21"})
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {
   public Boolean convert(Entity e) {
      return e.hasGravity();
   }

   @Override
   protected String getPropertyName() {
      return "gravity";
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
         entity.setGravity(delta == null ? true : (Boolean)delta[0]);
      }
   }

   static {
      register(ExprGravity.class, Boolean.class, "gravity", "entities");
   }
}
