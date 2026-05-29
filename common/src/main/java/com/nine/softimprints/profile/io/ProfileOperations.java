package com.nine.softimprints.profile.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.model.SurfaceMode;
import com.nine.softimprints.platform.Platform;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.catalog.entry.ImprintProfileEntry;
import com.nine.softimprints.profile.catalog.entry.InvalidProfileEntry;
import com.nine.softimprints.profile.catalog.entry.ValidProfileEntry;
import com.nine.softimprints.profile.catalog.entry.exception.ProfileLoadException;
import com.nine.softimprints.profile.catalog.entry.exception.UnsupportedProfileSchemaException;
import com.nine.softimprints.profile.io.json.*;
import com.nine.softimprints.profile.migrations.ProfileMigrations;
import com.nine.softimprints.profile.options.ImprintPreviewAssets;
import com.nine.softimprints.profile.options.block.SurfaceBlock;
import com.nine.softimprints.profile.options.layer.ImprintLayer;
import com.nine.softimprints.profile.options.resolution.ImprintResolution;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;
import com.nine.softimprints.profile.options.surface.ZeroLayerSource;
import com.nine.softimprints.profile.options.texture.ImprintTextureSet;
import com.nine.softimprints.profile.options.texture.ImprintTextures;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ProfileOperations {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<Identifier, ImprintProfile> convertToDomainMap(Map<Identifier, JsonProfile> map){
        return map.entrySet().stream().collect(HashMap::new,
                (m, entry) -> {
                    try {
                        var parsed = parseFromRawToDomain(entry.getKey(), entry.getValue());
                        m.put(entry.getKey(), parsed);
                    }
                    catch (Exception e){
                        SICommon.LOGGER.warn("Couldn't load profile {}: {}", entry.getKey(), e);
                    }
                },
                Map::putAll);
    }

    public record ProfileEntriesResult(
            Map<Identifier, ImprintProfileEntry> entries,
            Map<Identifier, JsonProfile> raw
    ) {}

    public static ProfileEntriesResult convertToEntries(Map<Identifier, SourcedJson> map) {
        Map<Identifier, ImprintProfileEntry> entries = new LinkedHashMap<>();
        Map<Identifier, JsonProfile> raw = new LinkedHashMap<>();
        map.forEach((id, value) -> {
            var source = value.source();
            try {
                JsonProfile json = parseFromJsonToRaw(value.json(), id);
                ImprintProfile parsed = parseFromRawToDomain(id, json);
                raw.put(id, json);
                entries.put(id, new ValidProfileEntry(id, parsed));
            } catch (ProfileLoadException e) {
                SICommon.LOGGER.warn("Couldn't load profile {}: {}", id, e.getMessage());
                entries.put(id, new InvalidProfileEntry(id, e.toIssue(), source));
            }
        });

        return new ProfileEntriesResult(entries, raw);
    }

    public static Map<Identifier, JsonProfile> convertToRawMap(Map<Identifier, JsonElement> map){
        return map.entrySet().stream().collect(HashMap::new,
                (m, entry) -> {
                    Identifier id = entry.getKey();
                    try {
                        m.put(id, parseFromJsonToRaw(entry.getValue(), id));
                    } catch (RuntimeException e) {
                        SICommon.LOGGER.warn("Couldn't parse profile {}: {}", id, e.getMessage());
                    }
                },
                Map::putAll);
    }

    @Nullable
    public static JsonProfile parseFromJsonToRaw(JsonElement json, @Nullable Identifier id) {
        JsonObject obj = json.getAsJsonObject();
        int version = obj.has(JsonProfile.SCHEMA_KEY) ? obj.get(JsonProfile.SCHEMA_KEY).getAsInt() : 1;
        if (version > JsonProfile.CURRENT_SCHEMA){
            throw new UnsupportedProfileSchemaException(version, JsonProfile.CURRENT_SCHEMA);
        }
        JsonElement toParse;

        if (version < JsonProfile.CURRENT_SCHEMA){
            toParse = ProfileMigrations.migrate(obj, version, JsonProfile.CURRENT_SCHEMA);
            if (Platform.CORE.inDev()){
                if (id != null) SICommon.LOGGER.info("Migrated profile {} from v{} to v{}", id, version, JsonProfile.CURRENT_SCHEMA);
                else SICommon.LOGGER.info("Migrated profile from v{} to v{}", version, JsonProfile.CURRENT_SCHEMA);
            }
        }
        else toParse = obj;

        return GSON.fromJson(toParse, JsonProfile.class);
    }

    public static JsonElement toJsonElement(JsonProfile profile) {
        return GSON.toJsonTree(profile);
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
                profile.resolution.mapSize()
        );

        JsonImprintPreviewAssets preview = new JsonImprintPreviewAssets(
                profile.preview().base().toString(),
                profile.preview().icon().toString()
        );

        int priority = profile.priority();

        return new JsonProfile(JsonProfile.CURRENT_SCHEMA, profile.layers, supportedBlocks, surface, sets, resolution, preview, priority);
    }

    public static ImprintProfile parseFromRawToDomain(Identifier id, JsonProfile jp) {
        Objects.requireNonNull(jp, "json_profile");

        var layers = parseLayer(jp);

        ImprintTextures textureSets = parseImprintTextures(jp);

        Set<SurfaceBlock> blocks = parseSupportedBlocks(jp);

        SurfaceSettings surface = parseSurfaceSettings(jp);

        ImprintResolution resolution = parseResolution(jp);

        ImprintPreviewAssets preview = parseImprintPreviewAssets(jp, textureSets);

        int priority = jp.priority();

        return new ImprintProfile(id, layers, blocks, surface, textureSets, resolution, preview, priority);
    }

    private static Set<SurfaceBlock> parseSupportedBlocks(JsonProfile jp) {
        if (jp.supportedBlocks() == null) return new HashSet<>();
        return jp.supportedBlocks().stream()
                .map(Identifier::parse)
                .map(SurfaceBlock::of)
                .collect(Collectors.toSet());
    }

    private static List<ImprintLayer> parseLayer(JsonProfile jp) {
        Objects.requireNonNull(jp.layers(), "layers");
        return jp.layers().stream()
                .map(jl -> new ImprintLayer(jl.value(), jl.enable(), jl.expand(), jl.innerJitter(), jl.outerJitter(), jl.erosion()))
                .toList();
    }


    private static ImprintTextures parseImprintTextures(JsonProfile jp) {
        Objects.requireNonNull(jp.textureSets(), "texture_sets");
        Objects.requireNonNull(jp.textureSets().selected(), "texture_sets.selected");
        Objects.requireNonNull(jp.textureSets().initLayer(), "texture_sets.init_layer");
        Objects.requireNonNull(jp.textureSets().texturesByValue(), "texture_sets.textures_by_value");
        return ImprintTextures.fromSets(
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

    }


    private static ImprintPreviewAssets parseImprintPreviewAssets(JsonProfile jp, @Nonnull ImprintTextures textureSets) {
        JsonImprintPreviewAssets raw = jp.preview();
        Identifier icon, base;

        if (raw == null) {
            icon = textureSets.zeroLayer();
            base = textureSets.zeroLayer();
        }
        else {
            if (raw.base() == null) base = textureSets.zeroLayer();
            else base = Identifier.parse(raw.base());
            if (raw.icon() == null) icon = textureSets.zeroLayer();
            else icon = Identifier.parse(raw.icon());
        }
        return new ImprintPreviewAssets(base, icon);
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
        return raw != null && raw.mapSize() != null
                ? new ImprintResolution(raw.mapSize())
                : ImprintResolution.DEFAULT;
    }


}
