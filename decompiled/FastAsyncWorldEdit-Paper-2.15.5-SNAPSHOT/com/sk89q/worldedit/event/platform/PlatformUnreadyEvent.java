package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.extension.platform.Platform;

public class PlatformUnreadyEvent extends PlatformEvent {
   public PlatformUnreadyEvent(Platform platform) {
      super(platform);
   }
}
