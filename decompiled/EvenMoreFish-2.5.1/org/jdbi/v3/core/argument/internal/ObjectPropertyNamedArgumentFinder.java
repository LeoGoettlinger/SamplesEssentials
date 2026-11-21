package org.jdbi.v3.core.argument.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;

public abstract class ObjectPropertyNamedArgumentFinder implements NamedArgumentFinder {
   protected final String prefix;
   protected final Object obj;
   private final Map<String, Optional<NamedArgumentFinder>> childArgumentFinders = new ConcurrentHashMap<>();

   protected ObjectPropertyNamedArgumentFinder(String prefix, Object obj) {
      this.prefix = prefix != null && !prefix.isEmpty() ? prefix + "." : "";
      this.obj = obj;
   }

   @Override
   public final Optional<Argument> find(String name, StatementContext ctx) {
      if (name.startsWith(this.prefix)) {
         String actualName = name.substring(this.prefix.length());
         int separator = actualName.indexOf(46);
         if (separator != -1) {
            String parentName = actualName.substring(0, separator);
            String childName = actualName.substring(separator + 1);
            return this.childArgumentFinders
               .computeIfAbsent(
                  parentName.endsWith("?") ? parentName.substring(0, parentName.length() - 1) : parentName,
                  pn -> this.getValue(pn, ctx).map(typedValue -> this.getValueNested(typedValue, parentName, childName))
               )
               .flatMap(arg -> arg.find(childName, ctx));
         } else {
            return this.getValue(actualName, ctx)
               .map(
                  tv -> ctx.findArgumentFor(tv.getType(), tv.getValue())
                     .orElseThrow(
                        () -> new UnableToCreateStatementException(
                           String.format("No argument factory registered for type [%s] for element [%s] on [%s]", tv.getType(), name, this.obj), ctx
                        )
                     )
               );
         }
      } else {
         return Optional.empty();
      }
   }

   private NamedArgumentFinder getValueNested(TypedValue typedValue, String parentName, String childName) {
      if (Objects.nonNull(typedValue.getValue())) {
         return this.getNestedArgumentFinder(typedValue);
      } else if (parentName.endsWith("?")) {
         return (n, c) -> Optional.of(c.getConfig(Arguments.class).getUntypedNullArgument());
      } else {
         throw new IllegalArgumentException(
            String.format(
               "Trying to bind nested argument [%s], but found null value at [%s], may mark it as an optional with [%s]",
               childName,
               parentName,
               parentName + "?"
            )
         );
      }
   }

   protected abstract Optional<TypedValue> getValue(String name, StatementContext ctx);

   protected abstract NamedArgumentFinder getNestedArgumentFinder(TypedValue obj);
}
