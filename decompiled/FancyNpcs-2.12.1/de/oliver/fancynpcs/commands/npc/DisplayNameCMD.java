package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotation.specifier.Greedy;
import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import java.util.List;
import java.util.stream.StreamSupport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.ClickEvent.Payload.Text;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum DisplayNameCMD {
   INSTANCE;

   private static final List<String> NONE_SUGGESTIONS = List.of("@none");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc displayname <npc> <name>")
   @Permission({"fancynpcs.command.npc.displayname"})
   public void onDisplayName(
      @NotNull final CommandSender sender, @NotNull final Npc npc, @Argument(suggestions = "DisplayNameCMD/none") @Greedy @NotNull final String name
   ) {
      String finalName = name.equalsIgnoreCase("@none") ? "<empty>" : name;
      if (this.hasBlockedCommands(finalName)) {
         this.translator.translate("command_input_contains_blocked_command").withPrefix().send(sender);
      } else {
         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.DISPLAY_NAME, finalName, sender).callEvent()) {
            npc.getData().setDisplayName(finalName);
            npc.updateForAll();
            this.translator
               .translate(finalName.equalsIgnoreCase("<empty>") ? "npc_displayname_set_empty" : "npc_displayname_set_name")
               .withPrefix()
               .replace("npc", npc.getData().getName())
               .replace("name", finalName)
               .send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      }
   }

   @Suggestions("DisplayNameCMD/none")
   public List<String> suggestNone(final CommandContext<CommandSender> sender, CommandInput input) {
      return NONE_SUGGESTIONS;
   }

   public boolean hasBlockedCommands(@NotNull final String message) {
      Component component = PaperColor.handler().translate(message);
      List<String> blockedCommands = FancyNpcs.getInstance().getFancyNpcConfig().getBlockedCommands();
      return StreamSupport.<Component>stream(component.iterable(ComponentIteratorType.DEPTH_FIRST, new ComponentIteratorFlag[0]).spliterator(), false)
         .anyMatch(it -> {
            ClickEvent event = it.clickEvent();
            if (event != null && event.action() == Action.RUN_COMMAND) {
               for (String blockedCommand : blockedCommands) {
                  String transformedBaseCommand = blockedCommand.replace('/', ' ').strip().split(" ")[0].replaceAll(".*?:+", "");
                  if (((Text)event.payload()).value().replace('/', ' ').strip().split(" ")[0].replaceAll(".*?:+", "").equalsIgnoreCase(transformedBaseCommand)) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         });
   }
}
