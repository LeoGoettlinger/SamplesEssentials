package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Ender Chest")
@Description({"The ender chest of a player."})
@Example("open the player's ender chest to the player")
@Since({"2.0"})
public class ExprEnderChest extends SimplePropertyExpression<Player, Inventory> {
   @Nullable
   public Inventory convert(Player p) {
      return p.getEnderChest();
   }

   @Override
   public Class<? extends Inventory> getReturnType() {
      return Inventory.class;
   }

   @Override
   protected String getPropertyName() {
      return "ender chest";
   }

   static {
      register(ExprEnderChest.class, Inventory.class, "ender[ ]chest[s]", "players");
   }
}
