package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;

public class StarRenderingManager {
    public VertexBuffer starsBuffer = new VertexBuffer();
    private BufferBuilder starBufferBuilder = Tessellator.getInstance().getBuffer();

    public void UpdateStars(int ticks) {
        starBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Star star : SpyglassAstronomyClient.stars) {
            star.Update(ticks);
            star.SetVertices(starBufferBuilder);
        }

        starsBuffer.bind();
        starsBuffer.upload(starBufferBuilder.end());
    }

}
