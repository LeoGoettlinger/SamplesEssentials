package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Whitelisted")
@Description({"Whether or not the server or a player is whitelisted, or the server is whitelist enforced."})
@Example.Examples({@Example("if the player is whitelisted:"), @Example("if the server is whitelisted:"), @Example("if the server whitelist is enforced:")})
@Since({"2.5.2, 2.9.0 (enforce, offline players)"})
@RequiredPlugins({"MC 1.17+ (enforce)"})
public class CondIsWhitelisted extends Condition {
   @Nullable
   private Expression<OfflinePlayer> players;
   private boolean isServer;
   private boolean isEnforce;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setNegated(parseResult.hasTag("not"));
      this.isServer = matchedPattern != 1;
      this.isEnforce = matchedPattern == 2;
      if (matchedPattern == 1) {
         this.players = (Expression<OfflinePlayer>)exprs[0];
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.isServer
         ? (this.isEnforce ? Bukkit.isWhitelistEnforced() : Bukkit.hasWhitelist()) ^ this.isNegated()
         : this.players.check(event, OfflinePlayer::isWhitelisted, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String negation = this.isNegated() ? "not" : "";
      if (this.isServer) {
         return this.isEnforce ? "the server whitelist is " + negation + " enforced" : "the server is " + negation + " whitelisted";
      } else {
         return this.players.toString(event, debug) + " is " + negation + " whitelisted";
      }
   }

   static {
      Skript.registerCondition(
         CondIsWhitelisted.class,
         "[the] server (is|not:(isn't|is not)) (in white[ ]list mode|white[ ]listed)",
         "%offlineplayers% (is|are|not:(isn't|is not|aren't|are not)) white[ ]listed",
         "[the] server white[ ]list (is|not:(isn't|is not)) enforced"
      );
   }
}
