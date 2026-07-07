package com.nine.softimprints.ui.component.profile;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.mixin.accessor.client.GuiGraphicsExtractorAccessor;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.ui.util.region.BoxRenderer;
import com.nine.softimprints.ui.util.region.BoxSkin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public final class ProfileCards {

    public static final Identifier MISSING_PROFILE_ICON =
            Identifier.fromNamespaceAndPath(SICommon.MODID, "icon/missing/missing_profile");

    public static final int ICON_INSET = 2;

    private ProfileCards() {
    }

    public record CardIcon(Identifier location, Identifier requested, boolean missing) {
    }

    public static CardIcon iconFor(
            GuiGraphicsExtractor graphics,
            ImprintProfile profile
    ) {
        Identifier requested = profile.preview().icon();
        TextureAtlasSprite sprite = ((GuiGraphicsExtractorAccessor) graphics).si$guiSprites().getSprite(requested);
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return new CardIcon(MISSING_PROFILE_ICON, requested, true);
        }
        return new CardIcon(requested, requested, false);
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            BoxSkin skin,
            CardIcon icon,
            int x,
            int y,
            int size
    ) {
        BoxRenderer.render(graphics, skin, x, y, size, size);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                icon.location(),
                x + ICON_INSET,
                y + ICON_INSET,
                size - ICON_INSET * 2,
                size - ICON_INSET * 2
        );
    }

}
