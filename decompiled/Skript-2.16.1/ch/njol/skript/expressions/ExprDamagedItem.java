package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Damaged Item")
@Description({"Directly damages an item. In MC versions 1.12.2 and lower, this can be used to apply data values to items/blocks"})
@Example.Examples({@Example("give player diamond sword with damage value 100"), @Example("set player's tool to diamond hoe damaged by 250"), @Example("give player diamond sword with damage 700 named \"BROKEN SWORD\""), @Example("set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"")})
@Since({"2.4"})
public class ExprDamagedItem extends PropertyExpression<ItemType, ItemType> {
   private Expression<Number> damage;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends ItemType>)exprs[0]);
      this.damage = (Expression<Number>)exprs[1];
      return true;
   }

   protected ItemType[] get(Event e, ItemType[] source) {
      Number damage = this.damage.getSingle(e);
      return damage == null ? source : this.get((ItemType[])source.clone(), item -> {
         item.iterator().forEachRemaining(i -> i.setDurability(damage.intValue()));
         return (ItemType)item;
      });
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.getExpr().toString(e, debug) + " with damage value " + this.damage.toString(e, debug);
   }

   static {
      Skript.registerExpression(
         ExprDamagedItem.class, ItemType.class, ExpressionType.COMBINED, "%itemtype% with (damage|data) [value] %number%", "%itemtype% damaged by %number%"
      );
   }
}
