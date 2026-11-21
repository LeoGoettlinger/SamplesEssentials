package github.nighter.smartspawner.libs.mariadb.export;

import github.nighter.smartspawner.libs.mariadb.HostAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public enum HaMode {
   REPLICATION("replication") {
      @Override
      public Optional<HostAddress> getAvailableHost(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
         HostAddress hostWithLessConnection = getHostWithLessConnections(hostAddresses, denyList, primary);
         return hostWithLessConnection != null
            ? Optional.of(hostWithLessConnection)
            : HaMode.getAvailableRoundRobinHost(this, hostAddresses, denyList, primary);
      }
   },
   SEQUENTIAL("sequential") {
      @Override
      public Optional<HostAddress> getAvailableHost(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
         return getAvailableHostInOrder(hostAddresses, denyList, primary);
      }
   },
   LOAD_BALANCE_READ("load-balance-read") {
      @Override
      public Optional<HostAddress> getAvailableHost(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
         return primary ? SEQUENTIAL.getAvailableHost(hostAddresses, denyList, true) : LOADBALANCE.getAvailableHost(hostAddresses, denyList, false);
      }
   },
   LOADBALANCE("load-balance") {
      @Override
      public Optional<HostAddress> getAvailableHost(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
         HostAddress hostWithLessConnection = getHostWithLessConnections(hostAddresses, denyList, primary);
         return hostWithLessConnection != null
            ? Optional.of(hostWithLessConnection)
            : HaMode.getAvailableRoundRobinHost(this, hostAddresses, denyList, primary);
      }
   },
   NONE("") {
      @Override
      public Optional<HostAddress> getAvailableHost(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
         return hostAddresses.isEmpty() ? Optional.empty() : Optional.of(hostAddresses.get(0));
      }
   };

   private final String value;
   private HostAddress lastRoundRobinPrimaryHost = null;
   private HostAddress lastRoundRobinSecondaryHost = null;

   private HaMode(String value) {
      this.value = value;
   }

   public static HaMode from(String value) {
      for (HaMode haMode : values()) {
         if (haMode.value.equalsIgnoreCase(value) || haMode.name().equalsIgnoreCase(value)) {
            return haMode;
         }
      }

      throw new IllegalArgumentException(String.format("Wrong argument value '%s' for HaMode", value));
   }

   public static Optional<HostAddress> getAvailableHostInOrder(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
      for (HostAddress hostAddress : hostAddresses) {
         if (hostAddress.primary == primary) {
            Long deniedTimeout = denyList.get(hostAddress);
            if (deniedTimeout == null) {
               return Optional.of(hostAddress);
            }

            if (deniedTimeout < System.currentTimeMillis()) {
               denyList.remove(hostAddress, deniedTimeout);
               return Optional.of(hostAddress);
            }
         }
      }

      return Optional.empty();
   }

   public static HostAddress getHostWithLessConnections(List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary) {
      long currentTime = System.currentTimeMillis();
      HostAddress hostAddressWithLessConnections = null;
      Iterator var6 = hostAddresses.iterator();

      while (true) {
         HostAddress hostAddress;
         while (true) {
            if (!var6.hasNext()) {
               return hostAddressWithLessConnections;
            }

            hostAddress = (HostAddress)var6.next();
            if (hostAddress.primary == primary) {
               Long deniedTimeout = denyList.get(hostAddress);
               if (deniedTimeout == null) {
                  break;
               }

               if (deniedTimeout <= System.currentTimeMillis()) {
                  denyList.remove(hostAddress, deniedTimeout);
                  break;
               }
            }
         }

         if (hostAddress.getThreadConnectedTimeout() == null || hostAddress.getThreadConnectedTimeout() < currentTime) {
            return null;
         }

         if (hostAddressWithLessConnections == null || hostAddressWithLessConnections.getThreadsConnected() > hostAddress.getThreadsConnected()) {
            hostAddressWithLessConnections = hostAddress;
         }
      }
   }

   public static Optional<HostAddress> getAvailableRoundRobinHost(
      HaMode haMode, List<HostAddress> hostAddresses, ConcurrentMap<HostAddress, Long> denyList, boolean primary
   ) {
      HostAddress lastChosenHost = primary ? haMode.lastRoundRobinPrimaryHost : haMode.lastRoundRobinSecondaryHost;
      List<HostAddress> loopList;
      if (lastChosenHost == null) {
         loopList = hostAddresses;
      } else {
         int lastChosenIndex = hostAddresses.indexOf(lastChosenHost);
         loopList = new ArrayList<>();
         loopList.addAll(hostAddresses.subList(lastChosenIndex + 1, hostAddresses.size()));
         loopList.addAll(hostAddresses.subList(0, lastChosenIndex + 1));
      }

      Iterator var9 = loopList.iterator();

      HostAddress hostAddress;
      while (true) {
         if (!var9.hasNext()) {
            return Optional.empty();
         }

         hostAddress = (HostAddress)var9.next();
         if (hostAddress.primary == primary) {
            Long deniedTimeout = denyList.get(hostAddress);
            if (deniedTimeout == null) {
               break;
            }

            if (deniedTimeout <= System.currentTimeMillis()) {
               denyList.remove(hostAddress, deniedTimeout);
               break;
            }
         }
      }

      if (primary) {
         haMode.lastRoundRobinPrimaryHost = hostAddress;
      } else {
         haMode.lastRoundRobinSecondaryHost = hostAddress;
      }

      return Optional.of(hostAddress);
   }

   public void resetLast() {
      this.lastRoundRobinPrimaryHost = null;
      this.lastRoundRobinSecondaryHost = null;
   }

   public abstract Optional<HostAddress> getAvailableHost(List<HostAddress> var1, ConcurrentMap<HostAddress, Long> var2, boolean var3);
}
