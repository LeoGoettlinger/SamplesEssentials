package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Type")
@Description({"Represents the type of a villager/zombie villager. This usually represents the biome the villager is from."})
@Example.Examples({@Example("set {_type} to villager type of {_villager}"), @Example("villager type of {_villager} = plains"), @Example("set villager type of event-entity to plains")})
@Since({"2.10"})
public class ExprVillagerType extends SimplePropertyExpression<LivingEntity, Type> {
   @Nullable
   public Type convert(LivingEntity from) {
      if (from instanceof Villager villager) {
         return villager.getVillagerType();
      } else {
         return from instanceof ZombieVillager zombie ? zombie.getVillagerType() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Type.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Type type = delta != null && delta[0] instanceof Type t ? t : null;
      if (type != null) {
         for (LivingEntity livingEntity : this.getExpr().getArray(event)) {
            if (livingEntity instanceof Villager villager) {
               villager.setVillagerType(type);
            } else if (livingEntity instanceof ZombieVillager zombie) {
               zombie.setVillagerType(type);
            }
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "villager type";
   }

   @Override
   public Class<? extends Type> getReturnType() {
      return Type.class;
   }

   static {
      register(ExprVillagerType.class, Type.class, "villager type", "livingentities");
   }
}
