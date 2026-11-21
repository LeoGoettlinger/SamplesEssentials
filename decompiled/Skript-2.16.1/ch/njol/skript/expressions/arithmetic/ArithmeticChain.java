package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Priority;

public class ArithmeticChain<L, R, T> implements ArithmeticGettable<T> {
   private static List<Set<Operator>> operatorGroups = null;
   private final ArithmeticGettable<L> left;
   private final ArithmeticGettable<R> right;
   private final Operator operator;
   private final Class<? extends T> returnType;
   @Nullable
   private final OperationInfo<? extends L, ? extends R, ? extends T> operationInfo;

   public ArithmeticChain(ArithmeticGettable<L> left, Operator operator, ArithmeticGettable<R> right, @Nullable OperationInfo<L, R, T> operationInfo) {
      this.left = left;
      this.right = right;
      this.operator = operator;
      this.operationInfo = operationInfo;
      this.returnType = operationInfo != null ? operationInfo.returnType() : Object.class;
   }

   @Nullable
   @Override
   public T get(Event event) {
      L left = this.left.get(event);
      if (left == null && this.left instanceof ArithmeticChain) {
         return null;
      } else {
         R right = this.right.get(event);
         if (right == null && this.right instanceof ArithmeticChain) {
            return null;
         } else {
            Class<? extends L> leftClass = (Class<? extends L>)(left != null ? left.getClass() : this.left.getReturnType());
            Class<? extends R> rightClass = (Class<? extends R>)(right != null ? right.getClass() : this.right.getReturnType());
            if (leftClass == Object.class && rightClass == Object.class) {
               return null;
            } else {
               OperationInfo<? extends L, ? extends R, ? extends T> operationInfo = this.operationInfo;
               if (left == null && leftClass == Object.class) {
                  operationInfo = this.lookupOperationInfo(rightClass, OperationInfo::right);
               } else if (right == null && rightClass == Object.class) {
                  operationInfo = this.lookupOperationInfo(leftClass, OperationInfo::left);
               } else if (operationInfo == null) {
                  operationInfo = Arithmetics.lookupOperationInfo(this.operator, leftClass, rightClass, this.returnType);
               }

               if (operationInfo == null) {
                  return null;
               } else {
                  left = left != null ? left : Arithmetics.getDefaultValue(operationInfo.left());
                  if (left == null) {
                     return null;
                  } else {
                     right = right != null ? right : Arithmetics.getDefaultValue(operationInfo.right());
                     return (T)(right == null ? null : operationInfo.operation().calculate(left, right));
                  }
               }
            }
         }
      }
   }

   @Nullable
   private OperationInfo<L, R, T> lookupOperationInfo(Class<?> anchor, Function<OperationInfo<?, ?, ?>, Class<?>> anchorFunction) {
      OperationInfo<?, ?, ?> operationInfo = Arithmetics.lookupOperationInfo(this.operator, anchor, anchor);
      return (OperationInfo<L, R, T>)(operationInfo != null
         ? operationInfo
         : (OperationInfo)Arithmetics.getOperations(this.operator)
            .stream()
            .filter(info -> anchorFunction.apply((OperationInfo<?, ?, ?>)info).isAssignableFrom(anchor))
            .filter(info -> Converters.converterExists(info.returnType(), this.returnType))
            .reduce((info, info2) -> anchorFunction.apply(info2) == anchor ? info2 : info)
            .orElse(null));
   }

   @Override
   public Class<? extends T> getReturnType() {
      return this.returnType;
   }

   private static void createOperatorGroups() {
      if (operatorGroups != null) {
         throw new RuntimeException("Operator groups have already been created");
      } else {
         operatorGroups = new LinkedList<>();
         List<Operator> operators = new LinkedList<>(Arithmetics.getAllOperators());
         Collections.reverse(operators);
         if (!operators.isEmpty()) {
            Set<Operator> currentGroup = new HashSet<>();
            Priority currentGroupPriority = operators.get(0).priority();

            for (Operator operator : operators) {
               if (operator.priority().compareTo(currentGroupPriority) != 0) {
                  if (!currentGroup.isEmpty()) {
                     operatorGroups.add(currentGroup);
                  }

                  currentGroup = new HashSet<>();
                  currentGroupPriority = operator.priority();
               }

               currentGroup.add(operator);
            }

            operatorGroups.add(currentGroup);
         }
      }
   }

   @Nullable
   public static <L, R, T> ArithmeticGettable<T> parse(List<?> chain) {
      if (operatorGroups == null) {
         createOperatorGroups();
      }

      for (Set<?> group : operatorGroups) {
         int lastIndex = Utils.findLastIndex(chain, group::contains);
         if (lastIndex != -1) {
            ArithmeticGettable<L> left = parse(chain.subList(0, lastIndex));
            Operator operator = (Operator)chain.get(lastIndex);
            ArithmeticGettable<R> right = parse(chain.subList(lastIndex + 1, chain.size()));
            if (left != null && right != null) {
               OperationInfo<L, R, T> operationInfo = null;
               if (left.getReturnType() != Object.class && right.getReturnType() != Object.class) {
                  operationInfo = (OperationInfo<L, R, T>)Arithmetics.lookupOperationInfo(
                     operator, (Class<L>)left.getReturnType(), (Class<R>)right.getReturnType()
                  );
                  if (operationInfo == null) {
                     return null;
                  }
               }

               return new ArithmeticChain<>(left, operator, right, operationInfo);
            }

            return null;
         }
      }

      if (chain.size() != 1) {
         throw new IllegalStateException();
      } else {
         return new ArithmeticExpressionInfo<>((Expression<? extends T>)chain.get(0));
      }
   }
}
