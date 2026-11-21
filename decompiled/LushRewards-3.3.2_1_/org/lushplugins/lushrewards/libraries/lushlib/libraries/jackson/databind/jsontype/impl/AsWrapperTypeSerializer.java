package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public class AsWrapperTypeSerializer extends TypeSerializerBase {
   public AsWrapperTypeSerializer(TypeIdResolver idRes, BeanProperty property) {
      super(idRes, property);
   }

   public AsWrapperTypeSerializer forProperty(BeanProperty prop) {
      return this._property == prop ? this : new AsWrapperTypeSerializer(this._idResolver, prop);
   }

   @Override
   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.WRAPPER_OBJECT;
   }

   protected String _validTypeId(String typeId) {
      return ClassUtil.nonNullString(typeId);
   }

   protected final void _writeTypeId(JsonGenerator g, String typeId) throws IOException {
      if (typeId != null) {
         g.writeTypeId(typeId);
      }
   }
}
