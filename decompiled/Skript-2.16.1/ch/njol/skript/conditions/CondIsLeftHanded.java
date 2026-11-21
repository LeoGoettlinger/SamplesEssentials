package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.MainHand;

@Name("Left Handed")
@Description({"Checks if living entities or players are left or right-handed. Armor stands are neither right nor left-handed."})
@Example("on damage of player:\n\tif victim is left handed:\n\t\tcancel event\n")
@Since({"2.8.0"})
public class CondIsLeftHanded extends PropertyCondition<LivingEntity> {
   private static final boolean CAN_USE_ENTITIES = Skript.methodExists(Mob.class, "isLeftHanded");
   private MainHand hand;

   @Override
   public boolean init(Expression[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.hand = parseResult.hasTag("left") ? MainHand.LEFT : MainHand.RIGHT;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   public boolean check(LivingEntity livingEntity) {
      if (CAN_USE_ENTITIES && livingEntity instanceof Mob mob) {
         return mob.isLeftHanded() == (this.hand == MainHand.LEFT);
      } else {
         return livingEntity instanceof HumanEntity humanEntity ? humanEntity.getMainHand() == this.hand : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return (this.hand == MainHand.LEFT ? "left" : "right") + " handed";
   }

   static {
      if (CAN_USE_ENTITIES) {
         register(CondIsLeftHanded.class, "(:left|right)( |-)handed", "livingentities");
      } else {
         register(CondIsLeftHanded.class, "(:left|right)( |-)handed", "players");
      }
   }
}
