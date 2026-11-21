package oshi.util.platform.windows;

import com.sun.jna.platform.win32.PdhUtil;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.PdhUtil.PdhEnumObjectItems;
import com.sun.jna.platform.win32.PdhUtil.PdhException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.Util;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class PerfCounterWildcardQuery {
   private static final Logger LOG = LoggerFactory.getLogger(PerfCounterWildcardQuery.class);
   private static final Set<String> FAILED_QUERY_CACHE = ConcurrentHashMap.newKeySet();

   private PerfCounterWildcardQuery() {
   }

   public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(
      Class<T> propertyEnum, String perfObject, String perfWmiClass
   ) {
      return queryInstancesAndValues(propertyEnum, perfObject, perfWmiClass, null);
   }

   public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(
      Class<T> propertyEnum, String perfObject, String perfWmiClass, String customFilter
   ) {
      if (!FAILED_QUERY_CACHE.contains(perfObject)) {
         Pair<List<String>, Map<T, List<Long>>> instancesAndValuesMap = queryInstancesAndValuesFromPDH(propertyEnum, perfObject, customFilter);
         if (!instancesAndValuesMap.getA().isEmpty()) {
            return instancesAndValuesMap;
         }

         if (Util.isBlank(customFilter)) {
            LOG.warn("Disabling further attempts to query {}.", perfObject);
            FAILED_QUERY_CACHE.add(perfObject);
         }
      }

      return queryInstancesAndValuesFromWMI(propertyEnum, perfWmiClass);
   }

   public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(Class<T> propertyEnum, String perfObject) {
      return queryInstancesAndValuesFromPDH(propertyEnum, perfObject, null);
   }

   public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(
      Class<T> propertyEnum, String perfObject, String customFilter
   ) {
      T[] props = (T[])propertyEnum.getEnumConstants();
      if (props.length < 2) {
         throw new IllegalArgumentException("Enum " + propertyEnum.getName() + " must have at least two elements, an instance filter and a counter.");
      } else {
         String instanceFilter = Util.isBlank(customFilter)
            ? ((PerfCounterWildcardQuery.PdhCounterWildcardProperty)((Enum[])propertyEnum.getEnumConstants())[0]).getCounter().toLowerCase(Locale.ROOT)
            : customFilter;
         String perfObjectLocalized = PerfCounterQuery.localizeIfNeeded(perfObject, true);
         PdhEnumObjectItems objectItems = null;

         try {
            objectItems = PdhUtil.PdhEnumObjectItems(null, null, perfObjectLocalized, 100);
         } catch (PdhException var19) {
            LOG.warn(
               "Failed to locate performance object for {} in the registry. Performance counters may be corrupt. {}", perfObjectLocalized, var19.getMessage()
            );
         }

         if (objectItems == null) {
            return new Pair<>(Collections.emptyList(), Collections.emptyMap());
         } else {
            List<String> instances = objectItems.getInstances();
            instances.removeIf(ix -> !Util.wildcardMatch(ix.toLowerCase(Locale.ROOT), instanceFilter));
            EnumMap<T, List<Long>> valuesMap = new EnumMap<>(propertyEnum);
            PerfCounterQueryHandler pdhQueryHandler = new PerfCounterQueryHandler();

            Pair var17;
            label82: {
               try {
                  EnumMap<T, List<PerfDataUtil.PerfCounter>> counterListMap = new EnumMap<>(propertyEnum);

                  for (int i = 1; i < props.length; i++) {
                     T prop = props[i];
                     List<PerfDataUtil.PerfCounter> counterList = new ArrayList<>(instances.size());

                     for (String instance : instances) {
                        PerfDataUtil.PerfCounter counter = PerfDataUtil.createCounter(
                           perfObject, instance, ((PerfCounterWildcardQuery.PdhCounterWildcardProperty)prop).getCounter()
                        );
                        if (!pdhQueryHandler.addCounterToQuery(counter)) {
                           var17 = new Pair<>(Collections.emptyList(), Collections.emptyMap());
                           break label82;
                        }

                        counterList.add(counter);
                     }

                     counterListMap.put(prop, counterList);
                  }

                  if (0L < pdhQueryHandler.updateQuery()) {
                     for (int i = 1; i < props.length; i++) {
                        T prop = props[i];
                        List<Long> values = new ArrayList<>();

                        for (PerfDataUtil.PerfCounter counter : counterListMap.get(prop)) {
                           values.add(pdhQueryHandler.queryCounter(counter));
                        }

                        valuesMap.put(prop, values);
                     }
                  }
               } catch (Throwable var20) {
                  try {
                     pdhQueryHandler.close();
                  } catch (Throwable var18) {
                     var20.addSuppressed(var18);
                  }

                  throw var20;
               }

               pdhQueryHandler.close();
               return new Pair<>(instances, valuesMap);
            }

            pdhQueryHandler.close();
            return var17;
         }
      }
   }

   public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromWMI(Class<T> propertyEnum, String wmiClass) {
      List<String> instances = new ArrayList<>();
      EnumMap<T, List<Long>> valuesMap = new EnumMap<>(propertyEnum);
      WmiQuery<T> query = new WmiQuery(wmiClass, propertyEnum);
      WmiResult<T> result = Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(query);
      if (result.getResultCount() > 0) {
         for (T prop : (Enum[])propertyEnum.getEnumConstants()) {
            if (prop.ordinal() == 0) {
               for (int i = 0; i < result.getResultCount(); i++) {
                  instances.add(WmiUtil.getString(result, prop, i));
               }
            } else {
               List<Long> values = new ArrayList<>();

               for (int i = 0; i < result.getResultCount(); i++) {
                  switch (result.getCIMType(prop)) {
                     case 18:
                        values.add((long)WmiUtil.getUint16(result, prop, i));
                        break;
                     case 19:
                        values.add(WmiUtil.getUint32asLong(result, prop, i));
                        break;
                     case 21:
                        values.add(WmiUtil.getUint64(result, prop, i));
                        break;
                     case 101:
                        values.add(WmiUtil.getDateTime(result, prop, i).toInstant().toEpochMilli());
                        break;
                     default:
                        throw new ClassCastException("Unimplemented CIM Type Mapping.");
                  }
               }

               valuesMap.put(prop, values);
            }
         }
      }

      return new Pair<>(instances, valuesMap);
   }

   public interface PdhCounterWildcardProperty {
      String getCounter();
   }
}
