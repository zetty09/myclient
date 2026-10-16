package com.myclient.modules;

import com.myclient.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModuleManager {
    public static final List<Module> ALL = new ArrayList<>();
    private static final Set<Module> HELD = new HashSet<>();

    public static Module ISLAND, MUSIC, NAMETAG, SPRINT, FULLBRIGHT;
    public static InfoModule INFO;

    private ModuleManager() {}

    private static <T extends Module> T add(T m) {
        ALL.add(m);
        return m;
    }

    /** Wywolac po Config.load(). */
    public static void init() {
        // visuale
        ISLAND = add(new Module("Dynamic Island", "Pill at the top with time, signal and notifications", true, Module.Category.VISUAL) {});
        INFO = add(new InfoModule());
        MUSIC = add(new Module("Music Player", "Music card on the HUD", false, Module.Category.VISUAL) {});
        NAMETAG = add(new Module("Client Tag", "Your nick with the client logo in third person (F5)", true, Module.Category.VISUAL) {});

        // moduly
        SPRINT = add(new Module("Sprint", "Sprints automatically while moving forward", false) {
            @Override
            public void onTick(MinecraftClient c) {
                ClientPlayerEntity p = c.player;
                if (p == null) return;
                if (c.options.forwardKey.isPressed() && !p.isSneaking() && !p.isUsingItem()
                        && !p.horizontalCollision && p.getHungerManager().getFoodLevel() > 6) {
                    p.setSprinting(true);
                }
            }
        });
        FULLBRIGHT = add(new Module("Fullbright", "Full brightness (client-side night vision)", false) {
            @Override
            public void onTick(MinecraftClient c) {
                if (c.player != null && !c.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    c.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, false, false));
                }
            }

            @Override
            public void onDisable() {
                MinecraftClient c = MinecraftClient.getInstance();
                if (c.player != null) c.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        });

        for (Module m : ALL) {
            Config.ModState st = Config.data.modules.get(m.name);
            if (st != null) {
                m.key = st.key;
                if (st.settings != null) {
                    for (Setting s : m.settings) {
                        Float v = st.settings.get(s.name);
                        if (v != null) s.fromFloat(v);
                    }
                }
                if (st.enabled) m.setEnabled(true, false);
            } else if (m.defaultOn) {
                m.setEnabled(true, false);
            }
        }
    }

    public static void save() {
        for (Module m : ALL) {
            Config.ModState st = new Config.ModState();
            st.enabled = m.enabled;
            st.key = m.key;
            for (Setting s : m.settings) st.settings.put(s.name, s.toFloat());
            Config.data.modules.put(m.name, st);
        }
        Config.save();
    }

    public static List<Module> of(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == cat) out.add(m);
        return out;
    }

    /** Co tick: logika modulow + bindy (tylko gdy nie ma otwartego ekranu). */
    public static void tick(MinecraftClient c) {
        for (Module m : ALL) {
            if (m.enabled) m.onTick(c);
        }
        if (c.currentScreen != null || c.getWindow() == null) {
            HELD.clear();
            return;
        }
        long handle = c.getWindow().getHandle();
        for (Module m : ALL) {
            if (m.key <= 0) continue;
            boolean down = InputUtil.isKeyPressed(handle, m.key);
            boolean was = HELD.contains(m);
            if (down && !was) {
                m.toggle();
                save();
            }
            if (down) HELD.add(m); else HELD.remove(m);
        }
    }
}
