package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ShowInTabCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc show_in_tab <npc> [state]")
   @Permission({"fancynpcs.command.npc.show_in_tab"})
   public void onCommand(@NotNull final CommandSender sender, @NotNull final Npc npc, @Nullable final Boolean state) {
      boolean finalState = state == null ? !npc.getData().isShowInTab() : state;
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SHOW_IN_TAB, finalState, sender).callEvent()) {
         npc.getData().setShowInTab(finalState);
         npc.removeForAll();
         npc.create();
         npc.spawnForAll();
         this.translator
            .translate(finalState ? "npc_show_in_tab_set_true" : "npc_show_in_tab_set_false")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
