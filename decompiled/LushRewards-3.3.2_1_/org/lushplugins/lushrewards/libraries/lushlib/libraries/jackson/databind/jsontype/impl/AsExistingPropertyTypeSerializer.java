package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;

public class AsExistingPropertyTypeSerializer extends AsPropertyTypeSerializer {
   public AsExistingPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
      super(idRes, property, propName);
   }

   public AsExistingPropertyTypeSerializer forProperty(BeanProperty prop) {
      return this._property == prop ? this : new AsExistingPropertyTypeSerializer(this._idResolver, prop, this._typePropertyName);
   }

   @Override
   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.EXISTING_PROPERTY;
   }
}
