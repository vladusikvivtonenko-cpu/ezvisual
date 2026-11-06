package com.ezbumhikvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class HudRenderer {
    static final int BG = 0xC0101018, BG2 = 0xA01A1A26, ACCENT = 0xFF8B6CFF, ACCENT2 = 0xFF5CC8FF,
            TEXT = 0xFFF1F1FA, DIM = 0xFF9494AE, GOOD = 0xFF5CFFA0, BAD = 0xFFFF6B6B;

    private static final Map<RegistryEntry<StatusEffect>, Integer> MAX = new HashMap<>();

    public static void render(DrawContext ctx, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (p == null || mc.options.hudHidden) return;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();

        Fx.render(ctx, w, h);
        if (Modules.WATERMARK.on) watermark(ctx, mc, p);
        if (Modules.HUD_INFO.on)  founder(ctx, mc, w);
        if (Modules.ARMOR.on)     armor(ctx, mc, p, h);
        if (Modules.BUFFS.on)     effects(ctx, mc, p, w);
        if (Modules.ITEM_GLOW.on) glow(ctx, p, w, h);
        if (Modules.FT_HELPER.on) cooldowns(ctx, mc, p, tc, h);
    }

    // ---------- watermark ----------
    private static void watermark(DrawContext c, MinecraftClient mc, PlayerEntity p) {
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String name = "ezbumhikvisuals";
        String rest = "  " + ping + " ms  " + mc.getCurrentFps() + " fps";
        int nw = mc.textRenderer.getWidth(name), rw = mc.textRenderer.getWidth(rest);
        int x = 6, y = 6, w = nw + rw + 16, h = 18;
        Gfx.rr(c, x - 1, y - 1, x + w + 1, y + h + 1, 6, 0x40000000);
        Gfx.rr(c, x, y, x + w, y + h, 5, BG);
        c.fillGradient(x + 6, y + h - 2, x + w - 6, y + h - 1, ACCENT, ACCENT2);
        c.drawText(mc.textRenderer, name, x + 8, y + 5, ACCENT, false);
        c.drawText(mc.textRenderer, rest, x + 8 + nw, y + 5, DIM, false);
    }

    private static void founder(DrawContext c, MinecraftClient mc, int w) {
        String s = "Founder: ezbumhikvisuals";
        int tw = mc.textRenderer.getWidth(s);
        int x = w - tw - 16, y = 6;
        Gfx.rr(c, x, y, x + tw + 10, y + 16, 5, BG);
        c.drawText(mc.textRenderer, s, x + 5, y + 4, ACCENT, false);
    }

    // ---------- armor: настоящие иконки брони ----------
    private static void armor(DrawContext c, MinecraftClient mc, PlayerEntity p, int h) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int x = 6, rowH = 22, pw = 92, ph = 4 * rowH + 22;
        int y = h / 2 - ph / 2;
        Gfx.rr(c, x, y, x + pw, y + ph, 6, BG);
        int total = 0, cnt = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack s = p.getEquippedStack(slots[i]);
            int ry = y + 5 + i * rowH;
            Gfx.rr(c, x + 4, ry, x + 22, ry + 18, 4, BG2);
            if (s.isEmpty()) {
                c.fill(x + 11, ry + 8, x + 15, ry + 10, 0xFF3A3A4C);
                c.drawText(mc.textRenderer, "-", x + 30, ry + 5, DIM, false);
                continue;
            }
            c.drawItem(s, x + 5, ry + 1);
            int pct = 100;
            if (s.getMaxDamage() > 0) pct = (s.getMaxDamage() - s.getDamage()) * 100 / s.getMaxDamage();
            total += pct; cnt++;
            int col = pct > 60 ? GOOD : pct > 25 ? 0xFFFFD25C : BAD;
            c.drawText(mc.textRenderer, pct + "%", x + 28, ry + 2, TEXT, false);
            int bw = pw - 34;
            c.fill(x + 28, ry + 13, x + 28 + bw, ry + 15, 0xFF2A2A38);
            c.fill(x + 28, ry + 13, x + 28 + bw * pct / 100, ry + 15, col);
        }
        String avg = "Armor " + (cnt == 0 ? 0 : total / cnt) + "%";
        c.drawText(mc.textRenderer, avg, x + 6, y + ph - 13, ACCENT, false);
    }

    // ---------- effects: иконки эффектов (сила, регенерация и т.д.) ----------
    private static void effects(DrawContext c, MinecraftClient mc, PlayerEntity p, int w) {
        Collection<StatusEffectInstance> list = p.getStatusEffects();
        MAX.keySet().removeIf(k -> list.stream().noneMatch(e -> e.getEffectType().equals(k)));
        int pw = 118, x = w - pw - 6, y = 28;
        for (StatusEffectInstance e : list) {
            RegistryEntry<StatusEffect> type = e.getEffectType();
            boolean inf = e.isInfinite();
            int d = e.getDuration();
            MAX.merge(type, d, Math::max);
            float frac = inf ? 1f : Math.min(1f, d / (float) Math.max(MAX.get(type), 1));
            boolean good = type.value().isBeneficial();
            int col = good ? GOOD : BAD;
            if (!inf && d < 200) col = (System.currentTimeMillis() / 250 % 2 == 0) ? BAD : 0xFFFFD25C;

            Gfx.rr(c, x, y, x + pw, y + 24, 6, BG);
            Gfx.rr(c, x + 3, y + 3, x + 21, y + 21, 4, BG2);
            type.getKey().ifPresent(k -> {
                Identifier id = k.getValue();
                Identifier sprite = Identifier.of(id.getNamespace(), "mob_effect/" + id.getPath());
                c.drawGuiTexture(RenderLayer::getGuiTextured, sprite, x + 3, y + 3, 18, 18);
            });
            String name = type.value().getName().getString();
            if (e.getAmplifier() > 0) name += " " + Gfx.roman(e.getAmplifier() + 1);
            if (mc.textRenderer.getWidth(name) > pw - 62) name = mc.textRenderer.trimToWidth(name, pw - 62);
            int sec = Math.max(d, 0) / 20;
            String time = inf ? "inf" : String.format("%d:%02d", sec / 60, sec % 60);
            c.drawText(mc.textRenderer, name, x + 26, y + 4, TEXT, false);
            c.drawText(mc.textRenderer, time, x + pw - 4 - mc.textRenderer.getWidth(time), y + 4, DIM, false);
            int bw = pw - 32;
            c.fill(x + 26, y + 16, x + 26 + bw, y + 18, 0xFF2A2A38);
            c.fill(x + 26, y + 16, x + 26 + (int) (bw * frac), y + 18, col);
            y += 27;
        }
    }

    // ---------- glow мечей/кирок ----------
    private static void glow(DrawContext c, PlayerEntity p, int w, int h) {
        float hue = Modules.LOW_END.on ? 0.75f : (System.currentTimeMillis() % 3000) / 3000f;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isEmpty() || !(s.isIn(ItemTags.SWORDS) || s.isIn(ItemTags.PICKAXES))) continue;
            int x = w / 2 - 90 + i * 20 + 2, y = h - 19;
            int rgb = Color.HSBtoRGB(hue, 0.6f, 1f) & 0xFFFFFF;
            boolean sel = p.getInventory().selectedSlot == i;
            int col = ((sel ? 0xFF : 0x90) << 24) | rgb;
            c.fill(x - 2, y - 2, x + 18, y - 1, col);
            c.fill(x - 2, y + 17, x + 18, y + 18, col);
            c.fill(x - 2, y - 2, x - 1, y + 18, col);
            c.fill(x + 17, y - 2, x + 18, y + 18, col);
            c.fill(x - 1, y - 1, x + 17, y + 17, ((sel ? 0x45 : 0x28) << 24) | rgb);
        }
    }

    // ---------- FT Helper: кулдауны ----------
    private static void cooldowns(DrawContext c, MinecraftClient mc, PlayerEntity p, RenderTickCounter tc, int h) {
        float td = tc.getTickDelta(false);
        int y = h / 2 + 62;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!(s.isOf(Items.SNOWBALL) || s.isOf(Items.CROSSBOW) || s.isOf(Items.ENDER_PEARL)
                    || s.isOf(Items.SPLASH_POTION) || s.isOf(Items.GOLDEN_APPLE))) continue;
            float cd = p.getItemCooldownManager().getCooldownProgress(s, td);
            if (cd <= 0) continue;
            Gfx.rr(c, 6, y, 98, y + 22, 6, BG);
            c.drawItem(s, 9, y + 3);
            c.drawText(mc.textRenderer, (int) (cd * 100) + "%", 30, y + 3, TEXT, false);
            c.fill(30, y + 15, 92, y + 17, 0xFF2A2A38);
            c.fill(30, y + 15, 30 + (int) (62 * (1 - cd)), y + 17, ACCENT);
            y += 25;
        }
    }
}
