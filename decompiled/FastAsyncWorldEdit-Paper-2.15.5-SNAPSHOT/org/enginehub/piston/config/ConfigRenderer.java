package org.enginehub.piston.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.renderer.ComponentRenderer;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ConfigRenderer implements ComponentRenderer<ConfigHolder> {
   private static final ConfigRenderer INSTANCE = new ConfigRenderer();

   public static ConfigRenderer getInstance() {
      return INSTANCE;
   }

   private ConfigRenderer() {
   }

   @NonNull
   public Component render(@NonNull Component component, @NonNull ConfigHolder context) {
      component = this.replaceSubcomponents(component, context);
      if (component instanceof TranslatableComponent tc) {
         Config<?> config = context.getConfigs().get(tc.key());
         if (config != null) {
            component = config.apply(tc);
         }
      }

      return component;
   }

   private Component replaceSubcomponents(Component component, ConfigHolder context) {
      if (component instanceof TranslatableComponent tc) {
         List<Component> originalArgs = tc.args();
         List<Component> replacementArgs = this.renderList(originalArgs, context);
         if (originalArgs != replacementArgs) {
            component = tc.args(replacementArgs);
         }
      }

      List<Component> original = component.children();
      List<Component> replacement = this.renderList(original, context);
      return original != replacement ? component.children(replacement) : component;
   }

   private List<Component> renderList(List<Component> input, ConfigHolder context) {
      if (input.isEmpty()) {
         return input;
      } else {
         Builder<Component> copy = ImmutableList.builder();
         boolean modified = false;

         for (Component component : input) {
            Component replacement = this.render(component, context);
            if (replacement != component) {
               modified = true;
            }

            copy.add(replacement);
         }

         return (List<Component>)(modified ? copy.build() : input);
      }
   }
}
