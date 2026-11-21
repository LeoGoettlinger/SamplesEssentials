package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.jdbi.v3.core.array.SqlArrayArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Beta;

public class Arguments implements JdbiConfig<Arguments> {
   private final List<QualifiedArgumentFactory> factories;
   private final Map<QualifiedType<?>, Function<Object, Argument>> preparedFactories = new ConcurrentHashMap<>();
   private final Set<QualifiedType<?>> didPrepare = ConcurrentHashMap.newKeySet();
   private ConfigRegistry registry;
   private Argument untypedNullArgument = new NullArgument(1111);
   private boolean bindingNullToPrimitivesPermitted = true;
   private boolean preparedArgumentsEnabled = true;

   public Arguments(final ConfigRegistry registry) {
      this.factories = new CopyOnWriteArrayList<>();
      this.registry = registry;
      this.register(new UntypedNullArgumentFactory());
      this.register(new PrimitivesArgumentFactory());
      this.register(new BoxedArgumentFactory());
      this.register(new SqlArgumentFactory());
      this.register(new InternetArgumentFactory());
      this.register(new SqlTimeArgumentFactory());
      this.register(new JavaTimeArgumentFactory());
      this.register(new SqlArrayArgumentFactory());
      this.register(new CharSequenceArgumentFactory());
      this.register(new EssentialsArgumentFactory());
      this.register(new JavaTimeZoneIdArgumentFactory());
      this.register(new NVarcharArgumentFactory());
      this.register(new EnumArgumentFactory());
      this.register(new OptionalArgumentFactory());
      this.register(new DirectArgumentFactory());
      this.register(new LegacyArgumentFactory());
   }

   private Arguments(final Arguments that) {
      this.factories = new CopyOnWriteArrayList<>(that.factories);
      this.untypedNullArgument = that.untypedNullArgument;
      this.bindingNullToPrimitivesPermitted = that.bindingNullToPrimitivesPermitted;
      this.preparedArgumentsEnabled = that.preparedArgumentsEnabled;
   }

   @Override
   public void setRegistry(final ConfigRegistry registry) {
      this.registry = registry;
   }

   public Arguments register(final ArgumentFactory factory) {
      return this.register(QualifiedArgumentFactory.adapt(this.registry, factory));
   }

   public Arguments register(final QualifiedArgumentFactory factory) {
      this.factories.add(0, factory);
      return this;
   }

   public Optional<Argument> findFor(final Type type, final Object value) {
      return this.findFor(QualifiedType.of(type), value);
   }

   public Optional<Argument> findFor(final QualifiedType<?> type, final Object value) {
      Function<Object, Argument> prepared = this.preparedFactories.get(type);
      if (prepared != null) {
         return Optional.of(prepared.apply(value));
      } else {
         Class<?> expectedClass = GenericTypes.getErasedType(type.getType());
         QualifiedType<?> expectedType;
         if (value != null && expectedClass == Object.class) {
            expectedType = type.mapType(t -> value.getClass());
         } else {
            expectedType = type;
         }

         for (QualifiedArgumentFactory factory : this.factories) {
            Optional<Argument> maybeBuilt = factory.build(expectedType, value, this.registry);
            if (maybeBuilt.isPresent()) {
               if (factory instanceof QualifiedArgumentFactory.Preparable p && this.didPrepare.add(expectedType)) {
                  p.prepare(expectedType, this.registry)
                     .ifPresent(argumentFactory -> this.preparedFactories.putIfAbsent(expectedType, (Function<Object, Argument>)argumentFactory));
               }

               return maybeBuilt;
            }
         }

         return Optional.empty();
      }
   }

   public Optional<Function<Object, Argument>> prepareFor(final Type type) {
      return this.prepareFor(QualifiedType.of(type));
   }

   @Beta
   public Optional<Function<Object, Argument>> prepareFor(final QualifiedType<?> type) {
      if (!this.isPreparedArgumentsEnabled()) {
         return Optional.empty();
      } else {
         Function<Object, Argument> prepared = this.preparedFactories.get(type);
         if (prepared != null) {
            return Optional.of(prepared);
         } else {
            for (QualifiedArgumentFactory factory : this.factories) {
               if (factory instanceof QualifiedArgumentFactory.Preparable preparable) {
                  Optional<Function<Object, Argument>> argumentFactory = preparable.prepare(type, this.registry);
                  if (argumentFactory.isPresent()) {
                     this.preparedFactories.putIfAbsent(type, argumentFactory.get());
                     return argumentFactory;
                  }
               }
            }

            return Optional.empty();
         }
      }
   }

   public List<QualifiedArgumentFactory> getFactories() {
      return Collections.unmodifiableList(this.factories);
   }

   public void setUntypedNullArgument(final Argument untypedNullArgument) {
      if (untypedNullArgument == null) {
         throw new IllegalArgumentException("the Argument itself may not be null");
      } else {
         this.untypedNullArgument = untypedNullArgument;
      }
   }

   public Argument getUntypedNullArgument() {
      return this.untypedNullArgument;
   }

   public boolean isBindingNullToPrimitivesPermitted() {
      return this.bindingNullToPrimitivesPermitted;
   }

   public void setBindingNullToPrimitivesPermitted(final boolean bindingNullToPrimitivesPermitted) {
      this.bindingNullToPrimitivesPermitted = bindingNullToPrimitivesPermitted;
   }

   public boolean isPreparedArgumentsEnabled() {
      return this.preparedArgumentsEnabled;
   }

   public void setPreparedArgumentsEnabled(final boolean preparedArgumentsEnabled) {
      this.preparedArgumentsEnabled = preparedArgumentsEnabled;
   }

   public Arguments createCopy() {
      return new Arguments(this);
   }
}
