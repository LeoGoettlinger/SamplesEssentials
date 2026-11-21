package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

@Name("Heal Reason")
@Description({"The <a href='#healreason'>heal reason</a> of a <a href='#heal'>heal event</a>."})
@Example("on heal:\n\theal reason is satiated\n\tsend \"You ate enough food and gained full health back!\"\n")
@Events({"heal"})
@Since({"2.5"})
public class ExprHealReason extends EventValueExpression<RegainReason> {
   public ExprHealReason() {
      super(RegainReason.class);
   }

   @Override
   public boolean setTime(int time) {
      return time == EventValues.TIME_FUTURE ? false : super.setTime(time);
   }

   static {
      register(ExprHealReason.class, RegainReason.class, "(regen|health regain|heal[ing]) (reason|cause)");
   }
}
