package de.oliver.cloud.annotations.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandContainerVisitor implements ElementVisitor<Void, Void> {
   private static final Collection<String> PERMITTED_CONSTRUCTOR_PARAMETER_TYPES = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList("de.oliver.cloud.annotations.AnnotationParser"))
   );
   private final ProcessingEnvironment processingEnvironment;
   private final Collection<String> validTypes;
   private final Map<Element, List<String>> errorsByElement = new HashMap<>();
   private boolean suitableConstructorFound;

   CommandContainerVisitor(@NonNull final ProcessingEnvironment processingEnvironment, @NonNull final Collection<String> validTypes) {
      this.processingEnvironment = processingEnvironment;
      this.validTypes = validTypes;
      this.suitableConstructorFound = false;
   }

   public Void visit(final Element e) {
      return this.visit(e, null);
   }

   public Void visit(final Element e, final Void unused) {
      return null;
   }

   public Void visitPackage(final PackageElement e, final Void unused) {
      return null;
   }

   public Void visitType(final TypeElement e, final Void unused) {
      if (!e.getModifiers().contains(Modifier.PUBLIC)) {
         this.processingEnvironment
            .getMessager()
            .printMessage(Kind.ERROR, String.format("@CommandContainer-annotated class must be public (%s)", e.getSimpleName()), e);
         return null;
      } else {
         for (Element enclosedElement : e.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
               enclosedElement.accept(this, null);
               if (this.suitableConstructorFound) {
                  break;
               }
            }
         }

         if (this.suitableConstructorFound) {
            this.validTypes.add(e.asType().toString());
            return null;
         } else {
            this.processingEnvironment
               .getMessager()
               .printMessage(Kind.ERROR, String.format("@CommandContainer-annotated class must have a suitable constructor (%s)", e.getSimpleName()), e);

            for (Entry<Element, List<String>> entry : this.errorsByElement.entrySet()) {
               this.processingEnvironment
                  .getMessager()
                  .printMessage(Kind.WARNING, "Constructor not suitable: " + String.join(", ", entry.getValue()), entry.getKey());
            }

            return null;
         }
      }
   }

   public Void visitVariable(final VariableElement e, final Void unused) {
      return null;
   }

   private void logError(final Element element, final String err) {
      this.errorsByElement.computeIfAbsent(element, $ -> new ArrayList<>()).add(err);
   }

   private void logInvalidParam(final VariableElement param) {
      List<String> errors = this.errorsByElement.computeIfAbsent(param, $ -> new ArrayList<>());
      String allowedParams = "Recognized parameter types: " + PERMITTED_CONSTRUCTOR_PARAMETER_TYPES;
      if (!errors.contains(allowedParams)) {
         errors.add(allowedParams);
      }

      errors.add("Parameter '" + param + "' is not a recognized type");
   }

   public Void visitExecutable(final ExecutableElement e, final Void unused) {
      if (!e.getModifiers().contains(Modifier.PUBLIC)) {
         this.logError(e, "Is not public.");
         return null;
      } else {
         boolean illegalParams = false;

         for (VariableElement variableElement : e.getParameters()) {
            boolean isDeclared = variableElement.asType() instanceof DeclaredType;
            if (!isDeclared) {
               this.logInvalidParam(variableElement);
               illegalParams = true;
            } else {
               DeclaredType type = (DeclaredType)variableElement.asType();
               Element typeElement = type.asElement();
               String typeName = typeElement.asType().toString().split("<")[0];
               if (!PERMITTED_CONSTRUCTOR_PARAMETER_TYPES.contains(typeName)) {
                  this.logInvalidParam(variableElement);
                  illegalParams = true;
               }
            }
         }

         if (illegalParams) {
            return null;
         } else {
            this.suitableConstructorFound = true;
            return null;
         }
      }
   }

   public Void visitTypeParameter(final TypeParameterElement e, final Void unused) {
      return null;
   }

   public Void visitUnknown(final Element e, final Void unused) {
      return null;
   }
}
