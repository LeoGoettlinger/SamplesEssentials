package de.oliver.fancynpcs.commands.arguments;

import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcManager;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

public class NpcArgument {
   public static final NpcArgument INSTANCE = new NpcArgument();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();
   private final NpcManager npcManager = FancyNpcs.getInstance().getNpcManager();

   private NpcArgument() {
   }

   private static boolean isUUID(@NotNull final String string) {
      try {
         UUID.fromString(string);
         return true;
      } catch (IllegalArgumentException var2) {
         return false;
      }
   }

   @Parser(
      name = "",
      suggestions = "npc"
   )
   @NotNull
   public Npc parse(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString();
      Npc npc;
      if (FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled() && context.sender() instanceof Player playerSender) {
         npc = this.npcManager.getNpc(value, playerSender.getUniqueId());
      } else if (isUUID(value)) {
         npc = this.npcManager.getNpcById(value);
      } else {
         npc = this.npcManager.getNpc(value);
      }

      return npc;
   }

   @Suggestions("npc")
   public List<String> suggestions(final CommandContext<C> context, final CommandInput input) {
      return FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled() && context.sender() instanceof Player playerSender
         ? this.npcManager
            .getAllNpcs()
            .stream()
            .filter(npc -> npc.getData().getCreator().equals(playerSender.getUniqueId()))
            .map(npc -> npc.getData().getName())
            .toList()
         : this.npcManager.getAllNpcs().stream().map(npc -> npc.getData().getName()).toList();
   }
}
