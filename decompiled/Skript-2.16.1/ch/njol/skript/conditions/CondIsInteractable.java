package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Interactable")
@Description({"Checks wether or not a block is interactable."})
@Example("on block break:\n\tif event-block is interactable:\n\t\tcancel event\n\t\tsend \"You cannot break interactable blocks!\"\n")
@Since({"2.5.2"})
public class CondIsInteractable extends PropertyCondition<ItemType> {
   public boolean check(ItemType item) {
      return item.getMaterial().isInteractable();
   }

   @Override
   protected String getPropertyName() {
      return "interactable";
   }

   static {
      register(CondIsInteractable.class, "interactable", "itemtypes");
   }
}
