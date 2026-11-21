package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

@Name("Spawn Reason")
@Description({"The <a href='#spawnreason'>spawn reason</a> in a <a href='#spawn'>spawn</a> event."})
@Example("on spawn:\n\tspawn reason is reinforcements or breeding\n\tcancel event\n")
@Since({"2.3"})
public class ExprSpawnReason extends EventValueExpression<SpawnReason> {
   public ExprSpawnReason() {
      super(SpawnReason.class);
   }

   static {
      register(ExprSpawnReason.class, SpawnReason.class, "spawn[ing] reason");
   }
}
