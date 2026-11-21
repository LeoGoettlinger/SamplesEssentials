package org.jdbi.v3.sqlobject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.internal.JdbiClassUtils;

class SqlMethodAnnotatedHandlerDecorator implements HandlerDecorator {
   @Override
   public Handler decorateHandler(Handler delegate, Class<?> sqlObjectType, Method method) {
      Handler handler = delegate;
      List<? extends Class<? extends Annotation>> annotationTypes = Stream.of(method, sqlObjectType)
         .map(AnnotatedElement::getAnnotations)
         .flatMap(Arrays::stream)
         .map(Annotation::annotationType)
         .filter(type -> type.isAnnotationPresent(SqlMethodDecoratingAnnotation.class))
         .collect((Collector<? super Class<? extends Annotation>, ?, List<? extends Class<? extends Annotation>>>)Collectors.toCollection(ArrayList::new));
      SqlObjectAnnotationHelper.findAnnotation(DecoratorOrder.class, method, sqlObjectType)
         .ifPresent(order -> annotationTypes.sort(this.createDecoratorComparator(order).reversed()));

      for (HandlerDecorator decorator : annotationTypes.stream()
         .map(type -> type.getAnnotation(SqlMethodDecoratingAnnotation.class))
         .map(a -> JdbiClassUtils.checkedCreateInstance(a.value()))
         .toList()) {
         handler = decorator.decorateHandler(handler, sqlObjectType, method);
      }

      return handler;
   }

   private Comparator<Class<? extends Annotation>> createDecoratorComparator(DecoratorOrder order) {
      List<Class<? extends Annotation>> ordering = Arrays.asList(order.value());
      return Comparator.comparingInt(type -> {
         int index = ordering.indexOf(type);
         return index == -1 ? ordering.size() : index;
      });
   }
}
