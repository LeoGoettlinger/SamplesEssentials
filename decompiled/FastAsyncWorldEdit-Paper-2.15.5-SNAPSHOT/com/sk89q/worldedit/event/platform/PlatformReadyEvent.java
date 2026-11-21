package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.extension.platform.Platform;

public class PlatformReadyEvent extends PlatformEvent {
   public PlatformReadyEvent(Platform platform) {
      super(platform);
   }
}
