package org.jdbi.v3.core.statement;

import com.google.errorprone.annotations.CheckReturnValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.NamedArgumentFinderFactory;
import org.jdbi.v3.core.argument.internal.TypedValue;
import org.jdbi.v3.core.internal.exceptions.CheckedConsumer;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;
import org.jdbi.v3.core.statement.internal.PreparedBinding;

class ArgumentBinder {
   final PreparedStatement stmt;
   final StatementContext ctx;
   final ParsedParameters params;
   private final Argument nullArgument;
   protected final ArgumentBinder.ArgumentFactoryLocator factoryLocator;

   ArgumentBinder(PreparedStatement stmt, StatementContext ctx, ParsedParameters params) {
      this.stmt = stmt;
      this.ctx = ctx;
      this.params = params;
      this.nullArgument = ctx.getConfig(Arguments.class).getUntypedNullArgument();
      this.factoryLocator = new ArgumentBinder.ArgumentFactoryLocator(ctx);
   }

   void bind(Binding binding) {
      if (this.params.isPositional()) {
         this.bindPositional(binding);
      } else {
         this.bindNamed(binding);
      }
   }

   void bindPositional(Binding binding) {
      for (int index = 0; index < this.params.getParameterCount(); index++) {
         if (!binding.positionals.containsKey(index)) {
            throw new UnableToCreateStatementException(String.format("Missing positional parameter %d in binding:%s", index, binding), this.ctx);
         }

         QualifiedType<?> type = this.factoryLocator.typeOf(binding.positionals.get(index));

         try {
            this.factoryLocator.argumentFactoryForType(type).apply(unwrap(binding.positionals.get(index))).apply(index + 1, this.stmt, this.ctx);
         } catch (SQLException var5) {
            throw new UnableToCreateStatementException("Exception while binding positional param at (0 based) position " + index, var5, this.ctx);
         }
      }

      boolean moreArgumentsProvidedThanDeclared = binding.positionals.size() != this.params.getParameterCount();
      if (moreArgumentsProvidedThanDeclared && !this.ctx.getConfig(SqlStatements.class).isUnusedBindingAllowed()) {
         throw new UnableToCreateStatementException("Superfluous positional param at (0 based) position " + this.params.getParameterCount(), this.ctx);
      }
   }

   void bindNamed(Binding binding) {
      List<String> paramNames = this.params.getParameterNames();
      this.bindNamedCheck(binding, paramNames);

      label43:
      for (int i = 0; i < paramNames.size(); i++) {
         String name = paramNames.get(i);

         try {
            Object value = binding.named.get(name);
            if (value == null) {
               if (binding.named.containsKey(name)) {
                  this.nullArgument.apply(i + 1, this.stmt, this.ctx);
               } else {
                  for (NamedArgumentFinder naf : binding.namedArgumentFinder) {
                     Optional<Argument> found = naf.find(name, this.ctx);
                     if (found.isPresent()) {
                        found.get().apply(i + 1, this.stmt, this.ctx);
                        continue label43;
                     }
                  }

                  throw this.missingNamedParameter(name, binding);
               }
            } else if (value instanceof Argument argument) {
               argument.apply(i + 1, this.stmt, this.ctx);
            } else {
               this.factoryLocator.argumentFactoryForType(this.factoryLocator.typeOf(value)).apply(unwrap(value)).apply(i + 1, this.stmt, this.ctx);
            }
         } catch (SQLException var9) {
            throw new UnableToCreateStatementException(String.format("Exception while binding named parameter '%s'", name), var9, this.ctx);
         }
      }
   }

   void bindNamedCheck(Binding binding, List<String> paramNames) {
      boolean argumentsProvidedButNoneDeclared = paramNames.isEmpty() && !binding.isEmpty();
      if (argumentsProvidedButNoneDeclared && !this.ctx.getConfig(SqlStatements.class).isUnusedBindingAllowed()) {
         throw new UnableToCreateStatementException(
            String.format(
               "Superfluous named parameters provided while the query declares none: '%s'. This check may be disabled by calling getConfig(SqlStatements.class).setUnusedBindingAllowed(true) or using @AllowUnusedBindings in SQL object.",
               binding
            ),
            this.ctx
         );
      }
   }

   @Deprecated(
      since = "3.11.0",
      forRemoval = true
   )
   Argument toArgument(Object found) {
      return this.factoryLocator.argumentFactoryForType(this.factoryLocator.typeOf(found)).apply(unwrap(found));
   }

   UnableToCreateStatementException missingNamedParameter(String name, Binding binding) {
      return new UnableToCreateStatementException(String.format("Missing named parameter '%s' in binding:%s", name, binding), this.ctx);
   }

   <T> Consumer<T> wrapCheckedConsumer(final String paramName, CheckedConsumer<T> consumer) {
      return t -> {
         try {
            consumer.accept(t);
         } catch (SQLException var5) {
            throw new UnableToCreateStatementException(String.format("Exception while binding named parameter '%s'", paramName), var5, this.ctx);
         } catch (Exception var6) {
            throw Sneaky.throwAnyway(var6);
         }
      };
   }

   @CheckReturnValue
   static Object unwrap(@Nullable Object maybeTypedValue) {
      return maybeTypedValue instanceof TypedValue t ? t.getValue() : maybeTypedValue;
   }

   static class ArgumentFactoryLocator {
      private final StatementContext ctx;
      final Map<QualifiedType<?>, Function<Object, Argument>> argumentFactoryByType = new HashMap<>();

      ArgumentFactoryLocator(StatementContext ctx) {
         this.ctx = ctx;
      }

