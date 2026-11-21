package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;

public class AsPropertyTypeSerializer extends AsArrayTypeSerializer {
   protected final String _typePropertyName;

   public AsPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
      super(idRes, property);
      this._typePropertyName = propName;
   }

   public AsPropertyTypeSerializer forProperty(BeanProperty prop) {
      return this._property == prop ? this : new AsPropertyTypeSerializer(this._idResolver, prop, this._typePropertyName);
   }

   @Override
   public String getPropertyName() {
      return this._typePropertyName;
   }

   @Override
   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.PROPERTY;
   }
}
