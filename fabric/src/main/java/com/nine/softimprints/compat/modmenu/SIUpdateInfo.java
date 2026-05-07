package com.nine.softimprints.compat.modmenu;


import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateInfo;
import net.minecraft.network.chat.Component;

public class SIUpdateInfo implements UpdateInfo {
	
	public static SIUpdateInfo NONE = new SIUpdateInfo(false, Component.empty(), "", UpdateChannel.RELEASE);
	
	private final boolean updateAvailable;
	private final Component updateMessage;
	private final String url;
	private final UpdateChannel channel;
	
	public SIUpdateInfo(boolean updateAvailable, Component updateMessage, String url, UpdateChannel channel) {
		this.updateAvailable = updateAvailable;
		this.updateMessage = updateMessage;
		this.url = url;
		this.channel = channel;
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
