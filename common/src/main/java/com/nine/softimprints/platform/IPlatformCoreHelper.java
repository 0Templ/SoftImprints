package com.nine.softimprints.platform;

import java.nio.file.Path;

public interface IPlatformCoreHelper {

	Path getConfigPath();

	boolean inDevEnvironment();

	String modVersion();

	String currentLoader();

	boolean isModLoaded(String id);

}
