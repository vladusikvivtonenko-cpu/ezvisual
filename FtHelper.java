package com.ezbumhikvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Показывает радиус на земле вокруг летящих снежков / зелий / стрел арбалета.
 * ВАЖНО: радиусы ниже — примерные. Подставь реальные значения предметов FunTime.
 */
public final class FtHelper {
    public static double SNOWBALL_FREEZE_R = 5.0;   // заморозка / снежок
    public static double POTION_R          = 4.0;   // зелье
    public static double CROSSBOW_FREEZE_R = 3.0;   // заморозка арбалета
    public static double DISORIENT_R       = 7.0;   // пласт дезориентации (если это снаряд)

    public static void renderWorld(WorldRenderContext ctx) {
        if (!Modules.FT_HELPER.on || ctx.world() == null || ctx.consumers() == null || ctx.matrixStack() == null) return;
        float td = ctx.tickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        MatrixStack.Entry e = ctx.matrixStack().peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
        int seg = Modules.LOW_END.on ? 24 : 48;

        for (Entity en : ctx.world().getEntities()) {
            double r; int color;
            if (en instanceof SnowballEntity)               { r = SNOWBALL_FREEZE_R; color = 0xFF66CCFF; }
            else if (en instanceof PotionEntity)            { r = POTION_R;          color = 0xFFFF66FF; }
            else if (en instanceof PersistentProjectileEntity) { r = CROSSBOW_FREEZE_R; color = 0xFF99EEFF; }
            else continue;
            Vec3d p = en.getLerpedPos(td).subtract(cam);
            circle(vc, e, p.x, p.y, p.z, r, seg, color);
        }
    }

    private static void circle(VertexConsumer vc, MatrixStack.Entry e, double cx, double cy, double cz, double r, int seg, int color) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float x0 = (float) (cx + Math.cos(a0) * r), z0 = (float) (cz + Math.sin(a0) * r);
            float x1 = (float) (cx + Math.cos(a1) * r), z1 = (float) (cz + Math.sin(a1) * r);
            float nx = x1 - x0, nz = z1 - z0;
            float len = (float) Math.sqrt(nx * nx + nz * nz);
            nx /= len; nz /= len;
            vc.vertex(e, x0, (float) cy, z0).color(color).normal(e, nx, 0, nz);
            vc.vertex(e, x1, (float) cy, z1).color(color).normal(e, nx, 0, nz);
        }
    }
}
