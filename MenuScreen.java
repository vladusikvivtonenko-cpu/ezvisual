package com.ezbumhikvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen extends Screen {
    private static final Identifier GIRL = Identifier.of("ezbumhikvisuals", "textures/gui/menu_girl.png");
    private static final int IMG_W = 206, IMG_H = 300;      // размер menu_girl.png (прозрачный фон)
    private static final int PW = 400, PH = 250, SIDE = 44, GIRL_OUT = 176;

    private static Modules.Cat tab = Modules.Cat.VISUALS;
    private String query = "";

    public MenuScreen() { super(Text.literal("EzbumhikVisuals")); }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fillGradient(0, 0, width, height, 0xA0080810, 0xC0100820); }
    @Override public boolean shouldPause() { return false; }

    private boolean girlShown() { return width >= PW + GIRL_OUT + 20; }
    private int px() { return ((girlShown() ? width - (PW + GIRL_OUT) : width - PW)) / 2; }
    private int py() { return (height - PH) / 2; }

    private List<Modules.M> visible() {
        List<Modules.M> out = new ArrayList<>();
        String q = query.toLowerCase();
        for (Modules.M m : Modules.ALL) {
            if (q.isEmpty() ? m.cat == tab : m.name.toLowerCase().contains(q)) out.add(m);
        }
        return out;
    }

    private int cardX(int i) { return px() + SIDE + 12 + (i % 2) * 170; }
    private int cardY(int i) { return py() + 46 + (i / 2) * 30; }

    private static Item tabIcon(Modules.Cat c) {
        return switch (c) { case VISUALS -> Items.NETHER_STAR; case HUD -> Items.COMPASS; case UTILITIES -> Items.ENDER_EYE; };
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        int x = px(), y = py();

        // тень + панель
        Gfx.rr(c, x - 3, y - 3, x + PW + 3, y + PH + 3, 14, 0x40000000);
        Gfx.rr(c, x, y, x + PW, y + PH, 12, 0xF0111119);
        Gfx.rr(c, x + 6, y + 6, x + SIDE - 4, y + PH - 6, 9, 0xFF181824);
        c.fillGradient(x + 14, y + 4, x + PW - 14, y + 5, 0xFF8B6CFF, 0xFF5CC8FF);

        // девушка без фона — выходит за край панели справа
        if (girlShown()) {
            int gx = x + PW - 30, gy = y + PH - IMG_H + 6;
            c.drawTexture(RenderLayer::getGuiTextured, GIRL, gx, gy, 0, 0, IMG_W, IMG_H, IMG_W, IMG_H, IMG_W, IMG_H);
        }

        // боковая панель — вкладки с предметами-иконками
        Modules.Cat[] cats = Modules.Cat.values();
        for (int i = 0; i < cats.length; i++) {
            int ty = y + 16 + i * 34;
            boolean sel = cats[i] == tab && query.isEmpty();
            Gfx.rr(c, x + 9, ty, x + SIDE - 7, ty + 28, 8, sel ? 0xFF6C4DFF : 0xFF22222F);
            c.drawItem(tabIcon(cats[i]).getDefaultStack(), x + 13, ty + 6);
        }

        // шапка: Visuals / HUD / Utilities
        int hx = x + SIDE + 12;
        for (Modules.Cat cat : cats) {
            String t = cat.title;
            c.drawText(textRenderer, t, hx, y + 14, cat == tab && query.isEmpty() ? 0xFFF1F1FA : 0xFF5E5E78, false);
            hx += textRenderer.getWidth(t);
            if (cat != cats[cats.length - 1]) { c.drawText(textRenderer, " / ", hx, y + 14, 0xFF3C3C52, false); hx += textRenderer.getWidth(" / "); }
        }

        // поиск
        int sx = x + PW - 150, sy = y + 10;
        Gfx.rr(c, sx, sy, sx + 120, sy + 16, 6, 0xFF1C1C28);
        boolean blink = System.currentTimeMillis() / 500 % 2 == 0;
        String shown = query.isEmpty() ? "Search..." : query + (blink ? "_" : "");
        c.drawText(textRenderer, textRenderer.trimToWidth(shown, 108), sx + 6, sy + 4, query.isEmpty() ? 0xFF5E5E78 : 0xFFF1F1FA, false);

        // карточки модулей
        List<Modules.M> list = visible();
        for (int i = 0; i < list.size(); i++) {
            Modules.M m = list.get(i);
            m.anim += ((m.on ? 1f : 0f) - m.anim) * 0.3f;
            int bx = cardX(i), by = cardY(i);
            boolean hover = mx >= bx && mx <= bx + 162 && my >= by && my <= by + 26;
            Gfx.rr(c, bx, by, bx + 162, by + 26, 7, hover ? 0xFF222232 : 0xFF1A1A26);
            c.drawText(textRenderer, m.name, bx + 9, by + 9, Gfx.lerp(0xFF6C6C86, 0xFFF1F1FA, m.anim), false);
            int tx = bx + 162 - 34, ty = by + 8;
            Gfx.rr(c, tx, ty, tx + 24, ty + 11, 5, Gfx.lerp(0xFF2C2C3C, 0xFF7A5CFF, m.anim));
            int kx = tx + 1 + (int) (13 * m.anim);
            Gfx.rr(c, kx, ty + 1, kx + 9, ty + 10, 4, 0xFFFFFFFF);
        }
        if (list.isEmpty()) c.drawText(textRenderer, "Nothing found", x + SIDE + 12, y + 50, 0xFF5E5E78, false);

        c.drawText(textRenderer, "Founder: ezbumhikvisuals", x + SIDE + 12, y + PH - 14, 0xFF8B6CFF, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = px(), y = py();
        Modules.Cat[] cats = Modules.Cat.values();
        for (int i = 0; i < cats.length; i++) {
            int ty = y + 16 + i * 34;
            if (mx >= x + 9 && mx <= x + SIDE - 7 && my >= ty && my <= ty + 28) { tab = cats[i]; query = ""; return true; }
        }
        List<Modules.M> list = visible();
        for (int i = 0; i < list.size(); i++) {
            int bx = cardX(i), by = cardY(i);
            if (mx >= bx && mx <= bx + 162 && my >= by && my <= by + 26) { list.get(i).on = !list.get(i).on; return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (query.length() < 20 && chr >= 32) { query += chr; return true; }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) { query = query.substring(0, query.length() - 1); return true; }
        if (EzbumhikClient.menuKey.matchesKey(key, scan)) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }
}
