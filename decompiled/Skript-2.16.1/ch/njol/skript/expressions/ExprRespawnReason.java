package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;

@Name("Respawn Reason")
@Description({"The <a href='#respawnreason'>respawn reason</a> in a <a href='#respawn'>respawn</a> event."})
@Example("on respawn:\n\tif respawn reason is end portal:\n\t\tbroadcast \"%player% took the end portal to the overworld!\"\n")
@Since({"2.14"})
public class ExprRespawnReason extends EventValueExpression<RespawnReason> {
   public ExprRespawnReason() {
      super(RespawnReason.class);
   }

   static {
      register(ExprRespawnReason.class, RespawnReason.class, "respawn[ing] reason");
   }
}
