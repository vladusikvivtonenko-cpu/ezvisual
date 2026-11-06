package com.ezbumhikvisuals;

import net.minecraft.client.gui.DrawContext;

/** Лёгкие 2D-эффекты (дождь / снег / космос) — без GLSL, почти не грузят FPS. */
public final class Fx {
    private static final long SEED = 1337L;

    public static void render(DrawContext ctx, int w, int h) {
        int n = Modules.LOW_END.on ? 40 : 140;
        double t = System.nanoTime() / 1.0e9;
        if (Modules.RAIN.on)  rain(ctx, w, h, n, t);
        if (Modules.SNOW.on)  snow(ctx, w, h, n, t);
        if (Modules.SPACE.on) space(ctx, w, h, n, t);
    }

    private static double rnd(int i, int k) {
        long x = (i * 73856093L) ^ (k * 19349663L) ^ SEED;
        x = (x ^ (x >>> 13)) * 1274126177L;
        return ((x ^ (x >>> 16)) & 0xFFFFFF) / (double) 0x1000000;
    }

    private static void rain(DrawContext c, int w, int h, int n, double t) {
        for (int i = 0; i < n; i++) {
            double sp = 400 + rnd(i, 1) * 300;
            int x = (int) (rnd(i, 2) * w);
            int y = (int) ((t * sp + rnd(i, 3) * h) % h);
            c.fill(x, y, x + 1, y + 9, 0x663C8CFF);
        }
    }

    private static void snow(DrawContext c, int w, int h, int n, double t) {
        for (int i = 0; i < n; i++) {
            double sp = 30 + rnd(i, 1) * 50;
            int x = (int) ((rnd(i, 2) * w + Math.sin(t + i) * 15 + w) % w);
            int y = (int) ((t * sp + rnd(i, 3) * h) % h);
            int s = rnd(i, 4) > 0.7 ? 3 : 2;
            c.fill(x, y, x + s, y + s, 0xAAFFFFFF);
        }
    }

    private static void space(DrawContext c, int w, int h, int n, double t) {
        c.fill(0, 0, w, h, 0x30050010);
        for (int i = 0; i < n; i++) {
            int x = (int) (rnd(i, 2) * w), y = (int) (rnd(i, 3) * h);
            double tw = 0.5 + 0.5 * Math.sin(t * (1 + rnd(i, 4) * 3) + i);
            int a = (int) (80 + 150 * tw);
            c.fill(x, y, x + 1, y + 1, (a << 24) | 0xCFD8FF);
        }
    }
}
