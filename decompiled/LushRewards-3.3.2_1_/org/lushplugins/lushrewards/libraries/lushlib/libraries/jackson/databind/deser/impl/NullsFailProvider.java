package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.NullValueProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.exc.InvalidNullException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.AccessPattern;

public class NullsFailProvider implements NullValueProvider, Serializable {
   private static final long serialVersionUID = 1L;
   protected final PropertyName _name;
   protected final JavaType _type;

   protected NullsFailProvider(PropertyName name, JavaType type) {
      this._name = name;
      this._type = type;
   }

   public static NullsFailProvider constructForProperty(BeanProperty prop) {
      return constructForProperty(prop, prop.getType());
   }

   public static NullsFailProvider constructForProperty(BeanProperty prop, JavaType type) {
      return new NullsFailProvider(prop.getFullName(), type);
   }

   public static NullsFailProvider constructForRootValue(JavaType t) {
      return new NullsFailProvider(null, t);
   }

   @Override
   public AccessPattern getNullAccessPattern() {
      return AccessPattern.DYNAMIC;
   }

   @Override
   public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      throw InvalidNullException.from(ctxt, this._name, this._type);
   }
}
