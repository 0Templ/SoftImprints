package com.nine.softimprints.ui.component.profile;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.catalog.entry.InvalidProfileEntry;
import com.nine.softimprints.profile.catalog.entry.issue.ProfileIssue;
import com.nine.softimprints.ui.context.EditorContext;
import com.nine.softimprints.ui.context.ProfilesSession;
import com.nine.softimprints.ui.util.constant.SIColors;
import com.nine.softimprints.ui.util.constant.SITextures;
import com.nine.softimprints.ui.util.region.BorderSides;
import com.nine.softimprints.ui.util.region.BoxRenderer;
import com.nine.softimprints.ui.util.region.BoxSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ProfileSwitchWidget extends AbstractWidget {

    private enum FilterMode { ALL, FILTERED }

    private static final int LABEL_ARROWS_PADDING = 4;
    private static final int ARROW_ZONE_WIDTH = 12;
    private static final int ARROW_ZONE_TOP_PADDING = 13;
    private static final int LABEL_Y_OFFSET = 3;
    private static final int TOOLTIP_MAX_WIDTH = 180;

    private static final int CARD_SIZE_STEP = 2;
    private static final int PROFILE_GAP = 4;
    private static final int ACTIVE_CARD_SIZE = 24;
    private static final int MIN_CARD_SIZE = 16;
    private static final int CARDS_PADDING = 2;
    private static final int CARDS_CENTER_Y_OFFSET = 6;
    private static final int MAX_VISIBLE_RADIUS = 4;

    private final EditorContext context;
    private final List<Identifier> allProfiles;
    private final List<ProfilesGroup> groups;


    private FilterMode filterMode = FilterMode.ALL;
    private int groupIndex;
    private int profileIndex;

    private Identifier lastAllProfile;
    private Identifier lastFilteredProfile;

    private boolean focusedLeft;
    private boolean focusedRight;

    private Label labelGroup = Label.empty();
    private Label labelProfile = Label.empty();
    private List<ProfileButtonData> visibleProfiles = List.of();

    private final Map<Identifier, Identifier> iconsCache = new HashMap<>();

    public ProfileSwitchWidget(
            int x,
            int y,
            int width,
            int height,
            EditorContext context
    ) {
        super(x, y, width, height, Component.empty());
        this.context = context;
        this.allProfiles = context.session().ids();
        this.groups = buildGroups(context.session());

        Identifier selected = context.currentId();
        this.lastAllProfile = selected;
        this.lastFilteredProfile = selected;

        syncGroupToProfile(selected);
        syncProfileIndexFromContext();
        rebuildVisibleProfiles();
        rebuildHeaderLayout();

        context.addSelectedProfileListener(this::onSelectedProfileUpdate);
    }

    private static List<ProfilesGroup> buildGroups(ProfilesSession session) {
        Map<String, List<Identifier>> byNamespace = new LinkedHashMap<>();
        for (Identifier id : session.ids()) {
            byNamespace.computeIfAbsent(id.getNamespace(), ignored -> new ArrayList<>()).add(id);
        }

        List<ProfilesGroup> result = new ArrayList<>();
        byNamespace.forEach((namespace, profiles) -> result.add(new ProfilesGroup(namespace, profiles)));
        return List.copyOf(result);
    }

    private void onSelectedProfileUpdate() {
        Identifier selected = context.currentId();
        if (filterMode == FilterMode.ALL) {
            lastAllProfile = selected;
        } else {
            lastFilteredProfile = selected;
        }
        syncProfileIndexFromContext();
        rebuildVisibleProfiles();
        rebuildHeaderLayout();
    }

    private void syncGroupToProfile(Identifier id) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).profiles().contains(id)) {
                groupIndex = i;
                return;
            }
        }
        groupIndex = 0;
    }

    private void syncProfileIndexFromContext() {
        List<Identifier> profiles = activeProfiles();
        if (profiles.isEmpty()) {
            profileIndex = 0;
            return;
        }

        int index = profiles.indexOf(context.currentId());
        if (index >= 0) {
            profileIndex = index;
            return;
        }

        profileIndex = Math.floorMod(profileIndex, profiles.size());
    }

    private List<Identifier> activeProfiles() {
        if (filterMode == FilterMode.ALL) {
            return allProfiles;
        }
        return filteredProfiles();
    }

    private List<Identifier> filteredProfiles() {
        if (groups.isEmpty()) {
            return List.of();
        }
        return groups.get(Math.floorMod(groupIndex, groups.size())).profiles();
    }

    private ProfilesGroup currentGroup() {
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(Math.floorMod(groupIndex, groups.size()));
    }

    private void showAllProfiles() {
        if (groups.isEmpty()) {
            return;
        }

        Identifier current = context.currentId();
        if (filterMode == FilterMode.FILTERED) {
            lastFilteredProfile = current;
        }
        filterMode = FilterMode.ALL;

        Identifier next = firstPresent(current, allProfiles);
        if (next == null) {
            next = firstPresent(lastAllProfile, allProfiles);
        }
        selectProfile(next == null ? current : next);
    }

    private void cycleFilterGroup(int delta) {
        if (groups.isEmpty()) {
            return;
        }

        if (filterMode == FilterMode.FILTERED) {
            int next = groupIndex + delta;
            if (next < 0 || next >= groups.size()) {
                showAllProfiles();
                return;
            }
            enterFilteredGroup(next);
            return;
        }

        lastAllProfile = context.currentId();
        enterFilteredGroup(delta < 0 ? groups.size() - 1 : 0);
    }

    private void enterFilteredGroup(int index) {
        if (groups.isEmpty()) {
            return;
        }

        Identifier current = context.currentId();
        if (filterMode == FilterMode.ALL) {
            lastAllProfile = current;
        } else {
            lastFilteredProfile = current;
        }
        filterMode = FilterMode.FILTERED;
        groupIndex = Math.floorMod(index, groups.size());

        List<Identifier> profiles = filteredProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        Identifier next = firstPresent(current, profiles);
        if (next == null) {
            next = firstPresent(lastFilteredProfile, profiles);
        }
        selectProfile(next == null ? profiles.getFirst() : next);
    }

    private void switchProfile(int delta) {
        List<Identifier> profiles = activeProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        int nextIndex = Math.floorMod(profileIndex + delta, profiles.size());
        selectProfile(profiles.get(nextIndex));
    }

    private void selectProfile(Identifier id) {
        boolean same = Objects.equals(context.currentId(), id);
        context.setCurrent(id);
        if (same) {
            onSelectedProfileUpdate();
        }
    }

    private static Identifier firstPresent(Identifier candidate, List<Identifier> profiles) {
        if (candidate != null && profiles.contains(candidate)) {
            return candidate;
        }
        return null;
    }

    private void rebuildHeaderLayout() {
        int padding = 2;
        int maxWidth = getWidth() - padding * 2;
        Font font = Minecraft.getInstance().font;
        Component groupText = groupLabel().copy().append(":");
        Component profileText = profileLabel();
        int spaceWidth = font.width(" ");
        int groupWidth = font.width(groupText);
        int profileWidth = font.width(profileText);
        int totalWidth = groupWidth + spaceWidth + profileWidth;

        int labelsX = getX() + (getWidth() - totalWidth) / 2;
        if (totalWidth > maxWidth) {
            labelsX = getX() + padding;
            groupWidth = (int) Math.min(groupWidth, maxWidth * 0.75F);
            profileWidth = maxWidth - groupWidth - spaceWidth;

        }
        int labelY = getY() + LABEL_Y_OFFSET;

        this.labelGroup = new Label(groupText, labelsX, labelY, groupWidth, font.lineHeight);
        this.labelProfile = new Label(
                profileText,
                labelsX + groupWidth + spaceWidth,
                labelY,
                profileWidth,
                font.lineHeight
        );
    }

    private Component groupLabel() {
        if (filterMode == FilterMode.ALL) {
            return Component.translatable("imprint_pack.all");
        }

        ProfilesGroup group = currentGroup();
        if (group == null) {
            return Component.translatable("imprint_pack.unknown");
        }
        return Component.translatable("imprint_pack." + group.namespace());
    }

    private Component profileLabel() {
        Identifier id = context.currentId();
        var ret = Component.translatable(profileTranslationKey(id));
        if (context.currentEntry() instanceof InvalidProfileEntry){
            ret.withColor(SIColors.SOFT_RED);
        }
        return ret;
    }

    private static String profileTranslationKey(Identifier id) {
        return "imprint_profile." + id.toLanguageKey();
    }

    private void rebuildVisibleProfiles() {
        this.visibleProfiles = buildVisibleProfiles();
    }

    private List<ProfileButtonData> buildVisibleProfiles() {
        List<Identifier> profiles = activeProfiles();
        int count = profiles.size();
        if (count == 0) {
            return List.of();
        }

        int radius = maxVisibleRadius();
        int activeSize = cardSizeForOffset(0);
        int yCenter = getY() + getHeight() / 2 + CARDS_CENTER_Y_OFFSET;
        int xCenter = getX() + getWidth() / 2;

        List<ProfileButtonData> result = new ArrayList<>();
        int activeX = xCenter - activeSize / 2;
        int activeY = yCenter - activeSize / 2;
        addProfileButton(result, profiles, 0, activeX, activeY, activeSize);

        int leftCursor = activeX;
        int rightCursor = activeX + activeSize;

        for (int step = 1; step <= radius; step++) {
            int size = cardSizeForOffset(step);
            int y = yCenter - size / 2;

            int leftX = leftCursor - PROFILE_GAP - size;
            addProfileButton(result, profiles, -step, leftX, y, size);
            leftCursor = leftX;

            int rightX = rightCursor + PROFILE_GAP;
            addProfileButton(result, profiles, step, rightX, y, size);
            rightCursor = rightX + size;
        }

        result.sort(Comparator.comparingInt(ProfileButtonData::x));
        return List.copyOf(result);
    }

    private void addProfileButton(
            List<ProfileButtonData> out,
            List<Identifier> profiles,
            int offset,
            int x,
            int y,
            int size
    ) {
        int targetIndex = Math.floorMod(profileIndex + offset, profiles.size());
        out.add(new ProfileButtonData(profiles.get(targetIndex), offset, targetIndex, size, x, y));
    }

    private int maxVisibleRadius() {
        int available = getWidth() - (ARROW_ZONE_WIDTH + LABEL_ARROWS_PADDING) * 2 - CARDS_PADDING * 2;
        int used = ACTIVE_CARD_SIZE;
        int radius = 0;

        while (radius < MAX_VISIBLE_RADIUS) {
            int next = radius + 1;
            int add = PROFILE_GAP * 2 + cardSizeForOffset(next) * 2;
            if (used + add > available) {
                return radius;
            }
            used += add;
            radius = next;
        }
        return radius;
    }

    private static int cardSizeForOffset(int offset) {
        return Math.max(
                MIN_CARD_SIZE,
                ACTIVE_CARD_SIZE - Math.abs(offset) * CARD_SIZE_STEP
        );
    }

    @Override
    protected void extractWidgetRenderState(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        rebuildHeaderLayout();
        rebuildVisibleProfiles();

        boolean left = onArrowLeft(mouseX, mouseY);
        boolean right = onArrowRight(mouseX, mouseY);
        if (!left) {
            focusedLeft = false;
        }
        if (!right) {
            focusedRight = false;
        }

        renderProfileCards(graphics, mouseX, mouseY);
        renderHeader(graphics, mouseX, mouseY);
        renderArrows(graphics, left, right);
    }

    private void renderHeader(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        boolean groupHovered = labelGroupHovered(mouseX, mouseY);
        boolean profileHovered = labelProfileHovered(mouseX, mouseY);

        renderLabel(graphics, labelGroup, groupHovered);
        renderLabel(graphics, labelProfile, profileHovered);

        if (groupHovered) {
            renderTooltip(graphics, groupTooltip(), mouseX, mouseY);
        }
    }

    private void renderLabel(GuiGraphicsExtractor graphics, Label label, boolean hovered) {
        int color = hovered ? SIColors.WHITE : SIColors.ALMOST_WHITE;
        graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrollingWithDefaultCenter(label.text().copy().withColor(color),
                        label.x(), label.x() + label.width(),
                        label.y(), label.y() + Minecraft.getInstance().font.lineHeight - 1);

    }

    private Component groupTooltip() {
        if (filterMode == FilterMode.ALL){
            return Component.translatable("gui.softimprints.profile_switch.group.tooltip.all");
        }
        else {
            return Component.translatable("gui.softimprints.profile_switch.group.tooltip.filtered")
                    .append("\n")
                    .append(Component.translatable("gui.softimprints.profile_switch.group.tooltip.filtered_all"));
        }
    }

    private void renderTooltip(GuiGraphicsExtractor graphics, Component tooltip, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.setTooltipForNextFrame(font, font.split(tooltip, TOOLTIP_MAX_WIDTH), mouseX, mouseY);
    }

    private void renderProfileCards(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        ProfileButtonData hovered = null;
        for (ProfileButtonData profile : visibleProfiles) {
            if (profile.containsMouse(mouseX, mouseY)) {
                hovered = profile;
                continue;
            }
            renderProfileCard(graphics, profile, mouseX, mouseY, false);
        }
        if (hovered != null) {
            renderProfileCard(graphics, hovered, mouseX, mouseY, true);
        }
    }

    private void renderProfileCard(GuiGraphicsExtractor graphics, ProfileButtonData data, double mouseX, double mouseY, boolean hovered) {
        renderCardBorders(graphics, data, hovered);
        var profile = ImprintProfiles.getProfile(data.identifier());
        if (profile != null) {
            renderCardIcon(graphics, data, profile);
        }
        var entry = context.session().entryOrNull(data.identifier);
        if (entry instanceof InvalidProfileEntry invalid){
            renderCardWaring(graphics, data, invalid.issue(), mouseX, mouseY, hovered);
        }
    }

    private void renderCardWaring(GuiGraphicsExtractor graphics, ProfileButtonData data,
                                  ProfileIssue issue,
                                  double mouseX, double mouseY,
                                  boolean hovered
    ) {

        int subBorderSize = (data.size / 2) + 2;
        int subBorderX = data.x() + (data.size - subBorderSize - 2);
        int subBorderY = data.y() + (data.size - subBorderSize - 2);

        graphics.enableScissor(data.x() + 2, data.y(), data.x() + data.size() - 2, subBorderY);
        Screen.extractMenuBackgroundTexture(
                graphics, Screen.MENU_BACKGROUND,
                data.x() + 2, data.y() + 2,
                0,
                0,
                data.size() - 4, data.size() - 4
        );
        graphics.disableScissor();

        graphics.enableScissor(data.x(), subBorderY, subBorderX, subBorderY + subBorderSize);

        Screen.extractMenuBackgroundTexture(
                graphics, Screen.MENU_BACKGROUND,
                data.x() + 2, data.y() + 2,
                0,
                0,
                data.size() - 4, data.size() - 4
        );
        graphics.disableScissor();

        BoxRenderer.render(graphics, hovered ? BoxSkins.TAB_HOVERED : BoxSkins.TAB,
                subBorderX,
                subBorderY,
                subBorderSize, subBorderSize,
                EnumSet.of(BorderSides.LEFT, BorderSides.TOP));

        Screen.extractMenuBackgroundTexture(
                graphics, Screen.MENU_BACKGROUND,
                subBorderX + 2,
                subBorderY + 2,
                0,
                0,
                subBorderSize - 2, subBorderSize - 2
        );

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(SICommon.MODID, "icon/warning"),
                subBorderX + 2,
                subBorderY + 2,
                subBorderSize - 2,
                subBorderSize - 2
        );

        if (hovered){
            renderTooltip(graphics, issue.tooltip(), (int) mouseX, (int) mouseY);
        }
    }

    private void renderCardIcon(GuiGraphicsExtractor graphics, ProfileButtonData data, ImprintProfile profile) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);

        var icon = iconsCache.computeIfAbsent(profile.id,
                _ -> profile.preview().icon()
        );

        TextureAtlasSprite sprite = atlas.getSprite(icon);

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                data.x() + 2,
                data.y() + 2,
                data.size() - 4,
                data.size() - 4
        );
    }


    private void renderCardBorders(GuiGraphicsExtractor graphics, ProfileButtonData data, boolean hovered){
        BoxRenderer.render(graphics, hovered ? BoxSkins.TAB_HOVERED : BoxSkins.TAB,
                data.x(), data.y(), data.size(), data.size());
    }

    private void renderArrows(GuiGraphicsExtractor graphics, boolean left, boolean right) {
        int arrowH = 8;
        int arrowY = getY() + (getHeight() / 2 - arrowH) + CARDS_CENTER_Y_OFFSET;
        int textureW = 8;
        int padding = (ARROW_ZONE_WIDTH / 2);
        renderArrow(graphics, getX() + padding, arrowY, 0, 0, left, focusedLeft);
        renderArrow(graphics, getX() + (getWidth() + padding - ARROW_ZONE_WIDTH) - textureW, arrowY, 16, 0, right, focusedRight);
    }

    private void renderArrow(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int u,
            int v,
            boolean hovered,
            boolean focused
    ) {
        graphics.pose().pushMatrix();
        if (focused) {
            scaleAroundCenter(graphics, x, y, 8, 16, 0.93F);
        }
        if (hovered) {
            v += 8;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED,
                SITextures.ARROWS,
                x, y,
                u, v,
                8, 16,
                4, 8,
                32, 32
        );
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        if (!this.active || !this.visible || (button != 0 && button != 1) || !this.isMouseOver(event.x(), event.y())) {
            return false;
        }

        rebuildHeaderLayout();
        rebuildVisibleProfiles();

        if (labelGroupHovered(event.x(), event.y())) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            if (button == 1) {
                showAllProfiles();
            } else {
                cycleFilterGroup(1);
            }
            return true;
        }

        if (labelProfileHovered(event.x(), event.y())) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            switchProfile(button == 1 ? -1 : 1);
            return true;
        }

        if (button != 0) {
            return false;
        }

        boolean left = onArrowLeft(event.x(), event.y());
        boolean right = onArrowRight(event.x(), event.y());
        if (left || right) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            focusedLeft = left;
            focusedRight = right;
            switchProfile(left ? -1 : 1);
            return true;
        }

        for (ProfileButtonData data : visibleProfiles) {
            if (data.containsMouse(event.x(), event.y())) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                profileIndex = data.targetIndex();
                selectProfile(data.identifier());
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        focusedLeft = false;
        focusedRight = false;
        return super.mouseReleased(event);
    }

    private static void scaleAroundCenter(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float scale) {
        float cx = x + w / 2.0F;
        float cy = y + h / 2.0F;

        graphics.pose().translate(cx, cy);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-cx, -cy);
    }

    private boolean labelGroupHovered(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) && labelGroup.containsMouse(mouseX, mouseY);
    }

    private boolean labelProfileHovered(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) && labelProfile.containsMouse(mouseX, mouseY);
    }

    private boolean onArrowLeft(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        return mouseX < getX() + ARROW_ZONE_WIDTH + LABEL_ARROWS_PADDING * 2 && mouseY > getY() + ARROW_ZONE_TOP_PADDING;
    }

    private boolean onArrowRight(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        return mouseX > getX() + getWidth() - ARROW_ZONE_WIDTH - LABEL_ARROWS_PADDING * 2 && mouseY > getY() + ARROW_ZONE_TOP_PADDING;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private record Label(Component text, int x, int y, int width, int height) {

        private static Label empty() {
            return new Label(Component.empty(), 0, 0, 0, 0);
        }

        private boolean containsMouse(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }
    }

    private record ProfileButtonData(
            Identifier identifier,
            int offset,
            int targetIndex,
            int size,
            int x,
            int y
    ) {
        private boolean containsMouse(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + size
                    && mouseY >= y && mouseY < y + size;
        }
    }
}
