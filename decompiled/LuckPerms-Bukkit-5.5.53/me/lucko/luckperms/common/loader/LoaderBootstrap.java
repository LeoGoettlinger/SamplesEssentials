package me.lucko.luckperms.common.loader;

public interface LoaderBootstrap {
   void onLoad();

   default void onEnable() {
   }

   default void onDisable() {
   }
}
