package com.myclient.visuals;

import com.myclient.modules.ArmorModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Widget: pancerz + trzymany przedmiot z wytrzymaloscia. */
public class ArmorHud extends HudElement {
    private static final int GREEN = 0xFF30D158, YELLOW = 0xFFFFD60A, RED = 0xFFFF453A;
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ArmorHud() {
        super("armor", "Armor HUD", 0.99f, 0.50f);
    }

    @Override
    public boolean enabled() {
        return ModuleManager.ARMOR != null && ModuleManager.ARMOR.enabled;
    }

    private static String text(ArmorModule m, ItemStack st) {
        if (st.isDamageable()) {
            int left = st.getMaxDamage() - st.getDamage();
            return switch (m.value.get()) {
                case "Number" -> String.valueOf(left);
                case "Percent" -> Math.round(left * 100f / st.getMaxDamage()) + "%";
                default -> "";
            };
        }
        return st.getCount() > 1 ? "x" + st.getCount() : "";
    }

    private static int color(ItemStack st) {
        if (!st.isDamageable()) return 0xFFFFFFFF;
        float f = (st.getMaxDamage() - st.getDamage()) / (float) st.getMaxDamage();
        return f > 0.5f ? GREEN : f > 0.2f ? YELLOW : RED;
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient c) {
        ArmorModule m = ModuleManager.ARMOR;
        TextRenderer tr = c.textRenderer;

        List<ItemStack> items = new ArrayList<>();
        if (c.player != null) {
            for (EquipmentSlot sl : SLOTS) {
                ItemStack st = c.player.getEquippedStack(sl);
                if (!st.isEmpty()) items.add(st);
            }
            if (m.heldItem.value) {
                ItemStack hand = c.player.getMainHandStack();
                if (!hand.isEmpty()) items.add(hand);
            }
        }

        List<String> texts = new ArrayList<>();
        int textW = 0;
        for (ItemStack st : items) {
            String t = text(m, st);
            texts.add(t);
            textW = Math.max(textW, tr.getWidth(t));
        }

        boolean vertical = m.vertical.value;
        int n = Math.max(1, items.size());
        int cell = 18;
        int rowW = 2 + 16 + (textW > 0 ? 3 + textW : 0) + 4;
        int uw, uh;
        if (vertical) {
            uw = rowW + 2;
            uh = n * cell + 4;
        } else {
            uw = 4 + n * (rowW - 2 + 4) ;
            uh = 18 + (textW > 0 ? 11 : 0) + 4;
        }
        if (items.isEmpty()) {
            uw = 50;
            uh = 18;
        }

        float s = m.scale.value;
        w = Math.round(uw * s);
        h = Math.round(uh * s);
        int x = x(ctx.getScaledWindowWidth()), y = y(ctx.getScaledWindowHeight());

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(x, y, 0);
        mat.scale(s, s, 1f);
        if (m.background.value) {
            int a = Math.round(m.opacity.value / 100f * 255f);
            Gfx.roundRect(ctx, 0, 0, uw, uh, 8, (a << 24) | 0x1C1C1E);
        }
        if (items.isEmpty()) {
            ctx.drawText(tr, "No armor", 6, 5, 0xFF8E8E93, true);
        } else {
            for (int i = 0; i < items.size(); i++) {
                ItemStack st = items.get(i);
                String t = texts.get(i);
                if (vertical) {
                    int iy = 2 + i * cell;
                    ctx.drawItem(st, 3, iy);
                    if (!t.isEmpty()) ctx.drawText(tr, t, 3 + 16 + 3, iy + 5, color(st), true);
                } else {
                    int ix = 3 + i * (rowW - 2 + 4);
                    ctx.drawItem(st, ix, 3);
                    if (!t.isEmpty()) ctx.drawText(tr, t, ix + (16 - tr.getWidth(t)) / 2, 3 + 17, color(st), true);
                }
            }
        }
        mat.pop();
    }
}
