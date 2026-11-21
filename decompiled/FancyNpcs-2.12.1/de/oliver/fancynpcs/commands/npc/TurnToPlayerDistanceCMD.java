package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum TurnToPlayerDistanceCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();
   private final List<String> DISTANCE_SUGGESTIONS = List.of("default");

   @Command("npc turn_to_player_distance <npc> <distance>")
   @Permission({"fancynpcs.command.npc.turn_to_player_distance"})
   public void onTurnToPlayerDistance(
      @NotNull final CommandSender sender, @NotNull final Npc npc, @Argument(parserName = "TurnToPlayerDistanceCMD/distance") final int distance
   ) {
      if (distance < -1) {
         this.translator.translate("npc_turn_to_player_distance_invalid").withPrefix().send(sender);
      } else {
         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.TURN_TO_PLAYER_DISTANCE, distance, sender).callEvent()) {
            npc.getData().setTurnToPlayerDistance(distance);
            if (distance == -1) {
               int defaultDistance = FancyNpcs.getInstance().getFancyNpcConfig().getTurnToPlayerDistance();
               this.translator
                  .translate("npc_turn_to_player_distance_default")
                  .withPrefix()
                  .replace("npc", npc.getData().getName())
                  .replace("distance", String.valueOf(defaultDistance))
                  .send(sender);
            } else {
               this.translator
                  .translate("npc_turn_to_player_distance_set")
                  .withPrefix()
                  .replace("npc", npc.getData().getName())
                  .replace("distance", String.valueOf(distance))
                  .send(sender);
            }
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      }
   }

   @Parser(
      name = "TurnToPlayerDistanceCMD/distance",
      suggestions = "TurnToPlayerDistanceCMD/distance"
   )
   @NotNull
   public Integer parse(final CommandContext<CommandSender> context, final CommandInput input) {
      if (input.peekString().equalsIgnoreCase("default")) {
         input.readString();
         return -1;
      } else {
         return input.readInteger();
      }
   }

   @Suggestions("TurnToPlayerDistanceCMD/distance")
   @NotNull
   public List<String> suggest(final CommandContext<CommandSender> context, final CommandInput input) {
      return this.DISTANCE_SUGGESTIONS;
   }
}
