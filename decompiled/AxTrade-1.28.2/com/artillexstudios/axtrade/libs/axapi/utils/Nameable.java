package com.artillexstudios.axtrade.libs.axapi.utils;

import com.artillexstudios.axtrade.libs.axapi.dependency.DependencyContainer;

public interface Nameable {
   static Nameable getInstance() {
      return DependencyContainer.getInstance(Nameable.class);
   }

   String getName();
}
