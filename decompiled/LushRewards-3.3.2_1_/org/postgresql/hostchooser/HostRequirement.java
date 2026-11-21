package org.postgresql.hostchooser;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum HostRequirement {
   any {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return status != HostStatus.ConnectFail;
      }
   },
   @Deprecated
   master {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return primary.allowConnectingTo(status);
      }
   },
   primary {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return status == HostStatus.Primary || status == HostStatus.ConnectOK;
      }
   },
   secondary {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return status == HostStatus.Secondary || status == HostStatus.ConnectOK;
      }
   },
   preferSecondary {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return status != HostStatus.ConnectFail;
      }
   },
   preferPrimary {
      @Override
      public boolean allowConnectingTo(@Nullable HostStatus status) {
         return status != HostStatus.ConnectFail;
      }
   };

   private HostRequirement() {
   }

   public abstract boolean allowConnectingTo(@Nullable HostStatus var1);

   public static HostRequirement getTargetServerType(String targetServerType) {
      String allowSlave = targetServerType.replace("lave", "econdary").replace("master", "primary");
      return valueOf(allowSlave);
   }
}
