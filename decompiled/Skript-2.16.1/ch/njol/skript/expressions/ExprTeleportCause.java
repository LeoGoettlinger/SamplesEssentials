package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Name("Teleport Cause")
@Description({"The <a href='#teleportcause'>teleport cause</a> within a player <a href='#teleport'>teleport</a> event."})
@Example("on teleport:\n\tteleport cause is nether portal, end portal or end gateway\n\tcancel event\n")
@Since({"2.2-dev35"})
public class ExprTeleportCause extends EventValueExpression<TeleportCause> {
   public ExprTeleportCause() {
      super(TeleportCause.class);
   }

   static {
      register(ExprTeleportCause.class, TeleportCause.class, "teleport (cause|reason|type)");
   }
}
