package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Experimental
public interface PropertyBaseSyntax<Handler extends PropertyHandler<?>> {
   @Nullable
   default String getBadTypesErrorMessage(@NotNull Expression<?> expr) {
      expr = LiteralUtils.defendExpression(expr);
      List<ClassInfo<?>> invalidTypes = new ArrayList<>();

      for (Class<?> type : expr.possibleReturnTypes()) {
         ClassInfo<?> info = Classes.getSuperClassInfo(type);
         if (!info.hasProperty(this.getProperty())) {
            invalidTypes.add(info);
         }
      }

      return "The expression '"
         + expr
         + "' returns the following types that do not have the "
         + this.getPropertyName()
         + " property: "
         + Classes.toString(invalidTypes.toArray(), true);
   }

   @NotNull
   Property<Handler> getProperty();

   default String getPropertyName() {
      return this.getProperty().name();
   }

   @Nullable
   static Expression<?> asProperty(Property<?> property, Expression<?> expr) {
      if (expr == null) {
         return null;
      } else {
         Set<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);
         Class<?>[] classes = classInfos.stream().map(ClassInfo::getC).toArray(Class[]::new);
         return classes.length == 0 ? null : LiteralUtils.defendExpression(expr).getConvertedExpression(classes);
      }
   }

   static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(Property<Handler> property, Expression<?> expr) {
      PropertyMap<Handler> propertyInfos = new PropertyMap<>();
      Set<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);

      for (Class<?> returnType : expr.possibleReturnTypes()) {
         ClassInfo<?> closestInfo = null;

         for (ClassInfo<?> propertiedClassInfo : classInfos) {
            if (propertiedClassInfo.getC() == returnType) {
               closestInfo = propertiedClassInfo;
               break;
            }

            if (propertiedClassInfo.getC().isAssignableFrom(returnType)
               && (closestInfo == null || closestInfo.getC().isAssignableFrom(propertiedClassInfo.getC()))) {
               closestInfo = propertiedClassInfo;
            }
         }

         if (closestInfo != null) {
            Property.PropertyInfo<Handler> propertyInfo = closestInfo.getPropertyInfo(property);
            if (propertyInfo != null) {
               PropertyHandler<?> clonedHandler = propertyInfo.handler().newInstance();
               if (clonedHandler.init(expr, expr.getParser())) {
                  propertyInfo = new Property.PropertyInfo<>(propertyInfo.property(), (Handler)clonedHandler);
               } else {
                  propertyInfo = null;
               }
            }

            ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
            propertyInfos.put(classInfo.getC(), propertyInfo);
            propertyInfos.put(closestInfo.getC(), propertyInfo);
         }
      }

      return propertyInfos;
   }
}
