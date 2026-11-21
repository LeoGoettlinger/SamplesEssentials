package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Open Book")
@Description({"Opens a written book to a player."})
@Example("open book player's tool to player")
@RequiredPlugins({"Minecraft 1.14.2+"})
@Since({"2.5.1"})
public class EffOpenBook extends Effect {
   private Expression<ItemType> book;
   private Expression<Player> players;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.book = (Expression<ItemType>)exprs[0];
      this.players = (Expression<Player>)exprs[1];
      return true;
   }

   @Override
   protected void execute(Event e) {
      ItemType itemType = this.book.getSingle(e);
      if (itemType != null) {
         ItemStack itemStack = itemType.getRandom();
         if (itemStack != null && itemStack.getType() == Material.WRITTEN_BOOK) {
            for (Player player : this.players.getArray(e)) {
               player.openBook(itemStack);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "open book " + this.book.toString(e, debug) + " to " + this.players.toString(e, debug);
   }

   static {
      if (Skript.methodExists(Player.class, "openBook", ItemStack.class)) {
         Skript.registerEffect(EffOpenBook.class, "(open|show) book %itemtype% (to|for) %players%");
      }
   }
}
