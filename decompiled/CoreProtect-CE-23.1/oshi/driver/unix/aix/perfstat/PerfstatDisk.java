package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_disk_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatDisk {
   private static final Perfstat PERF = Perfstat.INSTANCE;

   private PerfstatDisk() {
   }

   public static perfstat_disk_t[] queryDiskStats() {
      perfstat_disk_t diskStats = new perfstat_disk_t();
      int total = PERF.perfstat_disk(null, null, diskStats.size(), 0);
      if (total > 0) {
         perfstat_disk_t[] statp = (perfstat_disk_t[])diskStats.toArray(total);
         perfstat_id_t firstdiskStats = new perfstat_id_t();
         int ret = PERF.perfstat_disk(firstdiskStats, statp, diskStats.size(), total);
         if (ret > 0) {
            return statp;
         }
      }

      return new perfstat_disk_t[0];
   }
}
