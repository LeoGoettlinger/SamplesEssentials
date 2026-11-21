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

public enum TurnToPlayerCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc turn_to_player <npc> [state]")
   @Permission({"fancynpcs.command.npc.turn_to_player"})
   public void onTurnToPlayer(@NotNull final CommandSender sender, @NotNull final Npc npc, @Nullable final Boolean state) {
      if (state != null && npc.getData().isTurnToPlayer() != state) {
         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.TURN_TO_PLAYER, state, sender).callEvent()) {
            npc.getData().setTurnToPlayer(state);
            this.translator
               .translate(state ? "npc_turn_to_player_set_true" : "npc_turn_to_player_set_false")
               .withPrefix()
               .replace("npc", npc.getData().getName())
               .send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      } else if (state == null) {
         boolean currentState = npc.getData().isTurnToPlayer();
         this.translator
            .translate(currentState ? "npc_turn_to_player_set_true" : "npc_turn_to_player_set_false")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .send(sender);
      }
   }
}
