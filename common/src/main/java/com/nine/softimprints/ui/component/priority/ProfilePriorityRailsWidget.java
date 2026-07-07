package com.nine.softimprints.ui.component.priority;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.options.block.SurfaceBlock;
import com.nine.softimprints.ui.component.profile.ProfileCards;
import com.nine.softimprints.ui.context.EditorContext;
import com.nine.softimprints.ui.util.constant.SIColors;
import com.nine.softimprints.ui.util.region.BoxSkins;
import com.nine.softimprints.ui.util.region.ChromeRenderer;
import com.nine.softimprints.ui.util.sound.UISounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.*;

public class ProfilePriorityRailsWidget extends AbstractWidget {

    private static final int RAIL_COUNT = 5;
    private static final int BAND_SIZE = 1000;
    private static final int PRIORITY_STEP = 10;

    private static final double CHIP_VERTICAL_GAP = 4.0D;
    private static final double EDGE_GAP = 10.0D;
    private static final double PREFERRED_GAP = 5.0D;

    private static final int SEPARATOR_OVERHANG = 2;
    private static final int TOOLTIP_MAX_WIDTH = 180;
    private static final double DRAG_THRESHOLD = 3.0D;

    private static final double SPRING_STIFFNESS = 320.0D;
    private static final double SPRING_DAMPING = 28.0D;
    private static final double MAX_FRAME_DELTA_SECONDS = 0.05D;

    private static final double FOCUS_GAP_SECONDS = 0.25D;

    private static final double MAX_SUBSTEP_SECONDS = 1.0D / 240.0D;

    private static final double MAX_SCALE = 2.0D;

    private static final double HOVER_SCALE = 1.12D;

    private static final double SCALE_STIFFNESS = 3100.0D;
    private static final double SCALE_DAMPING = 70.0D;

    private static final double TILT_PER_VELOCITY = 0.0006D;
    private static final double MAX_TILT_RADIANS = 0.30D;

    private static final double DRAG_VELOCITY_SMOOTHING = 0.3D;
    private static final double MAX_DRAG_VELOCITY = 1200.0D;

    private static final double WAKE_RADIUS_CHIPS = 1.6D;

    private static final double WAKE_PUSH = 7.0D;

    private static final double BREATH_TILT_RADIANS = 0.02D;
    private static final double BREATH_HZ = 1.4D;

    private static final int CONFLICT_WIN_TINT = 0x3455FF55;
    private static final int CONFLICT_LOSE_TINT = 0x34FF5555;

    private final EditorContext context;
    private final Font font;
    private final Map<Identifier, ChipAnim> anims = new HashMap<>();
    private final Map<Identifier, RailObject> profileObjects = new HashMap<>();

    private final List<Decoration> decorations;

    private long lastFrameNanos;

    private double animTime;

    @Nullable
    private Identifier draggedId;
    @Nullable
    private Identifier hoveredNow;
    private double dragX;
    private double dragY;
    private double dragStartX;
    private double dragStartY;
    private boolean dragMoved;

    private Set<Identifier> dragConflicts = Set.of();

    private Map<Identifier, Integer> conflictTints = Map.of();
    private int draggedPreviewPriority;

    private int lastTickRail = Integer.MIN_VALUE;
    private int lastTickIndex = Integer.MIN_VALUE;

    public ProfilePriorityRailsWidget(
            int x,
            int y,
            int width,
            int height,
            EditorContext context
    ) {
        super(x, y, width, height, Component.empty());
        this.context = context;
        this.font = Minecraft.getInstance().font;

        this.decorations = getRandomDecorations();
    }

    private List<Decoration> getRandomDecorations(){
        List<List<Decoration>> list = List.of(
                List.of(new Decoration(
                        RAIL_COUNT - 1,
                        0,
                        new TitleObject(
                                Component.literal("◇"), 2.1F
                        ))
                ),
                List.of(new Decoration(
                        RAIL_COUNT - 2,
                        0,
                        new TitleObject(
                                Component.literal("☀"), 1.8F
                        ))
                ),
                List.of(new Decoration(
                        RAIL_COUNT - 4,
                        0,
                        new TitleObject(
                                Component.literal("❄"), 1.8F
                        ))
                ),
                List.of(new Decoration(
                        RAIL_COUNT - 3,
                        0,
                        new TitleObject(
                                Component.literal("🔥"), 1.8F
                        ))
                )
        );
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }


