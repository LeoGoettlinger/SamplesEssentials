package org.skriptlang.skript.registration;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.util.Priority;

final class DefaultSyntaxInfosImpl {
   static final class ExpressionImpl<E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Expression<E, R> {
      private final Class<R> returnType;

      ExpressionImpl(
         Origin origin, Class<E> type, @Nullable Supplier<E> supplier, SequencedCollection<String> patterns, Priority priority, @Nullable Class<R> returnType
      ) {
         super(origin, type, supplier, patterns, priority);
         Preconditions.checkNotNull(returnType, "An expression syntax info must have a return type.");
         this.returnType = returnType;
      }

      @Override
      public DefaultSyntaxInfos.Expression.Builder<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R> toBuilder() {
         DefaultSyntaxInfosImpl.ExpressionImpl.BuilderImpl<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R> builder = (DefaultSyntaxInfosImpl.ExpressionImpl.BuilderImpl<? extends DefaultSyntaxInfos.Expression.Builder<?, E, R>, E, R>)(new DefaultSyntaxInfosImpl.ExpressionImpl.BuilderImpl<>(
            this.type(), this.returnType
         ));
         super.toBuilder().applyTo(builder);
         return builder;
      }

      @Override
      public Class<R> returnType() {
         return this.returnType;
      }

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         } else if (other instanceof DefaultSyntaxInfos.Expression<?, ?> info) {
            return other.getClass() != DefaultSyntaxInfosImpl.ExpressionImpl.class && !other.equals(this)
               ? false
               : this.type() == info.type()
                  && Objects.equals(this.patterns(), info.patterns())
                  && Objects.equals(this.priority(), info.priority())
                  && this.returnType() == info.returnType();
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(super.hashCode(), this.returnType());
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this)
            .add("origin", this.origin())
            .add("type", this.type())
            .add("patterns", this.patterns())
            .add("priority", this.priority())
            .add("returnType", this.returnType())
            .toString();
      }

      static final class BuilderImpl<B extends DefaultSyntaxInfos.Expression.Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R>
         extends SyntaxInfoImpl.BuilderImpl<B, E>
         implements DefaultSyntaxInfos.Expression.Builder<B, E, R> {
         private final Class<R> returnType;

         BuilderImpl(Class<E> expressionClass, Class<R> returnType) {
            super(expressionClass);
            this.returnType = returnType;
         }

         @Override
         public DefaultSyntaxInfos.Expression<E, R> build() {
            return new DefaultSyntaxInfosImpl.ExpressionImpl<>(this.origin, this.type, this.supplier, this.patterns, this.priority, this.returnType);
         }
      }
   }

   static final class StructureImpl<E extends org.skriptlang.skript.lang.structure.Structure>
      extends SyntaxInfoImpl<E>
      implements DefaultSyntaxInfos.Structure<E> {
      @Nullable
      private final EntryValidator entryValidator;
      private final DefaultSyntaxInfos.Structure.NodeType nodeType;

      StructureImpl(
         Origin origin,
         Class<E> type,
         @Nullable Supplier<E> supplier,
         SequencedCollection<String> patterns,
         Priority priority,
         @Nullable EntryValidator entryValidator,
         DefaultSyntaxInfos.Structure.NodeType nodeType
      ) {
         super(origin, type, supplier, patterns, priority);
         if (!nodeType.canBeSection() && entryValidator != null) {
            throw new IllegalArgumentException("Simple Structures cannot have an EntryValidator");
         } else {
            this.entryValidator = entryValidator;
            this.nodeType = nodeType;
         }
      }

      @Override
      public DefaultSyntaxInfos.Structure.Builder<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E> toBuilder() {
         DefaultSyntaxInfosImpl.StructureImpl.BuilderImpl<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E> builder = (DefaultSyntaxInfosImpl.StructureImpl.BuilderImpl<? extends DefaultSyntaxInfos.Structure.Builder<?, E>, E>)(new DefaultSyntaxInfosImpl.StructureImpl.BuilderImpl<>(
            this.type()
         ));
         super.toBuilder().applyTo(builder);
         if (this.entryValidator != null) {
            builder.entryValidator(this.entryValidator);
         }

         builder.nodeType(this.nodeType);
         return builder;
      }

      @Nullable
      @Override
      public EntryValidator entryValidator() {
         return this.entryValidator;
      }

      @Override
      public DefaultSyntaxInfos.Structure.NodeType nodeType() {
         return this.nodeType;
      }

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         } else if (other instanceof DefaultSyntaxInfos.Structure<?> info) {
            return other.getClass() != DefaultSyntaxInfosImpl.StructureImpl.class && !other.equals(this)
               ? false
               : this.type() == info.type()
                  && Objects.equals(this.patterns(), info.patterns())
                  && Objects.equals(this.priority(), info.priority())
                  && Objects.equals(this.entryValidator(), info.entryValidator())
                  && this.nodeType() == info.nodeType();
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(super.hashCode(), this.entryValidator(), this.nodeType());
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this)
            .add("origin", this.origin())
            .add("type", this.type())
            .add("patterns", this.patterns())
            .add("priority", this.priority())
            .add("entryValidator", this.entryValidator())
            .toString();
      }

      static final class BuilderImpl<B extends DefaultSyntaxInfos.Structure.Builder<B, E>, E extends org.skriptlang.skript.lang.structure.Structure>
         extends SyntaxInfoImpl.BuilderImpl<B, E>
         implements DefaultSyntaxInfos.Structure.Builder<B, E> {
         @Nullable
         private EntryValidator entryValidator;
         private DefaultSyntaxInfos.Structure.NodeType nodeType = DefaultSyntaxInfos.Structure.NodeType.SECTION;

         BuilderImpl(Class<E> structureClass) {
            super(structureClass);
         }

         @Override
         public B entryValidator(EntryValidator entryValidator) {
            this.entryValidator = entryValidator;
            return (B)this;
         }

         @Override
         public B nodeType(DefaultSyntaxInfos.Structure.NodeType nodeType) {
            this.nodeType = nodeType;
            return (B)this;
         }

         @Override
         public DefaultSyntaxInfos.Structure<E> build() {
            return new DefaultSyntaxInfosImpl.StructureImpl<>(
               this.origin, this.type, this.supplier, this.patterns, this.priority, this.entryValidator, this.nodeType
            );
         }

         @Override
         public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
            super.applyTo(builder);
            if (builder instanceof DefaultSyntaxInfos.Structure.Builder<?, ?> structureBuilder) {
               if (this.entryValidator != null) {
                  structureBuilder.entryValidator(this.entryValidator);
               }

               structureBuilder.nodeType(this.nodeType);
            }
         }
      }
   }
}
