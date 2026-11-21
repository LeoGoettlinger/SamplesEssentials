package de.oliver.cloud.annotations.processing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.NonNull;

@SupportedAnnotationTypes({"de.oliver.cloud.annotations.processing.CommandContainer"})
public final class CommandContainerProcessor extends AbstractProcessor {
   public static final String PATH = "META-INF/commands/org.incendo.cloud.annotations.processing.CommandContainer";

   @Override
   public boolean process(@NonNull final Set<? extends TypeElement> annotations, @NonNull final RoundEnvironment roundEnv) {
      List<String> validTypes = new ArrayList<>();
      Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CommandContainer.class);
      if (elements.isEmpty()) {
         return false;
      } else {
         for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
               this.processingEnv
                  .getMessager()
                  .printMessage(
                     Kind.ERROR,
                     String.format("@Command found on unsupported element type '%s' (%s)", element.getKind().name(), element.getSimpleName().toString()),
                     element
                  );
               return false;
            }

            element.accept(new CommandContainerVisitor(this.processingEnv, validTypes), null);
         }

         for (String type : validTypes) {
            this.processingEnv.getMessager().printMessage(Kind.NOTE, String.format("Found valid @CommandContainer-annotated class: %s", type));
         }

         this.writeCommandFile(validTypes);
         return false;
      }
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latest();
   }

   private void writeCommandFile(@NonNull final List<String> types) {
      try {
         BufferedWriter writer = new BufferedWriter(
            this.processingEnv
               .getFiler()
               .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/commands/org.incendo.cloud.annotations.processing.CommandContainer")
               .openWriter()
         );

         try {
            for (String t : types) {
               writer.write(t);
               writer.newLine();
            }

            writer.flush();
         } catch (Throwable var6) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         writer.close();
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }
}
