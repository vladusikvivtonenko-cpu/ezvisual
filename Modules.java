package com.ezbumhikvisuals;

import java.util.List;

public final class Modules {
    public enum Cat { VISUALS("Visuals"), HUD("HUD"), UTILITIES("Utilities");
        public final String title; Cat(String t) { title = t; } }

    public static final class M {
        public final String name; public final Cat cat;
        public boolean on; public float anim;   // anim 0..1 для плавного переключателя
        M(String name, Cat cat, boolean on) { this.name = name; this.cat = cat; this.on = on; this.anim = on ? 1 : 0; }
    }

    // Visuals
    public static final M NO_FIRE   = new M("No Fire Overlay", Cat.VISUALS, true);
    public static final M NO_SHAKE  = new M("No Screen Shake", Cat.VISUALS, true);
    public static final M ITEM_GLOW = new M("Sword/Pickaxe Glow", Cat.VISUALS, true);
    public static final M RAIN      = new M("Rain FX", Cat.VISUALS, false);
    public static final M SNOW      = new M("Snow FX", Cat.VISUALS, false);
    public static final M SPACE     = new M("Space FX", Cat.VISUALS, false);
    // HUD
    public static final M WATERMARK = new M("Watermark", Cat.HUD, true);
    public static final M HUD_INFO  = new M("Founder Tag", Cat.HUD, true);
    public static final M ARMOR     = new M("Armor HUD", Cat.HUD, true);
    public static final M BUFFS     = new M("Effects HUD", Cat.HUD, true);
    // Utilities
    public static final M FT_HELPER = new M("FT Helper", Cat.UTILITIES, true);
    public static final M LOW_END   = new M("Low-End Mode", Cat.UTILITIES, false);

    public static final List<M> ALL = List.of(NO_FIRE, NO_SHAKE, ITEM_GLOW, RAIN, SNOW, SPACE,
            WATERMARK, HUD_INFO, ARMOR, BUFFS, FT_HELPER, LOW_END);

    private Modules() {}
}
