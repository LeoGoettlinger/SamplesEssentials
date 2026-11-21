package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Warden Most Angered At")
@Description({"The entity a warden is most angry at.", "A warden can be angry towards multiple entities with different anger levels."})
@Example("if the most angered entity of last spawned warden is not player:\n\tset the most angered entity of last spawned warden to player\n")
@Since({"2.11"})
public class ExprWardenAngryAt extends SimplePropertyExpression<LivingEntity, LivingEntity> {
   @Nullable
   public LivingEntity convert(LivingEntity livingEntity) {
      return livingEntity instanceof Warden warden ? warden.getEntityAngryAt() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(LivingEntity.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      LivingEntity target = (LivingEntity)delta[0];

      for (LivingEntity livingEntity : this.getExpr().getArray(event)) {
         if (livingEntity instanceof Warden warden) {
            warden.setAnger(target, 150);
         }
      }
   }

   @Override
   public Class<LivingEntity> getReturnType() {
      return LivingEntity.class;
   }

   @Override
   protected String getPropertyName() {
      return "most angered entity";
   }

   static {
      register(ExprWardenAngryAt.class, LivingEntity.class, "most angered entity", "livingentities");
   }
}
