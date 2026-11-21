package org.lushplugins.lushrewards.libraries.mysql.cj.conf;

import java.util.Arrays;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class EnumPropertyDefinition<T extends Enum<T>> extends AbstractPropertyDefinition<T> {
   private static final long serialVersionUID = -3297521968759540444L;
   private Class<T> enumType;

   public EnumPropertyDefinition(
      PropertyKey key, T defaultValue, boolean isRuntimeModifiable, String description, String sinceVersion, String category, int orderInCategory
   ) {
      super(key, defaultValue, isRuntimeModifiable, description, sinceVersion, category, orderInCategory);
      if (defaultValue == null) {
         throw ExceptionFactory.createException("Enum property '" + key.getKeyName() + "' cannot be initialized with null.");
      } else {
         this.enumType = defaultValue.getDeclaringClass();
      }
   }

   @Override
   public String[] getAllowableValues() {
      return Arrays.<Enum>stream(this.enumType.getEnumConstants()).map(Enum::toString).sorted().toArray(String[]::new);
   }

   public T parseObject(String value, ExceptionInterceptor exceptionInterceptor) {
      try {
         return Enum.valueOf(this.enumType, value.toUpperCase());
      } catch (Exception var4) {
         throw ExceptionFactory.createException(
            Messages.getString(
               "PropertyDefinition.1",
               new Object[]{this.getName(), StringUtils.stringArrayToString(this.getAllowableValues(), "'", "', '", "' or '", "'"), value}
            ),
            var4,
            exceptionInterceptor
         );
      }
   }

   @Override
   public RuntimeProperty<T> createRuntimeProperty() {
      return new EnumProperty<>(this);
   }
}
