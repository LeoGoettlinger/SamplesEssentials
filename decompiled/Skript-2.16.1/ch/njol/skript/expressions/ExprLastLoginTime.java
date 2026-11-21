package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

@Name("Last/First Login Time")
@Description({"When a player last/first logged in the server. 'last login' requires paper to get the last login, otherwise it will get the last time they were seen on the server."})
@Example("command /onlinefor:\n\ttrigger:\n\t\tsend \"You have been online for %difference between player's last login and now%.\"\n\t\tsend \"You first joined the server %difference between player's first login and now% ago.\"\n")
@Since({"2.5"})
public class ExprLastLoginTime extends SimplePropertyExpression<OfflinePlayer, Date> {
   private static boolean LAST_LOGIN = Skript.methodExists(OfflinePlayer.class, "getLastLogin");
   private boolean first;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.first = parseResult.mark == 2;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Date convert(OfflinePlayer player) {
      return new Date(this.first ? player.getFirstPlayed() : (LAST_LOGIN ? player.getLastLogin() : player.getLastPlayed()));
   }

   @Override
   public Class<? extends Date> getReturnType() {
      return Date.class;
   }

   @Override
   protected String getPropertyName() {
      return "last login date";
   }

   static {
      register(ExprLastLoginTime.class, Date.class, "(1¦last|2¦first) login", "offlineplayers");
   }
}
