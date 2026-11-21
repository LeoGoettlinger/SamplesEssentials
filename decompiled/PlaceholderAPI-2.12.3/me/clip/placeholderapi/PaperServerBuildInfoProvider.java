package me.clip.placeholderapi;

import io.papermc.paper.ServerBuildInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PaperServerBuildInfoProvider {
   private PaperServerBuildInfoProvider() {
   }

   @NotNull
   public static ServerVersionResolver.ServerBuild get() {
      ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
      return new ServerVersionResolver.ServerBuild(buildInfo.minecraftVersionId(), buildInfo.buildNumber());
   }
}
