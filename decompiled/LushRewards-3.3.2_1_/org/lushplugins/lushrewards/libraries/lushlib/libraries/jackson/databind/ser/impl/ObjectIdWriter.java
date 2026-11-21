package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.SerializableString;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.SerializedString;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;

public final class ObjectIdWriter {
   public final JavaType idType;
   public final SerializableString propertyName;
   public final ObjectIdGenerator<?> generator;
   public final JsonSerializer<Object> serializer;
   public final boolean alwaysAsId;

   protected ObjectIdWriter(JavaType t, SerializableString propName, ObjectIdGenerator<?> gen, JsonSerializer<?> ser, boolean alwaysAsId) {
      this.idType = t;
      this.propertyName = propName;
      this.generator = gen;
      this.serializer = (JsonSerializer<Object>)ser;
      this.alwaysAsId = alwaysAsId;
   }

   public static ObjectIdWriter construct(JavaType idType, PropertyName propName, ObjectIdGenerator<?> generator, boolean alwaysAsId) {
      String simpleName = propName == null ? null : propName.getSimpleName();
      SerializableString serName = simpleName == null ? null : new SerializedString(simpleName);
      return new ObjectIdWriter(idType, serName, generator, null, alwaysAsId);
   }

   public ObjectIdWriter withSerializer(JsonSerializer<?> ser) {
      return new ObjectIdWriter(this.idType, this.propertyName, this.generator, ser, this.alwaysAsId);
   }

   public ObjectIdWriter withAlwaysAsId(boolean newState) {
      return newState == this.alwaysAsId ? this : new ObjectIdWriter(this.idType, this.propertyName, this.generator, this.serializer, newState);
   }
}
