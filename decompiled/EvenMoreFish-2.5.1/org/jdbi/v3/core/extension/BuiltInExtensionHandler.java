package org.jdbi.v3.core.extension;

import org.jdbi.v3.core.config.ConfigRegistry;

enum BuiltInExtensionHandler implements ExtensionHandler {
   EQUALS_HANDLER {
      @Override
      public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
         return (handleSupplier, args) -> target == args[0];
      }
   },
   HASHCODE_HANDLER {
      @Override
      public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
         return (handleSupplier, args) -> System.identityHashCode(target);
      }
   },
   NULL_HANDLER {
      @Override
      public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
         return (handleSupplier, args) -> null;
      }
   };
}
