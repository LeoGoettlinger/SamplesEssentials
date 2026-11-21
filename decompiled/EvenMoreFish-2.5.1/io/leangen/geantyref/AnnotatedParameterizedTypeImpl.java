package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;

class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl implements AnnotatedParameterizedType {
   private final AnnotatedType[] typeArguments;

   AnnotatedParameterizedTypeImpl(ParameterizedType rawType, Annotation[] annotations, AnnotatedType[] typeArguments, AnnotatedType ownerType) {
      super(rawType, annotations);
      this.typeArguments = typeArguments;
      this.ownerType = ownerType;
   }

   @Override
   public AnnotatedType[] getAnnotatedActualTypeArguments() {
      return this.typeArguments;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return other instanceof AnnotatedParameterizedType && super.equals(other)
            ? GenericTypeReflector.typeArraysEqual(this.typeArguments, ((AnnotatedParameterizedType)other).getAnnotatedActualTypeArguments())
            : false;
      }
   }

   @Override
   public int hashCode() {
      return 127 * super.hashCode() ^ GenericTypeReflector.hashCode(this.typeArguments);
   }

   @Override
   public String toString() {
      ParameterizedType rawType = (ParameterizedType)this.type;
      String rawName = GenericTypeReflector.getTypeName(rawType.getRawType());
      StringBuilder typeName = new StringBuilder();
      if (this.ownerType != null) {
         typeName.append(this.ownerType).append('$');
         String prefix = rawType.getOwnerType() instanceof ParameterizedType
            ? ((Class)((ParameterizedType)rawType.getOwnerType()).getRawType()).getName() + "$"
            : ((Class)rawType.getOwnerType()).getName() + "$";
         if (rawName.startsWith(prefix)) {
            rawName = rawName.substring(prefix.length());
         }
      }

      typeName.append(rawName);
      return this.annotationsString()
         + typeName
         + (this.typeArguments != null && this.typeArguments.length > 0 ? "<" + this.typesString(this.typeArguments) + ">" : "");
   }
}
