package com.viaversion.viaversion.api.platform;

public interface ViaPlatformLoader {
   ViaPlatformLoader NOOP = new ViaPlatformLoader() {
      @Override
      public void load() {
      }

      @Override
      public void unload() {
      }
   };

   void load();

   void unload();
}
