package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DatabindContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;

public class MinimalClassNameIdResolver extends ClassNameIdResolver {
   protected final String _basePackageName;
   protected final String _basePackagePrefix;

   protected MinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory, PolymorphicTypeValidator ptv) {
      super(baseType, typeFactory, ptv);
      String base = baseType.getRawClass().getName();
      int ix = base.lastIndexOf(46);
      if (ix < 0) {
         this._basePackageName = "";
         this._basePackagePrefix = ".";
      } else {
         this._basePackagePrefix = base.substring(0, ix + 1);
         this._basePackageName = base.substring(0, ix);
      }
   }

   public static MinimalClassNameIdResolver construct(JavaType baseType, MapperConfig<?> config, PolymorphicTypeValidator ptv) {
      return new MinimalClassNameIdResolver(baseType, config.getTypeFactory(), ptv);
   }

   @Override
   public JsonTypeInfo.Id getMechanism() {
      return JsonTypeInfo.Id.MINIMAL_CLASS;
   }

   @Override
   public String idFromValue(Object value) {
      String n = value.getClass().getName();
      return n.startsWith(this._basePackagePrefix) ? n.substring(this._basePackagePrefix.length() - 1) : n;
   }

   @Override
   protected JavaType _typeFromId(String id, DatabindContext ctxt) throws IOException {
      if (id.startsWith(".")) {
         StringBuilder sb = new StringBuilder(id.length() + this._basePackageName.length());
         if (this._basePackageName.isEmpty()) {
            sb.append(id.substring(1));
         } else {
            sb.append(this._basePackageName).append(id);
         }

         id = sb.toString();
      }

      return super._typeFromId(id, ctxt);
   }
}
