package su.nightexpress.nightcore.commands.tree;

import java.util.Collection;
import java.util.Collections;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.context.Suggestions;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.core.config.CoreLang;

public class FlagNode extends CommandNode {
   private static final String PREFIX = "-";

   public FlagNode(@NonNull String name) {
      super(name, null, Collections.emptyList());
   }

   @NonNull
   @Override
   public Collection<? extends CommandNode> getRelevantNodes(@NonNull ArgumentReader reader) {
      return this.getChildren();
   }

   @Override
   public void parse(@NonNull ArgumentReader reader, @NonNull CommandContextBuilder contextBuilder) throws CommandSyntaxException {
      int cursor = reader.getCursor();
      String string = reader.getCursorArgument();
      contextBuilder.withNode(this, cursor);
      if (string.startsWith("-")) {
         if (string.length() >= 2) {
            String flagName = string.substring(1);
            if (contextBuilder.getExecutor() instanceof LiteralNode parent) {
               FlagNode realFlag = parent.getFlag(flagName);
               if (realFlag != null) {
                  contextBuilder.withFlag(realFlag.getName());
               }
            }
         }
      }
   }

   @Override
   protected void provideSuggestions(@NonNull ArgumentReader reader, @NonNull CommandContext context, @NonNull Suggestions suggestions) {
      if (context.getExecutor() instanceof LiteralNode parent) {
         suggestions.setSuggestions(parent.getFlags().stream().filter(flagNode -> !context.hasFlag(flagNode.getName())).map(FlagNode::getPrefixed).toList());
      }
   }

   @NonNull
   @Override
   public String getUsage() {
      return CoreLang.COMMAND_USAGE_OPTIONAL_FLAG.text().replace("%name%", this.getPrefixed());
   }

   @NonNull
   public String getPrefixed() {
      return "-" + this.name;
   }
}
