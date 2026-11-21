package org.sethomegui.SetHomeGUI.libs.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.Block;
import org.sethomegui.SetHomeGUI.libs.block.Comments;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api.DumpSettings;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api.RepresentToNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentLine;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentType;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.MappingNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.representer.StandardRepresenter;
import org.sethomegui.SetHomeGUI.libs.settings.dumper.DumperSettings;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;
import org.sethomegui.SetHomeGUI.libs.utils.format.NodeRole;

public class ExtendedRepresenter extends StandardRepresenter {
   private final GeneralSettings generalSettings;
   private final DumperSettings dumperSettings;
   private NodeRole nodeRole = NodeRole.KEY;

   public ExtendedRepresenter(@NotNull GeneralSettings generalSettings, @NotNull DumperSettings dumperSettings, @NotNull DumpSettings engineSettings) {
      super(engineSettings);
      this.generalSettings = generalSettings;
      this.dumperSettings = dumperSettings;
      RepresentToNode representSection = new ExtendedRepresenter.RepresentSection();
      RepresentToNode representSerializable = new ExtendedRepresenter.RepresentSerializable();
      super.representers.put(Section.class, representSection);
      super.representers.put(YamlDocument.class, representSection);
      super.representers.put(Enum.class, new ExtendedRepresenter.RepresentEnum());
      super.representers.put(String.class, new ExtendedRepresenter.RepresentString(super.representers.get(String.class)));

      for (Class<?> clazz : generalSettings.getSerializer().getSupportedClasses()) {
         super.representers.put(clazz, representSerializable);
      }

      for (Class<?> clazz : generalSettings.getSerializer().getSupportedParentClasses()) {
         super.parentClassRepresenters.put(clazz, representSerializable);
      }
   }

   public ExtendedRepresenter(@NotNull GeneralSettings generalSettings, @NotNull DumperSettings dumperSettings) {
      this(generalSettings, dumperSettings, dumperSettings.buildEngineSettings());
   }

   @Override
   protected Node representScalar(Tag tag, String value, ScalarStyle scalarStyle) {
      return super.representScalar(tag, value, this.dumperSettings.getScalarFormatter().format(tag, value, this.nodeRole, scalarStyle));
   }

   @Override
   protected Node representSequence(Tag tag, Iterable<?> sequence, FlowStyle flowStyle) {
      return super.representSequence(tag, sequence, this.dumperSettings.getSequenceFormatter().format(tag, sequence, this.nodeRole, flowStyle));
   }

   @Override
   protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
      return super.representMapping(tag, mapping, this.dumperSettings.getMappingFormatter().format(tag, mapping, this.nodeRole, flowStyle));
   }

   private Node applyComments(@Nullable Block<?> block, @NotNull NodeRole nodeRole, @NotNull Node node, boolean isRoot) {
      if (block == null) {
         return node;
      } else {
         if (this.allowBlockComments(isRoot)) {
            node.setBlockComments(Comments.get(block, nodeRole, Comments.Position.BEFORE));
            node.setEndComments(Comments.get(block, nodeRole, Comments.Position.AFTER));
         }

         List<CommentLine> inline = Comments.get(block, nodeRole, Comments.Position.INLINE);
         if (inline != null && !inline.isEmpty()) {
            if (this.allowInlineComments(node)) {
               node.setInLineComments(inline);
            } else if (this.allowBlockComments(isRoot)) {
               List<CommentLine> before = node.getBlockComments() == null ? new ArrayList<>(inline.size()) : new ArrayList<>(node.getBlockComments());

               for (CommentLine line : inline) {
                  before.add(
                     new CommentLine(
                        line.getStartMark(),
                        line.getEndMark(),
                        line.getValue(),
                        line.getCommentType() == CommentType.IN_LINE ? CommentType.BLOCK : line.getCommentType()
                     )
                  );
               }

               node.setBlockComments(before);
            }
         }

         return node;
      }
   }

   @Override
   protected NodeTuple representMappingEntry(Entry<?, ?> entry) {
      Block<?> block = entry.getValue() instanceof Block ? (Block)entry.getValue() : null;
      Node key = this.applyComments(block, this.nodeRole = NodeRole.KEY, this.representData(entry.getKey()), false);
      Node value = this.applyComments(
         block, this.nodeRole = NodeRole.VALUE, this.representData(block == null ? entry.getValue() : block.getStoredValue()), false
      );
      return new NodeTuple(key, value);
   }

   private boolean allowBlockComments(boolean isRoot) {
      return isRoot || this.settings.getDefaultFlowStyle() == FlowStyle.BLOCK;
   }

   private boolean allowInlineComments(@NotNull Node node) {
      return this.settings.getDefaultFlowStyle() == FlowStyle.BLOCK && node instanceof ScalarNode
         || this.settings.getDefaultFlowStyle() == FlowStyle.FLOW && (node instanceof SequenceNode || node instanceof MappingNode);
   }

   private class RepresentEnum implements RepresentToNode {
      private RepresentEnum() {
      }

      @Override
      public Node representData(Object data) {
         return ExtendedRepresenter.this.representData(((Enum)data).name());
      }
   }

   private class RepresentSection implements RepresentToNode {
      private RepresentSection() {
      }

      @Override
      public Node representData(Object data) {
         Section section = (Section)data;
         return ExtendedRepresenter.this.applyComments(
            section, NodeRole.VALUE, ExtendedRepresenter.this.representData(section.getStoredValue()), section.isRoot()
         );
      }
   }

   private class RepresentSerializable implements RepresentToNode {
      private RepresentSerializable() {
      }

      @Override
      public Node representData(Object data) {
         Object serialized = ExtendedRepresenter.this.generalSettings
            .getSerializer()
            .serialize(data, ExtendedRepresenter.this.generalSettings.getDefaultMapSupplier());
         return ExtendedRepresenter.this.representData(serialized == null ? data : serialized);
      }
   }

   private class RepresentString implements RepresentToNode {
      private final RepresentToNode previous;

      private RepresentString(@NotNull RepresentToNode previous) {
         this.previous = previous;
      }

      @Override
      public Node representData(Object data) {
         ScalarStyle previousStyle = ExtendedRepresenter.this.defaultScalarStyle;
         ExtendedRepresenter.this.defaultScalarStyle = ExtendedRepresenter.this.dumperSettings.getStringStyle();
         Node node = this.previous.representData(data);
         ExtendedRepresenter.this.defaultScalarStyle = previousStyle;
         return node;
      }
   }
}
