package com.sk89q.worldedit.internal.expression.invoke;

import com.google.common.base.Throwables;
import com.sk89q.worldedit.antlr4.runtime.ParserRuleContext;
import com.sk89q.worldedit.antlr4.runtime.Token;
import com.sk89q.worldedit.internal.expression.CompiledExpression;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.ExecutionData;
import com.sk89q.worldedit.internal.expression.ExpressionHelper;
import com.sk89q.worldedit.internal.expression.LocalSlot;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class ExpressionHandles {
   static final MethodType COMPILED_EXPRESSION_SIG = MethodType.methodType(Double.class, ExecutionData.class);
   private static final MethodHandle EVAL_EXCEPTION_CONSTR;
   private static final MethodHandle CALL_EXPRESSION;
   private static final MethodHandle GET_VARIABLE;
   private static final MethodHandle WHILE_FOR_LOOP_IMPL;
   private static final MethodHandle DO_WHILE_LOOP_IMPL;
   private static final MethodHandle SIMPLE_FOR_LOOP_IMPL;
   private static final MethodHandle SWITCH_IMPL;
   static final MethodHandle IS_NULL;
   static final MethodHandle DOUBLE_TO_BOOL;
   static final MethodHandle CALL_BINARY_OP;
   static final MethodHandle NEW_LS_CONSTANT;
   static final MethodHandle NEW_RETURN_EXCEPTION;
   static final MethodHandle RETURN_EXCEPTION_GET_RESULT;
   static final MethodHandle NULL_DOUBLE = dropData(MethodHandles.constant(Double.class, null));

   static MethodHandle boxDoubles(MethodHandle handle) {
      MethodType type = handle.type();
      type = MethodType.methodType(
         boxIfPrimitiveDouble(type.returnType()), type.parameterList().stream().map(ExpressionHandles::boxIfPrimitiveDouble).collect(Collectors.toList())
      );
      return handle.asType(type);
   }

   private static Class<?> boxIfPrimitiveDouble(Class<?> clazz) {
      return clazz == double.class ? Double.class : clazz;
   }

   static MethodHandle unboxDoubles(MethodHandle handle) {
      MethodType type = handle.type();
      type = MethodType.methodType(
         unboxIfDouble(type.returnType()), type.parameterList().stream().map(ExpressionHandles::unboxIfDouble).collect(Collectors.toList())
      );
      return handle.asType(type);
   }

   private static Class<?> unboxIfDouble(Class<?> clazz) {
      return clazz == Double.class ? double.class : clazz;
   }

   static Object safeInvoke(MethodHandle handle, ExpressionHandles.Invokable invokable) {
      try {
         return invokable.invoke(handle);
      } catch (Throwable var3) {
         Throwables.throwIfUnchecked(var3);
         throw new RuntimeException(var3);
      }
   }

   static Object standardInvoke(MethodHandle handle, ExecutionData data) {
      return safeInvoke(handle, h -> (Object)h.invoke((ExecutionData)data));
   }

   static Object constantInvoke(MethodHandle handle) {
      return standardInvoke(handle, ExecutionData.CONSTANT_EVALUATOR);
   }

   static MethodHandle dropData(MethodHandle handle) {
      return MethodHandles.dropArguments(handle, 0, ExecutionData.class);
   }

   static MethodHandle dedupData(MethodHandle doubleData) {
      return MethodHandles.permuteArguments(doubleData, COMPILED_EXPRESSION_SIG, 0, 0);
   }

   static LocalSlot.Variable initVariable(ExecutionData data, Token nameToken) {
      String name = nameToken.getText();
      return data.slots().initVariable(name).orElseThrow(() -> ExpressionHelper.evalException(nameToken, "Cannot overwrite non-variable '" + name + "'"));
   }

   private static Supplier<EvaluationException> varNotInitException(Token nameToken) {
      return () -> ExpressionHelper.evalException(nameToken, "'" + nameToken.getText() + "' is not initialized yet");
   }

   static MethodHandle mhGetVariable(Token nameToken) {
      return MethodHandles.insertArguments(GET_VARIABLE, 1, nameToken);
   }

   static LocalSlot.Variable getVariable(ExecutionData data, Token nameToken) {
      String name = nameToken.getText();
      LocalSlot slot = data.slots().getSlot(name).orElseThrow(varNotInitException(nameToken));
      if (!(slot instanceof LocalSlot.Variable)) {
         throw ExpressionHelper.evalException(nameToken, "'" + name + "' is not a variable");
      } else {
         return (LocalSlot.Variable)slot;
      }
   }

   static double getSlotValue(ExecutionData data, Token nameToken) {
      String name = nameToken.getText();
      return data.slots().getSlotValue(name).orElseThrow(varNotInitException(nameToken));
   }

   private static MethodHandle evalException(ParserRuleContext ctx, String message) {
      return MethodHandles.insertArguments(EVAL_EXCEPTION_CONSTR, 0, ExpressionHelper.getErrorPosition(ctx.start), message);
   }

   static MethodHandle throwEvalException(ParserRuleContext ctx, String message) {
      return MethodHandles.collectArguments(MethodHandles.throwException(Double.class, EvaluationException.class), 0, evalException(ctx, message));
   }

   private static boolean doubleToBool(double bool) {
      return bool != 0.0;
   }

   static double boolToDouble(boolean bool) {
      return bool ? 1.0 : 0.0;
   }

   static MethodHandle call(CompiledExpression runnable) {
      return CALL_EXPRESSION.bindTo(runnable).asType(COMPILED_EXPRESSION_SIG);
   }

   static MethodHandle whileLoop(MethodHandle condition, ExecNode body) {
      return MethodHandles.insertArguments(WHILE_FOR_LOOP_IMPL, 1, null, condition, body, null);
   }

   static MethodHandle forLoop(MethodHandle init, MethodHandle condition, ExecNode body, MethodHandle update) {
      return MethodHandles.insertArguments(WHILE_FOR_LOOP_IMPL, 1, init, condition, body, update);
   }

   private static Double whileForLoopImpl(ExecutionData data, @Nullable MethodHandle init, MethodHandle condition, ExecNode body, @Nullable MethodHandle update) {
      Double result = null;
      int iterations = 0;
      if (init != null) {
         standardInvoke(init, data);
      }

      while (standardInvoke(condition, data)) {
         ExpressionHelper.checkIterations(iterations, body.positionInLine());
         data.checkDeadline();
         iterations++;

         try {
            result = (Double)standardInvoke(body.handle(), data);
         } catch (BreakException var8) {
            if (!var8.doContinue) {
               break;
            }
         }

         if (update != null) {
            standardInvoke(update, data);
         }
      }

      return result;
   }

   static MethodHandle doWhileLoop(MethodHandle condition, ExecNode body) {
      return MethodHandles.insertArguments(DO_WHILE_LOOP_IMPL, 1, condition, body);
   }

   private static Double doWhileLoopImpl(ExecutionData data, MethodHandle condition, ExecNode body) {
      Double result = null;
      int iterations = 0;

      do {
         ExpressionHelper.checkIterations(iterations, body.positionInLine());
         data.checkDeadline();
         iterations++;

         try {
            result = (Double)standardInvoke(body.handle(), data);
         } catch (BreakException var6) {
            if (!var6.doContinue) {
               break;
            }
         }
      } while (standardInvoke(condition, data));

      return result;
   }

   static MethodHandle simpleForLoop(MethodHandle first, MethodHandle last, Token counter, ExecNode body) {
      return MethodHandles.insertArguments(SIMPLE_FOR_LOOP_IMPL, 1, first, last, counter, body);
   }

   private static Double simpleForLoopImpl(ExecutionData data, MethodHandle getFirst, MethodHandle getLast, Token counterToken, ExecNode body) {
      Double result = null;
      int iterations = 0;
      double first = (Double)standardInvoke(getFirst, data);
      double last = (Double)standardInvoke(getLast, data);
      LocalSlot.Variable variable = initVariable(data, counterToken);

      for (double i = first; i <= last; i++) {
         ExpressionHelper.checkIterations(iterations, body.positionInLine());
         data.checkDeadline();
         iterations++;
         variable.setValue(i);

         try {
            result = (Double)standardInvoke(body.handle(), data);
         } catch (BreakException var15) {
            if (!var15.doContinue) {
               break;
            }
         }
      }

      return result;
   }

   static MethodHandle switchStatement(Double2ObjectMap<ExecNode> cases, MethodHandle getValue, @Nullable ExecNode defaultCase) {
      return MethodHandles.insertArguments(SWITCH_IMPL, 1, cases, getValue, defaultCase);
   }

   private static Double switchImpl(ExecutionData data, Double2ObjectMap<ExecNode> cases, MethodHandle getValue, @Nullable ExecNode defaultCase) {
      double value = (Double)standardInvoke(getValue, data);
      boolean matched = false;
      Double evaluated = null;
      boolean falling = false;
      ObjectIterator brk = Double2ObjectMaps.fastIterable(cases).iterator();

      while (brk.hasNext()) {
         Entry<ExecNode> entry = (Entry<ExecNode>)brk.next();
         if (falling || entry.getDoubleKey() == value) {
            matched = true;

            try {
               evaluated = (Double)standardInvoke(((ExecNode)entry.getValue()).handle(), data);
               falling = true;
            } catch (BreakException var13) {
               ExpressionHelper.check(!var13.doContinue, ((ExecNode)entry.getValue()).positionInLine(), "Cannot continue in a switch");
               falling = false;
               break;
            }
         }
      }

      if ((falling || !matched) && defaultCase != null) {
         try {
            evaluated = (Double)standardInvoke(defaultCase.handle(), data);
         } catch (BreakException var12) {
            ExpressionHelper.check(!var12.doContinue, defaultCase.positionInLine(), "Cannot continue in a switch");
         }
      }

      return evaluated;
   }

   private ExpressionHandles() {
   }

   static {
      Lookup lookup = MethodHandles.lookup();

      try {
         EVAL_EXCEPTION_CONSTR = lookup.findConstructor(EvaluationException.class, MethodType.methodType(void.class, int.class, String.class));
         CALL_EXPRESSION = lookup.findVirtual(CompiledExpression.class, "execute", MethodType.methodType(Double.class, ExecutionData.class));
         GET_VARIABLE = lookup.findStatic(
            ExpressionHandles.class, "getVariable", MethodType.methodType(LocalSlot.Variable.class, ExecutionData.class, Token.class)
         );
         WHILE_FOR_LOOP_IMPL = lookup.findStatic(
            ExpressionHandles.class,
            "whileForLoopImpl",
            MethodType.methodType(Double.class, ExecutionData.class, MethodHandle.class, MethodHandle.class, ExecNode.class, MethodHandle.class)
         );
         DO_WHILE_LOOP_IMPL = lookup.findStatic(
            ExpressionHandles.class, "doWhileLoopImpl", MethodType.methodType(Double.class, ExecutionData.class, MethodHandle.class, ExecNode.class)
         );
         SIMPLE_FOR_LOOP_IMPL = lookup.findStatic(
            ExpressionHandles.class,
            "simpleForLoopImpl",
            MethodType.methodType(Double.class, ExecutionData.class, MethodHandle.class, MethodHandle.class, Token.class, ExecNode.class)
         );
         SWITCH_IMPL = lookup.findStatic(
            ExpressionHandles.class,
            "switchImpl",
            MethodType.methodType(Double.class, ExecutionData.class, Double2ObjectMap.class, MethodHandle.class, ExecNode.class)
         );
         IS_NULL = lookup.findStatic(Objects.class, "isNull", MethodType.methodType(boolean.class, Object.class));
         DOUBLE_TO_BOOL = boxDoubles(lookup.findStatic(ExpressionHandles.class, "doubleToBool", MethodType.methodType(boolean.class, double.class)));
         CALL_BINARY_OP = lookup.findVirtual(DoubleBinaryOperator.class, "applyAsDouble", MethodType.methodType(double.class, double.class, double.class))
            .asType(MethodType.methodType(Double.class, DoubleBinaryOperator.class, double.class, double.class));
         NEW_LS_CONSTANT = lookup.findConstructor(LocalSlot.Constant.class, MethodType.methodType(void.class, double.class));
         NEW_RETURN_EXCEPTION = lookup.findConstructor(ReturnException.class, MethodType.methodType(void.class, Double.class));
         RETURN_EXCEPTION_GET_RESULT = lookup.findVirtual(ReturnException.class, "getResult", MethodType.methodType(Double.class));
      } catch (IllegalAccessException | NoSuchMethodException var2) {
         throw new IllegalStateException(var2);
      }
   }

   @FunctionalInterface
   interface Invokable {
      Object invoke(MethodHandle handle) throws Throwable;
   }
}
