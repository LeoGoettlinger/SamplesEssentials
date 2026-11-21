package de.exlll.configlib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

final class YamlWriter {
   private final OutputStream outputStream;
   private final YamlConfigurationProperties properties;
   private BufferedWriter writer;

   YamlWriter(OutputStream outputStream, YamlConfigurationProperties properties) {
      this.outputStream = Validator.requireNonNull(outputStream, "output stream");
      this.properties = Validator.requireNonNull(properties, "configuration properties");
   }

   YamlWriter(Path configurationFile, YamlConfigurationProperties properties) {
      Validator.requireNonNull(configurationFile, "configuration file");

      try {
         this.outputStream = Files.newOutputStream(configurationFile);
         this.properties = properties;
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public void writeYaml(String yaml, Queue<CommentNode> nodes) {
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.outputStream, this.properties.getCharset()))) {
         this.writer = writer;
         this.writeHeader();
         this.writeContent(yaml, nodes);
         this.writeFooter();
      } catch (IOException var13) {
         throw new RuntimeException(var13);
      } finally {
         this.writer = null;
      }
   }

   private void writeHeader() throws IOException {
      if (this.properties.getHeader() != null) {
         this.writeAsComment(this.properties.getHeader());
         this.writer.newLine();
      }
   }

   private void writeFooter() throws IOException {
      if (this.properties.getFooter() != null) {
         this.writer.newLine();
         this.writeAsComment(this.properties.getFooter());
      }
   }

   private void writeAsComment(String comment) throws IOException {
      String[] lines = comment.split("\n");
      this.writeComments(Arrays.asList(lines), 0);
   }

   private void writeComments(List<String> comments, int indentLevel) throws IOException {
      String indent = "  ".repeat(indentLevel);

      for (String comment : comments) {
         if (comment.isEmpty()) {
            this.writer.newLine();
         } else {
            String line = indent + "# " + comment;
            this.writeLine(line);
         }
      }
   }

   private void writeLine(String line) throws IOException {
      this.writer.write(line);
      this.writer.newLine();
   }

   private void writeContent(String yaml, Queue<CommentNode> nodes) throws IOException {
      if (nodes.isEmpty()) {
         this.writer.write(yaml);
      } else {
         this.writeCommentedYaml(yaml, nodes);
      }
   }

   private void writeCommentedYaml(String yaml, Queue<CommentNode> nodes) throws IOException {
      CommentNode node = nodes.poll();
      int currentIndentLevel = 0;

      for (String line : yaml.split("\n")) {
         if (node == null) {
            this.writeLine(line);
         } else {
            List<String> elementNames = node.elementNames();
            String indent = "  ".repeat(currentIndentLevel);
            String lineStart = indent + elementNames.get(currentIndentLevel) + ":";
            if (!line.startsWith(lineStart)) {
               this.writeLine(line);
            } else {
               int commentIndentLevel = elementNames.size() - 1;
               if (currentIndentLevel++ == commentIndentLevel) {
                  this.writeComments(node.comments(), commentIndentLevel);
                  if ((node = nodes.poll()) != null) {
                     currentIndentLevel = lengthCommonPrefix(node.elementNames(), elementNames);
                  }
               }

               this.writeLine(line);
            }
         }
      }
   }

   static int lengthCommonPrefix(List<String> l1, List<String> l2) {
      int maxLen = Math.min(l1.size(), l2.size());
      int result = 0;

      for (int i = 0; i < maxLen; i++) {
         String s1 = l1.get(i);
         String s2 = l2.get(i);
         if (!s1.equals(s2)) {
            return result;
         }

         result++;
      }

      return result;
   }
}
