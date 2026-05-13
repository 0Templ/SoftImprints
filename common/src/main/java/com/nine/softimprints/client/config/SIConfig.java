package com.nine.softimprints.client.config;

import com.nine.softimprints.client.config.option.ConfigComment;
import com.nine.softimprints.client.config.option.ConfigRange;
import com.nine.softimprints.client.config.option.ConfigSection;
import com.nine.softimprints.client.config.option.ConfigSide;

import java.util.ArrayList;
import java.util.List;

public class SIConfig {

    public static final class General {

        public static final ConfigValue<Boolean> ENABLE_IMPRINTS =
                ConfigImpl.register(
                        "enable_imprints",
                        true,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Master switch for all imprint capture.")
                                .line("When disabled, imprint generation and rendering stop completely.")
                );

        public static final ConfigValue<Double> IMPRINT_STEP_DISTANCE =
                ConfigImpl.register(
                        "imprint_step_distance",
                        0.15D,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(0.01D, 1.00D),
                        ConfigComment.of("Minimum movement, in blocks, before placing the next imprint stamp.")
                                .line("Larger value = sparser trail")
                );

        public static final ConfigValue<Boolean> IMPRINT_MODEL_CONTACT =
                ConfigImpl.register(
                        "imprint_model_contact",
                        true,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Use render-model snapshots to place stamps from the visible entity shape.")
                                .line("When disabled, bounding-box contact is used instead.")
                );

        public static final ConfigValue<ModelContactOffscreenPolicy> MODEL_CONTACT_OFFSCREEN_POLICY =
                ConfigImpl.register(
                        "model_contact_offscreen_policy",
                        ModelContactOffscreenPolicy.ON_DEMAND,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("How model-contact should handle entities missing a usable render snapshot.")
                                .line("DISABLED = never force offscreen captures.")
                                .line("ON_DEMAND = request a forced capture when a usable model snapshot is missing.")
                );

        public static final ConfigValue<ModelContactFallbackPolicy> MODEL_CONTACT_FALLBACK_POLICY =
                ConfigImpl.register(
                        "model_contact_fallback_policy",
                        ModelContactFallbackPolicy.BOUNDING_BOX,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("What to do when model-contact cannot produce a usable contact shape.")
                                .line("SKIP = skip this entity's imprint tick.")
                                .line("BOUNDING_BOX = use the entity bounding box for this stamp.")
                );


        public static final ConfigValue<Boolean> IMPRINT_ACCUMULATE_SNAPSHOTS =
                ConfigImpl.register(
                        "imprint_accumulate_snapshots",
                        true,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Combine render-model snapshots captured during the same game tick.")
                                .line("This can preserve fast limb motion better than using only the latest pose.")
                );

        public static final ConfigValue<Double> IMPRINT_CONTACT_BAND_HEIGHT =
                ConfigImpl.register(
                        "imprint_contact_band_height",
                        0.12D,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(0.00D, 0.50D),
                        ConfigComment.of("Vertical tolerance above the surface for model-contact sampling.")
                                .line("0.0 keeps the band razor-thin; larger values are more forgiving.")
                );

        public static final ConfigValue<Double> IMPRINT_ROTATION_STEP_DEGREES =
                ConfigImpl.register(
                        "imprint_rotation_step_degrees",
                        10.0D,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(0.0D, 360D),
                        ConfigComment.of("Minimum body rotation before placing a new model-contact stamp.")
                                .line("0.0 disables rotation-triggered updates.")
                );

    }

    public static final class Performance {

        public static final ConfigValue<Integer> IMPRINT_CHUNK_REBUILD_TICK_RATE =
                ConfigImpl.register(
                        "imprint_chunk_rebuild_tick_rate",
                        1,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(1, 200),
                        ConfigComment.of("How often dirty chunks are queued for visual rebuilds after imprint changes.")
                                .line("1 = every tick")
                );

        public static final ConfigValue<Integer> IMPRINT_WRITE_CHECK_TICK_RATE =
                ConfigImpl.register(
                        "imprint_write_check_tick_rate",
                        1,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(1, 200),
                        ConfigComment.of("How often the client flushes pending imprint writes into the cache.")
                                .line("1 = every tick, 2 = every 2 ticks, etc.")
                );

