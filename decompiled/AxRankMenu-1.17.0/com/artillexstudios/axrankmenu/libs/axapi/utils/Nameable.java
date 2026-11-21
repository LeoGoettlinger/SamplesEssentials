package com.artillexstudios.axrankmenu.libs.axapi.utils;

import com.artillexstudios.axrankmenu.libs.axapi.dependency.DependencyContainer;

public interface Nameable {
   static Nameable getInstance() {
      return DependencyContainer.getInstance(Nameable.class);
   }

   String getName();
}
