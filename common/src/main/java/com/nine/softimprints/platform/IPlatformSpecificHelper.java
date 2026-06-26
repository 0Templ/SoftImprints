package com.nine.softimprints.platform;

import com.nine.softimprints.core.contact.model.render.MeshCaptureSubmitNodeCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;

public interface IPlatformSpecificHelper {

    //


    MeshCaptureSubmitNodeCollector nodeCollector(
            Entity entity,
            Vec3 cameraPos
    );


}
