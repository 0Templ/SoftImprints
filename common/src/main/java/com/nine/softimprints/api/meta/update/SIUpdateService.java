package com.nine.softimprints.api.meta.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.api.meta.distribution.Distribution;
import com.nine.softimprints.platform.Platform;
import net.minecraft.SharedConstants;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SIUpdateService {


    public static final String UPDATE_JSON_URL = "https://raw.githubusercontent.com/0Templ/ModVersions/refs/heads/main/v2/snow-imprints.json";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Soft Imprints Update Checker");
        thread.setDaemon(true);
        return thread;
    });

    private static final AtomicReference<CompletableFuture<SIUpdateResult>> CHECK = new AtomicReference<>();
    private static final AtomicReference<SIUpdateResult> CURRENT = new AtomicReference<>(SIUpdateResult.CHECKING);

    private SIUpdateService() {
    }

    public static synchronized void startAsync() {
        if (CHECK.get() != null) {
            return;
        }
        CompletableFuture<SIUpdateResult> future = CompletableFuture
                .supplyAsync(SIUpdateService::fetch, EXECUTOR)
                .exceptionally(e -> {
                    SICommon.LOGGER.debug("Couldn't check candidates info: {}", e.getMessage());
                    return SIUpdateResult.FAILED;
                });
        CHECK.set(future);
        future.thenAccept(CURRENT::set);
    }

    public static SIUpdateResult current() {
        if (CHECK.get() == null) {
            startAsync();
        }
        return CURRENT.get();
    }

    public static boolean hasUpdate() {
        return current().updateAvailable();
    }

    public static SIUpdateResult await() {
        if (CHECK.get() == null) {
            startAsync();
        }
        CompletableFuture<SIUpdateResult> future = CHECK.get();
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SICommon.LOGGER.debug("Update check was interrupted: {}", e.getMessage());
            return SIUpdateResult.FAILED;
        } catch (Exception e) {
            SICommon.LOGGER.debug("Update check did not complete in time: {}", e.getMessage());
            return SIUpdateResult.FAILED;
        }
    }

    private static SIUpdateResult fetch() {
        JsonObject updateInfo = tryGetUrlInfo();
        if (updateInfo == null) {
            return SIUpdateResult.FAILED;
        }

        String mcVersion = SharedConstants.getCurrentVersion().name();
        String loader = Platform.CORE.currentLoader();

        String currentModVersion = Platform.CORE.modVersion();

        Map<Distribution, SIUpdateCandidate> updates = new HashMap<>();

        Map<Distribution, DistributionInfo> updateVersions = getDistributionInfos(mcVersion, loader, updateInfo);
        updateVersions.forEach((distro,info) -> {
            if (compareVersions(currentModVersion, info.version)){
                updates.put(distro, new SIUpdateCandidate(SIUpdateChannel.RELEASE, info.url, info.version));
            }
        });
        if (updates.isEmpty()) {
            return SIUpdateResult.NONE;
        }
        return SIUpdateResult.available(updates);
    }

    private static boolean compareVersions(String currentVersion, String updateVersion) {
        if (compareStrings(currentVersion, updateVersion) >= 0) {
            return false;
        }
        return true;
    }

    public record DistributionInfo(
            String version,
            String url
    ) {}

    public static Map<Distribution, DistributionInfo> getDistributionInfos(
            String mcVersion,
            String loader,
            JsonObject json
    ) {
        Map<Distribution, DistributionInfo> infos = new EnumMap<>(Distribution.class);

        if (json == null || !json.has("versions") || !json.get("versions").isJsonObject()) {
            return infos;
        }

        JsonObject versions = json.getAsJsonObject("versions");

        if (!versions.has(mcVersion) || !versions.get(mcVersion).isJsonObject()) {
            return infos;
        }

        JsonObject mcVersions = versions.getAsJsonObject(mcVersion);

        String normalizedLoader = loader.toLowerCase(Locale.ROOT);

        if (!mcVersions.has(normalizedLoader) || !mcVersions.get(normalizedLoader).isJsonObject()) {
            return infos;
        }

        JsonObject loaderObject = mcVersions.getAsJsonObject(normalizedLoader);

        if (!loaderObject.has("sources") || !loaderObject.get("sources").isJsonObject()) {
            return infos;
        }

        JsonObject sources = loaderObject.getAsJsonObject("sources");

        for (Map.Entry<String, JsonElement> entry : sources.entrySet()) {
            Distribution distribution = Distribution.fromJsonKey(entry.getKey());

            if (distribution == null || !entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject sourceObject = entry.getValue().getAsJsonObject();

            if (!sourceObject.has("version") || !sourceObject.has("url")) {
                continue;
            }

            String version = sourceObject.get("version").getAsString();
            String url = sourceObject.get("url").getAsString();

            infos.put(distribution, new DistributionInfo(version, url));
        }

        return infos;
    }

    private static JsonObject tryGetUrlInfo() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPDATE_JSON_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return JsonParser.parseString(response.body()).getAsJsonObject();
            }
        } catch (Exception e) {
            if (Platform.CORE.inDevEnvironment()){
                SICommon.LOGGER.warn("Couldn't fetch candidates info: {}", e.getMessage());
            }
        }
        return null;
    }

    public static int compareStrings(String v1, String v2) {
        var splitV1 = v1.split("[.-]");
        var splitV2 = v2.split("[.-]");
        int len = Math.max(splitV1.length, splitV2.length);

        for (int i = 0; i < len; i++) {
            var partV1 = i < splitV1.length ? splitV1[i] : "0";
            var partV2 = i < splitV2.length ? splitV2[i] : "0";
            boolean digitV1 = partV1.matches("\\d+");
            boolean digitV2 = partV2.matches("\\d+");
            int compared;
            if (digitV1 && digitV2) {
                compared = Integer.compare(Integer.parseInt(partV1), Integer.parseInt(partV2));
            } else {
                compared = compareChars(partV1, partV2);
            }
            if (compared != 0) return compared;
        }
        return 0;
    }

    private static int compareChars(String partV1, String partV2) {
        char[] charsV1 = partV1.toLowerCase().toCharArray();
        char[] charsV2 = partV2.toLowerCase().toCharArray();
        int charsLen = Math.max(charsV1.length, charsV2.length);
        for (int j = 0; j < charsLen; j++) {
            char chV1 = j < charsV1.length ? charsV1[j] : '0';
            char chV2 = j < charsV2.length ? charsV2[j] : '0';
            int c1 = charToInt(chV1);
            int c2 = charToInt(chV2);
            if (c1 != c2) {
                return c1 > c2 ? 1 : -1;
            }
        }
        return 0;
    }

    private static int charToInt(char c) {
        int ret = c;
        if (c >= '0' && c <= '9') {
            ret -= 48;
        }
        return ret;
    }
}
