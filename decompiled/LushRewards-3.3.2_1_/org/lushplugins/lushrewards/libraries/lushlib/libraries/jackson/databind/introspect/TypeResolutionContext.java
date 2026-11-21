package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect;

import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeBindings;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;

public interface TypeResolutionContext {
   JavaType resolveType(Type var1);

   public static class Basic implements TypeResolutionContext {
      private final TypeFactory _typeFactory;
      private final TypeBindings _bindings;

      public Basic(TypeFactory tf, TypeBindings b) {
         this._typeFactory = tf;
         this._bindings = b;
      }

      @Override
      public JavaType resolveType(Type type) {
         return this._typeFactory.resolveMemberType(type, this._bindings);
      }
   }

   public static class Empty implements TypeResolutionContext {
      private final TypeFactory _typeFactory;

      public Empty(TypeFactory tf) {
         this._typeFactory = tf;
      }

      @Override
      public JavaType resolveType(Type type) {
         return this._typeFactory.constructType(type);
      }
   }
}
