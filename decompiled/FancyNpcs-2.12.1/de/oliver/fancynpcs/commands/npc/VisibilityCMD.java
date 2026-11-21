package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.data.property.NpcVisibility;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum VisibilityCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc visibility <npc> <visibility>")
   @Permission({"fancynpcs.command.npc.visibility"})
   public void onVisibility(@NotNull final CommandSender sender, @NotNull final Npc npc, @NotNull final NpcVisibility visibility) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.VISIBILITY, visibility, sender).callEvent()) {
         npc.getData().setVisibility(visibility);
         npc.checkAndUpdateVisibilityForAll();
         this.translator
            .translate("npc_visibility_set")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("visibility", visibility.toString())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
