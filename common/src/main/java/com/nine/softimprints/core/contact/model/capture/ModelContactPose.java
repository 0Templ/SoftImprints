package com.nine.softimprints.core.contact.model.capture;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class ModelContactPose {
    private ModelContactPose() {}

    public static Matrix4f relative(Matrix4fc entityOrigin, Matrix4fc submittedModel) {
        Matrix4f result = new Matrix4f(entityOrigin).invert().mul(submittedModel);
        if (!result.isFinite()) throw new IllegalArgumentException("Invalid model capture transform");
        return result;
    }
}
