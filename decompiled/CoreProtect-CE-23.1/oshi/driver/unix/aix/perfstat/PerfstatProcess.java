package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_process_t;
import java.util.Arrays;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatProcess {
   private static final Perfstat PERF = Perfstat.INSTANCE;

   private PerfstatProcess() {
   }

   public static perfstat_process_t[] queryProcesses() {
      perfstat_process_t process = new perfstat_process_t();
      int procCount = PERF.perfstat_process(null, null, process.size(), 0);
      if (procCount > 0) {
         perfstat_process_t[] proct = (perfstat_process_t[])process.toArray(procCount);
         perfstat_id_t firstprocess = new perfstat_id_t();
         int ret = PERF.perfstat_process(firstprocess, proct, process.size(), procCount);
         if (ret > 0) {
            return Arrays.copyOf(proct, ret);
         }
      }

      return new perfstat_process_t[0];
   }
}
