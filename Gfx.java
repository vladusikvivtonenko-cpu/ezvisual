package com.ezbumhikvisuals;

import net.minecraft.client.gui.DrawContext;

/** Мелкие помощники отрисовки (скруглённые прямоугольники, цвета). Свой код. */
public final class Gfx {
    public static void rr(DrawContext c, int x1, int y1, int x2, int y2, int r, int color) {
        r = Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        if (r <= 0) { c.fill(x1, y1, x2, y2, color); return; }
        c.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int d = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            c.fill(x1 + d, y1 + i, x2 - d, y1 + i + 1, color);
            c.fill(x1 + d, y2 - 1 - i, x2 - d, y2 - i, color);
        }
    }

    public static int lerp(int a, int b, float t) {
        int aa = (a >>> 24), ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = (b >>> 24), br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
             | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    public static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 0 && n < r.length ? r[n] : String.valueOf(n);
    }

    private Gfx() {}
}
