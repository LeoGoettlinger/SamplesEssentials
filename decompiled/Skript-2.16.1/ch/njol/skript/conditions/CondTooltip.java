package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Has Item Tooltips")
@Description({"Whether the entire or additional tooltip of an item is shown or hidden.", "The 'entire tooltip' is what shows to the player when they hover an item (i.e. name, lore, etc.).", "The 'additional tooltip' hides certain information from certain items (potions, maps, books, fireworks, and banners)."})
@Example.Examples({@Example("send true if entire tooltip of player's tool is shown"), @Example("if additional tooltip of {_item} is hidden:")})
@RequiredPlugins({"Spigot 1.20.5+"})
@Since({"2.9.0"})
public class CondTooltip extends Condition {
   private Expression<ItemType> items;
   private boolean entire;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.items = (Expression<ItemType>)exprs[0];
      this.entire = !parseResult.hasTag("additional");
      this.setNegated(parseResult.hasTag("shown") ^ (matchedPattern == 1 || matchedPattern == 3));
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.entire
         ? this.items.check(event, item -> item.getItemMeta().isHideTooltip(), this.isNegated())
         : this.items.check(event, item -> item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP), this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the "
         + (this.entire ? "entire" : "additional")
         + " tooltip of "
         + this.items.toString(event, debug)
         + " is "
         + (this.isNegated() ? "hidden" : "shown");
   }

   static {
      if (Skript.methodExists(ItemMeta.class, "isHideTooltip")) {
         Skript.registerCondition(
            CondTooltip.class,
            "[the] [entire|:additional] tool[ ]tip[s] of %itemtypes% (is|are) (:shown|hidden)",
            "[the] [entire|:additional] tool[ ]tip[s] of %itemtypes% (isn't|is not|aren't|are not) (:shown|hidden)",
            "%itemtypes%'[s] [entire|:additional] tool[ ]tip[s] (is|are) (:shown|hidden)",
            "%itemtypes%'[s] [entire|:additional] tool[ ]tip[s] (isn't|is not|aren't|are not) (:shown|hidden)"
         );
      }
   }
}
