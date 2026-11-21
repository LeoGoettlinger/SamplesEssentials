package org.jdbi.v3.core.statement;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import org.jdbi.v3.core.statement.internal.OptionalEvent;

@Category({"jdbi"})
@Name("jdbi.statement")
@Label("Jdbi Statement")
public final class JdbiStatementEvent extends Event implements OptionalEvent {
   public static final String NAME = "jdbi.statement";
   @Label("Type")
   public String type;
   @Label("SQL")
   public String sql;
   @Label("Parameters")
   public String parameters;
   @Label("Result rows mapped")
   public long rowsMapped;
   @Label("Trace ID")
   public String traceId;
}
