package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Leash Holder")
@Description({"The leash holder of an entity."})
@Example("set {_example} to the leash holder of the target mob")
@Since({"2.3, 2.16 (all leashable entities)"})
public class ExprLeashHolder extends SimplePropertyExpression<Entity, Entity> {
   @Nullable
   public Entity convert(Entity entity) {
      if (entity instanceof Leashable leashable) {
         return leashable.isLeashed() ? leashable.getLeashHolder() : null;
      } else {
         return null;
      }
   }

   @Override
   public Class<Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   protected String getPropertyName() {
      return "leash holder";
   }

   static {
      register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "entities");
   }
}
