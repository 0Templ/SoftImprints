package com.nine.softimprints.core.contact.model.capture;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class ModelContactMeshVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final ModelContactCaptureSession session;

    private final double[] x = new double[4];
    private final double[] y = new double[4];
    private final double[] z = new double[4];

    private int vertexCount;

    public ModelContactMeshVertexConsumer(VertexConsumer delegate, ModelContactCaptureSession session) {
        this.delegate = delegate;
        this.session = session;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        captureVertex(x, y, z);
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                          float normalX, float normalY, float normalZ) {
        captureVertex(x, y, z);
        this.delegate.addVertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        this.delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float lineWidth) {
        this.delegate.setLineWidth(lineWidth);
        return this;
    }

    private void captureVertex(float x, float y, float z) {
        int index = this.vertexCount++;
        this.x[index] = x;
        this.y[index] = y;
        this.z[index] = z;

        if (this.vertexCount < 4) {
            return;
        }

        this.session.captureMeshQuad(
                this.x[0], this.y[0], this.z[0],
                this.x[1], this.y[1], this.z[1],
                this.x[2], this.y[2], this.z[2],
                this.x[3], this.y[3], this.z[3]
        );
        this.vertexCount = 0;
    }
}