      @Nonnull
      QualifiedType<?> typeOf(@Nullable Object value) {
         return value instanceof TypedValue t
            ? t.getType()
            : this.ctx.getConfig(Qualifiers.class).qualifiedTypeOf(Optional.ofNullable(value).map(Object::getClass).orElse(Object.class));
      }

      Function<Object, Argument> argumentFactoryForType(QualifiedType<?> type) {
         return this.argumentFactoryByType.computeIfAbsent(type, qt -> {
            Arguments args = this.ctx.getConfig(Arguments.class);
            Function<Object, Argument> factory = args.prepareFor(type).orElse(v -> args.findFor(type, v).orElseThrow(() -> this.factoryNotFound(type, v)));
            return value -> DescribedArgument.wrap(this.ctx, factory.apply(value), value);
         });
      }

      private UnableToCreateStatementException factoryNotFound(QualifiedType<?> qualifiedType, Object value) {
         Type type = qualifiedType.getType();
         if (type instanceof Class<?> clazz) {
            TypeVariable<?>[] typeVars = clazz.getTypeParameters();
            if (typeVars.length > 0) {
               return new UnableToCreateStatementException(
                  "No type parameters found for erased type '"
                     + type
                     + Arrays.toString((Object[])typeVars)
                     + "' of qualified type '"
                     + qualifiedType
                     + "'. To bind a generic type, prefer using bindByType."
               );
            }
         }

         return new UnableToCreateStatementException("No argument factory registered for '" + value + "' of qualified type " + qualifiedType, this.ctx);
      }
   }

   static class Prepared extends ArgumentBinder {
      private final PreparedBatch batch;
      private final Consumer<PreparedBinding> preparedBinder;
      private final List<String> paramNames;

      Prepared(PreparedBatch batch, ParsedParameters params, PreparedBinding preparedBindingTemplate) {
         super(batch.stmt, batch.getContext(), params);
         this.batch = batch;
         this.paramNames = params.getParameterNames();
         this.preparedBinder = this.prepareBinder(preparedBindingTemplate);
      }

      private Consumer<PreparedBinding> prepareBinder(PreparedBinding preparedBindingTemplate) {
         List<Consumer<PreparedBinding>> innerBinders = new ArrayList<>(this.paramNames.size());

         for (int i = 0; i < this.paramNames.size(); i++) {
            int index = i;
            String name = this.paramNames.get(i);
            Object value = preparedBindingTemplate.named.get(name);
            if (value == null && !preparedBindingTemplate.named.containsKey(name)) {
               Optional<Entry<NamedArgumentFinderFactory.PrepareKey, Function<Object, Argument>>> preparation = preparedBindingTemplate.prepareKeys
                  .keySet()
                  .stream()
                  .map(pk -> new SimpleImmutableEntry<>(pk, this.batch.preparedFinders.get(pk)))
                  .flatMap(
                     e -> e.getValue()
                        .apply(name)
                        .map(pf -> new SimpleImmutableEntry<>((NamedArgumentFinderFactory.PrepareKey)e.getKey(), (Function<Object, Argument>)pf))
                        .stream()
                  )
                  .findFirst();
               if (preparation.isPresent()) {
                  Entry<NamedArgumentFinderFactory.PrepareKey, Function<Object, Argument>> p = preparation.get();
                  Map<NamedArgumentFinderFactory.PrepareKey, Function<Object, Argument>> accessors = new HashMap<>();
                  accessors.put(p.getKey(), p.getValue());
                  innerBinders.add(this.wrapCheckedConsumer(name, binding -> {
                     Object bound = binding.prepareKeys.get(p.getKey());
                     Argument argument = bound != null ? p.getValue().apply(bound) : this.preparedArgument(accessors, binding, name);
                     argument.apply(index + 1, this.stmt, this.ctx);
                  }));
               } else {
                  innerBinders.add(this.wrapCheckedConsumer(name, binding -> this.dynamicNamedArgument(binding, name).apply(index + 1, this.stmt, this.ctx)));
               }
            } else {
               Function<Object, Argument> binder = this.factoryLocator.argumentFactoryForType(this.factoryLocator.typeOf(value));
               innerBinders.add(this.wrapCheckedConsumer(name, binding -> binder.apply(unwrap(binding.named.get(name))).apply(index + 1, this.stmt, this.ctx)));
            }
         }

         return binding -> innerBinders.forEach(b -> b.accept(binding));
      }

      private Argument preparedArgument(Map<NamedArgumentFinderFactory.PrepareKey, Function<Object, Argument>> accessors, PreparedBinding binding, String name) {
         for (Entry<NamedArgumentFinderFactory.PrepareKey, Object> entry : binding.prepareKeys.entrySet()) {
            Function<Object, Argument> accessor = accessors.computeIfAbsent(entry.getKey(), key -> {
               Function<String, Optional<Function<Object, Argument>>> finder = this.batch.preparedFinders.get(key);
               return finder == null ? null : finder.apply(name).orElse(null);
            });
            if (accessor != null) {
               return accessor.apply(entry.getValue());
            }
         }

         return this.dynamicNamedArgument(binding, name);
      }

      private Argument dynamicNamedArgument(PreparedBinding binding, String name) {
         return binding.namedArgumentFinder
            .stream()
            .flatMap(naf -> naf.find(name, this.ctx).stream())
            .findFirst()
            .orElseGet(
               () -> binding.realizedBackupArgumentFinders
                  .get()
                  .stream()
                  .flatMap(naf -> naf.find(name, this.ctx).stream())
                  .findFirst()
                  .orElseThrow(() -> this.missingNamedParameter(name, binding))
            );
      }

      @Override
      void bindNamed(Binding binding) {
         this.bindNamedCheck(binding, this.paramNames);
         this.preparedBinder.accept((PreparedBinding)binding);
      }
   }
}
