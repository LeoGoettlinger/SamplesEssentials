package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

public final class Arithmetics {
   private static final Map<Operator, List<OperationInfo<?, ?, ?>>> OPERATIONS = Collections.synchronizedMap(new HashMap<>());
   private static final Map<Operator, Map<Arithmetics.OperandTypes, OperationInfo<?, ?, ?>>> CACHED_OPERATIONS = Collections.synchronizedMap(new HashMap<>());
   private static final Map<Operator, Map<Arithmetics.OperandTypes, OperationInfo<?, ?, ?>>> CACHED_CONVERTED_OPERATIONS = Collections.synchronizedMap(
      new HashMap<>()
   );
   private static final Map<Class<?>, DifferenceInfo<?, ?>> DIFFERENCES = Collections.synchronizedMap(new HashMap<>());
   private static final Map<Class<?>, DifferenceInfo<?, ?>> CACHED_DIFFERENCES = Collections.synchronizedMap(new HashMap<>());
   private static final Map<Class<?>, Supplier<?>> DEFAULT_VALUES = Collections.synchronizedMap(new HashMap<>());
   private static final Map<Class<?>, Supplier<?>> CACHED_DEFAULT_VALUES = Collections.synchronizedMap(new HashMap<>());

   public static <T> void registerOperation(Operator operator, Class<T> type, Operation<T, T, T> operation) {
      registerOperation(operator, type, type, type, operation);
   }

