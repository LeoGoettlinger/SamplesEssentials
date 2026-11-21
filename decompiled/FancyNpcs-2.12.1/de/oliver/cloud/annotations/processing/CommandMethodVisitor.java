package de.oliver.cloud.annotations.processing;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.ArgumentMode;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.SyntaxFragment;
import de.oliver.cloud.annotations.SyntaxParserImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.NonNull;

class CommandMethodVisitor implements ElementVisitor<Void, Void> {
   private final ProcessingEnvironment processingEnvironment;
   private final SyntaxParserImpl syntaxParser;

   CommandMethodVisitor(@NonNull final ProcessingEnvironment processingEnvironment) {
      this.processingEnvironment = processingEnvironment;
      this.syntaxParser = new SyntaxParserImpl();
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
      return null;
   }

   public Void visitVariable(final VariableElement e, final Void unused) {
      return null;
   }

   public Void visitExecutable(final ExecutableElement e, final Void unused) {
      if (!e.getModifiers().contains(Modifier.PUBLIC)) {
         this.processingEnvironment
            .getMessager()
            .printMessage(Kind.WARNING, String.format("@Command annotated methods should be public (%s)", e.getSimpleName()), e);
      }

      if (e.getModifiers().contains(Modifier.STATIC)) {
         this.processingEnvironment
            .getMessager()
            .printMessage(Kind.ERROR, String.format("@Command annotated methods should be non-static (%s)", e.getSimpleName()), e);
      }

      if (e.getReturnType().toString().equals("Void")) {
         this.processingEnvironment
            .getMessager()
            .printMessage(Kind.ERROR, String.format("@Command annotated methods should return void (%s)", e.getSimpleName()), e);
      }

      Command[] commands = e.getAnnotationsByType(Command.class);

      for (Command command : commands) {
         List<String> annotatedArgumentNames = e.getParameters()
            .stream()
            .map(parameter -> parameter.getAnnotation(Argument.class))
            .filter(Objects::nonNull)
            .map(Argument::value)
            .filter(name -> !name.equals("__INFERRED_ARGUMENT_NAME__"))
            .collect(Collectors.toList());
         List<String> parameterArgumentNames = new ArrayList<>(annotatedArgumentNames);
         e.getParameters().stream().filter(parameter -> {
            Argument argumentx = parameter.getAnnotation(Argument.class);
            return argumentx == null || "__INFERRED_ARGUMENT_NAME__".equals(argumentx.value());
         }).map(parameter -> parameter.getSimpleName().toString()).forEach(parameterArgumentNames::add);
         List<String> parsedArgumentNames = new ArrayList<>(parameterArgumentNames.size());
         List<SyntaxFragment> syntaxFragments = this.syntaxParser.parseSyntax(null, command.value());
         boolean foundOptional = false;

         for (SyntaxFragment fragment : syntaxFragments) {
            if (fragment.argumentMode() != ArgumentMode.LITERAL) {
               if (!parameterArgumentNames.contains(fragment.major())) {
                  this.processingEnvironment
                     .getMessager()
                     .printMessage(Kind.ERROR, String.format("@Argument(\"%s\") is missing from @Command (%s)", fragment.major(), e.getSimpleName()), e);
               }

               if (fragment.argumentMode() == ArgumentMode.REQUIRED) {
                  if (foundOptional) {
                     this.processingEnvironment
                        .getMessager()
                        .printMessage(
                           Kind.ERROR, String.format("Required argument '%s' cannot succeed an optional argument (%s)", fragment.major(), e.getSimpleName()), e
                        );
                  }
               } else {
                  foundOptional = true;
               }

               parsedArgumentNames.add(fragment.major());
            }
         }

         for (String argument : annotatedArgumentNames) {
            if (!parsedArgumentNames.contains(argument)) {
               this.processingEnvironment
                  .getMessager()
                  .printMessage(Kind.ERROR, String.format("Argument '%s' is missing from the @Command syntax (%s)", argument, e.getSimpleName()), e);
            }
         }
      }

      return null;
   }

   public Void visitTypeParameter(final TypeParameterElement e, final Void unused) {
      return null;
   }

   public Void visitUnknown(final Element e, final Void unused) {
      return null;
   }
}
