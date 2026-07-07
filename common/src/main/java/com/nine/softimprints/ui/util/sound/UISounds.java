package com.nine.softimprints.ui.util.sound;

import com.nine.softimprints.profile.ImprintProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;

public final class UISounds {

    private static final Tone CHIP_PICKUP = new Tone(SoundEvents.ITEM_PICKUP, 0.10F, 0.85F, 0.06F);
    private static final Tone CHIP_PLACE = new Tone(SoundEvents.ITEM_PICKUP, 0.18F, 0.70F, 0.15F);
    private static final Tone CHIP_CANCEL = new Tone(SoundEvents.ITEM_PICKUP, 0.18F, 0.63F, 0.25F);

    private static final Tone SLOT_TICK = new Tone(SoundEvents.NOTE_BLOCK_HAT.value(), 0.12F, 1.90F, 0.10F);
    private static final Tone RAIL_TICK = new Tone(SoundEvents.NOTE_BLOCK_HAT.value(), 0.15F, 1.45F, 0.08F);

    private UISounds() {
    }

    public static void chipPickup() {
        CHIP_PICKUP.play();
    }

    public static void chipPlace() {
        CHIP_PLACE.play();
    }

    public static void chipPlace(@Nullable ImprintProfile profile) {
        SoundEvent event = profile == null ? null : resolve(profile.preview().landingSound());
        (event == null ? CHIP_PLACE : CHIP_PLACE.withEvent(event)).play();
    }

    @Nullable
    private static SoundEvent resolve(@Nullable Identifier id) {
        if (id == null) return null;
        return BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(null);
    }

    public static void chipCancel() {
        CHIP_CANCEL.play();
    }

    public static void slotTick() {
        SLOT_TICK.play();
    }

    public static void railTick() {
        RAIL_TICK.play();
    }

    private record Tone(
            SoundEvent event,
            float volume,
            float pitch,
            float jitter
    ) {

        private Tone withEvent(SoundEvent replacement) {
            return new Tone(replacement, volume, pitch, jitter);
        }

        private void play() {
            float pitched = pitch + (float) (Math.random() - 0.5D) * 2.0F * jitter;
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(event, pitched, volume));
        }
    }

}
