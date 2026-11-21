package org.jdbi.v3.core.argument;

import org.jdbi.v3.core.argument.internal.PojoPropertyArguments;
import org.jdbi.v3.core.argument.internal.TypedValue;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.reflect.internal.BeanPropertiesFactory;

@Deprecated(
   since = "3.6.0"
)
public class BeanPropertyArguments extends PojoPropertyArguments {
   public BeanPropertyArguments(String prefix, Object bean) {
      this(prefix, bean, new ConfigRegistry());
   }

   public BeanPropertyArguments(String prefix, Object bean, ConfigRegistry config) {
      super(prefix, bean, BeanPropertiesFactory.propertiesFor(bean.getClass(), config), config);
   }

   @Override
   protected NamedArgumentFinder getNestedArgumentFinder(TypedValue o) {
      return new BeanPropertyArguments(null, o.getValue(), this.config);
   }
}
