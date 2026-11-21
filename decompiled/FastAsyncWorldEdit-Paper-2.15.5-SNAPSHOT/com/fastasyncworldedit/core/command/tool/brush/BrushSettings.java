package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.command.tool.scroll.Scroll;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BrushSettings {
   private static final Expression DEFAULT_SIZE = Expression.compile("1");
   private final Map<BrushSettings.SettingType, Object> constructor = new ConcurrentHashMap<>();
   private Brush brush;
   private Mask mask;
   private Mask sourceMask;
   private ResettableExtent transform;
   private Pattern material;
   private Expression size = DEFAULT_SIZE;
   private final Set<String> permissions = new HashSet<>();
   private Scroll scrollAction;

   public BrushSettings() {
      this.constructor.put(BrushSettings.SettingType.PERMISSIONS, this.permissions);
   }

   public BrushSettings setBrush(Brush brush) {
      Brush tmp = this.brush;
      if (tmp != brush) {
         if (brush == null || tmp != null && tmp.getClass() != brush.getClass()) {
            this.clear();
         }

         this.brush = brush;
      }

      return this;
   }

   public BrushSettings clear() {
      this.brush = null;
      this.mask = null;
      this.sourceMask = null;
      this.transform = null;
      this.material = null;
      this.scrollAction = null;
      this.size = DEFAULT_SIZE;
      this.permissions.clear();
      this.constructor.clear();
      return this;
   }

   public BrushSettings clearPerms() {
      this.permissions.clear();
      return this;
   }

   public BrushSettings addSetting(BrushSettings.SettingType type, String args) {
      this.constructor.put(type, args);
      return this;
   }

   public Map<BrushSettings.SettingType, Object> getSettings() {
      return this.constructor;
   }

   public BrushSettings setMask(Mask mask) {
      if (mask == null) {
         this.constructor.remove(BrushSettings.SettingType.MASK);
      }

      this.mask = mask;
      return this;
   }

   public BrushSettings setSourceMask(Mask mask) {
      if (mask == null) {
         this.constructor.remove(BrushSettings.SettingType.SOURCE_MASK);
      }

      this.sourceMask = mask;
      return this;
   }

   public BrushSettings setTransform(ResettableExtent transform) {
      if (transform == null) {
         this.constructor.remove(BrushSettings.SettingType.TRANSFORM);
      }

      this.transform = transform;
      return this;
   }

   public BrushSettings setFill(Pattern pattern) {
      if (pattern == null) {
         this.constructor.remove(BrushSettings.SettingType.FILL);
      }

      this.material = pattern;
      return this;
   }

   public BrushSettings setSize(Expression size) {
      Preconditions.checkNotNull(size);
      this.size = size;
      if (size == DEFAULT_SIZE) {
         this.constructor.remove(BrushSettings.SettingType.SIZE);
      } else {
         this.constructor.put(BrushSettings.SettingType.SIZE, size.toString());
      }

      return this;
   }

   public BrushSettings setSize(double size) {
      return this.setSize(Expression.compile(Double.toString(size)));
   }

   public BrushSettings setScrollAction(Scroll scrollAction) {
      if (scrollAction == null) {
         this.constructor.remove(BrushSettings.SettingType.SCROLL_ACTION);
      }

      this.scrollAction = scrollAction;
      return this;
   }

   public BrushSettings addPermission(String permission) {
      this.permissions.add(permission);
      return this;
   }

   public BrushSettings addPermissions(String... perms) {
      Collections.addAll(this.permissions, perms);
      return this;
   }

   public Brush getBrush() {
      return this.brush;
   }

   public Mask getMask() {
      return this.mask;
   }

   public Mask getSourceMask() {
      return this.sourceMask;
   }

   public ResettableExtent getTransform() {
      return this.transform;
   }

   public Pattern getMaterial() {
      return this.material;
   }

   public double getSize() {
      try {
         return this.size.evaluate();
      } catch (EvaluationException var2) {
         throw new RuntimeException(var2);
      }
   }

   public Expression getSizeExpression() {
      return this.size;
   }

   public Set<String> getPermissions() {
      return Collections.unmodifiableSet(this.permissions);
   }

   public Scroll getScrollAction() {
      return this.scrollAction;
   }

   public boolean canUse(Actor actor) {
      Set<String> perms = this.getPermissions();

      for (String perm : perms) {
         if (actor.hasPermission(perm)) {
            return true;
         }
      }

      return perms.isEmpty();
   }

   @Override
   public String toString() {
      String name = (String)this.getSettings().get(BrushSettings.SettingType.BRUSH);
      if (name != null) {
         return name;
      } else {
         name = this.brush.getClass().getName();
         return name.substring(name.lastIndexOf(46) + 1);
      }
   }

   public static enum SettingType {
      BRUSH,
      SIZE,
      MASK,
      SOURCE_MASK,
      TRANSFORM,
      FILL,
      PERMISSIONS,
      SCROLL_ACTION;
   }
}
