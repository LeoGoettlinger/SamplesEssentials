package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

class AttributedConfigurationNodeImpl
   extends AbstractCommentedConfigurationNode<AttributedConfigurationNode, AttributedConfigurationNodeImpl>
   implements AttributedConfigurationNode {
   private String tagName;
   private final Map<String, String> attributes = new LinkedHashMap<>();

   protected AttributedConfigurationNodeImpl(
      final String tagName, @Nullable final Object path, @Nullable final AttributedConfigurationNodeImpl parent, final ConfigurationOptions options
   ) {
      super(path, parent, options);
      this.tagName = Objects.requireNonNull(tagName);
   }

   protected AttributedConfigurationNodeImpl(
      final String tagName, @Nullable final AttributedConfigurationNodeImpl parent, final AttributedConfigurationNodeImpl copyOf
   ) {
      super(parent, copyOf);
      this.tagName = Objects.requireNonNull(tagName);
   }

   @Override
   public String tagName() {
      return this.tagName;
   }

   public AttributedConfigurationNodeImpl tagName(final String tagName) {
      if (Objects.requireNonNull(tagName, "tag name").isEmpty()) {
         throw new IllegalArgumentException("Tag name cannot be null/empty");
      } else {
         this.tagName = tagName;
         return this;
      }
   }

   public AttributedConfigurationNodeImpl addAttribute(final String name, final String value) {
      if (Objects.requireNonNull(name, "name").isEmpty()) {
         throw new IllegalArgumentException("Attribute name cannot be null/empty");
      } else {
         this.attachIfNecessary();
         this.attributes.put(name, value);
         return this;
      }
   }

   public AttributedConfigurationNodeImpl removeAttribute(final String name) {
      this.attributes.remove(name);
      return this;
   }

   public AttributedConfigurationNodeImpl attributes(final Map<String, String> attributes) {
      for (String name : attributes.keySet()) {
         if (Objects.requireNonNull(name, "name").isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null/empty");
         }
      }

      this.attributes.clear();
      if (!attributes.isEmpty()) {
         this.attachIfNecessary();
         this.attributes.putAll(attributes);
      }

      return this;
   }

   @Override
   public Map<String, String> attributes() {
      return UnmodifiableCollections.copyOf(this.attributes);
   }

   @Override
   public boolean hasAttributes() {
      return !this.attributes.isEmpty();
   }

   @Nullable
   @Override
   public String attribute(final String name) {
      return this.attributes.get(name);
   }

   @Override
   public boolean empty() {
      return super.empty() && this.attributes.isEmpty();
   }

   protected AttributedConfigurationNodeImpl createNode(final Object path) {
      return new AttributedConfigurationNodeImpl("element", path, this, this.options());
   }

   public AttributedConfigurationNode from(final ConfigurationNode that) {
      if (that instanceof AttributedConfigurationNode) {
         AttributedConfigurationNode node = (AttributedConfigurationNode)that;
         this.tagName(node.tagName());
         this.attributes(node.attributes());
      }

      return (AttributedConfigurationNode)super.from(that);
   }

   public AttributedConfigurationNode mergeFrom(final ConfigurationNode other) {
      if (other instanceof AttributedConfigurationNode) {
         AttributedConfigurationNode node = (AttributedConfigurationNode)other;
         this.tagName(node.tagName());

         for (Entry<String, String> attribute : node.attributes().entrySet()) {
            this.addAttribute(attribute.getKey(), attribute.getValue());
         }
      }

      return (AttributedConfigurationNode)super.mergeFrom(other);
   }

   protected AttributedConfigurationNodeImpl copy(@Nullable final AttributedConfigurationNodeImpl parent) {
      AttributedConfigurationNodeImpl copy = new AttributedConfigurationNodeImpl(this.tagName, parent, this);
      copy.attributes.putAll(this.attributes);
      COMMENT_UPDATER.set(copy, this.comment);
      return copy;
   }

   public AttributedConfigurationNodeImpl self() {
      return this;
   }

   protected AttributedConfigurationNodeImpl implSelf() {
      return this;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof AttributedConfigurationNodeImpl)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         AttributedConfigurationNodeImpl that = (AttributedConfigurationNodeImpl)o;
         return this.tagName.equals(that.tagName) && this.attributes.equals(that.attributes);
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.tagName.hashCode();
      return 31 * result + this.attributes.hashCode();
   }

   @Override
   public String toString() {
      return "AttributedConfigurationNodeImpl{super="
         + super.toString()
         + ", comment="
         + this.comment
         + ", tagName="
         + this.tagName
         + ", attributes="
         + this.attributes
         + '}';
   }
}
