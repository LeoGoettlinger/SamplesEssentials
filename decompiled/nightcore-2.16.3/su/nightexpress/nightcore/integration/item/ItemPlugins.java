package su.nightexpress.nightcore.integration.item;

import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.Lists;

public class ItemPlugins {
   public static final String EXECUTABLE_ITEMS = "ExecutableItems";
   public static final String EXCELLENT_CRATES = "ExcellentCrates";
   public static final String ITEMS_ADDER = "ItemsAdder";
   public static final String MMOITEMS = "MMOItems";
   public static final String NEXO = "Nexo";
   public static final String ORAXEN = "Oraxen";
   public static final String CRAFT_ENGINE = "CraftEngine";

   @NonNull
   public static List<String> values() {
      return Lists.newList("ExcellentCrates", "ExecutableItems", "ItemsAdder", "MMOItems", "Nexo", "Oraxen", "CraftEngine");
   }
}
