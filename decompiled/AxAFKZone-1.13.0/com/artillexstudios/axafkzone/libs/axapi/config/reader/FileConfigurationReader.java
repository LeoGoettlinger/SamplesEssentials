package com.artillexstudios.axafkzone.libs.axapi.config.reader;

import com.artillexstudios.axafkzone.libs.axapi.config.YamlConstructor;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axafkzone.libs.axapi.config.annotation.Comment;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.Yaml;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.comments.CommentLine;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.comments.CommentType;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.AnchorNode;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.MappingNode;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.Node;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.NodeTuple;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.Tag;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.reader.UnicodeReader;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import it.unimi.dsi.fastutil.Pair;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class FileConfigurationReader implements Handler {
   private final TypeAdapterHolder holder;
   private final YamlConstructor constructor;
   private final Yaml yaml;

   public FileConfigurationReader(Yaml yaml, YamlConstructor constructor, TypeAdapterHolder holder) {
      this.holder = holder;
      this.constructor = constructor;
      this.yaml = yaml;
   }

   @Override
   public Pair<Map<String, Object>, Map<String, Comment>> read(InputStream stream, Object instance) {
      LinkedHashMap<String, Object> contents = new LinkedHashMap<>();
      Map<String, Comment> comments = new HashMap<>();
      this.load("", (MappingNode)this.yaml.compose(new UnicodeReader(stream)), contents, comments);
      return Pair.of(contents, comments);
   }

   @Override
   public String write(Map<String, Object> contents, Map<String, Comment> comments) {
      StringWriter writer = new StringWriter();
      this.yaml.serialize(this.map(contents, comments, ""), writer);
      return writer.toString();
   }

   private MappingNode map(Map<String, Object> map, Map<String, Comment> comments, String path) {
      List<NodeTuple> nodes = new ArrayList<>();

      for (Entry<String, Object> entry : map.entrySet()) {
         Node key = this.yaml.represent(entry.getKey());
         Node value;
         if (entry.getValue() instanceof Map<?, ?> m) {
            value = this.map(UncheckedUtils.unsafeCast(m), comments, path.isEmpty() ? entry.getKey() : path + "." + entry.getKey());
         } else {
            value = this.yaml.represent(entry.getValue());
         }

         Comment comment = comments.get(path.isEmpty() ? entry.getKey() : path + "." + entry.getKey());
         if (comment != null) {
            List<CommentLine> lines = new ArrayList<>();
            String[] split = comment.value().split("\n");

            for (String string : split) {
               lines.add(new CommentLine(null, null, string, comment.type() == Comment.CommentType.BLOCK ? CommentType.BLOCK : CommentType.IN_LINE));
            }

            if (comment.type() == Comment.CommentType.BLOCK) {
               key.setBlockComments(lines);
            } else {
               key.setInLineComments(lines);
            }
         }

         nodes.add(new NodeTuple(key, value));
      }

      return new MappingNode(Tag.MAP, nodes, DumperOptions.FlowStyle.BLOCK);
   }

   private void load(String path, MappingNode node, LinkedHashMap<String, Object> map, Map<String, Comment> comments) {
      if (node != null) {
         this.constructor.flatten(node);

         for (NodeTuple nodeTuple : node.getValue()) {
            Node key = nodeTuple.getKeyNode();
            String keyString = String.valueOf(this.constructor.construct(key));
            Node value = nodeTuple.getValueNode();

            while (value instanceof AnchorNode) {
               AnchorNode anchorNode = (AnchorNode)value;
               value = anchorNode.getRealNode();
            }

            String path1 = path.isEmpty() ? keyString : path + "." + keyString;
            if (value instanceof MappingNode mappingNode) {
               LinkedHashMap<String, Object> newSection = new LinkedHashMap<>();
               map.put(keyString, newSection);
               this.load(keyString, mappingNode, newSection, comments);
            } else {
               map.put(keyString, this.constructor.construct(value));
            }

            List<CommentLine> blockComments = key.getBlockComments();
            if (blockComments != null) {
               final StringBuilder commentValue = new StringBuilder();

               for (int i = 0; i < blockComments.size(); i++) {
                  CommentLine blockComment = blockComments.get(i);
                  commentValue.append(blockComment.getValue());
                  if (i + 1 < blockComments.size()) {
                     commentValue.append('\n');
                  }
               }

               if (!commentValue.isEmpty()) {
                  comments.put(path1, new Comment() {
                     @Override
                     public Class<? extends Annotation> annotationType() {
                        return Comment.class;
                     }

                     @Override
                     public String value() {
                        return commentValue.toString();
                     }

                     @Override
                     public Comment.CommentType type() {
                        return Comment.CommentType.BLOCK;
                     }
                  });
               }
            }

            List<CommentLine> inlineComments = key.getInLineComments();
            if (inlineComments != null) {
               final StringBuilder commentValue = new StringBuilder();

               for (int ix = 0; ix < inlineComments.size(); ix++) {
                  CommentLine comment = inlineComments.get(ix);
                  commentValue.append(comment.getValue());
                  if (ix + 1 < inlineComments.size()) {
                     commentValue.append('\n');
                  }
               }

               if (!commentValue.isEmpty()) {
                  comments.put(path1, new Comment() {
                     @Override
                     public Class<? extends Annotation> annotationType() {
                        return Comment.class;
                     }

                     @Override
                     public String value() {
                        return commentValue.toString();
                     }

                     @Override
                     public Comment.CommentType type() {
                        return Comment.CommentType.INLINE;
                     }
                  });
               }
            }
         }
      }
   }
}
