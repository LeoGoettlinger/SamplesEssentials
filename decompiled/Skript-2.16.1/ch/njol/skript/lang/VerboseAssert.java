package ch.njol.skript.lang;

import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;

public interface VerboseAssert {
   String getExpectedMessage(Event var1);

   String getReceivedMessage(Event var1);

   static String getExpressionValue(Expression<?> expression, Event event) {
      return Classes.toString(expression.getAll(event), expression.getAnd());
   }
}
