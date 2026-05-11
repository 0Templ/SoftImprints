package com.nine.softimprints.client.ui.screen;

import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.SIImprintProfiles;
import com.nine.softimprints.client.profile.util.ImprintProfilePair;
import com.nine.softimprints.client.ui.cache.UICache;
import com.nine.softimprints.client.ui.component.profile.ProfileSwitchWidget;
import com.nine.softimprints.client.ui.component.group.GroupEntry;
import com.nine.softimprints.client.ui.component.group.GroupSwitcher;
import com.nine.softimprints.client.ui.component.list.ConfigListWidget;
import com.nine.softimprints.client.ui.component.preview.widget.ImprintPreviewWidget;
import com.nine.softimprints.client.ui.component.slider.ExtendedSlider;
import com.nine.softimprints.client.ui.component.widget.button.ApproveButton;
import com.nine.softimprints.client.ui.context.EditorContext;
import com.nine.softimprints.client.ui.context.PreviewSettings;
import com.nine.softimprints.client.ui.context.ProfilesSession;
import com.nine.softimprints.client.ui.layout.LayoutRect;
import com.nine.softimprints.client.ui.screen.settings.*;
import com.nine.softimprints.client.ui.util.constant.SIColors;
import com.nine.softimprints.client.ui.util.constant.SIText;
import com.nine.softimprints.client.ui.util.region.ChromeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SIConfigScreen extends Screen {

    private static final int TOP_CONTENT_OFFSET = 40;
    private static final int BOTTOM_CONTENT_OFFSET = 38;
    private static final int HEADER_LINE_OFFSET = 2;

    private static final double PREVIEW_COLUMN_RATIO = 0.365D;
    private static final double RIGHT_COLUMN_RATIO = 0.18D;
    private static final int COLUMN_GAP = 2;

    private static int SWITCHER_HEIGHT = 42;
    private static final int LEFT_SWITCHER_INSET = 6;
    private static final int PREVIEW_SWITCHER_INSET = 6;
    private static final int RIGHT_SWITCHER_INSET = 6;

    private static final int PREVIEW_FRAME_INSET = 2;
    private static final int PROFILE_SWITCHER_GAP = 6;

    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_PADDING = 6;
    private static final int CONTROL_GAP = 4;

    private static final int BOTTOM_BUTTON_WIDTH = 150;
    private static final int BOTTOM_BUTTON_GAP = 8;
    private static final int BOTTOM_BUTTON_BOTTOM_INSET = 28;

    private static final int MIN_PREVIEW_RESOLUTION = 32;
    private static final int MIN_BRUSH_SIZE = 1;
    private static final int MAX_BRUSH_SIZE = 48;

    private static final Identifier INWORLD_MENU_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    private final Screen parent;
    private final EditorContext context;
    private final PreviewSettings previewSettings;
    private final List<SettingsGroupFactory> settingsGroupFactories;

    private ConfigListWidget settingsListWidget;
    private GroupsPanel groupsPanel;
    private GroupNavigation settingsNavigation;

    private GroupSwitcher<EditorGroup> leftGroupSwitcher;
    private GroupSwitcher<EditorGroup> previewGroupSwitcher;
    private GroupSwitcher<EditorGroup> rightGroupSwitcher;

    private ImprintPreviewWidget previewWidget;
    private ProfileSwitchWidget profileSwitchWidget;

    private Layout layout;

    public SIConfigScreen(Screen parent) {
        super(Component.translatable("screen.softimprints.config.title"));
        this.parent = parent;
        this.context = new EditorContext(new ProfilesSession(loadProfiles()));
        this.previewSettings = new PreviewSettings();
        this.settingsGroupFactories = createSettingsGroupFactories();

        this.context.addSelectedProfileListener(this::onSelectedProfileChanged);
    }

    @Override
    protected void init() {
        super.init();
        this.layout = layout();

        setupSettingsArea(layout);
        setupGroupSwitchers(layout);
        setupPreviewArea(layout);
        setupRightControls(layout);
        setupBottomButtons();
    }


    private void setupSettingsArea(Layout layout) {
        LayoutRect bounds = layout.settingsList();
        int w = Math.min(370, bounds.width());
        int x = bounds.width() / 2 - w / 2;
        this.settingsListWidget = new ConfigListWidget(
                x, bounds.y(),
                w, bounds.height()
        );
        this.addRenderableWidget(settingsListWidget);
    }

    private void setupGroupSwitchers(Layout layout) {
        this.groupsPanel = new GroupsPanel(
                settingsListWidget,
                new GroupBuildContext(context, previewSettings),
                settingsGroupFactories
        );
        this.groupsPanel.rebuild();
        this.settingsNavigation = new GroupNavigation(groupsPanel);

        this.leftGroupSwitcher = addGroupSwitcher(GroupZone.LEFT, layout.leftSwitcher());
        this.previewGroupSwitcher = addGroupSwitcher(GroupZone.MIDDLE, layout.previewSwitcher());
        this.rightGroupSwitcher = addGroupSwitcher(GroupZone.RIGHT, layout.rightSwitcher());
    }

    private GroupSwitcher<EditorGroup> addGroupSwitcher(GroupZone zone, LayoutRect bounds) {
        GroupSwitcher<EditorGroup> switcher = new GroupSwitcher<>(
                bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                settingsNavigation::select
        );
        factoriesIn(zone).forEach(factory ->
                switcher.addGroupEntry(GroupEntry.option(factory.key(), factory.label()))
        );
        this.settingsNavigation.register(zone, switcher);
        this.settingsNavigation.select(UICache.editorGroup());
        this.addRenderableWidget(switcher);
        return switcher;
    }

    private void setupPreviewArea(Layout layout) {
        LayoutRect previewBounds = layout.preview();
        this.previewWidget = new ImprintPreviewWidget(
                previewBounds.x(), previewBounds.y(),
                previewBounds.width(), previewBounds.height(),
                this.previewSettings, this.context
        );
        this.addRenderableWidget(previewWidget);

        LayoutRect switcherBounds = layout.profileSwitcher();
        this.profileSwitchWidget = new ProfileSwitchWidget(
                switcherBounds.x(), switcherBounds.y(),
                switcherBounds.width(), switcherBounds.height(),
                this.context
        );
        this.addRenderableWidget(profileSwitchWidget);
    }

    private void setupRightControls(Layout layout) {
        LayoutRect controls = layout.rightControls();
        int y = controls.y();

        y = addStackedControl(clearPreviewButton(), controls, y);
        y = addStackedControl(resolutionSlider(), controls, y);
        y = addStackedControl(brushSizeSlider(), controls, y);
        addStackedControl(debugModeButton(), controls, y);

        var resetButton = ApproveButton.base(0, 0, 1, CONTROL_HEIGHT,
                Component.empty(),
                _ -> {
                    context.resetAllDrafts();
                    groupsPanel.rebuildFull();
                });

        resetButton.setTooltip(Tooltip.create(Component.translatable("config.softimprints.reset.tooltip")));
        place(resetButton, controls.x(), controls.bottom() - CONTROL_HEIGHT, controls.width());
        this.addRenderableWidget(resetButton);
    }

    private void setupBottomButtons() {
        int totalWidth = BOTTOM_BUTTON_WIDTH * 2 + BOTTOM_BUTTON_GAP;
        int y = this.height - BOTTOM_BUTTON_BOTTOM_INSET;
        int x = (this.width - totalWidth) / 2;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, _ -> closeToParent())
                .bounds(x, y, BOTTOM_BUTTON_WIDTH, CONTROL_HEIGHT).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> applyAndClose())
                .bounds(x + BOTTOM_BUTTON_WIDTH + BOTTOM_BUTTON_GAP, y, BOTTOM_BUTTON_WIDTH, CONTROL_HEIGHT).build());
    }

    private Button clearPreviewButton() {
        return Button.builder(Component.translatable("config.softimprints.preview.clear"), _ -> previewWidget.clearPreview())
                .bounds(0, 0, 1, CONTROL_HEIGHT)
                .build();
    }

    private ExtendedSlider resolutionSlider() {
        return ExtendedSlider.builder("config.softimprints.preview.resolution")
                .bounds(0, 0, 1, CONTROL_HEIGHT)
                .range(MIN_PREVIEW_RESOLUTION, previewWidget.maxResolution())
                .value(previewSettings.resolution())
                .step(1.0D, 0)
                .build()
                .addListener(v -> {
                    previewSettings.setResolution(Math.toIntExact(Math.round(v)));
                    previewWidget.setViewResolution(previewSettings.resolution());
                });
    }

    private ExtendedSlider brushSizeSlider() {
        return ExtendedSlider.builder("config.softimprints.preview.brush_size")
                .bounds(0, 0, 1, CONTROL_HEIGHT)
                .range(MIN_BRUSH_SIZE, MAX_BRUSH_SIZE)
                .value(previewSettings.brushSize())
                .step(1.0D, 0)
                .build()
                .addListener(v -> previewSettings.setBrushSize(Math.toIntExact(Math.round(v))));
    }

    private Button debugModeButton() {
        return Button.builder(debugModeText(), button -> {
                    previewSettings.toggleDebugMode();
                    previewWidget.refreshRenderCache();
                    button.setMessage(debugModeText());
                })
                .bounds(0, 0, 1, CONTROL_HEIGHT)
                .build();
    }

    private Component debugModeText() {
        return Component.translatable("config.softimprints.preview.debug", SIText.onOffState(previewSettings.debugMode()));
    }

    // ── widget placement helpers ─────────────────────────────────────────

    private int addStackedControl(AbstractWidget widget, LayoutRect controls, int y) {
        place(widget, controls.x(), y, controls.width());
        this.addRenderableWidget(widget);
        return y + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private static void place(AbstractWidget widget, int x, int y, int width) {
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);
    }

    // ── lifecycle ────────────────────────────────────────────────────────

    private void onSelectedProfileChanged() {
        if (settingsNavigation != null) {
            settingsNavigation.onProfileChanged();
        }
    }

    @Override
    public void onClose() {
        applyAndClose();
    }

    private void applyAndClose() {
        context.saveChanged();
        closeToParent();
    }

    private void closeToParent() {
        context.notifyOnCloseListeners();
        minecraft.setScreen(parent);
    }

    private List<ImprintProfilePair> loadProfiles() {
        var builtInsById = ImprintProfiles.builtInProfiles()
                .collect(Collectors.toMap(ImprintProfile::id, Function.identity()));

        return ImprintProfiles.profiles()
                .sorted(Comparator
                        .comparing((ImprintProfile p) -> !p.id().equals(SIImprintProfiles.SNOW_PROFILE))
                        .thenComparing(p -> p.id().toString())
                )
                .map(profile -> new ImprintProfilePair(
                        profile,
                        builtInsById.getOrDefault(profile.id(), profile)
                ))
                .toList();
    }

    private List<SettingsGroupFactory> createSettingsGroupFactories() {
        return List.of(
                new LayersGroupFactory(),
                new BlocksGroupFactory(),
                new TargetsGroupFactory(),
                new GeneralGroupFactory(),
                new StorageGroupFactory(),
                new PluginsGroupFactory()
        );
    }

    private List<SettingsGroupFactory> factoriesIn(GroupZone zone) {
        return settingsGroupFactories.stream()
                .filter(factory -> factory.zone() == zone)
                .toList();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 4, SIColors.WHITE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        renderChromeLines(graphics, layout);
        renderSettingsBackground(graphics);
        renderPreviewBackground(graphics, layout);
        renderRightBackground(graphics, layout);
    }

    private void renderChromeLines(GuiGraphicsExtractor graphics, Layout layout) {
        renderHeaderLineAroundSwitchers(graphics);
        ChromeRenderer.footerLine(graphics, 0, this.height - BOTTOM_CONTENT_OFFSET, width);
        ChromeRenderer.rightDivider(graphics, layout.previewPanel().x() - COLUMN_GAP, layout.y(), layout.height());
        ChromeRenderer.leftDivider(graphics, layout.rightPanel().x() - COLUMN_GAP, layout.y(), layout.height());
    }

    private void renderHeaderLineAroundSwitchers(GuiGraphicsExtractor graphics) {
        int y = TOP_CONTENT_OFFSET - HEADER_LINE_OFFSET;
        int x = 0;
        x = renderHeaderLineBefore(graphics, leftGroupSwitcher, x, y);
        x = renderHeaderLineBefore(graphics, previewGroupSwitcher, x, y);
        x = renderHeaderLineBefore(graphics, rightGroupSwitcher, x, y);
        if (x < width) {
            ChromeRenderer.headerLine(graphics, x, y, width - x);
        }
    }

    private int renderHeaderLineBefore(
            GuiGraphicsExtractor graphics,
            GroupSwitcher<EditorGroup> switcher,
            int x, int y
    ) {
        if (switcher == null || switcher.size() == 0) return x;
        if (switcher.getX() > x) {
            ChromeRenderer.headerLine(graphics, x, y, switcher.getX() - x);
        }
        return Math.max(x, switcher.getX() + switcher.getWidth());
    }

    private void renderSettingsBackground(GuiGraphicsExtractor graphics) {
        if (settingsListWidget == null) return;
        var layout = layout();
        drawMenuBackground(
                graphics,
                layout.settingsX(), layout.y(),
                layout.settingsW(), layout.height()
        );
    }

    private void renderPreviewBackground(GuiGraphicsExtractor graphics, Layout layout) {
        if (previewWidget == null || profileSwitchWidget == null) return;

        LayoutRect panel = layout.previewPanel();
        drawMenuBackground(
                graphics,
                panel.x(), panel.y(),
                panel.width(), previewWidget.getHeight() + PREVIEW_FRAME_INSET * 2
        );

        ChromeRenderer.footerLine(
                graphics,
                panel.x(),
                previewWidget.getY() + PREVIEW_FRAME_INSET + previewWidget.getHeight(),
                panel.width()
        );

        drawMenuBackground(
                graphics,
                panel.x(), profileSwitchWidget.getY(),
                panel.width(), profileSwitchWidget.getHeight()
        );
    }

    private void renderRightBackground(GuiGraphicsExtractor graphics, Layout layout) {
        LayoutRect panel = layout.rightPanel();
        extractMenuBackground(graphics, panel.x(), panel.y(), panel.width(), panel.height());
    }

    public static void extractMenuBackground(Minecraft mc, GuiGraphicsExtractor graphics,
                                              int x, int y, int width, int height) {
        extractMenuBackgroundTexture(
                graphics,
                mc.level == null ? MENU_BACKGROUND : INWORLD_MENU_BACKGROUND,
                x, y,
                0.0F, 0.0F,
                width, height
        );
    }

    private void drawMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        extractMenuBackground(this.minecraft, graphics, x, y, width, height);
    }

    private Layout layout() {
        int totalW = this.width;
        int contentH = this.height - TOP_CONTENT_OFFSET - BOTTOM_CONTENT_OFFSET;

        int previewW = (int) Math.round(totalW * PREVIEW_COLUMN_RATIO);
        int rightW = (int) Math.round(totalW * RIGHT_COLUMN_RATIO);
        int settingsW = totalW - previewW - rightW;

        int settingsX = 0;
        int previewX = settingsX + settingsW;
        int rightX = previewX + previewW;

        return new Layout(
                TOP_CONTENT_OFFSET,
                contentH,
                settingsX, settingsW,
                previewX + COLUMN_GAP, previewW - COLUMN_GAP,
                rightX + COLUMN_GAP, rightW - COLUMN_GAP
        );
    }

    /**
     * Geometry of all major regions on the screen. Three columns:
     * settings (left), preview (centre), right-side controls.
     */
    private record Layout(
            int y,
            int height,
            int settingsX, int settingsW,
            int previewX, int previewW,
            int rightX, int rightW
    ) {

        int switcherY() {
            return y - 23;
        }

        LayoutRect settingsList() {
            return new LayoutRect(settingsX, y, settingsW, height);
        }

        LayoutRect previewPanel() {
            return new LayoutRect(previewX, y, previewW, height);
        }

        LayoutRect rightPanel() {
            return new LayoutRect(rightX, y, rightW, height);
        }

        LayoutRect leftSwitcher() {
            return new LayoutRect(
                    settingsX + LEFT_SWITCHER_INSET, switcherY(),
                    settingsW - LEFT_SWITCHER_INSET * 2, 23
            );
        }

        LayoutRect previewSwitcher() {
            return new LayoutRect(
                    previewX + PREVIEW_SWITCHER_INSET, switcherY(),
                    previewW - PREVIEW_SWITCHER_INSET * 2, ((23))
            );
        }

        LayoutRect rightSwitcher() {
            return new LayoutRect(
                    rightX + RIGHT_SWITCHER_INSET, switcherY(),
                    rightW - RIGHT_SWITCHER_INSET * 2 - 1, ((23))
            );
        }

        int previewBodyHeight() {
            return height - PROFILE_SWITCHER_GAP - SWITCHER_HEIGHT;
        }

        LayoutRect preview() {
            return new LayoutRect(
                    previewX + PREVIEW_FRAME_INSET, y + PREVIEW_FRAME_INSET,
                    previewW - PREVIEW_FRAME_INSET * 2, previewBodyHeight()
            );
        }

        LayoutRect profileSwitcher() {
            return new LayoutRect(
                    previewX, y + previewBodyHeight() + PROFILE_SWITCHER_GAP,
                    previewW, SWITCHER_HEIGHT
            );
        }

        LayoutRect rightControls() {
            return rightPanel().inset(CONTROL_PADDING, CONTROL_GAP, CONTROL_PADDING, CONTROL_GAP);
        }
    }
}
