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

public enum VisibilityDistanceCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();
   private final List<String> DISTANCE_SUGGESTIONS = List.of("always_visible", "default", "not_visible");

   @Command("npc visibility_distance <npc> <distance>")
   @Permission({"fancynpcs.command.npc.visibility_distance"})
   public void onVisibilityDistance(
      @NotNull final CommandSender sender, @NotNull final Npc npc, @Argument(parserName = "VisibilityDistanceCMD/distance") final int distance
   ) {
      int finalDistance = Math.clamp((long)distance, -1, Integer.MAX_VALUE);
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.VISIBILITY_DISTANCE, distance, sender).callEvent()) {
         npc.getData().setVisibilityDistance(finalDistance);
         npc.updateForAll();
         this.translator
            .translate(
               finalDistance == -1
                  ? "npc_visibility_distance_set_default"
                  : (
                     finalDistance == 0
                        ? "npc_visibility_distance_set_not_visible"
                        : (finalDistance == Integer.MAX_VALUE ? "npc_visibility_distance_set_always_visible" : "npc_visibility_distance_set_value")
                  )
            )
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace(
               "distance",
               finalDistance > -1 ? String.valueOf(finalDistance) : String.valueOf(FancyNpcs.getInstance().getFancyNpcConfig().getVisibilityDistance())
            )
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }

   @Parser(
      name = "VisibilityDistanceCMD/distance",
      suggestions = "VisibilityDistanceCMD/distance"
   )
   @NotNull
   public Integer parse(final CommandContext<CommandSender> context, final CommandInput input) {
      if (input.peekString().equalsIgnoreCase("default")) {
         input.readString();
         return -1;
      } else if (input.peekString().equalsIgnoreCase("not_visible")) {
         input.readString();
         return 0;
      } else if (input.peekString().equalsIgnoreCase("always_visible")) {
         input.readString();
         return Integer.MAX_VALUE;
      } else {
         return input.readInteger();
      }
   }

   @Suggestions("VisibilityDistanceCMD/distance")
   @NotNull
   public List<String> suggest(final CommandContext<CommandSender> context, final CommandInput input) {
      return this.DISTANCE_SUGGESTIONS;
   }
}
