package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_netinterface_t;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatNetInterface {
   private static final Perfstat PERF = Perfstat.INSTANCE;

   private PerfstatNetInterface() {
   }

   public static perfstat_netinterface_t[] queryNetInterfaces() {
      perfstat_netinterface_t netinterface = new perfstat_netinterface_t();
      int total = PERF.perfstat_netinterface(null, null, netinterface.size(), 0);
      if (total > 0) {
         perfstat_netinterface_t[] statp = (perfstat_netinterface_t[])netinterface.toArray(total);
         perfstat_id_t firstnetinterface = new perfstat_id_t();
         int ret = PERF.perfstat_netinterface(firstnetinterface, statp, netinterface.size(), total);
         if (ret > 0) {
            return statp;
         }
      }

      return new perfstat_netinterface_t[0];
   }
}
