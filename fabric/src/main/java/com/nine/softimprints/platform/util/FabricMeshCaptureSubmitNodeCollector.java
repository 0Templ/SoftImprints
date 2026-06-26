package com.nine.softimprints.platform.util;

import com.nine.softimprints.core.contact.model.render.MeshCaptureSubmitNodeCollector;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class FabricMeshCaptureSubmitNodeCollector extends MeshCaptureSubmitNodeCollector {

    public FabricMeshCaptureSubmitNodeCollector(
            Entity entity,
            Vec3 cameraPos
    ) {
        super(entity, cameraPos);
    }

    @Override
    public <T extends SubmitNode> void submitCustom(
            SubmitRenderPhase<T> phase,
            T node
    ) {


    }


}
