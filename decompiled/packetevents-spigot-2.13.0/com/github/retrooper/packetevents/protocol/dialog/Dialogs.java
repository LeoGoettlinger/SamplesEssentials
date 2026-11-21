package com.github.retrooper.packetevents.protocol.dialog;

import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Dialogs {
   private static final ActionButton DEFAULT_BACK_BUTTON = new ActionButton(new CommonButtonData(Component.translatable("gui.back"), null, 200), null);
   private static final VersionedRegistry<Dialog> REGISTRY = new VersionedRegistry<>("dialog");
   public static final Dialog SERVER_LINKS = define(
      "server_links",
      new ServerLinksDialog(
         new CommonDialogData(
            Component.translatable("menu.server_links.title"),
            Component.translatable("menu.server_links"),
            true,
            true,
            DialogAction.CLOSE,
            Collections.emptyList(),
            Collections.emptyList()
         ),
         DEFAULT_BACK_BUTTON,
         1,
         310
      )
   );
   public static final Dialog CUSTOM_OPTIONS = define(
      "custom_options",
      new DialogListDialog(
         new CommonDialogData(
            Component.translatable("menu.custom_options.title"),
            Component.translatable("menu.custom_options"),
            true,
            true,
            DialogAction.CLOSE,
            Collections.emptyList(),
            Collections.emptyList()
         ),
         new MappedEntitySet<>(new ResourceLocation("pause_screen_additions")),
         DEFAULT_BACK_BUTTON,
         1,
         310
      )
   );
   public static final Dialog QUICK_ACTIONS = define(
      "quick_actions",
      new DialogListDialog(
         new CommonDialogData(
            Component.translatable("menu.quick_actions.title"),
            Component.translatable("menu.quick_actions"),
            true,
            true,
            DialogAction.CLOSE,
            Collections.emptyList(),
            Collections.emptyList()
         ),
         new MappedEntitySet<>(new ResourceLocation("quick_actions")),
         DEFAULT_BACK_BUTTON,
         1,
         310
      )
   );

   private Dialogs() {
   }

   @Internal
   public static Dialog define(String name, Dialog dialog) {
      return REGISTRY.define(name, dialog::copy);
   }

   public static VersionedRegistry<Dialog> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
