package de.oliver.fancynpcs.api;

import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.data.property.NpcVisibility;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NpcData {
   private final String id;
   private final String name;
   private final UUID creator;
   private String displayName;
   private SkinData skin;
   private boolean mirrorSkin;
   private Location location;
   private boolean showInTab;
   private boolean spawnEntity;
   private boolean collidable;
   private boolean glowing;
   private NamedTextColor glowingColor;
   private EntityType type;
   private Map<NpcEquipmentSlot, ItemStack> equipment;
   private Consumer<Player> onClick;
   private Map<ActionTrigger, List<NpcAction.NpcActionData>> actions;
   private boolean turnToPlayer;
   private int turnToPlayerDistance = -1;
   private float interactionCooldown;
   private float scale;
   private int visibilityDistance;
   private Map<NpcAttribute, String> attributes;
   private NpcVisibility visibility;
   private boolean isDirty;

   public NpcData(
      String id,
      String name,
      UUID creator,
      String displayName,
      SkinData skin,
      Location location,
      boolean showInTab,
      boolean spawnEntity,
      boolean collidable,
      boolean glowing,
      NamedTextColor glowingColor,
      EntityType type,
      Map<NpcEquipmentSlot, ItemStack> equipment,
      boolean turnToPlayer,
      int turnToPlayerDistance,
      Consumer<Player> onClick,
      Map<ActionTrigger, List<NpcAction.NpcActionData>> actions,
      float interactionCooldown,
      float scale,
      int visibilityDistance,
      Map<NpcAttribute, String> attributes,
      boolean mirrorSkin
   ) {
      this.id = id;
      this.name = name;
      this.creator = creator;
      this.displayName = displayName;
      this.skin = skin;
      this.location = location;
      this.showInTab = showInTab;
      this.spawnEntity = spawnEntity;
      this.collidable = collidable;
      this.glowing = glowing;
      this.glowingColor = glowingColor;
      this.type = type;
      this.equipment = equipment;
      this.onClick = onClick;
      this.actions = actions;
      this.turnToPlayer = turnToPlayer;
      this.turnToPlayerDistance = turnToPlayerDistance;
      this.interactionCooldown = interactionCooldown;
      this.scale = scale;
      this.visibilityDistance = visibilityDistance;
      this.attributes = attributes;
      this.mirrorSkin = mirrorSkin;
      this.isDirty = true;
   }

   public NpcData(String name, UUID creator, Location location) {
      this.id = UUID.randomUUID().toString();
      this.name = name;
      this.creator = creator;
      this.location = location;
      this.displayName = name;
      this.type = EntityType.PLAYER;
      this.showInTab = false;
      this.spawnEntity = true;
      this.collidable = true;
      this.glowing = false;
      this.glowingColor = NamedTextColor.WHITE;
      this.onClick = p -> {};
      this.actions = new ConcurrentHashMap<>();
      this.turnToPlayer = false;
      this.turnToPlayerDistance = -1;
      this.interactionCooldown = 0.0F;
      this.scale = 1.0F;
      this.visibilityDistance = -1;
      this.equipment = new ConcurrentHashMap<>();
      this.attributes = new ConcurrentHashMap<>();
      this.mirrorSkin = false;
      this.visibility = NpcVisibility.ALL;
      this.isDirty = true;
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public UUID getCreator() {
      return this.creator == null ? UUID.fromString("00000000-0000-0000-0000-000000000000") : this.creator;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public NpcData setDisplayName(String displayName) {
      this.displayName = displayName;
      this.isDirty = true;
      return this;
   }

   public SkinData getSkinData() {
      return this.skin;
   }

   public NpcData setSkinData(SkinData skinData) {
      this.skin = skinData;
      this.isDirty = true;
      return this;
   }

   public NpcData setSkin(String skin, SkinData.SkinVariant variant) {
      SkinData data = FancyNpcsPlugin.get().getSkinManager().getByIdentifier(skin, variant);
      data.setIdentifier(skin);
      return this.setSkinData(data);
   }

   public NpcData setSkin(String skin) {
      return this.setSkin(skin, SkinData.SkinVariant.AUTO);
   }

   public Location getLocation() {
      return this.location;
   }

   public NpcData setLocation(Location location) {
      this.location = location;
      this.isDirty = true;
      return this;
   }

   public boolean isShowInTab() {
      return this.showInTab;
   }

   public NpcData setShowInTab(boolean showInTab) {
      this.showInTab = showInTab;
      this.isDirty = true;
      return this;
   }

   public boolean isSpawnEntity() {
      return this.spawnEntity;
   }

   public NpcData setSpawnEntity(boolean spawnEntity) {
      this.spawnEntity = spawnEntity;
      this.isDirty = true;
      return this;
   }

   public boolean isCollidable() {
      return this.collidable;
   }

   public NpcData setCollidable(boolean collidable) {
      this.collidable = collidable;
      this.isDirty = true;
      return this;
   }

   public boolean isGlowing() {
      return this.glowing;
   }

   public NpcData setGlowing(boolean glowing) {
      this.glowing = glowing;
      this.isDirty = true;
      return this;
   }

   public NamedTextColor getGlowingColor() {
      return this.glowingColor;
   }

   public NpcData setGlowingColor(NamedTextColor glowingColor) {
      this.glowingColor = glowingColor;
      this.isDirty = true;
      return this;
   }

   public EntityType getType() {
      return this.type;
   }

   public NpcData setType(EntityType type) {
      this.type = type;
      this.attributes.clear();
      this.isDirty = true;
      return this;
   }

   public Map<NpcEquipmentSlot, ItemStack> getEquipment() {
      return this.equipment;
   }

   public NpcData setEquipment(Map<NpcEquipmentSlot, ItemStack> equipment) {
      this.equipment = equipment;
      this.isDirty = true;
      return this;
   }

   public NpcData addEquipment(NpcEquipmentSlot slot, ItemStack item) {
      this.equipment.put(slot, item);
      this.isDirty = true;
      return this;
   }

   public Consumer<Player> getOnClick() {
      return this.onClick;
   }

   public NpcData setOnClick(Consumer<Player> onClick) {
      this.onClick = onClick;
      this.isDirty = true;
      return this;
   }

   public Map<ActionTrigger, List<NpcAction.NpcActionData>> getActions() {
      return this.actions;
   }

   public NpcData setActions(Map<ActionTrigger, List<NpcAction.NpcActionData>> actions) {
      this.actions = actions;
      this.isDirty = true;
      return this;
   }

   public List<NpcAction.NpcActionData> getActions(ActionTrigger trigger) {
      return this.actions.getOrDefault(trigger, new ArrayList<>());
   }

   public NpcData setActions(ActionTrigger trigger, List<NpcAction.NpcActionData> actions) {
      this.actions.put(trigger, actions);
      this.isDirty = true;
      return this;
   }

   public NpcData addAction(ActionTrigger trigger, int order, NpcAction action, String value) {
      List<NpcAction.NpcActionData> a = this.actions.getOrDefault(trigger, new ArrayList<>());
      a.add(new NpcAction.NpcActionData(order, action, value));
      this.actions.put(trigger, a);
      this.isDirty = true;
      return this;
   }

   public NpcData removeAction(ActionTrigger trigger, NpcAction action) {
      List<NpcAction.NpcActionData> a = this.actions.getOrDefault(trigger, new ArrayList<>());
      a.removeIf(ad -> ad.action().equals(action));
      this.actions.put(trigger, a);
      this.isDirty = true;
      return this;
   }

   public boolean isTurnToPlayer() {
      return this.turnToPlayer;
   }

   public NpcData setTurnToPlayer(boolean turnToPlayer) {
      this.turnToPlayer = turnToPlayer;
      this.isDirty = true;
      return this;
   }

   public int getTurnToPlayerDistance() {
      return this.turnToPlayerDistance;
   }

   public NpcData setTurnToPlayerDistance(int distance) {
      this.turnToPlayerDistance = distance;
      this.isDirty = true;
      return this;
   }

   public float getInteractionCooldown() {
      return this.interactionCooldown;
   }

   public NpcData setInteractionCooldown(float interactionCooldown) {
      this.interactionCooldown = interactionCooldown;
      return this;
   }

   public float getScale() {
      return this.scale;
   }

   public NpcData setScale(float scale) {
      this.scale = scale;
      this.isDirty = true;
      return this;
   }

   public int getVisibilityDistance() {
      return this.visibilityDistance;
   }

   public NpcData setVisibilityDistance(int visibilityDistance) {
      this.visibilityDistance = visibilityDistance;
      this.isDirty = true;
      return this;
   }

   public Map<NpcAttribute, String> getAttributes() {
      return this.attributes;
   }

   public void addAttribute(NpcAttribute attribute, String value) {
      this.attributes.put(attribute, value);
      this.isDirty = true;
   }

   public void removeAttribute(NpcAttribute attribute) {
      this.attributes.remove(attribute);
      this.isDirty = true;
   }

   public void clearAttributes() {
      this.attributes.clear();
      this.isDirty = true;
   }

   public void applyAllAttributes(Npc npc) {
      for (NpcAttribute attribute : this.attributes.keySet()) {
         attribute.apply(npc, this.attributes.get(attribute));
      }
   }

   public boolean isMirrorSkin() {
      return this.mirrorSkin;
   }

   public NpcData setMirrorSkin(boolean mirrorSkin) {
      this.mirrorSkin = mirrorSkin;
      this.isDirty = true;
      return this;
   }

   public NpcVisibility getVisibility() {
      return this.visibility == null ? NpcVisibility.ALL : this.visibility;
   }

   public NpcData setVisibility(NpcVisibility visibility) {
      this.visibility = visibility;
      this.isDirty = true;
      return this;
   }

   public boolean isDirty() {
      return this.isDirty;
   }

   public void setDirty(boolean dirty) {
      this.isDirty = dirty;
   }
}
