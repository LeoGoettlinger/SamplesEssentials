package com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions;

import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.CloseAction;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.ConsoleCommandAction;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.MessageAction;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.PageChangeAction;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.PlayerCommandAction;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation.RefreshAction;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public enum Actions {
   INSTANCE;

   private final Map<String, ActionProvider<?>> registered = new HashMap<>();

   private Actions() {
      this.register("close", CloseAction::new);
      this.register("console", ConsoleCommandAction::new);
      this.register("message", MessageAction::new);
      this.register("page", PageChangeAction::new);
      this.register("player", PlayerCommandAction::new);
      this.register("refresh", RefreshAction::new);
   }

   public void register(String id, ActionProvider<?> action) {
      this.registered.put(id.toLowerCase(Locale.ENGLISH), action);
   }

   public List<Action<?>> parseAll(List<String> lines) {
      if (lines != null && !lines.isEmpty()) {
         List<Action<?>> actions = new ArrayList<>(lines.size());

         for (String line : lines) {
            Action<?> parse = this.parse(line);
            if (parse != null) {
               actions.add(parse);
            }
         }

         return actions;
      } else {
         return List.of();
      }
   }

   public Action<?> parse(String line) {
      if (line != null && !line.isBlank()) {
         String id = StringUtils.substringBetween(line, "[", "]").toLowerCase(Locale.ENGLISH);
         ActionProvider<?> provider = this.registered.get(id);
         if (provider == null) {
            LogUtils.info("No actionprovider found with id {}!", id);
            return null;
         } else {
            String arguments = line.substring(id.length() + 2).strip();
            if (FeatureFlags.DEBUG.get()) {
               LogUtils.debug("Creating action with id: {} with arguments: {}", id, arguments);
            }

            try {
               return provider.provide(arguments);
            } catch (IllegalArgumentException var6) {
               LogUtils.error("Failed to construct action with id: {} and arguments: {}!", id, arguments);
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
