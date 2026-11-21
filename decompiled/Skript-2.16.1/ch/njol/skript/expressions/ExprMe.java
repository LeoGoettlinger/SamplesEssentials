package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Me")
@Description({"A 'me' expression that can be used in players' effect commands only."})
@Example.Examples({@Example("!heal me"), @Example("!kick myself"), @Example("!give a diamond axe to me")})
@Since({"2.1.1"})
public class ExprMe extends SimpleExpression<Player> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return this.getParser().isCurrentEvent(EffectCommandEvent.class);
   }

   @Nullable
   protected Player[] get(Event e) {
      if (!(e instanceof EffectCommandEvent)) {
         return null;
      } else {
         CommandSender commandSender = ((EffectCommandEvent)e).getSender();
         return commandSender instanceof Player ? new Player[]{(Player)commandSender} : null;
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Player> getReturnType() {
      return Player.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "me";
   }

   static {
      Skript.registerExpression(ExprMe.class, Player.class, ExpressionType.SIMPLE, "me", "my[self]");
   }
}
