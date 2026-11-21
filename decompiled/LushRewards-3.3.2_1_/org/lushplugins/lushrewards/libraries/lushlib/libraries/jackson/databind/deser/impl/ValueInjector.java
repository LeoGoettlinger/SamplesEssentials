package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyMetadata;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMember;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Annotations;

public class ValueInjector extends BeanProperty.Std {
   private static final long serialVersionUID = 1L;
   protected final Object _valueId;

   public ValueInjector(PropertyName propName, JavaType type, AnnotatedMember mutator, Object valueId) {
      super(propName, type, null, mutator, PropertyMetadata.STD_OPTIONAL);
      this._valueId = valueId;
   }

   @Deprecated
   public ValueInjector(PropertyName propName, JavaType type, Annotations contextAnnotations, AnnotatedMember mutator, Object valueId) {
      this(propName, type, mutator, valueId);
   }

   public Object findValue(DeserializationContext context, Object beanInstance) throws JsonMappingException {
      return context.findInjectableValue(this._valueId, this, beanInstance);
   }

   public void inject(DeserializationContext context, Object beanInstance) throws IOException {
      this._member.setValue(beanInstance, this.findValue(context, beanInstance));
   }
}
