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
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Profession")
@Description({"Represents the profession of a villager/zombie villager."})
@Example.Examples({@Example("set {_p} to villager profession of event-entity"), @Example("villager profession of event-entity = nitwit profession"), @Example("set villager profession of {_villager} to librarian profession"), @Example("delete villager profession of event-entity")})
@Since({"2.10"})
public class ExprVillagerProfession extends SimplePropertyExpression<LivingEntity, Profession> {
   @Nullable
   public Profession convert(LivingEntity from) {
      if (from instanceof Villager villager) {
         return villager.getProfession();
      } else {
         return from instanceof ZombieVillager zombie ? zombie.getVillagerProfession() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE -> (Class[])CollectionUtils.array(Profession.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Profession profession = delta != null && delta[0] instanceof Profession pro ? pro : Profession.NONE;

      for (LivingEntity livingEntity : this.getExpr().getArray(event)) {
         if (livingEntity instanceof Villager villager) {
            villager.setProfession(profession);
         } else if (livingEntity instanceof ZombieVillager zombie) {
            zombie.setVillagerProfession(profession);
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "villager profession";
   }

   @Override
   public Class<? extends Profession> getReturnType() {
      return Profession.class;
   }

   static {
      register(ExprVillagerProfession.class, Profession.class, "villager profession", "livingentities");
   }
}
