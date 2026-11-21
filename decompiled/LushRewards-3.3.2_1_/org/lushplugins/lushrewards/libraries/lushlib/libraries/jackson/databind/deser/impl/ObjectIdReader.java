package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.SettableBeanProperty;

public class ObjectIdReader implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final JavaType _idType;
   public final PropertyName propertyName;
   public final ObjectIdGenerator<?> generator;
   public final ObjectIdResolver resolver;
   protected final JsonDeserializer<Object> _deserializer;
   public final SettableBeanProperty idProperty;

   protected ObjectIdReader(
      JavaType t, PropertyName propName, ObjectIdGenerator<?> gen, JsonDeserializer<?> deser, SettableBeanProperty idProp, ObjectIdResolver resolver
   ) {
      this._idType = t;
      this.propertyName = propName;
      this.generator = gen;
      this.resolver = resolver;
      this._deserializer = (JsonDeserializer<Object>)deser;
      this.idProperty = idProp;
   }

   public static ObjectIdReader construct(
      JavaType idType, PropertyName propName, ObjectIdGenerator<?> generator, JsonDeserializer<?> deser, SettableBeanProperty idProp, ObjectIdResolver resolver
   ) {
      return new ObjectIdReader(idType, propName, generator, deser, idProp, resolver);
   }

   public JsonDeserializer<Object> getDeserializer() {
      return this._deserializer;
   }

   public JavaType getIdType() {
      return this._idType;
   }

   public boolean maySerializeAsObject() {
      return this.generator.maySerializeAsObject();
   }

   public boolean isValidReferencePropertyName(String name, JsonParser parser) {
      return this.generator.isValidReferencePropertyName(name, parser);
   }

   public Object readObjectReference(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return this._deserializer.deserialize(jp, ctxt);
   }
}
