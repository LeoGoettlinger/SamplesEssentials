package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Game Mode")
@Description({"The gamemode of a player. See <a href=\"#gamemode\">Gamemodes</a>\n"})
@Example.Examples({@Example("player's gamemode is survival"), @Example("set the player's gamemode to creative"), @Example("set gamemode of player to adventure")})
@Since({"1.0"})
public class ExprGameMode extends PropertyExpression<Player, GameMode> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprGameMode.class, GameMode.class, "game[ ]mode", "players", false).supplier(ExprGameMode::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends Player>)expressions[0]);
      return true;
   }

   protected GameMode[] get(Event event, Player[] source) {
      Player eventPlayer;
      if (this.getTime() >= 0 && event instanceof PlayerGameModeChangeEvent playerEvent && !Delay.isDelayed(event)) {
         eventPlayer = playerEvent.getPlayer();
      } else {
         eventPlayer = null;
      }

      return this.get(source, player -> player == eventPlayer ? ((PlayerGameModeChangeEvent)event).getNewGameMode() : player.getGameMode());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : CollectionUtils.array(GameMode.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      GameMode gamemode = delta == null ? Bukkit.getDefaultGameMode() : (GameMode)delta[0];
      List<? extends Player> players = this.getExpr().stream(event).toList();
      if (this.getTime() >= 0
         && event instanceof PlayerGameModeChangeEvent playerEvent
         && players.contains(playerEvent.getPlayer())
         && !Delay.isDelayed(event)
         && playerEvent.getNewGameMode() != gamemode) {
         playerEvent.setCancelled(true);
      }

      for (Player player : players) {
         player.setGameMode(gamemode);
      }
   }

   @Override
   public Class<GameMode> getReturnType() {
      return GameMode.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append("the gamemode of", this.getExpr()).toString();
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, PlayerGameModeChangeEvent.class);
   }
}
