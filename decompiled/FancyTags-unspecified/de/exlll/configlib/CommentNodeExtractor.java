package de.exlll.configlib;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

final class CommentNodeExtractor {
   private final FieldFilter fieldFilter;
   private final NameFormatter nameFormatter;
   private final boolean outputNull;

   CommentNodeExtractor(ConfigurationProperties properties) {
      this.fieldFilter = Validator.requireNonNull(properties.getFieldFilter(), "field filter");
      this.nameFormatter = Validator.requireNonNull(properties.getNameFormatter(), "name formatter");
      this.outputNull = properties.outputNulls();
   }

   public Queue<CommentNode> extractCommentNodes(Object elementHolder) {
      Validator.requireConfigurationType(elementHolder.getClass());
      Queue<CommentNode> result = new ArrayDeque<>();
      ArrayDeque<String> elementNameStack = new ArrayDeque<>(List.of(""));
      ArrayDeque<CommentNodeExtractor.State> stateStack = new ArrayDeque<>(List.of(this.stateFromObject(elementHolder)));

      while (!stateStack.isEmpty()) {
         CommentNodeExtractor.State state = stateStack.removeLast();
         elementNameStack.removeLast();

         while (state.iterator.hasNext()) {
            ConfigurationElement<?> element = (ConfigurationElement<?>)state.iterator.next();
            Object elementValue = element.value(state.elementHolder);
            if (elementValue != null || this.outputNull) {
               String elementName = element.name();
               Optional<CommentNode> commentNode = this.createNodeIfCommentPresent(element.element(), elementName, elementNameStack);
               commentNode.ifPresent(result::add);
               Class<?> elementType = element.type();
               if (elementValue != null && Reflect.isConfigurationType(elementType)) {
                  stateStack.addLast(state);
                  elementNameStack.addLast(this.nameFormatter.format(elementName));
                  state = this.stateFromObject(elementValue);
               }
            }
         }
      }

      return result;
   }

   private CommentNodeExtractor.State stateFromObject(Object elementHolder) {
      Class<?> type = elementHolder.getClass();
      Iterator iter = type.isRecord() ? this.recordComponentElements(elementHolder) : this.fieldElements(elementHolder);
      return new CommentNodeExtractor.State(iter, elementHolder);
   }

   private Optional<CommentNode> createNodeIfCommentPresent(AnnotatedElement element, String elementName, Deque<String> elementNameStack) {
      if (element.isAnnotationPresent(Comment.class)) {
         List<String> comments = Arrays.stream(element.getAnnotation(Comment.class).value()).flatMap(s -> Arrays.stream(s.split("\n", -1))).toList();
         String formattedName = this.nameFormatter.format(elementName);
         ArrayList<String> elementNames = new ArrayList<>(elementNameStack);
         elementNames.add(formattedName);
         CommentNode result = new CommentNode(comments, elementNames);
         return Optional.of(result);
      } else {
         return Optional.empty();
      }
   }

   private Iterator<ConfigurationElements.FieldElement> fieldElements(Object configuration) {
      return FieldExtractors.CONFIGURATION.extract(configuration.getClass()).filter(this.fieldFilter).map(ConfigurationElements.FieldElement::new).iterator();
   }

   private Iterator<ConfigurationElements.RecordComponentElement> recordComponentElements(Object record) {
      return Arrays.stream(record.getClass().getRecordComponents()).map(ConfigurationElements.RecordComponentElement::new).iterator();
   }

   private record State(Iterator<? extends ConfigurationElement<?>> iterator, Object elementHolder) {
   }
}
