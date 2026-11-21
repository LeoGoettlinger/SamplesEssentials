package org.jdbi.v3.core.statement;

public class NoTemplateEngine implements TemplateEngine {
   @Override
   public String render(final String template, final StatementContext ctx) {
      return template;
   }
}
