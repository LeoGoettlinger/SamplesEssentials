package org.jdbi.v3.core.statement;

import java.util.function.Function;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.meta.Beta;

@Beta
public class StatementExceptions implements JdbiConfig<StatementExceptions> {
   private Function<StatementException, String> messageRendering = StatementExceptions.MessageRendering.SHORT_STATEMENT;
   private int lengthLimit = 1024;

   public StatementExceptions() {
   }

   private StatementExceptions(StatementExceptions other) {
      this.messageRendering = other.messageRendering;
      this.lengthLimit = other.lengthLimit;
   }

   public int getLengthLimit() {
      return this.lengthLimit;
   }

   public StatementExceptions setLengthLimit(int lengthLimit) {
      this.lengthLimit = lengthLimit;
      return this;
   }

   public Function<StatementException, String> getMessageRendering() {
      return this.messageRendering;
   }

   public StatementExceptions setMessageRendering(Function<StatementException, String> messageRendering) {
      this.messageRendering = messageRendering;
      return this;
   }

   public StatementExceptions createCopy() {
      return new StatementExceptions(this);
   }

   protected static String limit(String s, int len) {
      if (s == null) {
         return null;
      } else {
         String truncated = s.substring(0, Math.min(len, s.length()));
         boolean isTruncated = len < s.length();
         if (isTruncated) {
            truncated = truncated + "[...]";
         }

         return truncated;
      }
   }

   public static enum MessageRendering implements Function<StatementException, String> {
      NONE {
         @Override
         public String render(StatementException exc, StatementContext ctx) {
            return exc.getShortMessage();
         }
      },
      PARAMETERS {
         @Override
         public String render(StatementException exc, StatementContext ctx) {
            return String.format("%s [arguments:%s]", exc.getShortMessage(), ctx.getBinding());
         }
      },
      SHORT_STATEMENT {
         @Override
         public String render(StatementException exc, StatementContext ctx) {
            int limit = ctx.getConfig(StatementExceptions.class).getLengthLimit();
            return String.format(
               "%s [statement:\"%s\", arguments:%s]",
               exc.getShortMessage(),
               StatementExceptions.limit(ctx.getRenderedSql(), limit),
               StatementExceptions.limit(ctx.getBinding().toString(), limit)
            );
         }
      },
      DETAIL {
         @Override
         public String render(StatementException exc, StatementContext ctx) {
            return String.format(
               "%s [statement:\"%s\", rewritten:\"%s\", parsed:\"%s\", arguments:%s]",
               exc.getShortMessage(),
               ctx.getRawSql(),
               ctx.getRenderedSql(),
               ctx.getParsedSql(),
               ctx.getBinding()
            );
         }
      };

      public String apply(StatementException exc) {
         StatementContext ctx = exc.getStatementContext();
         return ctx == null ? NONE.render(exc, null) : this.render(exc, ctx);
      }

      protected abstract String render(StatementException exc, StatementContext ctx);
   }
}
