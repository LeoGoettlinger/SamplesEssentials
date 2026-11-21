package oshi.util.platform.unix.solaris;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.solaris.Kstat2StatusException;
import com.sun.jna.platform.unix.solaris.LibKstat;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2Handle;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2Map;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2MatcherList;
import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatCtl;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatNamed;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.GuardedBy;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.unix.solaris.SolarisOperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

@ThreadSafe
public final class KstatUtil {
   private static final Logger LOG = LoggerFactory.getLogger(KstatUtil.class);
   private static final Lock CHAIN = new ReentrantLock();
   @GuardedBy("CHAIN")
   private static KstatCtl kstatCtl = null;

   private KstatUtil() {
   }

   public static synchronized KstatUtil.KstatChain openChain() {
      CHAIN.lock();
      if (kstatCtl == null) {
         kstatCtl = LibKstat.INSTANCE.kstat_open();
      }

      return new KstatUtil.KstatChain(kstatCtl);
   }

   public static String dataLookupString(Kstat ksp, String name) {
      if (ksp.ks_type != 1 && ksp.ks_type != 4) {
         throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
      } else {
         Pointer p = LibKstat.INSTANCE.kstat_data_lookup(ksp, name);
         if (p == null) {
            LOG.debug("Failed to lookup kstat value for key {}", name);
            return "";
         } else {
            KstatNamed data = new KstatNamed(p);
            switch (data.data_type) {
               case 0:
                  return Native.toString(data.value.charc, StandardCharsets.UTF_8);
               case 1:
                  return Integer.toString(data.value.i32);
               case 2:
                  return FormatUtil.toUnsignedString(data.value.ui32);
               case 3:
                  return Long.toString(data.value.i64);
               case 4:
                  return FormatUtil.toUnsignedString(data.value.ui64);
               case 5:
               case 6:
               case 7:
               case 8:
               default:
                  LOG.error("Unimplemented kstat data type {}", data.data_type);
                  return "";
               case 9:
                  return data.value.str.addr.getString(0L);
            }
         }
      }
   }

   public static long dataLookupLong(Kstat ksp, String name) {
      if (ksp.ks_type != 1 && ksp.ks_type != 4) {
         throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
      } else {
         Pointer p = LibKstat.INSTANCE.kstat_data_lookup(ksp, name);
         if (p == null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(
                  "Failed lo lookup kstat value on {}:{}:{} for key {}",
                  new Object[]{
                     Native.toString(ksp.ks_module, StandardCharsets.US_ASCII), ksp.ks_instance, Native.toString(ksp.ks_name, StandardCharsets.US_ASCII), name
                  }
               );
            }

            return 0L;
         } else {
            KstatNamed data = new KstatNamed(p);
            switch (data.data_type) {
               case 1:
                  return data.value.i32;
               case 2:
                  return FormatUtil.getUnsignedInt(data.value.ui32);
               case 3:
                  return data.value.i64;
               case 4:
                  return data.value.ui64;
               default:
                  LOG.error("Unimplemented or non-numeric kstat data type {}", data.data_type);
                  return 0L;
            }
         }
      }
   }

   public static Object[] queryKstat2(String mapStr, String... names) {
      if (!SolarisOperatingSystem.HAS_KSTAT2) {
         throw new UnsupportedOperationException("Kstat2 requires Solaris 11.4+. Use SolarisOperatingSystem#HAS_KSTAT2 to test this.");
      } else {
         Object[] result = new Object[names.length];
         Kstat2MatcherList matchers = new Kstat2MatcherList();
         CHAIN.lock();

         try {
            matchers.addMatcher(0, mapStr);
            Kstat2Handle handle = new Kstat2Handle();

            try {
               Kstat2Map map = handle.lookupMap(mapStr);

               for (int i = 0; i < names.length; i++) {
                  result[i] = map.getValue(names[i]);
               }
            } finally {
               handle.close();
            }
         } catch (Kstat2StatusException var16) {
            LOG.debug("Failed to get stats on {} for names {}: {}", new Object[]{mapStr, Arrays.toString((Object[])names), var16.getMessage()});
         } finally {
            CHAIN.unlock();
            matchers.free();
         }

         return result;
      }
   }

   public static List<Object[]> queryKstat2List(String beforeStr, String afterStr, String... names) {
      if (!SolarisOperatingSystem.HAS_KSTAT2) {
         throw new UnsupportedOperationException("Kstat2 requires Solaris 11.4+. Use SolarisOperatingSystem#HAS_KSTAT2 to test this.");
      } else {
         List<Object[]> results = new ArrayList<>();
         int s = 0;
         Kstat2MatcherList matchers = new Kstat2MatcherList();
         CHAIN.lock();

         try {
            matchers.addMatcher(1, beforeStr + "*" + afterStr);
            Kstat2Handle handle = new Kstat2Handle();

            try {
               for (s = 0; s < Integer.MAX_VALUE; s++) {
                  Object[] result = new Object[names.length];
                  Kstat2Map map = handle.lookupMap(beforeStr + s + afterStr);

                  for (int i = 0; i < names.length; i++) {
                     result[i] = map.getValue(names[i]);
                  }

                  results.add(result);
               }
            } finally {
               handle.close();
            }
         } catch (Kstat2StatusException var19) {
            LOG.debug(
               "Failed to get stats on {}{}{} for names {}: {}", new Object[]{beforeStr, s, afterStr, Arrays.toString((Object[])names), var19.getMessage()}
            );
         } finally {
            CHAIN.unlock();
            matchers.free();
         }

         return results;
      }
   }

   public static final class KstatChain implements AutoCloseable {
      private final KstatCtl localCtlRef;

      private KstatChain(KstatCtl ctl) {
         this.localCtlRef = ctl;
         this.update();
      }

      @GuardedBy("CHAIN")
      public boolean read(Kstat ksp) {
         int retry = 0;

         while (0 > LibKstat.INSTANCE.kstat_read(this.localCtlRef, ksp, null)) {
            if (11 != Native.getLastError() || 5 <= ++retry) {
               if (KstatUtil.LOG.isDebugEnabled()) {
                  KstatUtil.LOG
                     .debug(
                        "Failed to read kstat {}:{}:{}",
                        new Object[]{
                           Native.toString(ksp.ks_module, StandardCharsets.US_ASCII), ksp.ks_instance, Native.toString(ksp.ks_name, StandardCharsets.US_ASCII)
                        }
                     );
               }

               return false;
            }

            Util.sleep(8 << retry);
         }

         return true;
      }

      @GuardedBy("CHAIN")
      public Kstat lookup(String module, int instance, String name) {
         return LibKstat.INSTANCE.kstat_lookup(this.localCtlRef, module, instance, name);
      }

      @GuardedBy("CHAIN")
      public List<Kstat> lookupAll(String module, int instance, String name) {
         List<Kstat> kstats = new ArrayList<>();

         for (Kstat ksp = LibKstat.INSTANCE.kstat_lookup(this.localCtlRef, module, instance, name); ksp != null; ksp = ksp.next()) {
            if ((module == null || module.equals(Native.toString(ksp.ks_module, StandardCharsets.US_ASCII)))
               && (instance < 0 || instance == ksp.ks_instance)
               && (name == null || name.equals(Native.toString(ksp.ks_name, StandardCharsets.US_ASCII)))) {
               kstats.add(ksp);
            }
         }

         return kstats;
      }

      @GuardedBy("CHAIN")
      public int update() {
         return LibKstat.INSTANCE.kstat_chain_update(this.localCtlRef);
      }

      @Override
      public void close() {
         KstatUtil.CHAIN.unlock();
      }
   }
}
