package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import java.util.Arrays;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class VersionMapper {
   private final ClientVersion[] versions;
   private final ClientVersion[] reversedVersions;

   public VersionMapper(ClientVersion... versions) {
      this.versions = (ClientVersion[])versions.clone();
      Arrays.sort((Object[])this.versions);
      this.reversedVersions = new ClientVersion[this.versions.length];
      int i = this.versions.length - 1;

      for (int j = 0; i >= 0; j++) {
         this.reversedVersions[j] = this.versions[i];
         i--;
      }
   }

   public VersionMapper withExtra(ClientVersion extraStep) {
      if (Arrays.binarySearch(this.versions, extraStep) >= 0) {
         return this;
      } else {
         ClientVersion[] clonedVersions = Arrays.copyOf(this.versions, this.versions.length + 1);
         clonedVersions[clonedVersions.length - 1] = extraStep;
         return new VersionMapper(clonedVersions);
      }
   }

   public ClientVersion[] getVersions() {
      return this.versions;
   }

   public ClientVersion[] getReversedVersions() {
      return this.reversedVersions;
   }

   public int getIndex(ClientVersion version) {
      int index = this.reversedVersions.length - 1;

      for (ClientVersion v : this.reversedVersions) {
         if (version.isNewerThanOrEquals(v)) {
            return index;
         }

         index--;
      }

      return 0;
   }

   public int size() {
      return this.versions.length;
   }
}
