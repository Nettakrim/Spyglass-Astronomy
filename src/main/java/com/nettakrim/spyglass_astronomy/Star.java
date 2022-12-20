package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.render.BufferBuilder;

public class Star {
    public Star() {

    }

    public void render(BufferBuilder buffer) {
        double d = random.nextFloat() * 2.0f - 1.0f;
        double e = random.nextFloat() * 2.0f - 1.0f;
        double f = random.nextFloat() * 2.0f - 1.0f;
        double g = 0.15f + random.nextFloat() * 0.1f;
        double h = d * d + e * e + f * f;
        if (!(h < 1.0) || !(h > 0.01)) continue;
        h = 1.0 / Math.sqrt(h);
        double j = (d *= h) * 100.0;
        double k = (e *= h) * 100.0;
        double l = (f *= h) * 100.0;
        double m = Math.atan2(d, f);
        double n = Math.sin(m);
        double o = Math.cos(m);
        double p = Math.atan2(Math.sqrt(d * d + f * f), e);
        double q = Math.sin(p);
        double r = Math.cos(p);
        double s = random.nextDouble() * Math.PI * 2.0;
        double t = Math.sin(s);
        double u = Math.cos(s);
        for (int v = 0; v < 4; ++v) {
            double ab;
            double w = 0.0;
            double x = (double)((v & 2) - 1) * g;
            double y = (double)((v + 1 & 2) - 1) * g;
            double z = 0.0;
            double aa = x * u - y * t;
            double ac = ab = y * u + x * t;
            double ad = aa * q + 0.0 * r;
            double ae = 0.0 * q - aa * r;
            double af = ae * n - ac * o;
            double ag = ad;
            double ah = ac * n + ae * o;
            buffer.vertex(j + af, k + ag, l + ah).next();
        }
    }
}
