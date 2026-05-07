package com.nine.softimprints.model.render;

/**
 * Strategy that emits imprint geometry into a NeoForge parts list. Mirrors the fabric-side
 * interface; one instance per surface mode (OVERLAY, TOP).
 */
public interface ImprintSurfaceRenderer {

    void emit(ImprintRenderContext context);

}
