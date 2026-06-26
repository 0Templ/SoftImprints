package com.nine.softimprints.platform;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.core.contact.model.render.MeshCaptureSubmitNodeCollector;
import com.nine.softimprints.platform.util.FabricMeshCaptureSubmitNodeCollector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;

public class FabricPlatformSpecificHelper implements IPlatformSpecificHelper {


    @Override
    public MeshCaptureSubmitNodeCollector nodeCollector(
            Entity entity,
            Vec3 cameraPos
    ) {
        return new FabricMeshCaptureSubmitNodeCollector(entity, cameraPos);
    }
}
