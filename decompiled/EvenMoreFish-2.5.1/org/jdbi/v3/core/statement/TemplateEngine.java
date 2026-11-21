package org.jdbi.v3.core.statement;

import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;

@FunctionalInterface
public interface TemplateEngine {
   TemplateEngine NOP = new NoTemplateEngine();

   String render(String template, StatementContext ctx);

   default Optional<Function<StatementContext, String>> parse(String template, ConfigRegistry config) {
      return Optional.empty();
   }

   @FunctionalInterface
   public interface Parsing extends TemplateEngine {
      @Override
      default String render(String template, StatementContext ctx) {
         return this.parse(template, ctx.getConfig())
            .orElseThrow(() -> new UnableToCreateStatementException("Caching template engine did not prepare"))
            .apply(ctx);
      }

      @Override
      Optional<Function<StatementContext, String>> parse(String template, ConfigRegistry config);
   }
}
