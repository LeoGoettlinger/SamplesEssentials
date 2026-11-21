package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.SimpleMessage;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.utils.GlowingColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum GlowingCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc glowing <npc> [color]")
   @Permission({"fancynpcs.command.npc.glowing"})
   public void onGlowing(@NotNull final CommandSender sender, @NotNull final Npc npc, @Nullable final GlowingColor color) {
      if (color == null) {
         boolean isGlowingToggled = !npc.getData().isGlowing();
         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.GLOWING, isGlowingToggled, sender).callEvent()) {
            npc.getData().setGlowing(isGlowingToggled);
            npc.updateForAll();
            this.translator
               .translate(isGlowingToggled ? "npc_glowing_set_true" : "npc_glowing_set_false")
               .withPrefix()
               .replace("npc", npc.getData().getName())
               .send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      } else if (color == GlowingColor.DISABLED) {
         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.GLOWING, false, sender).callEvent()) {
            npc.getData().setGlowing(false);
            npc.updateForAll();
            this.translator.translate("npc_glowing_set_false").withPrefix().replace("npc", npc.getData().getName()).send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      } else if (!npc.getData().isGlowing() && !new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.GLOWING, true, sender).callEvent()) {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      } else if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.GLOWING_COLOR, color.getColor(), sender).callEvent()) {
         npc.getData().setGlowingColor(color.getColor());
         if (!npc.getData().isGlowing()) {
            npc.getData().setGlowing(true);
         }

         npc.updateForAll();
         this.translator
            .translate("npc_glowing_set_color_success")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("color", ((SimpleMessage)this.translator.translate(color.getTranslationKey())).getMessage())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