   public static <L, R> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Operation<L, R, L> operation) {
      registerOperation(operator, leftClass, rightClass, leftClass, operation);
   }

   public static <L, R> void registerOperation(
      Operator operator, Class<L> leftClass, Class<R> rightClass, Operation<L, R, L> operation, Operation<R, L, L> commutativeOperation
   ) {
      registerOperation(operator, leftClass, rightClass, leftClass, operation);
      registerOperation(operator, rightClass, leftClass, leftClass, commutativeOperation);
   }

   public static <L, R, T> void registerOperation(
      Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType, Operation<L, R, T> operation, Operation<R, L, T> commutativeOperation
   ) {
      registerOperation(operator, leftClass, rightClass, returnType, operation);
      registerOperation(operator, rightClass, leftClass, returnType, commutativeOperation);
   }

   public static <L, R, T> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType, Operation<L, R, T> operation) {
      Skript.checkAcceptRegistrations();
      if (exactOperationExists(operator, leftClass, rightClass)) {
         throw new SkriptAPIException(
            "There's already a " + operator.getName() + " operation registered for types '" + leftClass.getName() + "' and '" + rightClass.getName() + "'"
         );
      } else {
         getRawOperations(operator).add(new OperationInfo<>(leftClass, rightClass, returnType, operation));
      }
   }

   public static boolean exactOperationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
      for (OperationInfo<?, ?, ?> info : getRawOperations(operator)) {
         if (info.left() == leftClass && info.right() == rightClass) {
            return true;
         }
      }

      return false;
   }

   public static boolean operationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
      return getOperationInfo(operator, leftClass, rightClass) != null;
   }

   private static List<OperationInfo<?, ?, ?>> getRawOperations(Operator operator) {
      return OPERATIONS.computeIfAbsent(operator, o -> Collections.synchronizedList(new ArrayList<>()));
   }

   @UnmodifiableView
   public static List<OperationInfo<?, ?, ?>> getOperations(Operator operator) {
      return Collections.unmodifiableList(getRawOperations(operator));
   }

   @Unmodifiable
   public static <T> List<OperationInfo<T, ?, ?>> getOperations(Operator operator, Class<T> type) {
      return getOperations(operator).stream().filter(info -> info.left().isAssignableFrom(type)).collect(Collectors.toList());
   }

   @Unmodifiable
   public static <L> List<OperationInfo<L, ?, ?>> lookupLeftOperations(Operator operator, Class<L> leftClass) {
      return getOperations(operator)
         .stream()
         .map(info -> info.left().isAssignableFrom(leftClass) ? info : info.getConverted(leftClass, info.right(), info.returnType()))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   @Unmodifiable
   public static <R> List<OperationInfo<?, R, ?>> lookupRightOperations(Operator operator, Class<R> rightClass) {
      return getOperations(operator)
         .stream()
         .map(info -> info.right().isAssignableFrom(rightClass) ? info : info.getConverted(info.left(), rightClass, info.returnType()))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   @Nullable
   public static <L, R, T> OperationInfo<L, R, T> getOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
      OperationInfo<L, R, ?> info = getOperationInfo(operator, leftClass, rightClass);
      return (OperationInfo<L, R, T>)(info != null && returnType.isAssignableFrom(info.returnType()) ? info : null);
   }

   @Nullable
   public static <L, R> OperationInfo<L, R, ?> getOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass) {
      assertIsOperationsDoneLoading();
      Arithmetics.OperandTypes operandTypes = new Arithmetics.OperandTypes(leftClass, rightClass);
      Map<Arithmetics.OperandTypes, OperationInfo<?, ?, ?>> operations = CACHED_OPERATIONS.computeIfAbsent(
         operator, o -> Collections.synchronizedMap(new HashMap<>())
      );
      OperationInfo<L, R, ?> operationInfo = (OperationInfo<L, R, ?>)operations.get(operandTypes);
      if (operations.containsKey(operandTypes)) {
         return operationInfo;
      } else {
         operationInfo = (OperationInfo<L, R, ?>)getOperations(operator)
            .stream()
            .filter(info -> info.left().isAssignableFrom(leftClass) && info.right().isAssignableFrom(rightClass))
            .reduce((info, info2) -> info2.left() == leftClass && info2.right() == rightClass ? info2 : info)
            .orElse(null);
         operations.put(operandTypes, operationInfo);
         return operationInfo;
      }
   }

   @Nullable
   public static <L, R, T> Operation<L, R, T> getOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
      OperationInfo<L, R, T> info = getOperationInfo(operator, leftClass, rightClass, returnType);
      return info == null ? null : info.operation();
   }

   @Nullable
   public static <L, R> Operation<L, R, ?> getOperation(Operator operator, Class<L> leftClass, Class<R> rightClass) {
      OperationInfo<L, R, ?> info = getOperationInfo(operator, leftClass, rightClass);
      return info == null ? null : info.operation();
   }

   @Nullable
   public static <L, R, T> OperationInfo<L, R, T> lookupOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
      OperationInfo<L, R, ?> info = lookupOperationInfo(operator, leftClass, rightClass);
      return info != null ? info.getConverted(leftClass, rightClass, returnType) : null;
   }

   @Nullable
   public static <L, R> OperationInfo<L, R, ?> lookupOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass) {
      OperationInfo<L, R, ?> operationInfo = getOperationInfo(operator, leftClass, rightClass);
      if (operationInfo != null) {
         return operationInfo;
      } else {
         Arithmetics.OperandTypes operandTypes = new Arithmetics.OperandTypes(leftClass, rightClass);
         Map<Arithmetics.OperandTypes, OperationInfo<?, ?, ?>> operations = CACHED_CONVERTED_OPERATIONS.computeIfAbsent(
            operator, o -> Collections.synchronizedMap(new HashMap<>())
         );
         operationInfo = (OperationInfo<L, R, ?>)operations.get(operandTypes);
         if (operations.containsKey(operandTypes)) {
            return operationInfo;
         } else {
            for (OperationInfo<?, ?, ?> info : getOperations(operator)) {
               OperationInfo<L, R, ?> convertedInfo = info.getConverted(leftClass, rightClass, info.returnType());
               if (convertedInfo != null) {
                  operations.put(operandTypes, convertedInfo);
                  return convertedInfo;
               }
            }

            operations.put(operandTypes, null);
            return null;
         }
      }
   }

   @Nullable
   public static <L, R, T> T calculate(Operator operator, L left, R right, Class<T> returnType) {
      Operation<L, R, T> operation = getOperation(operator, (Class<L>)left.getClass(), (Class<R>)right.getClass(), returnType);
      return operation == null ? null : operation.calculate(left, right);
   }

   @Nullable
   public static <L, R, T> T calculateUnsafe(Operator operator, L left, R right) {
      Operation<L, R, T> operation = (Operation<L, R, T>)getOperation(operator, (Class<L>)left.getClass(), (Class<R>)right.getClass());
      return operation == null ? null : operation.calculate(left, right);
   }

   public static <T> void registerDifference(Class<T> type, Operation<T, T, T> operation) {
      registerDifference(type, type, operation);
   }

   public static <T, R> void registerDifference(Class<T> type, Class<R> returnType, Operation<T, T, R> operation) {
      Skript.checkAcceptRegistrations();
      if (exactDifferenceExists(type)) {
         throw new SkriptAPIException("There's already a difference registered for type '" + type + "'");
      } else {
         DIFFERENCES.put(type, new DifferenceInfo<>(type, returnType, operation));
      }
   }

   public static boolean exactDifferenceExists(Class<?> type) {
      return DIFFERENCES.containsKey(type);
   }

   public static boolean differenceExists(Class<?> type) {
      return getDifferenceInfo(type) != null;
   }

   @Nullable
   public static <T, R> DifferenceInfo<T, R> getDifferenceInfo(Class<T> type, Class<R> returnType) {
      DifferenceInfo<T, ?> info = getDifferenceInfo(type);
      return (DifferenceInfo<T, R>)(info != null && returnType.isAssignableFrom(info.returnType()) ? info : null);
   }

   @Nullable
   public static <T> DifferenceInfo<T, ?> getDifferenceInfo(Class<T> type) {
      if (Skript.isAcceptRegistrations()) {
         throw new SkriptAPIException("Differences cannot be retrieved until Skript has finished registrations.");
      } else if (CACHED_DIFFERENCES.containsKey(type)) {
         return (DifferenceInfo<T, ?>)CACHED_DIFFERENCES.get(type);
      } else {
         DifferenceInfo<T, ?> difference = null;
         if (DIFFERENCES.containsKey(type)) {
            difference = (DifferenceInfo<T, ?>)DIFFERENCES.get(type);
            CACHED_DIFFERENCES.put(type, difference);
            return difference;
         } else {
            for (Entry<Class<?>, DifferenceInfo<?, ?>> entry : DIFFERENCES.entrySet()) {
               if (entry.getKey().isAssignableFrom(type)) {
                  difference = (DifferenceInfo<T, ?>)entry.getValue();
                  break;
               }
            }

            CACHED_DIFFERENCES.put(type, difference);
            return difference;
         }
      }
   }

   @Nullable
   public static <T, R> Operation<T, T, R> getDifference(Class<T> type, Class<R> returnType) {
      DifferenceInfo<T, R> info = getDifferenceInfo(type, returnType);
      return info == null ? null : info.operation();
   }

   @Nullable
   public static <T> Operation<T, T, ?> getDifference(Class<T> type) {
      DifferenceInfo<T, ?> info = getDifferenceInfo(type);
      return info == null ? null : info.operation();
   }

   @Nullable
   public static <T, R> R difference(T left, T right, Class<R> returnType) {
      Operation<T, T, R> operation = getDifference((Class<T>)left.getClass(), returnType);
      return operation == null ? null : operation.calculate(left, right);
   }

   @Nullable
   public static <T, R> R differenceUnsafe(T left, T right) {
      Operation<T, T, R> operation = (Operation<T, T, R>)getDifference((Class<T>)left.getClass());
      return operation == null ? null : operation.calculate(left, right);
   }

   public static <T> void registerDefaultValue(Class<T> type, Supplier<T> supplier) {
      Skript.checkAcceptRegistrations();
      if (DEFAULT_VALUES.containsKey(type)) {
         throw new SkriptAPIException("There's already a default value registered for type '" + type.getName() + "'");
      } else {
         DEFAULT_VALUES.put(type, supplier);
      }
   }

   @Nullable
   public static <R, T extends R> R getDefaultValue(Class<T> type) {
      if (Skript.isAcceptRegistrations()) {
         throw new SkriptAPIException("Default values cannot be retrieved until Skript has finished registrations.");
      } else {
         Supplier<R> supplier = null;
         if (CACHED_DEFAULT_VALUES.containsKey(type)) {
            supplier = (Supplier<R>)CACHED_DEFAULT_VALUES.get(type);
            return supplier != null ? supplier.get() : null;
         } else if (DEFAULT_VALUES.containsKey(type)) {
            supplier = (Supplier<R>)DEFAULT_VALUES.get(type);
            CACHED_DEFAULT_VALUES.put(type, supplier);
            return supplier.get();
         } else {
            for (Entry<Class<?>, Supplier<?>> entry : DEFAULT_VALUES.entrySet()) {
               if (entry.getKey().isAssignableFrom(type)) {
                  supplier = (Supplier<R>)entry.getValue();
                  break;
               }
            }

            CACHED_DEFAULT_VALUES.put(type, supplier);
            return supplier != null ? supplier.get() : null;
         }
      }
   }

   private static void assertIsOperationsDoneLoading() {
      if (Skript.isAcceptRegistrations()) {
         throw new SkriptAPIException("Operations cannot be retrieved until Skript has finished registrations.");
      }
   }

   public static Collection<Class<?>> getAllReturnTypes(Operator operator) {
      Set<Class<?>> types = new HashSet<>();

      for (OperationInfo<?, ?, ?> info : getRawOperations(operator)) {
         types.add(info.returnType());
      }

      return types;
   }

   public static Set<Operator> getAllOperators() {
      List<Operator> operators = new LinkedList<>(OPERATIONS.keySet());
      Collections.sort(operators);
      return new LinkedHashSet<>(operators);
   }

   private Arithmetics() {
      throw new UnsupportedOperationException();
   }

   private record OperandTypes(Class<?> left, Class<?> right) {
   }
}
