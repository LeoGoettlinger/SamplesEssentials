package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.BeanPropertyWriter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.BeanSerializerBase;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.NameTransformer;

public class UnwrappingBeanSerializer extends BeanSerializerBase implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final NameTransformer _nameTransformer;

   public UnwrappingBeanSerializer(BeanSerializerBase src, NameTransformer transformer) {
      super(src, transformer);
      this._nameTransformer = transformer;
   }

   public UnwrappingBeanSerializer(UnwrappingBeanSerializer src, ObjectIdWriter objectIdWriter) {
      super(src, objectIdWriter);
      this._nameTransformer = src._nameTransformer;
   }

   public UnwrappingBeanSerializer(UnwrappingBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
      super(src, objectIdWriter, filterId);
      this._nameTransformer = src._nameTransformer;
   }

   protected UnwrappingBeanSerializer(UnwrappingBeanSerializer src, Set<String> toIgnore) {
      this(src, toIgnore, null);
   }

   protected UnwrappingBeanSerializer(UnwrappingBeanSerializer src, Set<String> toIgnore, Set<String> toInclude) {
      super(src, toIgnore, toInclude);
      this._nameTransformer = src._nameTransformer;
   }

   protected UnwrappingBeanSerializer(UnwrappingBeanSerializer src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
      super(src, properties, filteredProperties);
      this._nameTransformer = src._nameTransformer;
   }

   @Override
   public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
      return new UnwrappingBeanSerializer(this, transformer);
   }

   @Override
   public boolean isUnwrappingSerializer() {
      return true;
   }

   @Override
   public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
      return new UnwrappingBeanSerializer(this, objectIdWriter);
   }

   @Override
   public BeanSerializerBase withFilterId(Object filterId) {
      return new UnwrappingBeanSerializer(this, this._objectIdWriter, filterId);
   }

   @Override
   protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
      return new UnwrappingBeanSerializer(this, toIgnore, toInclude);
   }

   @Override
   protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
      return new UnwrappingBeanSerializer(this, properties, filteredProperties);
   }

   @Override
   protected BeanSerializerBase asArraySerializer() {
      return this;
   }

   @Override
   public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.setCurrentValue(bean);
      if (this._objectIdWriter != null) {
         this._serializeWithObjectId(bean, gen, provider, false);
      } else {
         if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
         } else {
            this.serializeFields(bean, gen, provider);
         }
      }
   }

   @Override
   public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      if (provider.isEnabled(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)) {
         provider.reportBadDefinition(
            this.handledType(),
            "Unwrapped property requires use of type information: cannot serialize without disabling `SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS`"
         );
      }

      gen.setCurrentValue(bean);
      if (this._objectIdWriter != null) {
         this._serializeWithObjectId(bean, gen, provider, typeSer);
      } else {
         if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
         } else {
            this.serializeFields(bean, gen, provider);
         }
      }
   }

   @Override
   public String toString() {
      return "UnwrappingBeanSerializer for " + this.handledType().getName();
   }
}
