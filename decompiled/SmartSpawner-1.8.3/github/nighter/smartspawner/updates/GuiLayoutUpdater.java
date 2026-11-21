package github.nighter.smartspawner.updates;

import github.nighter.smartspawner.SmartSpawner;
import java.io.File;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class GuiLayoutUpdater {
   private static final String GUI_LAYOUTS_DIR = "gui_layouts";
   private static final String[] LAYOUT_FILES = new String[]{"storage_gui.yml", "main_gui.yml", "sell_confirm_gui.yml"};
   private static final String[] LAYOUT_NAMES = new String[]{"default", "DonutSMP", "DonutSMP_v2"};
   private static final String BUTTON_PREFIX = "slot_";
   private static final YamlMigrator.SectionMatcher BUTTON_SECTIONS = (defaults, path) -> path.startsWith("slot_") && !path.contains(".");
   private final SmartSpawner plugin;

   public GuiLayoutUpdater(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void checkAndUpdateLayouts() {
      File layoutsDir = new File(this.plugin.getDataFolder(), "gui_layouts");
      layoutsDir.mkdirs();

      for (String layoutName : LAYOUT_NAMES) {
         File layoutDir = new File(layoutsDir, layoutName);
         layoutDir.mkdirs();

         for (String fileName : LAYOUT_FILES) {
            File dataFile = new File(layoutDir, fileName);
            String resource = "gui_layouts/" + layoutName + "/" + fileName;
            YamlMigrator.migrate(
               dataFile,
               this.plugin.getResource(resource),
               List.of(),
               ConfigMigrations.GUI_LAYOUT,
               true,
               this.hasAnyButton(dataFile) ? YamlMigrator.OwnedSection.userManaged(BUTTON_SECTIONS) : null,
               this.plugin.getLogger()
            );
         }
      }
   }

   private boolean hasAnyButton(File file) {
      if (!file.exists()) {
         return false;
      } else {
         for (String key : YamlConfiguration.loadConfiguration(file).getKeys(false)) {
            if (key.startsWith("slot_")) {
               return true;
            }
         }

         return false;
      }
   }
}
