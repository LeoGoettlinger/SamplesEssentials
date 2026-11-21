package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyMetadata;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.ConcreteBeanPropertyBase;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;

public abstract class PropertyWriter extends ConcreteBeanPropertyBase implements Serializable {
   private static final long serialVersionUID = 1L;

   protected PropertyWriter(PropertyMetadata md) {
      super(md);
   }

   protected PropertyWriter(BeanPropertyDefinition propDef) {
      super(propDef.getMetadata());
   }

   protected PropertyWriter(PropertyWriter base) {
      super(base);
   }

   @Override
   public abstract String getName();

   @Override
   public abstract PropertyName getFullName();

   public <A extends Annotation> A findAnnotation(Class<A> acls) {
      A ann = this.getAnnotation(acls);
      if (ann == null) {
         ann = this.getContextAnnotation(acls);
      }

      return ann;
   }

   @Override
   public abstract <A extends Annotation> A getAnnotation(Class<A> var1);

   @Override
   public abstract <A extends Annotation> A getContextAnnotation(Class<A> var1);

   public abstract void serializeAsField(Object var1, JsonGenerator var2, SerializerProvider var3) throws Exception;

   public abstract void serializeAsOmittedField(Object var1, JsonGenerator var2, SerializerProvider var3) throws Exception;

   public abstract void serializeAsElement(Object var1, JsonGenerator var2, SerializerProvider var3) throws Exception;

   public abstract void serializeAsPlaceholder(Object var1, JsonGenerator var2, SerializerProvider var3) throws Exception;

   @Override
   public abstract void depositSchemaProperty(JsonObjectFormatVisitor var1, SerializerProvider var2) throws JsonMappingException;

   @Deprecated
   public abstract void depositSchemaProperty(ObjectNode var1, SerializerProvider var2) throws JsonMappingException;
}
