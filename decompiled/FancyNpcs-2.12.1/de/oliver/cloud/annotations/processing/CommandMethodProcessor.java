package de.oliver.cloud.annotations.processing;

import de.oliver.cloud.annotations.Command;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"de.oliver.cloud.annotations.Command"})
public final class CommandMethodProcessor extends AbstractProcessor {
   @Override
   public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
      Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Command.class);
      if (elements.isEmpty()) {
         return false;
      } else {
         for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
               element.accept(new CommandMethodVisitor(this.processingEnv), null);
            }
         }

         return false;
      }
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latest();
   }
}
