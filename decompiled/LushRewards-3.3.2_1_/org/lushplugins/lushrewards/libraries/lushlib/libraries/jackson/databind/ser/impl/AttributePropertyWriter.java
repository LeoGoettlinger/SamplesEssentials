package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.VirtualBeanPropertyWriter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Annotations;

public class AttributePropertyWriter extends VirtualBeanPropertyWriter {
   private static final long serialVersionUID = 1L;
   protected final String _attrName;

   protected AttributePropertyWriter(String attrName, BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
      this(attrName, propDef, contextAnnotations, declaredType, propDef.findInclusion());
   }

   protected AttributePropertyWriter(
      String attrName, BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType, JsonInclude.Value inclusion
   ) {
      super(propDef, contextAnnotations, declaredType, null, null, null, inclusion, null);
      this._attrName = attrName;
   }

   public static AttributePropertyWriter construct(String attrName, BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
      return new AttributePropertyWriter(attrName, propDef, contextAnnotations, declaredType);
   }

   protected AttributePropertyWriter(AttributePropertyWriter base) {
      super(base);
      this._attrName = base._attrName;
   }

   @Override
   public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
      throw new IllegalStateException("Should not be called on this type");
   }

   @Override
   protected Object value(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception {
      return prov.getAttribute(this._attrName);
   }
}
