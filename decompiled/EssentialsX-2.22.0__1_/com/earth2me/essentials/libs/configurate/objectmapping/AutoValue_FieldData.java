package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Constraint;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.NodeResolver;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.lang.reflect.AnnotatedType;
import java.util.List;

final class AutoValue_FieldData<I, O> extends FieldData<I, O> {
   private final String name;
   private final AnnotatedType resolvedType;
   private final List<Constraint<?>> constraints;
   private final List<Processor<?>> processors;
   private final FieldData.Deserializer<I> deserializer;
   private final CheckedFunction<O, Object, Exception> serializer;
   private final NodeResolver nodeResolver;

   AutoValue_FieldData(
      String name,
      AnnotatedType resolvedType,
      List<Constraint<?>> constraints,
      List<Processor<?>> processors,
      FieldData.Deserializer<I> deserializer,
      CheckedFunction<O, Object, Exception> serializer,
      NodeResolver nodeResolver
   ) {
      if (name == null) {
         throw new NullPointerException("Null name");
      } else {
         this.name = name;
         if (resolvedType == null) {
            throw new NullPointerException("Null resolvedType");
         } else {
            this.resolvedType = resolvedType;
            if (constraints == null) {
               throw new NullPointerException("Null constraints");
            } else {
               this.constraints = constraints;
               if (processors == null) {
                  throw new NullPointerException("Null processors");
               } else {
                  this.processors = processors;
                  if (deserializer == null) {
                     throw new NullPointerException("Null deserializer");
                  } else {
                     this.deserializer = deserializer;
                     if (serializer == null) {
                        throw new NullPointerException("Null serializer");
                     } else {
                        this.serializer = serializer;
                        if (nodeResolver == null) {
                           throw new NullPointerException("Null nodeResolver");
                        } else {
                           this.nodeResolver = nodeResolver;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public String name() {
      return this.name;
   }

   @Override
   public AnnotatedType resolvedType() {
      return this.resolvedType;
   }

   @Override
   List<Constraint<?>> constraints() {
      return this.constraints;
   }

   @Override
   List<Processor<?>> processors() {
      return this.processors;
   }

   @Override
   FieldData.Deserializer<I> deserializer() {
      return this.deserializer;
   }

   @Override
   CheckedFunction<O, Object, Exception> serializer() {
      return this.serializer;
   }

   @Override
   NodeResolver nodeResolver() {
      return this.nodeResolver;
   }

   @Override
   public String toString() {
      return "FieldData{name="
         + this.name
         + ", resolvedType="
         + this.resolvedType
         + ", constraints="
         + this.constraints
         + ", processors="
         + this.processors
         + ", deserializer="
         + this.deserializer
         + ", serializer="
         + this.serializer
         + ", nodeResolver="
         + this.nodeResolver
         + "}";
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FieldData)) {
         return false;
      } else {
         FieldData<?, ?> that = (FieldData<?, ?>)o;
         return this.name.equals(that.name())
            && this.resolvedType.equals(that.resolvedType())
            && this.constraints.equals(that.constraints())
            && this.processors.equals(that.processors())
            && this.deserializer.equals(that.deserializer())
            && this.serializer.equals(that.serializer())
            && this.nodeResolver.equals(that.nodeResolver());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.name.hashCode();
      h$ *= 1000003;
      h$ ^= this.resolvedType.hashCode();
      h$ *= 1000003;
      h$ ^= this.constraints.hashCode();
      h$ *= 1000003;
      h$ ^= this.processors.hashCode();
      h$ *= 1000003;
      h$ ^= this.deserializer.hashCode();
      h$ *= 1000003;
      h$ ^= this.serializer.hashCode();
      h$ *= 1000003;
      return h$ ^ this.nodeResolver.hashCode();
   }
}
