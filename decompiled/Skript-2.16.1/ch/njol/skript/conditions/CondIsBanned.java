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
import java.net.InetSocketAddress;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@Name("Is Banned")
@Description({"Checks whether a player or IP is banned."})
@Example.Examples({@Example("player is banned"), @Example("victim is not IP-banned"), @Example("\"127.0.0.1\" is banned")})
@Since({"1.4"})
public class CondIsBanned extends PropertyCondition<Object> {
   private boolean ipBanned;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Object>)exprs[0]);
      this.setNegated(matchedPattern >= 2);
      this.ipBanned = matchedPattern % 2 != 0;
      return true;
   }

   @Override
   public boolean check(Object obj) {
      if (obj instanceof Player player) {
         if (this.ipBanned) {
            InetSocketAddress sockAddr = player.getAddress();
            return sockAddr == null ? false : Bukkit.getIPBans().contains(sockAddr.getAddress().getHostAddress());
         } else {
            return player.isBanned();
         }
      } else if (obj instanceof OfflinePlayer offlinePlayer) {
         return offlinePlayer.isBanned();
      } else if (!(obj instanceof String)) {
         assert false;

         return false;
      } else {
         return Bukkit.getIPBans().contains(obj)
            || !this.ipBanned && Bukkit.getBannedPlayers().stream().anyMatch(offPlayer -> offPlayer != null && obj.equals(offPlayer.getName()));
      }
   }

   @Override
   protected String getPropertyName() {
      return this.ipBanned ? "IP-banned" : "banned";
   }

   static {
      Skript.registerCondition(
         CondIsBanned.class,
         "%offlineplayers/strings% (is|are) banned",
         "%players/strings% (is|are) IP(-| |)banned",
         "%offlineplayers/strings% (isn't|is not|aren't|are not) banned",
         "%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned"
      );
   }
}
