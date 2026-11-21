package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;

public class InvalidDefinitionException extends JsonMappingException {
   protected final JavaType _type;
   protected transient BeanDescription _beanDesc;
   protected transient BeanPropertyDefinition _property;

   protected InvalidDefinitionException(JsonParser p, String msg, JavaType type) {
      super(p, msg);
      this._type = type;
      this._beanDesc = null;
      this._property = null;
   }

   protected InvalidDefinitionException(JsonGenerator g, String msg, JavaType type) {
      super(g, msg);
      this._type = type;
      this._beanDesc = null;
      this._property = null;
   }

   protected InvalidDefinitionException(JsonParser p, String msg, BeanDescription bean, BeanPropertyDefinition prop) {
      super(p, msg);
      this._type = bean == null ? null : bean.getType();
      this._beanDesc = bean;
      this._property = prop;
   }

   protected InvalidDefinitionException(JsonGenerator g, String msg, BeanDescription bean, BeanPropertyDefinition prop) {
      super(g, msg);
      this._type = bean == null ? null : bean.getType();
      this._beanDesc = bean;
      this._property = prop;
   }

   public static InvalidDefinitionException from(JsonParser p, String msg, BeanDescription bean, BeanPropertyDefinition prop) {
      return new InvalidDefinitionException(p, msg, bean, prop);
   }

   public static InvalidDefinitionException from(JsonParser p, String msg, JavaType type) {
      return new InvalidDefinitionException(p, msg, type);
   }

   public static InvalidDefinitionException from(JsonGenerator g, String msg, BeanDescription bean, BeanPropertyDefinition prop) {
      return new InvalidDefinitionException(g, msg, bean, prop);
   }

   public static InvalidDefinitionException from(JsonGenerator g, String msg, JavaType type) {
      return new InvalidDefinitionException(g, msg, type);
   }

   public JavaType getType() {
      return this._type;
   }

   public BeanDescription getBeanDescription() {
      return this._beanDesc;
   }

   public BeanPropertyDefinition getProperty() {
      return this._property;
   }
}
