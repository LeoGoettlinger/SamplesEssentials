package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;

@Name("Is Online")
@Description({"Checks whether a player is online. The 'connected' pattern will return false once this player leaves the server, even if they rejoin. Be aware that using the 'connected' pattern with a variable will not have this special behavior. Use the direct event-player or other non-variable expression for best results."})
@Example.Examples({@Example("player is online"), @Example("player-argument is offline"), @Example("while player is connected:\n\twait 60 seconds\n\tsend \"hello!\" to player\n"), @Example("# The following will act like `{_player} is online`.\n# Using variables with `is connected` will not behave the same as with non-variables.\nwhile {_player} is connected:\n\tbroadcast \"online!\"\n\twait 1 tick\n")})
@Since({"1.4"})
public class CondIsOnline extends PropertyCondition<OfflinePlayer> {
   private boolean connected;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends OfflinePlayer>)exprs[0]);
      this.setNegated(matchedPattern == 1 ^ parseResult.hasTag("offline"));
      this.connected = parseResult.hasTag("connected");
      return true;
   }

   public boolean check(OfflinePlayer op) {
      return this.connected ? op.isConnected() : op.isOnline();
   }

   @Override
   protected String getPropertyName() {
      return this.connected ? "connected" : "online";
   }

   static {
      if (Skript.methodExists(OfflinePlayer.class, "isConnected")) {
         register(CondIsOnline.class, "(online|:offline|:connected)", "offlineplayers");
      } else {
         register(CondIsOnline.class, "(online|:offline)", "offlineplayers");
      }
   }
}
