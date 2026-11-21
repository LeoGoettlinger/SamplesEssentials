package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Default;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.MultiMessage;
import de.oliver.fancynpcs.FancyNpcs;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum HelpCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc help [page]")
   @Permission({"fancynpcs.command.npc"})
   public void onHelp(@NotNull final CommandSender sender, @Argument(suggestions = "HelpCMD/page") @Default("1") final int page) {
      MultiMessage contents = (MultiMessage)this.translator.translate("npc_help_contents");
      int maxPage = (int)Math.ceil(contents.getRawMessages().size() / 6.0F);
      int finalPage = Math.clamp((long)page, 1, maxPage);
      this.translator.translate("npc_help_page_header").replace("page", String.valueOf(finalPage)).replace("max_page", String.valueOf(maxPage)).send(sender);
      contents.page(finalPage, 6).send(sender);
      this.translator.translate("npc_help_page_footer").replace("page", String.valueOf(finalPage)).replace("max_page", String.valueOf(maxPage)).send(sender);
   }

   @Suggestions("HelpCMD/page")
   public List<String> suggestPage(final CommandContext<CommandSender> context, final CommandInput input) {
      MultiMessage contents = (MultiMessage)this.translator.translate("npc_help_contents");
      final int maxPage = contents.getRawMessages().size() / 6 + 1;
      return new ArrayList<String>() {
         {
            Objects.requireNonNull(HelpCMD.this);

            for (int i = 1; i <= maxPage; i++) {
               this.add(String.valueOf(i));
            }
         }
      };
   }
}
