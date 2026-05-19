package com.nine.softimprints.client.platform;

import java.nio.file.Path;

public interface IPlatformCoreHelper {

	Path getConfigPath();

	boolean inDev();

}
