package org.jdbi.v3.core.statement;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.internal.exceptions.Unchecked;

class DefineNamedBindingsStatementCustomizer implements StatementCustomizer {
   @Override
   public void beforeTemplating(PreparedStatement stmt, StatementContext ctx) {
      Set<String> alreadyDefined = ctx.getAttributes().keySet();
      Binding binding = ctx.getBinding();
      DefineNamedBindingsStatementCustomizer.SetNullHandler handler = new DefineNamedBindingsStatementCustomizer.SetNullHandler(ctx);
      binding.getNames()
         .stream()
         .filter(name -> !alreadyDefined.contains(name))
         .forEach(name -> binding.findForName(name, ctx).ifPresent(a -> handler.define(name, a)));
   }

   private static class SetNullHandler implements InvocationHandler {
      private static final Map<Class<?>, Object> DEFAULT_VALUES = Stream.of(
            boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class
         )
         .collect(Collectors.toMap(Function.identity(), DefineNamedBindingsStatementCustomizer.SetNullHandler::defaultValue));
      private final StatementContext ctx;
      private final PreparedStatement fakeStmt = (PreparedStatement)Proxy.newProxyInstance(
         Thread.currentThread().getContextClassLoader(), new Class[]{PreparedStatement.class}, this
      );
      private boolean setNull;
      private boolean setCalled;

      SetNullHandler(StatementContext ctx) {
         this.ctx = ctx;
      }

      private static Object defaultValue(Class<?> clazz) {
         return Array.get(Array.newInstance(clazz, 1), 0);
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws SQLException {
         if ("unwrap".equals(method.getName()) && args.length == 1 && method.getParameterTypes()[0].equals(Class.class)) {
            throw new SQLException(
               "The current implementation of DefineNamedBindings is incompatible with arguments that rely on java.sql.Wrapper.unwrap(Class<?>)"
            );
         } else if ("getConnection".equals(method.getName())) {
            return this.ctx.getConnection();
         } else {
            if (method.getName().startsWith("set")) {
               this.setCalled = true;
               boolean argNull = args.length > 1 && args[1] == null;
               this.setNull = argNull || "setNull".equals(method.getName());
            }

            return DEFAULT_VALUES.get(method.getReturnType());
         }
      }

      void define(String name, Argument arg) {
         this.setNull = false;
         this.setCalled = false;
         Unchecked.runnable(() -> arg.apply(1, this.fakeStmt, this.ctx)).run();
         if (this.setCalled) {
            this.ctx.define(name, !this.setNull);
         }
      }
   }
}
