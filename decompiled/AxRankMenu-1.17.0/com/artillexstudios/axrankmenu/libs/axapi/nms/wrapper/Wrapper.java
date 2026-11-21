package com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper;

public interface Wrapper<T> {
   default void update() {
      this.update(false);
   }

   void update(boolean force);

   T wrapped();

   Object asMinecraft();
}
