package com.nine.softimprints.platform;

import com.google.gson.JsonObject;
import com.nine.softimprints.api.meta.update.SIUpdateCandidate;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformCoreHelper {

	Path getConfigPath();

	boolean inDev();

	String modVersion();


	String loader();

	/**
	 * @return {@code true} if a mod with the given id is loaded on the current platform.
	 */
	boolean modLoaded(String id);

}
