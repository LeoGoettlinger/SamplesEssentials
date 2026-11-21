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

public enum CollidableCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc collidable <npc> [state]")
   @Permission({"fancynpcs.command.npc.collidable"})
   public void onCollidable(@NotNull final CommandSender sender, @NotNull final Npc npc, @Nullable final Boolean state) {
      boolean finalState = state == null ? !npc.getData().isCollidable() : state;
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.COLLIDABLE, finalState, sender).callEvent()) {
         npc.getData().setCollidable(finalState);
         npc.removeForAll();
         npc.spawnForAll();
         this.translator
            .translate(finalState ? "npc_collidable_set_true" : "npc_collidable_set_false")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
