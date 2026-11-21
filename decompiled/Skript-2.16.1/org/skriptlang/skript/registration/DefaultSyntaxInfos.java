package org.skriptlang.skript.registration;

import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;

public sealed interface DefaultSyntaxInfos permits SyntaxInfo {
   public interface Expression<E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfo<E> {
      @Contract("_, _, _ , _-> new")
      static <E extends ch.njol.skript.lang.Expression<R>, R> DefaultSyntaxInfos.Expression<E, R> simple(
         Class<E> expressionClass, Supplier<E> instanceSupplier, Class<R> returnType, String... patterns
      ) {
         return builder(expressionClass, returnType).supplier(instanceSupplier).addPatterns(patterns).build();
      }

      @Contract("_, _ -> new")
      static <E extends ch.njol.skript.lang.Expression<R>, R> DefaultSyntaxInfos.Expression.Builder<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R> builder(
         Class<E> expressionClass, Class<R> returnType
      ) {
         return (DefaultSyntaxInfos.Expression.Builder<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R>)(new DefaultSyntaxInfosImpl.ExpressionImpl.BuilderImpl<>(
            expressionClass, returnType
         ));
      }

      @Contract("-> new")
      DefaultSyntaxInfos.Expression.Builder<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R> toBuilder();

      Class<R> returnType();

      public interface Builder<B extends DefaultSyntaxInfos.Expression.Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R>
         extends SyntaxInfo.Builder<B, E> {
         @Contract("-> new")
         DefaultSyntaxInfos.Expression<E, R> build();
      }
   }

   public interface Structure<E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo<E> {
      @Contract("_, _, _ -> new")
      static <E extends org.skriptlang.skript.lang.structure.Structure> DefaultSyntaxInfos.Structure<E> simple(
         Class<E> structureClass, Supplier<E> instanceSupplier, String... patterns
      ) {
         return builder(structureClass).supplier(instanceSupplier).addPatterns(patterns).build();
      }

      @Contract("_ -> new")
      static <E extends org.skriptlang.skript.lang.structure.Structure> DefaultSyntaxInfos.Structure.Builder<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E> builder(
         Class<E> structureClass
      ) {
         return (DefaultSyntaxInfos.Structure.Builder<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E>)(new DefaultSyntaxInfosImpl.StructureImpl.BuilderImpl<>(
            structureClass
         ));
      }

      @Contract("-> new")
      DefaultSyntaxInfos.Structure.Builder<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E> toBuilder();

      @Nullable
      EntryValidator entryValidator();

      DefaultSyntaxInfos.Structure.NodeType nodeType();

      public interface Builder<B extends DefaultSyntaxInfos.Structure.Builder<B, E>, E extends org.skriptlang.skript.lang.structure.Structure>
         extends SyntaxInfo.Builder<B, E> {
         @Contract("_ -> this")
         B entryValidator(EntryValidator var1);

         B nodeType(DefaultSyntaxInfos.Structure.NodeType var1);

         @Contract("-> new")
         DefaultSyntaxInfos.Structure<E> build();
      }

      public static enum NodeType {
         SIMPLE,
         SECTION,
         BOTH;

         public boolean canBeSimple() {
            return this != SECTION;
         }

         public boolean canBeSection() {
            return this != SIMPLE;
         }
      }
   }
}
