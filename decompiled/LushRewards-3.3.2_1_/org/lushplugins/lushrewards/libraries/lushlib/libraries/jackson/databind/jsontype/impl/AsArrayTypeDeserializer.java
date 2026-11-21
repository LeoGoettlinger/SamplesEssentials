package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util.JsonParserSequence;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.TokenBuffer;

public class AsArrayTypeDeserializer extends TypeDeserializerBase implements Serializable {
   private static final long serialVersionUID = 1L;

   public AsArrayTypeDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
      super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
   }

   public AsArrayTypeDeserializer(AsArrayTypeDeserializer src, BeanProperty property) {
      super(src, property);
   }

   @Override
   public TypeDeserializer forProperty(BeanProperty prop) {
      return prop == this._property ? this : new AsArrayTypeDeserializer(this, prop);
   }

   @Override
   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.WRAPPER_ARRAY;
   }

   @Override
   public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return this._deserialize(jp, ctxt);
   }

   @Override
   public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return this._deserialize(jp, ctxt);
   }

   @Override
   public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return this._deserialize(jp, ctxt);
   }

   @Override
   public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return this._deserialize(jp, ctxt);
   }

   protected Object _deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.canReadTypeId()) {
         Object typeId = p.getTypeId();
         if (typeId != null) {
            return this._deserializeWithNativeTypeId(p, ctxt, typeId);
         }
      }

      boolean hadStartArray = p.isExpectedStartArrayToken();
      String typeId = this._locateTypeId(p, ctxt);
      JsonDeserializer<Object> deser = this._findDeserializer(ctxt, typeId);
      if (this._typeIdVisible && !this._usesExternalId() && p.hasToken(JsonToken.START_OBJECT)) {
         TokenBuffer tb = ctxt.bufferForInputBuffering(p);
         tb.writeStartObject();
         tb.writeFieldName(this._typePropertyName);
         tb.writeString(typeId);
         p.clearCurrentToken();
         p = JsonParserSequence.createFlattened(false, tb.asParser(p), p);
         p.nextToken();
      }

      if (hadStartArray && p.currentToken() == JsonToken.END_ARRAY) {
         return deser.getNullValue(ctxt);
      } else {
         Object value = deser.deserialize(p, ctxt);
         if (hadStartArray && p.nextToken() != JsonToken.END_ARRAY) {
            ctxt.reportWrongTokenException(
               this.baseType(), JsonToken.END_ARRAY, "expected closing `JsonToken.END_ARRAY` after type information and deserialized value"
            );
         }

         return value;
      }
   }

   protected String _locateTypeId(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
         if (this._defaultImpl != null) {
            return this._idResolver.idFromBaseType();
         } else {
            ctxt.reportWrongTokenException(
               this.baseType(), JsonToken.START_ARRAY, "need Array value to contain `As.WRAPPER_ARRAY` type information for class " + this.baseTypeName()
            );
            return null;
         }
      } else {
         JsonToken t = p.nextToken();
         if (t != JsonToken.VALUE_STRING && (t == null || !t.isScalarValue())) {
            ctxt.reportWrongTokenException(
               this.baseType(), JsonToken.VALUE_STRING, "need String, Number of Boolean value that contains type id (for subtype of %s)", this.baseTypeName()
            );
            return null;
         } else {
            String result = p.getText();
            p.nextToken();
            return result;
         }
      }
   }

   protected boolean _usesExternalId() {
      return false;
   }
}
