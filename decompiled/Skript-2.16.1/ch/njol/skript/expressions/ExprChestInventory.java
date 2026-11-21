package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Custom Chest Inventory")
@Description({"Returns a chest inventory with the given amount of rows and the name.", "Use the <a href=#EffOpenInventory>open inventory</a> effect to open it."})
@Example.Examples({@Example("open chest inventory with 1 row named \"test\" to player"), @Example("set {_inventory} to a chest inventory with 1 row\nset slot 4 of {_inventory} to a diamond named \"example\"\nopen {_inventory} to player\n"), @Example("open chest inventory named \"<#00ff00>hex coloured title!\" with 6 rows to player")})
@Since({"2.2-dev34, 2.8.0 (chat format)"})
public class ExprChestInventory extends SimpleExpression<Inventory> {
   private static final Component DEFAULT_CHEST_TITLE = InventoryType.CHEST.defaultTitle();
   private static final int DEFAULT_CHEST_ROWS = InventoryType.CHEST.getDefaultSize() / 9;
   @Nullable
   private Expression<Number> rows;
   @Nullable
   private Expression<Component> name;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.name = (Expression<Component>)exprs[matchedPattern];
      this.rows = (Expression<Number>)exprs[matchedPattern ^ 1];
      return true;
   }

   protected Inventory[] get(Event event) {
      Component name = this.name != null ? this.name.getOptionalSingle(event).orElse(DEFAULT_CHEST_TITLE) : DEFAULT_CHEST_TITLE;
      Number rows = (Number)(this.rows != null ? this.rows.getOptionalSingle(event).orElse(DEFAULT_CHEST_ROWS) : DEFAULT_CHEST_ROWS);
      int size = rows.intValue() * 9;
      if (size % 9 != 0) {
         size = 27;
      }

      if (size < 0) {
         size = 0;
      }

      if (size > 54) {
         size = 54;
      }

      return CollectionUtils.array(Bukkit.createInventory(null, size, name));
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Inventory> getReturnType() {
      return Inventory.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .append("a chest inventory")
         .appendIf(this.name != null, "named", this.name)
         .appendIf(this.rows != null, "with", this.rows, "rows")
         .toString();
   }

   static {
      Skript.registerExpression(
         ExprChestInventory.class,
         Inventory.class,
         ExpressionType.COMBINED,
         "[a] [new] chest inventory (named|with name) %textcomponent% [with %-number% row[s]]",
         "[a] [new] chest inventory with %number% row[s] [(named|with name) %-textcomponent%]"
      );
   }
}