        public static final ConfigValue<Boolean> CLEAR_IMPRINTS_ON_CHUNK_UNLOAD =
                ConfigImpl.register(
                        "clear_imprints_on_chunk_unload",
                        false,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Drop cached imprint maps when a chunk unloads.")
                );

        public static final ConfigValue<Integer> MAX_SECTIONS_REBUILD_PER_ITERATION =
                ConfigImpl.register(
                        "max_sections_rebuild_per_iteration",
                        6,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(1, 21),
                        ConfigComment.of("Maximum dirty render sections processed per rebuild iteration.")
                                .line("The maximum slider value means unlimited.")
                );

        public static final ConfigValue<Integer> MAX_TRACK_ENTITIES =
                ConfigImpl.register(
                        "max_track_entities",
                        101,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(1, 101),
                        ConfigComment.of("Maximum entities around the observer checked by the imprint tracker each tick.")
                                .line("The maximum slider value means unlimited.")
                );

        public static final ConfigValue<Integer> ENTITIES_TRACK_RANGE =
                ConfigImpl.register(
                        "entities_track_range",
                        257,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(0, 257),
                        ConfigComment.of("Maximum distance from the observer for entity imprint tracking.")
                                .line("The maximum slider value means unlimited.")
                );


        public static final ConfigValue<Integer> MAX_OFFSCREEN_CAPTURES_PER_FRAME =
                ConfigImpl.register(
                        "max_offscreen_captures_per_frame",
                        24,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(0, 65),
                        ConfigComment.of("Maximum model-contact offscreen entity captures per render frame.")
                                .line("0 disables forced offscreen captures.")
                );


        public static final ConfigValue<Integer> MAX_CACHED_IMPRINT_BLOCKS =
                ConfigImpl.register(
                        "max_cached_imprint_blocks",
                        4096,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(128, 4096),
                        ConfigComment.of("Maximum imprint block maps kept in the client cache before old entries are discarded.")
                                .line("Higher values preserve more distant imprints but use more memory.")
                );


        public static final ConfigValue<Integer> IMPRINT_SNAPSHOT_INTERVAL =
                ConfigImpl.register(
                        "imprint_snapshot_interval",
                        12,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        new ConfigRange<>(1, 20),
                        ConfigComment.of("Capture render-model snapshots every N render frames while target entities are visible.")
                                .line("1 = every frame")
                );
    }

    public static final class Targets {

        public static final ConfigValue<List<String>> IMPRINT_TARGET_BLACKLIST =
                ConfigImpl.register(
                        "imprint_target_blacklist",
                        List.of(),
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Entity type ids that should never leave imprints in BLACKLIST mode.")
                                .line("Example: [\"minecraft:enderman\"]")
                );

        public static final ConfigValue<List<String>> IMPRINT_TARGET_WHITELIST =
                ConfigImpl.register(
                        "imprint_target_whitelist",
                        new ArrayList<>(),
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Entity type ids allowed to leave imprints in WHITELIST mode.")
                                .line("Example: [\"minecraft:boat\"]")
                );

        public static final ConfigValue<EntityTargetFilterMode> IMPRINT_TARGET_FILTER_MODE =
                ConfigImpl.register(
                        "imprint_target_filter_mode",
                        EntityTargetFilterMode.BLACKLIST,
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Entity target filter mode for imprint tracking.")
                                .line("BLACKLIST = all valid entities except ids in imprint_target_blacklist.")
                                .line("WHITELIST = only ids in imprint_target_whitelist.")
                );
    }

    public static final class Plugins {

        public static final ConfigValue<List<String>> DISABLED_PLUGINS =
                ConfigImpl.register(
                        "disabled_plugins",
                        new ArrayList<>(),
                        ConfigSection.GENERAL,
                        ConfigSide.CLIENT,
                        ConfigComment.of("Soft Imprints plugin ids disabled by the config screen.")
                                .line("Resolvers from disabled plugins are ignored while the plugin remains disabled.")
                );

    }

    public static void init() {
    }
}
