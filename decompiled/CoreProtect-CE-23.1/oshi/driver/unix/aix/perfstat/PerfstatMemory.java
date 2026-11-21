package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_memory_total_t;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatMemory {
   private static final Perfstat PERF = Perfstat.INSTANCE;

   private PerfstatMemory() {
   }

   public static perfstat_memory_total_t queryMemoryTotal() {
      perfstat_memory_total_t memory = new perfstat_memory_total_t();
      int ret = PERF.perfstat_memory_total(null, memory, memory.size(), 1);
      return ret > 0 ? memory : new perfstat_memory_total_t();
   }
}
