package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

@Deprecated(
   since = "2.14",
   forRemoval = true
)
public enum ExpressionType {
   /** @deprecated */
   SIMPLE(SyntaxInfo.SIMPLE),
   /** @deprecated */
   EVENT(EventValueExpression.DEFAULT_PRIORITY),
   /** @deprecated */
   COMBINED(SyntaxInfo.COMBINED),
   /** @deprecated */
   PROPERTY(PropertyExpression.DEFAULT_PRIORITY),
   /** @deprecated */
   PATTERN_MATCHES_EVERYTHING(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

   private final Priority priority;

   private ExpressionType(Priority priority) {
      this.priority = priority;
   }

   public Priority priority() {
      return this.priority;
   }

   @Nullable
   public static ExpressionType fromModern(Priority priority) {
      if (priority == SyntaxInfo.SIMPLE) {
         return SIMPLE;
      } else if (priority == EventValueExpression.DEFAULT_PRIORITY) {
         return EVENT;
      } else if (priority == SyntaxInfo.COMBINED) {
         return COMBINED;
      } else if (priority == PropertyExpression.DEFAULT_PRIORITY) {
         return PROPERTY;
      } else {
         return priority == SyntaxInfo.PATTERN_MATCHES_EVERYTHING ? PATTERN_MATCHES_EVERYTHING : null;
      }
   }
}
