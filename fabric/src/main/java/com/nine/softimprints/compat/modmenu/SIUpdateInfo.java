package com.nine.softimprints.compat.modmenu;


import com.nine.softimprints.api.meta.update.SIUpdateCandidate;
import com.nine.softimprints.api.meta.update.SIUpdateChannel;
import com.nine.softimprints.api.meta.update.SIUpdateResult;
import com.nine.softimprints.core.Constants;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import net.minecraft.network.chat.Component;

public class SIUpdateInfo implements UpdateInfo {

    public static SIUpdateInfo NONE = new SIUpdateInfo(false, Component.empty(), "", UpdateChannel.RELEASE);

    private final boolean updateAvailable;
    private final Component updateMessage;
    private final String url;
    private final UpdateChannel channel;

    public SIUpdateInfo(
            boolean updateAvailable,
            Component updateMessage,
            String url,
            UpdateChannel channel
    ) {
        this.updateAvailable = updateAvailable;
        this.updateMessage = updateMessage;
        this.url = url;
        this.channel = channel;
    }

    public static SIUpdateInfo from(SIUpdateResult result) {
        var updates = result.candidates();
        if (!result.updateAvailable() || updates == null || updates.isEmpty()) {
            return NONE;
        }
        SIUpdateCandidate upd = updates.get(Constants.PREFERRED_DISTRO);
        if (upd == null) {
            var first = updates.entrySet().stream().findFirst();
            if (first.isPresent()) {
                upd = first.get().getValue();
            }
        }
        if (upd == null) return NONE;
        return new SIUpdateInfo(
                true,
                Component.literal(upd.version()),
                upd.url(),
                toModMenuChannel(upd.channel())
        );
    }

    private static UpdateChannel toModMenuChannel(SIUpdateChannel channel) {
        return switch (channel) {
            case RELEASE -> UpdateChannel.RELEASE;
            case BETA -> UpdateChannel.BETA;
            case ALPHA -> UpdateChannel.ALPHA;
        };
    }

    @Override
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    @Override
    public Component getUpdateMessage() {
        return updateMessage;
    }

    @Override
    public String getDownloadLink() {
        return url;
    }

    @Override
    public UpdateChannel getUpdateChannel() {
        return channel;
    }

}
