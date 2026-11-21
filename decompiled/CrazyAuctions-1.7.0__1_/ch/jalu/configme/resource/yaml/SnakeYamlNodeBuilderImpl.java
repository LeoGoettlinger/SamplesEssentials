package ch.jalu.configme.resource.yaml;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.properties.convertresult.ValueWithComments;
import ch.jalu.configme.utils.StreamUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public class SnakeYamlNodeBuilderImpl implements SnakeYamlNodeBuilder {
   private final Set<UUID> usedUniqueCommentIds = new HashSet<>();

   @NotNull
   @Override
   public Node createYamlNode(@NotNull Object obj, @NotNull String path, @NotNull ConfigurationData configurationData, int numberOfNewLines) {
      Object value = ValueWithComments.unwrapValue(obj);
      if (value instanceof Enum) {
         value = ((Enum)value).name();
      }

      Node node;
      if (value instanceof String) {
         node = this.createStringNode((String)value);
      } else if (value instanceof Number) {
         node = this.createNumberNode((Number)value);
      } else if (value instanceof Boolean) {
         node = this.createBooleanNode((Boolean)value);
      } else if (value instanceof Iterable) {
         Stream<?> stream = StreamSupport.stream(((Iterable)value).spliterator(), false);
         node = this.createSequenceNode(stream, path, configurationData);
      } else if (value instanceof Map) {
         node = this.createMapNode((Map<String, ?>)value, path, configurationData);
      } else {
         if (!(value instanceof Object[])) {
            throw new IllegalArgumentException("Unsupported value of type: " + (value == null ? null : value.getClass().getName()));
         }

         Stream<?> stream = Arrays.stream((Object[])value);
         node = this.createSequenceNode(stream, path, configurationData);
      }

      List<CommentLine> commentLines = this.collectComments(obj, path, configurationData, numberOfNewLines);
      node.setBlockComments(commentLines);
      return node;
   }

   @NotNull
   @Override
   public Node createKeyNode(@NotNull String key) {
      return this.createStringNode(key);
   }

   @NotNull
   @Override
   public Stream<CommentLine> createCommentLines(@NotNull String comment) {
      return "\n".equals(comment)
         ? Stream.of(new CommentLine(null, null, "", CommentType.BLANK_LINE))
         : Arrays.stream(comment.split("\\n", -1)).map(text -> new CommentLine(null, null, " ".concat(text), CommentType.BLOCK));
   }

   @Override
   public void transferComments(@NotNull Node valueNode, @NotNull Node keyNode) {
      if (valueNode.getBlockComments() != null && !valueNode.getBlockComments().isEmpty()) {
         keyNode.setBlockComments(valueNode.getBlockComments());
         valueNode.setBlockComments(Collections.emptyList());
      }
   }

   @NotNull
   protected Node createStringNode(@NotNull String value) {
      return new ScalarNode(Tag.STR, value, null, null, ScalarStyle.PLAIN);
   }

   @NotNull
   protected Node createNumberNode(@NotNull Number value) {
      Tag tag = !(value instanceof Double) && !(value instanceof Float) && !(value instanceof BigDecimal) ? Tag.INT : Tag.FLOAT;
      return new ScalarNode(tag, value.toString(), null, null, ScalarStyle.PLAIN);
   }

   @NotNull
   protected Node createBooleanNode(boolean value) {
      return new ScalarNode(Tag.BOOL, String.valueOf(value), null, null, ScalarStyle.PLAIN);
   }

   @NotNull
   protected Node createSequenceNode(@NotNull Stream<?> entries, @NotNull String path, @NotNull ConfigurationData configurationData) {
      AtomicInteger counter = new AtomicInteger();
      String pathPrefix = path.isEmpty() ? "" : path.concat(".");
      List<Node> values = entries.<Node>map(entry -> {
         String entryPath = pathPrefix.concat(Integer.toString(counter.getAndIncrement()));
         return this.createYamlNode(entry, entryPath, configurationData, 0);
      }).collect(Collectors.toList());
      return new SequenceNode(Tag.SEQ, values, FlowStyle.BLOCK);
   }

   @NotNull
   protected Node createMapNode(@NotNull Map<String, ?> value, String path, @NotNull ConfigurationData configurationData) {
      String pathPrefix = path.isEmpty() ? "" : path.concat(".");
      List<NodeTuple> nodeEntries = new ArrayList<>(value.size());

      for (Entry<String, ?> entry : value.entrySet()) {
         Node keyNode = this.createKeyNode(entry.getKey());
         Node valueNode = this.createYamlNode(entry.getValue(), pathPrefix.concat(entry.getKey()), configurationData, 0);
         this.transferComments(valueNode, keyNode);
         nodeEntries.add(new NodeTuple(keyNode, valueNode));
      }

      return new MappingNode(Tag.MAP, nodeEntries, FlowStyle.BLOCK);
   }

   @NotNull
   protected List<CommentLine> collectComments(@NotNull Object value, @NotNull String path, @NotNull ConfigurationData configurationData, int numberOfNewLines) {
      Stream<String> emptyLineStream = StreamUtils.repeat("\n", numberOfNewLines);
      Stream<String> configDataStream = configurationData.getCommentsForSection(path).stream();
      Stream<String> additionalCommentsStream = ValueWithComments.streamThroughCommentsIfApplicable(value, this.usedUniqueCommentIds);
      return Stream.of(emptyLineStream, configDataStream, additionalCommentsStream)
         .flatMap(Function.identity())
         .flatMap(this::createCommentLines)
         .collect(Collectors.toList());
   }

   @NotNull
   protected final Set<UUID> getUsedUniqueCommentIds() {
      return this.usedUniqueCommentIds;
   }
}
