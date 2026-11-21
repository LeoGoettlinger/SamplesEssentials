package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum ScaleCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc scale <npc> <factor>")
   @Permission({"fancynpcs.command.npc.scale"})
   public void onScale(@NotNull final CommandSender sender, @NotNull final Npc npc, final float factor) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SCALE, factor, sender).callEvent()) {
         npc.getData().setScale(factor);
         npc.updateForAll();
         this.translator
            .translate("npc_scale_set_success")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("scale", String.valueOf(factor))
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
