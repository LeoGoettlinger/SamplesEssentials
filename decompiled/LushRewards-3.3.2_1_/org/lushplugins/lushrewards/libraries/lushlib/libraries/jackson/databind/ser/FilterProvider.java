package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public abstract class FilterProvider {
   @Deprecated
   public abstract BeanPropertyFilter findFilter(Object var1);

   public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
      BeanPropertyFilter old = this.findFilter(filterId);
      return old == null ? null : SimpleBeanPropertyFilter.from(old);
   }
}
