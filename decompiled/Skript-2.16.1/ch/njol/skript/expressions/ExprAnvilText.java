package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Anvil Text Input")
@Description({"An expression to get the name to be applied to an item in an anvil inventory."})
@Example("on inventory click:\n\ttype of event-inventory is anvil inventory\n\tif the anvil text input of the event-inventory is \"FREE OP\":\n\t\tban player\n")
@Since({"2.7"})
public class ExprAnvilText extends SimplePropertyExpression<Inventory, String> {
   @Nullable
   public String convert(Inventory inv) {
      return !(inv instanceof AnvilInventory) ? null : ((AnvilInventory)inv).getRenameText();
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String getPropertyName() {
      return "anvil text input";
   }

   static {
      register(ExprAnvilText.class, String.class, "anvil [inventory] (rename|text) input", "inventories");
   }
}
