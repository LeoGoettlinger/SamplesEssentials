package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;

@Name("Quit Reason")
@Description({"The <a href='#quitreason'>quit reason</a> as to why a player disconnected in a <a href='#quit'>quit</a> event."})
@Example("on quit:\n\tquit reason was kicked\n\tplayer is banned\n\tclear {server::player::%uuid of player%::*}\n")
@Since({"2.8.0"})
public class ExprQuitReason extends EventValueExpression<QuitReason> {
   public ExprQuitReason() {
      super(QuitReason.class);
   }

   @Override
   public boolean setTime(int time) {
      return time != EventValues.TIME_FUTURE;
   }

   static {
      if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason")) {
         register(ExprQuitReason.class, QuitReason.class, "(quit|disconnect) (cause|reason)");
      }
   }
}
