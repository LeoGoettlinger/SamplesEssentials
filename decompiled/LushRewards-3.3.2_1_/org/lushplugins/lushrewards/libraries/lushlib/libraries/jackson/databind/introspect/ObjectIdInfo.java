package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.SimpleObjectIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public class ObjectIdInfo {
   protected final PropertyName _propertyName;
   protected final Class<? extends ObjectIdGenerator<?>> _generator;
   protected final Class<? extends ObjectIdResolver> _resolver;
   protected final Class<?> _scope;
   protected final boolean _alwaysAsId;
   private static final ObjectIdInfo EMPTY = new ObjectIdInfo(PropertyName.NO_NAME, Object.class, null, false, null);

   public ObjectIdInfo(PropertyName name, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen, Class<? extends ObjectIdResolver> resolver) {
      this(name, scope, gen, false, resolver);
   }

   protected ObjectIdInfo(PropertyName prop, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen, boolean alwaysAsId) {
      this(prop, scope, gen, alwaysAsId, SimpleObjectIdResolver.class);
   }

   protected ObjectIdInfo(
      PropertyName prop, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen, boolean alwaysAsId, Class<? extends ObjectIdResolver> resolver
   ) {
      this._propertyName = prop;
      this._scope = scope;
      this._generator = gen;
      this._alwaysAsId = alwaysAsId;
      if (resolver == null) {
         resolver = SimpleObjectIdResolver.class;
      }

      this._resolver = resolver;
   }

   public static ObjectIdInfo empty() {
      return EMPTY;
   }

   public ObjectIdInfo withAlwaysAsId(boolean state) {
      return this._alwaysAsId == state ? this : new ObjectIdInfo(this._propertyName, this._scope, this._generator, state, this._resolver);
   }

   public PropertyName getPropertyName() {
      return this._propertyName;
   }

   public Class<?> getScope() {
      return this._scope;
   }

   public Class<? extends ObjectIdGenerator<?>> getGeneratorType() {
      return this._generator;
   }

   public Class<? extends ObjectIdResolver> getResolverType() {
      return this._resolver;
   }

   public boolean getAlwaysAsId() {
      return this._alwaysAsId;
   }

   @Override
   public String toString() {
      return "ObjectIdInfo: propName="
         + this._propertyName
         + ", scope="
         + ClassUtil.nameOf(this._scope)
         + ", generatorType="
         + ClassUtil.nameOf(this._generator)
         + ", alwaysAsId="
         + this._alwaysAsId;
   }
}
