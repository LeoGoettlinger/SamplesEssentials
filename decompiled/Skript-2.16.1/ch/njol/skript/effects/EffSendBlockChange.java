package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Send Block Change")
@Description({"Makes a player see a block as something else or as the original."})
@Example.Examples({@Example("make player see block at player as dirt"), @Example("make player see player's target block as campfire[facing=south]"), @Example("make all players see (blocks in radius 5 of location(0, 0, 0)) as bedrock\nmake all players see (blocks in radius 5 of location(0, 0, 0)) as original\n")})
@Since({"2.2-dev37c, 2.5.1 (block data support), 2.12 (as original)"})
public class EffSendBlockChange extends Effect {
   private Expression<Player> players;
   private Expression<Location> locations;
   @Nullable
   private Expression<Object> type;
   private boolean asOriginal;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.locations = (Expression<Location>)exprs[1];
      this.asOriginal = matchedPattern == 1;
      if (!this.asOriginal) {
         this.type = (Expression<Object>)exprs[2];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      if (this.asOriginal) {
         Player[] players = this.players.getArray(event);

         for (Location location : this.locations.getArray(event)) {
            for (Player player : players) {
               player.sendBlockChange(location, location.getBlock().getBlockData());
            }
         }
      } else {
         assert this.type != null;

         Object type = this.type.getSingle(event);
         if (type != null) {
            Player[] players = this.players.getArray(event);
            if (type instanceof ItemType itemType) {
               for (Location location : this.locations.getArray(event)) {
                  for (Player player : players) {
                     itemType.sendBlockChange(player, location);
                  }
               }
            } else if (type instanceof BlockData blockData) {
               for (Location location : this.locations.getArray(event)) {
                  for (Player player : players) {
                     player.sendBlockChange(location, blockData);
                  }
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("make", this.players, "see", this.locations, "as");
      if (this.asOriginal) {
         builder.append("original");
      } else {
         assert this.type != null;

         builder.append(this.type);
      }

      return builder.toString();
   }

   static {
      Skript.registerEffect(
         EffSendBlockChange.class,
         "make %players% see %locations% as %itemtype/blockdata%",
         "make %players% see %locations% as [the|its|their] (original|normal|actual) [block[s]]"
      );
   }
}
