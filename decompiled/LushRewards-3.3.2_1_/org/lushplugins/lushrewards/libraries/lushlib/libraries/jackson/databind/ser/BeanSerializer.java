package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import java.io.IOException;
import java.util.Set;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl.BeanAsArraySerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl.ObjectIdWriter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.BeanSerializerBase;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.NameTransformer;

public class BeanSerializer extends BeanSerializerBase {
   private static final long serialVersionUID = 29L;

   public BeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
      super(type, builder, properties, filteredProperties);
   }

   protected BeanSerializer(BeanSerializerBase src) {
      super(src);
   }

   protected BeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
      super(src, objectIdWriter);
   }

   protected BeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
      super(src, objectIdWriter, filterId);
   }

   protected BeanSerializer(BeanSerializerBase src, Set<String> toIgnore, Set<String> toInclude) {
      super(src, toIgnore, toInclude);
   }

   protected BeanSerializer(BeanSerializerBase src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
      super(src, properties, filteredProperties);
   }

   @Deprecated
   public static BeanSerializer createDummy(JavaType forType) {
      return new BeanSerializer(forType, null, NO_PROPS, null);
   }

   public static BeanSerializer createDummy(JavaType forType, BeanSerializerBuilder builder) {
      return new BeanSerializer(forType, builder, NO_PROPS, null);
   }

   @Override
   public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
      return new UnwrappingBeanSerializer(this, unwrapper);
   }

   @Override
   public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
      return new BeanSerializer(this, objectIdWriter, this._propertyFilterId);
   }

   @Override
   public BeanSerializerBase withFilterId(Object filterId) {
      return new BeanSerializer(this, this._objectIdWriter, filterId);
   }

   @Override
   protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
      return new BeanSerializer(this, toIgnore, toInclude);
   }

   @Override
   protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
      return new BeanSerializer(this, properties, filteredProperties);
   }

   @Override
   protected BeanSerializerBase asArraySerializer() {
      return (BeanSerializerBase)(this._objectIdWriter == null && this._anyGetterWriter == null && this._propertyFilterId == null
         ? new BeanAsArraySerializer(this)
         : this);
   }

   @Override
   public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
      if (this._objectIdWriter != null) {
         gen.setCurrentValue(bean);
         this._serializeWithObjectId(bean, gen, provider, true);
      } else {
         gen.writeStartObject(bean);
         if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
         } else {
            this.serializeFields(bean, gen, provider);
         }

         gen.writeEndObject();
      }
   }

   @Override
   public String toString() {
      return "BeanSerializer for " + this.handledType().getName();
   }
}
