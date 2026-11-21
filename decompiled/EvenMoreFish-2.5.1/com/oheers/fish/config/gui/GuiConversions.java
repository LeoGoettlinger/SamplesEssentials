package com.oheers.fish.config.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.gui.impl.ApplyBaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.boostedyaml.route.Route;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GuiConversions {
   public void performCheck() {
      File guisFile = new File(EvenMoreFish.getInstance().getDataFolder(), "guis.yml");
      if (guisFile.exists() && guisFile.isFile()) {
         EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of gui configs.");
         File guiDir = this.getGuiDirectory();
         if (!guiDir.exists()) {
            guiDir.mkdirs();
         }

         ConfigBase config = new ConfigBase(guisFile, EvenMoreFish.getInstance(), false);
         Map<String, Map<Route, Object>> mappedPageButtons = this.mapPageButtons(config.getConfig().getSection("general"));

         for (String key : config.getConfig().getRoutesAsStrings(false)) {
            Section section = config.getConfig().getSection(key);
            if (section != null) {
               this.convertSectionToFile(section, mappedPageButtons);
            }
         }

         this.finalizeConversion(config);
      }
   }

   private void finalizeConversion(@NonNull ConfigBase config) {
      File file = config.getFile();
      file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "guis.yml.old"));
      file.delete();
      Logging.info(ComponentMessage.componentMessage("<yellow>Your gui configs have been automatically converted to the new format.").get());
   }

   public File getGuiDirectory() {
      return new File(EvenMoreFish.getInstance().getDataFolder(), "gui");
   }

   private void convertSectionToFile(@NonNull Section section, @NonNull Map<String, Map<Route, Object>> mappedPageButtons) {
      String id = section.getNameAsString();
      if (id != null) {
         String var5 = section.getNameAsString();

         GuiConfig config = (GuiConfig)(switch (var5) {
            case "main-menu" -> MainMenuGuiConfig.getInstance();
            case "sell-menu-normal" -> SellMenuNormalGuiConfig.getInstance();
            case "sell-menu-confirm" -> SellMenuConfirmGuiConfig.getInstance();
            case "baits-menu" -> BaitsMenuGuiConfig.getInstance();
            case "apply-baits-menu" -> ApplyBaitsMenuGuiConfig.getInstance();
            case "journal-menu" -> JournalRaritiesGuiConfig.getInstance();
            case "journal-rarity" -> JournalFishGuiConfig.getInstance();
            default -> null;
         });
         if (config != null) {
            if (config.isPaginated() && !mappedPageButtons.isEmpty()) {
               mappedPageButtons.forEach((key, map) -> config.getConfig().createSection(key).setAll((Map<Route, Object>)map));
            }

            config.getConfig().setAll(section.getRouteMappedValues(true));
            config.save();
         }
      }
   }

   private Map<String, Map<Route, Object>> mapPageButtons(@Nullable Section general) {
      if (general == null) {
         return Map.of();
      } else {
         Map<String, Map<Route, Object>> mapped = new HashMap<>();

         for (String key : general.getRoutesAsStrings(false)) {
            Section section = general.getSection(key);
            switch (key) {
               case "first-page":
                  section.set("character", "f");
                  section.set("page-action", "first");
                  break;
               case "previous-page":
                  section.set("character", "p");
                  section.set("page-action", "previous");
                  break;
               case "next-page":
                  section.set("character", "n");
                  section.set("page-action", "next");
                  break;
               case "last-page":
                  section.set("character", "l");
                  section.set("page-action", "last");
                  break;
               default:
                  continue;
            }

            mapped.put(key, section.getRouteMappedValues(true));
         }

         return mapped;
      }
   }
}
