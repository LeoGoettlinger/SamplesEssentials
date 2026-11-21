package org.jdbi.v3.core.argument;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;
import org.jdbi.v3.meta.Beta;

@FunctionalInterface
public interface QualifiedArgumentFactory {
   Optional<Argument> build(QualifiedType<?> type, Object value, ConfigRegistry config);

   static QualifiedArgumentFactory adapt(ConfigRegistry config, ArgumentFactory factory) {
      if (factory instanceof ArgumentFactory.Preparable preparable) {
         return adapt(config, preparable);
      } else {
         Set<Annotation> qualifiers = config.get(Qualifiers.class).findFor(factory.getClass());
         return (type, value, cfg) -> type.hasQualifiers(qualifiers) ? factory.build(type.getType(), value, cfg) : Optional.empty();
      }
   }

   static QualifiedArgumentFactory.Preparable adapt(ConfigRegistry config, ArgumentFactory.Preparable factory) {
      return QualifiedArgumentFactory.Preparable.adapt(config, factory);
   }

   @Beta
   public interface Preparable extends QualifiedArgumentFactory {
      Optional<Function<Object, Argument>> prepare(QualifiedType<?> type, ConfigRegistry config);

      @Deprecated(
         since = "3.15.0",
         forRemoval = true
      )
      default Collection<QualifiedType<?>> prePreparedTypes() {
         return Collections.emptyList();
      }

      static QualifiedArgumentFactory.Preparable adapt(ConfigRegistry config, ArgumentFactory.Preparable factory) {
         return new QualifiedArgumentFactory.Preparable() {
            final Set<Annotation> qualifiers = config.get(Qualifiers.class).findFor(factory.getClass());
            final Collection<QualifiedType<?>> prePreparedTypes = Collections.unmodifiableList(
               factory.prePreparedTypes().stream().map(QualifiedType::of).map(qt -> qt.withAnnotations(this.qualifiers)).toList()
            );

            @Override
            public Optional<Argument> build(QualifiedType<?> type, Object value, ConfigRegistry cfg) {
               return type.hasQualifiers(this.qualifiers) ? factory.build(type.getType(), value, cfg) : Optional.empty();
            }

            @Override
            public Optional<Function<Object, Argument>> prepare(QualifiedType<?> type, ConfigRegistry cfg) {
               return type.hasQualifiers(this.qualifiers) ? factory.prepare(type.getType(), cfg) : Optional.empty();
            }

            @Deprecated(
               since = "3.39.0",
               forRemoval = true
            )
            @Override
            public Collection<QualifiedType<?>> prePreparedTypes() {
               return this.prePreparedTypes;
            }

            @Override
            public String toString() {
               return "Qualified[" + factory + "]";
            }
         };
      }
   }
}
