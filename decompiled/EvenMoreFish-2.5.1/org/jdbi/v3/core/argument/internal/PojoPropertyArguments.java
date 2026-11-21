package org.jdbi.v3.core.argument.internal;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jdbi.v3.core.annotation.internal.JdbiAnnotations;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.reflect.internal.PojoProperties;
import org.jdbi.v3.core.mapper.reflect.internal.PojoTypes;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;

public class PojoPropertyArguments extends ObjectPropertyNamedArgumentFinder {
   protected final PojoProperties<?> properties;
   protected final ConfigRegistry config;
   private final Set<String> names;

   public PojoPropertyArguments(String prefix, Object obj, Type type, ConfigRegistry config) {
      this(
         prefix,
         obj,
         config.get(PojoTypes.class).findFor(type).orElseThrow(() -> new UnableToCreateStatementException("Couldn't find pojo type of " + obj.getClass())),
         config
      );
   }

   protected PojoPropertyArguments(String prefix, Object obj, PojoProperties<?> properties, ConfigRegistry config) {
      super(prefix, obj);
      this.properties = properties;
      this.config = config;
      this.names = properties.getProperties()
         .entrySet()
         .stream()
         .filter(e -> JdbiAnnotations.isBound((PojoProperties.PojoProperty<?>)e.getValue()))
         .map(Entry::getKey)
         .collect(Collectors.toSet());
   }

   @Override
   protected Optional<TypedValue> getValue(String name, StatementContext ctx) {
      return Optional.ofNullable(this.properties.getProperties().get(name))
         .map(PojoProperties.PojoProperty.class::cast)
         .filter(JdbiAnnotations::isBound)
         .map(p -> new TypedValue(p.getQualifiedType(), p.get(this.obj)));
   }

   @Override
   public Collection<String> getNames() {
      return this.names;
   }

   @Override
   protected NamedArgumentFinder getNestedArgumentFinder(TypedValue value) {
      return new PojoPropertyArguments(null, value.getValue(), value.getType().getType(), this.config);
   }

   @Override
   public String toString() {
      return "{lazy bean property arguments \"" + this.obj + "\"}";
   }
}
