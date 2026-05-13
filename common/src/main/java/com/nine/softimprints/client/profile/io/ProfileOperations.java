package com.nine.softimprints.client.profile.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.io.json.JsonProfile;
import com.nine.softimprints.client.profile.io.json.JsonImprintResolution;
import com.nine.softimprints.client.profile.io.json.JsonSurfaceSettings;
import com.nine.softimprints.client.profile.io.json.JsonTextureSets;
import com.nine.softimprints.client.profile.migrations.ProfileMigrations;
import com.nine.softimprints.client.profile.options.block.SurfaceBlock;
import com.nine.softimprints.client.profile.options.layer.ImprintLayer;
import com.nine.softimprints.client.profile.options.resoltuion.ImprintResolution;
import com.nine.softimprints.client.profile.options.surface.SurfaceSettings;
import com.nine.softimprints.client.profile.options.surface.ZeroLayerSource;
import com.nine.softimprints.client.profile.options.texture.ImprintTextureSet;
import com.nine.softimprints.client.profile.options.texture.ImprintTextureSets;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ProfileOperations {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<Identifier, ImprintProfile> convertToDomainMap(Map<Identifier, JsonProfile> map){
        return map.entrySet().stream().collect(HashMap::new,
                (m, entry) -> {
                    var parsed = parseFromRawToDomain(entry.getKey(), entry.getValue());
                    m.put(entry.getKey(), parsed);
                },
                Map::putAll);
    }

    public static Map<Identifier, JsonProfile> convertToRawMap(Map<Identifier, JsonElement> map){
        return map.entrySet().stream().collect(HashMap::new,
                (m, entry) -> {
                    Identifier id = entry.getKey();
                    try {
                        m.put(id, parseFromJsonToRaw(entry.getValue()));
                    } catch (RuntimeException e) {
                        SICommon.LOGGER.warn("Skipping profile {}: {}", id, e.getMessage());
                    }
                },
                Map::putAll);
    }

    @Nullable
    public static JsonProfile parseFromJsonToRaw(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        int version = obj.has(JsonProfile.SCHEMA_KEY) ? obj.get(JsonProfile.SCHEMA_KEY).getAsInt() : 1;
        if (version > JsonProfile.CURRENT_SCHEMA){
            throw new IllegalArgumentException(
                    "Profile schema " + version + " is newer than supported " + JsonProfile.CURRENT_SCHEMA
            );
        }
        JsonElement toParse = version < JsonProfile.CURRENT_SCHEMA
                ? ProfileMigrations.migrate(obj, version, JsonProfile.CURRENT_SCHEMA)
                : obj;


        return GSON.fromJson(toParse, JsonProfile.class);
    }

    public static JsonElement toJsonElement(JsonProfile profile) {
        return GSON.toJsonTree(profile);
    }

    public static ImprintProfile parseFromRawToDomain(Identifier id, JsonProfile jp) {
        var layers = jp.layers().stream()
                .map(jl -> new ImprintLayer(jl.value(), jl.enable(), jl.expand(), jl.innerJitter(), jl.outerJitter(), jl.erosion()))
                .toList();


        ImprintTextureSets textureSets = ImprintTextureSets.fromSets(
                jp.textureSets().selected(),
                Identifier.parse(jp.textureSets().initLayer()),
                jp.textureSets().texturesByValue().entrySet().stream()
                        .map(v -> {
                            Map<Byte, Identifier> texturesByValue = new HashMap<>();
                            for (var element : v.getValue().entrySet()){
                                byte b = Byte.parseByte(element.getKey());
                                Identifier texture = Identifier.parse(element.getValue());
                                texturesByValue.put(b, texture);
                            }
                            return new ImprintTextureSet(
                                    v.getKey(),
                                    texturesByValue
                            );
                        }).toList()
        );

        Set<SurfaceBlock> blocks = jp.supportedBlocks().stream()
                .map(Identifier::parse)
                .map(SurfaceBlock::of)
                .collect(Collectors.toSet());
        SurfaceSettings surface = parseSurfaceSettings(jp);

        ImprintResolution resolution = parseResolution(jp);

        return new ImprintProfile(id, layers, blocks, surface, textureSets, resolution);
    }

    public static JsonProfile toJsonModel(ImprintProfile profile) {

        Set<String> supportedBlocks = profile.supportedBlocks().stream()
                .map(SurfaceBlock::id)
                .map(Identifier::toString).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        JsonTextureSets sets = new JsonTextureSets(
                profile.textureSets.selected(),
                profile.textureSets.zeroLayer().toString(),
                profile.textureSets.map().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().texturesByValue().entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .collect(Collectors.toMap(
                                                v -> String.valueOf(v.getKey()),
                                                v -> v.getValue().toString(),
                                                (a, b) -> b,
                                                LinkedHashMap::new
                                        )),
                                (a, b) -> b,
                                LinkedHashMap::new
                        )));

        JsonSurfaceSettings surface = new JsonSurfaceSettings(
                profile.surface().mode(),
                profile.surface().zeroLayerSource()
        );

        JsonImprintResolution resolution = new JsonImprintResolution(
                profile.resolution.mapSize(),
                profile.resolution.textureSize()
        );

        return new JsonProfile(JsonProfile.CURRENT_SCHEMA, profile.layers, supportedBlocks, surface, sets, resolution);
    }

    private static SurfaceSettings parseSurfaceSettings(JsonProfile jp) {
        JsonSurfaceSettings surface = jp.surface();

        SurfaceMode mode = surface != null && surface.mode() != null
                ? surface.mode()
                : SurfaceSettings.DEFAULT.mode();

        ZeroLayerSource zeroLayerSource = surface != null && surface.zeroLayerSource() != null
                ? surface.zeroLayerSource()
                : ZeroLayerSource.SURFACE;

        return new SurfaceSettings(mode, zeroLayerSource);
    }

    private static ImprintResolution parseResolution(JsonProfile jp) {
        JsonImprintResolution raw = jp.resolution();

        int mapSize = raw != null && raw.mapSize() != null
                ? raw.mapSize()
                : ImprintResolution.DEFAULT.mapSize();

        int textureSize = raw != null && raw.textureSize() != null
                ? raw.textureSize()
                : mapSize;


        return new ImprintResolution(mapSize, textureSize);
    }


}
