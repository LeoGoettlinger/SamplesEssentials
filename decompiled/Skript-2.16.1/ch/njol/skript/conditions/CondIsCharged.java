package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;

@Name("Is Charged")
@Description({"Checks if a creeper, wither, or wither skull is charged (powered)."})
@Example("if the last spawned creeper is charged:\n\tbroadcast \"A charged creeper is at %location of last spawned creeper%\"\n")
@Since({"2.5, 2.10 (withers, wither skulls)"})
public class CondIsCharged extends PropertyCondition<Entity> {
   public boolean check(Entity entity) {
      if (entity instanceof Creeper creeper) {
         return creeper.isPowered();
      } else if (entity instanceof WitherSkull witherSkull) {
         return witherSkull.isCharged();
      } else {
         return entity instanceof Wither wither ? wither.isCharged() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "charged";
   }

   static {
      register(CondIsCharged.class, "(charged|powered)", "entities");
   }
}
