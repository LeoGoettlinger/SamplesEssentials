package org.lushplugins.lushrewards.utils.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class UserDataExclusionStrategy implements ExclusionStrategy {
   public boolean shouldSkipField(FieldAttributes field) {
      return field.getName().equals("uuid") || field.getName().equals("moduleId");
   }

   public boolean shouldSkipClass(Class<?> clazz) {
      return false;
   }
}
