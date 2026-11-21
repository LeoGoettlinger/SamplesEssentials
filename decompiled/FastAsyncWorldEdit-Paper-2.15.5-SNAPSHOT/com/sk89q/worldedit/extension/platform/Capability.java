package com.sk89q.worldedit.extension.platform;

public enum Capability {
   GAME_HOOKS {
      @Override
      void initialize(PlatformManager platformManager, Platform platform) {
         platform.setGameHooksEnabled(true);
      }

      @Override
      void uninitialize(PlatformManager platformManager, Platform platform) {
         platform.setGameHooksEnabled(false);
      }
   },
   CONFIGURATION,
   USER_COMMANDS {
      @Override
      void initialize(PlatformManager platformManager, Platform platform) {
         platformManager.getPlatformCommandManager().registerCommandsWith(platform);
      }

      @Override
      void uninitialize(PlatformManager platformManager, Platform platform) {
         platformManager.getPlatformCommandManager().removeCommands();
      }
   },
   PERMISSIONS,
   WORLDEDIT_CUI,
   WORLD_EDITING {};

   void initialize(PlatformManager platformManager, Platform platform) {
   }

   void uninitialize(PlatformManager platformManager, Platform platform) {
   }

   void ready(PlatformManager platformManager, Platform platform) {
   }

   void unready(PlatformManager platformManager, Platform platform) {
   }
}
