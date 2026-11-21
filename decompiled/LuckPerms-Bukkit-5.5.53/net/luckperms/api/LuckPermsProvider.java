package net.luckperms.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class LuckPermsProvider {
   private static LuckPerms instance = null;

   @NonNull
   public static LuckPerms get() {
      LuckPerms instance = LuckPermsProvider.instance;
      if (instance == null) {
         throw new LuckPermsProvider.NotLoadedException();
      } else {
         return instance;
      }
   }

   @Internal
   static void register(LuckPerms instance) {
      LuckPermsProvider.instance = instance;
   }

   @Internal
   static void unregister() {
      instance = null;
   }

   @Internal
   private LuckPermsProvider() {
      throw new UnsupportedOperationException("This class cannot be instantiated.");
   }

   private static final class NotLoadedException extends IllegalStateException {
      private static final String MESSAGE = "The LuckPerms API isn't loaded yet!\nThis could be because:\n  a) the LuckPerms plugin is not installed or it failed to enable\n  b) the plugin in the stacktrace does not declare a dependency on LuckPerms\n  c) the plugin in the stacktrace is retrieving the API before the plugin 'enable' phase\n     (call the #get method in onEnable, not the constructor!)\n  d) the plugin in the stacktrace is incorrectly 'shading' the LuckPerms API into its jar\n";

      NotLoadedException() {
         super(
            "The LuckPerms API isn't loaded yet!\nThis could be because:\n  a) the LuckPerms plugin is not installed or it failed to enable\n  b) the plugin in the stacktrace does not declare a dependency on LuckPerms\n  c) the plugin in the stacktrace is retrieving the API before the plugin 'enable' phase\n     (call the #get method in onEnable, not the constructor!)\n  d) the plugin in the stacktrace is incorrectly 'shading' the LuckPerms API into its jar\n"
         );
      }
   }
}
