package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.LivingEntity;

@Name("Is Playing Dead")
@Description({"Checks to see if an axolotl is playing dead."})
@Example("if last spawned axolotl is playing dead:\n\tmake last spawned axolotl stop playing dead\n")
@Since({"2.11"})
public class CondIsPlayingDead extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity instanceof Axolotl axolotl ? axolotl.isPlayingDead() : false;
   }

   @Override
   protected String getPropertyName() {
      return "playing dead";
   }

   static {
      PropertyCondition.register(CondIsPlayingDead.class, PropertyCondition.PropertyType.BE, "playing dead", "livingentities");
   }
}
