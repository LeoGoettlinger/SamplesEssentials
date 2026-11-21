package revxrsal.commands.core;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.InvalidHelpPageException;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.process.ContextResolver;

final class BaseCommandHelp<T> extends ArrayList<T> implements CommandHelp<T> {
   @Override
   public CommandHelp<T> paginate(int page, int elementsPerPage) throws InvalidHelpPageException {
      if (this.isEmpty()) {
         return new BaseCommandHelp<>();
      } else {
         BaseCommandHelp<T> list = new BaseCommandHelp<>();
         int size = this.getPageSize(elementsPerPage);
         if (page > size) {
            throw new InvalidHelpPageException(this, page, elementsPerPage);
         } else {
            int listIndex = page - 1;
            int l = Math.min(page * elementsPerPage, this.size());

            for (int i = listIndex * elementsPerPage; i < l; i++) {
               list.add(this.get(i));
            }

            return list;
         }
      }
   }

   @Range(
      from = 1L,
      to = Long.MAX_VALUE
   )
   @Override
   public int getPageSize(int elementsPerPage) {
      if (elementsPerPage < 1) {
         throw new IllegalArgumentException("Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
      } else {
         return this.size() / elementsPerPage + (this.size() % elementsPerPage == 0 ? 0 : 1);
      }
   }

   static final class Resolver implements ContextResolver<CommandHelp<?>> {
      private final BaseCommandHandler handler;

      public Resolver(BaseCommandHandler handler) {
         this.handler = handler;
      }

      public CommandHelp<?> resolve(@NotNull ContextResolver.ContextResolverContext context) {
         if (this.handler.getHelpWriter() == null) {
            throw new IllegalArgumentException("No help writer is registered!");
         } else {
            ExecutableCommand helpCommand = context.command();
            CommandHelpWriter<?> writer = this.handler.getHelpWriter();
            BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
            CommandCategory parent = helpCommand.getParent();
            CommandPath parentPath = parent == null ? null : parent.getPath();
            this.handler.executables.values().stream().sorted().forEach(c -> {
               if ((parentPath == null || parentPath.isParentOf(c.getPath())) && c != helpCommand) {
                  Object generated = writer.generate(c, context.actor());
                  if (generated != null) {
                     entries.add(generated);
                  }
               }
            });
            return entries;
         }
      }
   }
}
