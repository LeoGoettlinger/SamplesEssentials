package com.viaversion.viaversion.api.protocol.version;

import com.google.common.base.Preconditions;

public record SubVersionRange(String baseVersion, int rangeFrom, int rangeTo) {
   public SubVersionRange(String baseVersion, int rangeFrom, int rangeTo) {
      Preconditions.checkNotNull(baseVersion);
      Preconditions.checkArgument(rangeFrom >= 0);
      Preconditions.checkArgument(rangeTo > rangeFrom);
      this.baseVersion = baseVersion;
      this.rangeFrom = rangeFrom;
      this.rangeTo = rangeTo;
   }
}