    @Override
    protected void extractWidgetRenderState(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        double dt = frameDelta();
        animTime += dt;

        renderSeparators(graphics);

        List<List<RailObject>> rails = buildRails();
        int hoverRail = draggedId != null && dragMoved ? railAt(dragY) : -1;
        int insertIndex = hoverRail >= 0 ? insertionIndex(rails.get(hoverRail), dragX) : -1;
        tickDragSlot(hoverRail, insertIndex);

        RailObject hovered = draggedId == null ? objectAt(mouseX, mouseY) : null;
        Identifier hoveredKey = hovered == null ? null : hovered.key();
        this.hoveredNow = hoveredKey;

        stepAnimations(rails, hoverRail, insertIndex, hoveredKey, dt);
        updateConflictPreview(rails, hoverRail, insertIndex);

        Identifier selected = context.currentId();

        for (List<RailObject> rail : rails) {
            for (RailObject object : rail) {
                Identifier key = object.key();
                if (key.equals(hoveredKey) || key.equals(draggedId)) continue;
                ChipAnim anim = anims.get(key);
                if (anim == null) continue;
                renderObject(graphics, object, anim, key.equals(selected));
            }
        }

        if (hovered != null) {
            ChipAnim anim = anims.get(hoveredKey);
            if (anim != null) {
                renderObject(graphics, hovered, anim, true);
            }
            Component tooltip = hovered.tooltip();
            if (tooltip != null) {
                renderTooltip(graphics, tooltip, mouseX, mouseY);
            }
        }

        if (draggedId != null) {
            ChipAnim anim = anims.get(draggedId);
            if (anim != null) {
                RailObject dragged = profileObject(draggedId);
                renderObject(graphics, dragged, anim, true);
                if (dragMoved && hoverRail >= 0) {
                    Component tooltip = dragged.tooltip();
                    if (tooltip != null) {
                        renderTooltip(graphics, tooltip, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private void renderSeparators(GuiGraphicsExtractor graphics) {
        for (int rail = 1; rail < RAIL_COUNT; rail++) {
            int y = (int) Math.round(rowTop(rail) + rowHeight()) - 1;
            ChromeRenderer.headerLine(
                    graphics,
                    getX() - SEPARATOR_OVERHANG,
                    y,
                    getWidth() + SEPARATOR_OVERHANG * 2
            );
        }
    }

    private void renderObject(
            GuiGraphicsExtractor graphics,
            RailObject object,
            ChipAnim anim,
            boolean highlighted
    ) {
        int size = (int) Math.round(chipSize());
        float centerX = (float) (anim.x + size / 2.0D);
        float centerY = (float) (anim.y + size / 2.0D);

        Identifier key = object.key();
        double tilt = Mth.clamp(anim.vx * TILT_PER_VELOCITY, -MAX_TILT_RADIANS, MAX_TILT_RADIANS);
        if (key.equals(hoveredNow) || key.equals(draggedId)) {
            tilt += Math.sin(animTime * Math.PI * 2.0D * BREATH_HZ) * BREATH_TILT_RADIANS;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().rotate((float) tilt);
        graphics.pose().scale((float) anim.scale, (float) anim.scale);
        object.render(graphics, -size / 2, -size / 2, size, highlighted);
        graphics.pose().popMatrix();
    }

    private Component chipTooltip(Identifier id) {
        int priority = id.equals(draggedId) && dragMoved ? draggedPreviewPriority : priorityOf(id);
        return Component.translatable("imprint_profile." + id.toLanguageKey())
                .append(Component.literal(" (" + priority + ")").withColor(SIColors.SOFT_SOFT_GRAY));
    }

    private void renderTooltip(
            GuiGraphicsExtractor graphics,
            Component tooltip,
            int mouseX,
            int mouseY
    ) {
        graphics.setTooltipForNextFrame(font, font.split(tooltip, TOOLTIP_MAX_WIDTH), mouseX, mouseY);
    }



    private List<List<RailObject>> buildRails() {
        List<List<RailObject>> rails = new ArrayList<>(RAIL_COUNT);
        for (int i = 0; i < RAIL_COUNT; i++) {
            rails.add(new ArrayList<>());
        }

        List<Identifier> ids = new ArrayList<>();
        for (Identifier id : context.session().ids()) {
            if (context.session().draft(id) == null) continue;
            ids.add(id);
        }
        ids.sort(Comparator
                .comparingInt(this::priorityOf)
                .thenComparing(Identifier::toString));

        for (Identifier id : ids) {
            if (id.equals(draggedId) && dragMoved) continue;
            rails.get(railOf(priorityOf(id))).add(profileObject(id));
        }
        for (Decoration decoration : decorations) {
            List<RailObject> rail = rails.get(decoration.rail);
            rail.add(Mth.clamp(decoration.slotIndex, 0, rail.size()), decoration.object());
        }
        return rails;
    }

    private RailObject profileObject(Identifier id) {
        return profileObjects.computeIfAbsent(id, ProfileObject::new);
    }

    @Nullable
    private RailObject objectByKey(Identifier key) {
        RailObject profile = profileObjects.get(key);
        if (profile != null) return profile;
        for (Decoration decoration : decorations) {
            if (decoration.object().key().equals(key)) {
                return decoration.object();
            }
        }
        return null;
    }

    private int priorityOf(Identifier id) {
        var draft = context.session().draft(id);
        return draft == null ? 0 : draft.getDraft().priority();
    }

    private int railOf(int priority) {
        return Mth.clamp(priority / BAND_SIZE, 0, RAIL_COUNT - 1);
    }

    private int slotPriority(
            int rail,
            int index
    ) {
        return rail * BAND_SIZE + Math.min((index + 1) * PRIORITY_STEP, BAND_SIZE - 1);
    }

    private Set<Identifier> conflictsWith(Identifier id) {
        var draft = context.session().draft(id);
        if (draft == null) return Set.of();
        Set<Block> blocks = surfaceBlocks(draft.getDraft());
        if (blocks.isEmpty()) return Set.of();

        Set<Identifier> result = new HashSet<>();
        for (Identifier other : context.session().ids()) {
            if (other.equals(id)) continue;
            var otherDraft = context.session().draft(other);
            if (otherDraft == null) continue;
            for (Block block : surfaceBlocks(otherDraft.getDraft())) {
                if (blocks.contains(block)) {
                    result.add(other);
                    break;
                }
            }
        }
        return result;
    }

    private static Set<Block> surfaceBlocks(ImprintProfile profile) {
        Set<Block> result = new HashSet<>();
        for (SurfaceBlock surface : profile.supportedBlocks()) {
            Block block = surface.block();
            if (block != null) result.add(block);
        }
        return result;
    }

    private Map<Identifier, Integer> previewPriorities(
            List<List<RailObject>> rails,
            int hoverRail,
            int insertIndex
    ) {
        Map<Identifier, Integer> result = new HashMap<>();
        for (Identifier id : context.session().ids()) {
            if (context.session().draft(id) == null) continue;
            result.put(id, priorityOf(id));
        }
        if (draggedId == null || hoverRail < 0) return result;

        List<RailObject> order = new ArrayList<>(rails.get(hoverRail));
        order.add(Mth.clamp(insertIndex, 0, order.size()), profileObject(draggedId));
        int slot = 0;
        for (RailObject object : order) {
            if (!(object instanceof ProfileObject profile)) continue;
            result.put(profile.id, slotPriority(hoverRail, slot++));
        }
        return result;
    }

    private void updateConflictPreview(
            List<List<RailObject>> rails,
            int hoverRail,
            int insertIndex
    ) {
        if (draggedId == null || !dragMoved || hoverRail < 0) {
            conflictTints = Map.of();
            return;
        }

        Map<Identifier, Integer> preview = previewPriorities(rails, hoverRail, insertIndex);
        draggedPreviewPriority = preview.getOrDefault(draggedId, priorityOf(draggedId));

        if (dragConflicts.isEmpty()) {
            conflictTints = Map.of();
            return;
        }
        Map<Identifier, Integer> tints = new HashMap<>();
        for (Identifier id : dragConflicts) {
            Integer other = preview.get(id);
            if (other == null) continue;
            tints.put(id, draggedPreviewPriority > other ? CONFLICT_WIN_TINT : CONFLICT_LOSE_TINT);
        }
        conflictTints = tints;
    }

    private void tickDragSlot(
            int hoverRail,
            int insertIndex
    ) {
        if (draggedId == null || !dragMoved || hoverRail < 0) {
            return;
        }
        if (lastTickRail != Integer.MIN_VALUE) {
            if (hoverRail != lastTickRail) {
                UISounds.railTick();
            } else if (insertIndex != lastTickIndex) {
                UISounds.slotTick();
            }
        }
        lastTickRail = hoverRail;
        lastTickIndex = insertIndex;
    }

    private double frameDelta() {
        long now = System.nanoTime();
        double raw = lastFrameNanos == 0L ? 0.0D : (now - lastFrameNanos) / 1.0E9D;
        lastFrameNanos = now;
        if (raw <= 0.0D || raw > FOCUS_GAP_SECONDS) {
            return 0.0D;
        }
        return Math.min(raw, MAX_FRAME_DELTA_SECONDS);
    }

    private void stepAnimations(
            List<List<RailObject>> rails,
            int hoverRail,
            int insertIndex,
            @Nullable Identifier hoveredKey,
            double dt
    ) {
        Set<Identifier> alive = new HashSet<>();
        boolean waking = draggedId != null && dragMoved;
        double wakeRadius = chipSize() * WAKE_RADIUS_CHIPS;

        for (int rail = 0; rail < RAIL_COUNT; rail++) {
            List<RailObject> objects = rails.get(rail);
            boolean withGhost = rail == hoverRail;
            int count = objects.size() + (withGhost ? 1 : 0);
            double gap = gapFor(count);
            double y = chipTop(rail);

            int slot = 0;
            for (RailObject object : objects) {
                if (withGhost && slot == insertIndex) slot++;
                double x = slotX(slot, gap);
                slot++;
                if (waking) {
                    x += wakeOffset(x + chipSize() / 2.0D, y + chipSize() / 2.0D, wakeRadius);
                }

                Identifier key = object.key();
                alive.add(key);
                ChipAnim anim = anims.get(key);
                if (anim == null) {
                    anims.put(key, ChipAnim.at(x, y));
                } else {
                    anim.springTo(x, y, dt);
                }
            }
        }

        if (draggedId != null) {
            alive.add(draggedId);
            ChipAnim anim = anims.computeIfAbsent(draggedId, ignored -> ChipAnim.at(dragX, dragY));
            if (dragMoved) {
                anim.pin(dragX - chipSize() / 2.0D, dragY - chipSize() / 2.0D, dt);
            }
        }

        anims.keySet().retainAll(alive);

        for (var entry : anims.entrySet()) {
            Identifier id = entry.getKey();
            boolean lifted = id.equals(hoveredKey) || id.equals(draggedId);
            entry.getValue().springScaleTo(lifted ? HOVER_SCALE : 1.0D, dt);
        }
    }

    private double wakeOffset(
            double centerX,
            double centerY,
            double radius
    ) {
        double dx = centerX - dragX;
        double dy = centerY - dragY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance >= radius) return 0.0D;
        double falloff = 1.0D - distance / radius;
        return Math.copySign(WAKE_PUSH * falloff * falloff, dx == 0.0D ? 1.0D : dx);
    }

    private double gapFor(int count) {
        if (count <= 1) return PREFERRED_GAP;
        double available = contentWidth() - EDGE_GAP * 2.0D - count * chipSize();
        return Math.min(PREFERRED_GAP, available / (count - 1));
    }

    private double slotX(
            int slot,
            double gap
    ) {
        return getX() + EDGE_GAP + slot * (chipSize() + gap);
    }

    private int insertionIndex(
            List<RailObject> railObjects,
            double cursorX
    ) {
        int count = railObjects.size() + 1;
        double gap = gapFor(count);
        double slotWidth = chipSize() + gap;
        double local = cursorX - getX() - EDGE_GAP - chipSize() / 2.0D;
        int index = (int) Math.round(local / Math.max(1.0D, slotWidth));
        return Mth.clamp(index, 0, railObjects.size());
    }

    private double rowHeight() {
        return getHeight() / (double) RAIL_COUNT;
    }

    private double chipSize() {
        return Math.max(8.0D, rowHeight() - CHIP_VERTICAL_GAP * 2.0D);
    }

    private double rowTop(int rail) {
        return getY() + (RAIL_COUNT - 1 - rail) * rowHeight();
    }

    private double chipTop(int rail) {
        return rowTop(rail) + (rowHeight() - chipSize()) / 2.0D;
    }

    private int railAt(double mouseY) {
        double clamped = Mth.clamp(mouseY, getY(), getY() + getHeight() - 1);
        int zoneFromTop = (int) ((clamped - getY()) / rowHeight());
        return Mth.clamp(RAIL_COUNT - 1 - zoneFromTop, 0, RAIL_COUNT - 1);
    }

    private double contentWidth() {
        return getWidth();
    }

    @Nullable
    private RailObject objectAt(
            double mouseX,
            double mouseY
    ) {
        RailObject best = null;
        double bestDistance = Double.MAX_VALUE;
        double size = chipSize();
        for (var entry : anims.entrySet()) {
            RailObject object = objectByKey(entry.getKey());
            if (object == null || !object.draggable()) continue;
            ChipAnim anim = entry.getValue();
            if (mouseX < anim.x || mouseX >= anim.x + size
                    || mouseY < anim.y || mouseY >= anim.y + size) {
                continue;
            }
            double distance = Math.abs(mouseX - (anim.x + size / 2.0D));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = object;
            }
        }
        return best;
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (!this.active || !this.visible || event.button() != 0 || !this.isMouseOver(event.x(), event.y())) {
            return false;
        }

        RailObject hit = objectAt(event.x(), event.y());
        if (hit != null) {
            UISounds.chipPickup();
            draggedId = hit.key();
            dragX = event.x();
            dragY = event.y();
            dragStartX = event.x();
            dragStartY = event.y();
            dragMoved = false;
            dragConflicts = conflictsWith(draggedId);
            lastTickRail = Integer.MIN_VALUE;
            lastTickIndex = Integer.MIN_VALUE;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (draggedId == null) {
            return super.mouseDragged(event, dx, dy);
        }

        dragX = event.x();
        dragY = event.y();
        if (Math.abs(dragX - dragStartX) + Math.abs(dragY - dragStartY) > DRAG_THRESHOLD) {
            dragMoved = true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (draggedId == null) {
            return super.mouseReleased(event);
        }

        Identifier id = draggedId;
        boolean moved = dragMoved;
        draggedId = null;
        dragMoved = false;
        dragConflicts = Set.of();
        conflictTints = Map.of();

        if (!moved) {
            context.setCurrent(id);
            return true;
        }

        if (!isMouseOver(event.x(), event.y())) {
            UISounds.chipCancel();
            return true;
        }

        int rail = railAt(event.y());
        List<List<RailObject>> rails = buildRails();
        RailObject dragged = profileObject(id);
        for (List<RailObject> railObjects : rails) {
            railObjects.remove(dragged);
        }
        List<RailObject> order = rails.get(rail);
        order.add(insertionIndex(order, event.x()), dragged);

        renumber(rail, order);
        rememberDecorationSlots(rails);

        var draft = context.session().draft(id);
        UISounds.chipPlace(draft == null ? null : draft.getDraft());
        return true;
    }

    private void rememberDecorationSlots(List<List<RailObject>> rails) {
        for (Decoration decoration : decorations) {
            int index = rails.get(decoration.rail).indexOf(decoration.object);
            if (index >= 0) {
                decoration.slotIndex = index;
            }
        }
    }

    private void renumber(
            int rail,
            List<RailObject> order
    ) {
        int slot = 0;
        for (RailObject object : order) {
            if (!(object instanceof ProfileObject profile)) continue;
            int priority = slotPriority(rail, slot++);
            var draft = context.session().draft(profile.id);
            if (draft == null || draft.getDraft().priority() == priority) continue;
            draft.updateDraft(p -> p.toBuilder().setPriority(priority).build());
        }
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private interface RailObject {

        Identifier key();

        default boolean draggable() {
            return false;
        }

        void render(
                GuiGraphicsExtractor graphics,
                int x,
                int y,
                int size,
                boolean highlighted
        );

        @Nullable
        default Component tooltip() {
            return null;
        }
    }

    private static final class Decoration {

        private final RailObject object;
        private final int rail;
        private int slotIndex;

        private Decoration(
                int rail,
                int slotIndex,
                RailObject object
        ) {
            this.rail = rail;
            this.slotIndex = slotIndex;
            this.object = object;
        }

        private RailObject object() {
            return object;
        }
    }

    private final class ProfileObject implements RailObject {

        private final Identifier id;

        private ProfileObject(Identifier id) {
            this.id = id;
        }

        @Override
        public Identifier key() {
            return id;
        }

        @Override
        public boolean draggable() {
            return true;
        }

        @Override
        public void render(
                GuiGraphicsExtractor graphics,
                int x,
                int y,
                int size,
                boolean highlighted
        ) {
            var draft = context.session().draft(id);
            if (draft == null) return;
            ProfileCards.render(graphics,
                    highlighted ? BoxSkins.TAB_HOVERED : BoxSkins.TAB,
                    ProfileCards.iconFor(graphics, draft.getDraft()),
                    x, y, size);

            Integer tint = conflictTints.get(id);
            if (tint != null) {
                graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, tint);
            }
        }

        @Override
        public Component tooltip() {
            return chipTooltip(id);
        }
    }

    private final class TitleObject implements RailObject {

        private final Identifier key;
        private final Component text;
        private final float scale;

        private TitleObject(Component text){
            this(text, 1);
        }

        private TitleObject(
                Component text,
                float scale
        ) {
            this.key = Identifier.fromNamespaceAndPath(SICommon.MODID, "decor");
            this.text = text;
            this.scale = scale;
        }

        @Override
        public Identifier key() {
            return key;
        }

        @Override
        public void render(
                GuiGraphicsExtractor graphics,
                int x,
                int y,
                int size,
                boolean highlighted
        ) {
            var pose = graphics.pose();
            pose.pushMatrix();

            pose.scale(scale, scale);

            graphics.centeredText(font, text,
                    x + size / 2,
                    y + (size - font.lineHeight) / 2,
                    SIColors.ALMOST_WHITE);

            pose.popMatrix();
        }
    }

    private static final class ChipAnim {

        private double x;
        private double y;
        private double vx;
        private double vy;
        private double scale = 1.0D;
        private double scaleVelocity;

        private static ChipAnim at(
                double x,
                double y
        ) {
            ChipAnim anim = new ChipAnim();
            anim.x = x;
            anim.y = y;
            return anim;
        }

        private void pin(
                double targetX,
                double targetY,
                double dt
        ) {
            if (dt > 0.0D) {
                double instantVx = (targetX - x) / dt;
                double instantVy = (targetY - y) / dt;
                vx = clampVelocity(vx + (instantVx - vx) * DRAG_VELOCITY_SMOOTHING);
                vy = clampVelocity(vy + (instantVy - vy) * DRAG_VELOCITY_SMOOTHING);
            }
            this.x = targetX;
            this.y = targetY;
        }

        private static double clampVelocity(double velocity) {
            return Mth.clamp(velocity, -MAX_DRAG_VELOCITY, MAX_DRAG_VELOCITY);
        }

        private void springTo(
                double targetX,
                double targetY,
                double dt
        ) {
            if (dt <= 0.0D) return;
            for (double step = dt; step > 0.0D; step -= MAX_SUBSTEP_SECONDS) {
                double h = Math.min(step, MAX_SUBSTEP_SECONDS);
                vx += (SPRING_STIFFNESS * (targetX - x) - SPRING_DAMPING * vx) * h;
                vy += (SPRING_STIFFNESS * (targetY - y) - SPRING_DAMPING * vy) * h;
                x += vx * h;
                y += vy * h;
            }
        }

        private void springScaleTo(
                double target,
                double dt
        ) {
            if (dt <= 0.0D) return;
            for (double step = dt; step > 0.0D; step -= MAX_SUBSTEP_SECONDS) {
                double h = Math.min(step, MAX_SUBSTEP_SECONDS);
                scaleVelocity += (SCALE_STIFFNESS * (target - scale) - SCALE_DAMPING * scaleVelocity) * h;
                scale += scaleVelocity * h;
            }
            scale = Mth.clamp(scale, 0.0D, MAX_SCALE);
        }
    }

}
