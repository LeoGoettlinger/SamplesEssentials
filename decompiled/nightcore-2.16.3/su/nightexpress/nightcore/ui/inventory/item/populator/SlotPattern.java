package su.nightexpress.nightcore.ui.inventory.item.populator;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.configuration.codec.ConfigCodec;
import su.nightexpress.nightcore.configuration.codec.ConfigCodecs;

public class SlotPattern implements SlotProvider, Writeable {
   private static final ConfigCodec<TreeMap<Integer, int[]>> MAP_TYPE = ConfigCodecs.forIntTreeMap(ConfigCodecs.INT_ARRAY);
   private final TreeMap<Integer, int[]> layouts;

   public SlotPattern() {
      this(new TreeMap<>());
   }

   public SlotPattern(@NonNull SortedMap<Integer, int[]> layouts) {
      this.layouts = new TreeMap<>(layouts);
   }

   @NonNull
   public static SlotPattern of(@NonNull SortedMap<Integer, int[]> layouts) {
      return new SlotPattern(layouts);
   }

   @NonNull
   public static SlotPattern read(@NonNull FileConfig config, @NonNull String path) {
      return new SlotPattern(config.get(path, MAP_TYPE, new TreeMap<>()));
   }

   @Override
   public void write(@NonNull FileConfig config, @NonNull String path) {
      config.set(path, MAP_TYPE, this.layouts);
   }

   @NonNull
   public SlotPattern with(int amount, int... slots) {
      this.layouts.put(amount, slots);
      return this;
   }

   @Override
   public int[] getSlots(int itemsAmount) {
      if (this.layouts.isEmpty()) {
         return new int[0];
      } else {
         Entry<Integer, int[]> entry = this.layouts.floorEntry(itemsAmount);
         return entry == null ? this.layouts.firstEntry().getValue() : entry.getValue();
      }
   }

   @NonNull
   public SortedMap<Integer, int[]> getLayouts() {
      return this.layouts;
   }
}
