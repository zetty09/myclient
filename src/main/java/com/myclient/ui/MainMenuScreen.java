package com.myclient.ui;

import com.myclient.ClientInfo;
import com.myclient.Config;
import com.myclient.cosmetics.WingType;
import com.myclient.modules.BoolSetting;
import com.myclient.modules.Module;
import com.myclient.modules.ModuleManager;
import com.myclient.modules.NumberSetting;
import com.myclient.modules.Setting;
import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Menu klienta w stylu iOS: sidebar, karty z przelacznikami, prawy klik = ustawienia. */
public class MainMenuScreen extends Screen {
    private static final int PW = 480, PH = 292, SIDEBAR = 122;
    private static final int BLUE = ClientInfo.ACCENT, WHITE = 0xFFFFFFFF, GREY = 0xFF8E8E93;
    private static final int CARD = 0xFF2C2C2E, CARD_HOVER = 0xFF3A3A3C;
    private static final int POPW = 244;
    private static final int CARD_H = 36, CARD_STEP = 42;

    private record Tab(String name, String glyph, int color, String subtitle) {}

    private static final Tab[] TABS = {
            new Tab("Modules", "M", 0xFFFF9F0A, "Gameplay features"),
            new Tab("Visuals", "V", 0xFFBF5AF2, "Widgets shown on your screen"),
            new Tab("Cosmetics", "C", 0xFFFF375F, "Customize your look"),
            new Tab("User Interface", "U", 0xFF0A84FF, "Move and edit your HUD"),
    };

    private int px, py;
    private int tab = 0;
    private float scroll = 0, maxScroll = 0;
    private float openT = 0, contentT = 0, popupT = 0, pillY = -1;
    private float dt = 0.016f;
    private long lastNs = System.nanoTime();
    private final Map<Object, Float> anim = new HashMap<>();

    private Module popup;
    private boolean listening;
    private NumberSetting dragging;

    public MainMenuScreen() {
        super(Text.literal(ClientInfo.NAME));
    }

    @Override
    protected void init() {
        px = (width - PW) / 2;
        py = (height - PH) / 2;
    }

    // ---------------- uklad ----------------

    private int cx() { return px + SIDEBAR + 16; }
    private int cw() { return PW - SIDEBAR - 32; }
    private int top() { return py + 50; }
    private int bottom() { return py + PH - 26; }
    private int tabY(int i) { return i < 3 ? py + 58 + i * 24 : py + 150; }

    private List<Module> list() {
        return ModuleManager.of(tab == 0 ? Module.Category.MODULE : Module.Category.VISUAL);
    }

    private int cardY(int i) { return top() + i * CARD_STEP - (int) scroll; }

    private static String keyName(int key) {
        if (key <= 0) return "None";
        try {
            return InputUtil.fromKeyCode(key, -1).getLocalizedText().getString();
        } catch (Exception e) {
            return "Key " + key;
        }
    }

    private float approach(Object key, float target) {
        float cur = anim.getOrDefault(key, target);
        cur += (target - cur) * (1f - (float) Math.exp(-dt * 16f));
        anim.put(key, cur);
        return cur;
    }

    private void text(DrawContext ctx, String s, int x, int y, int color) {
        Gfx.text(ctx, textRenderer, s, x, y, color, false);
    }

    // ---------------- render ----------------

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);

        long now = System.nanoTime();
        dt = Math.min((now - lastNs) / 1_000_000_000f, 0.05f);
        lastNs = now;
        float k = 1f - (float) Math.exp(-dt * 12f);
        openT += (1f - openT) * k;
        contentT += (1f - contentT) * k;
        popupT += ((popup != null ? 1f : 0f) - popupT) * k;
        float targetPill = tabY(tab);
        if (pillY < 0) pillY = targetPill;
        pillY += (targetPill - pillY) * k;

        var mat = ctx.getMatrices();
        float s = 0.94f + 0.06f * openT;
        float ccx = px + PW / 2f, ccy = py + PH / 2f;
        mat.push();
        mat.translate(ccx, ccy, 0);
        mat.scale(s, s, 1f);
        mat.translate(-ccx, -ccy, 0);

        Gfx.alpha = openT;
        drawFrame(ctx, mouseX, mouseY);

        Gfx.alpha = openT * contentT;
        mat.push();
        mat.translate(0, (1f - contentT) * 8f, 0);
        switch (tab) {
            case 0, 1 -> drawCards(ctx, mouseX, mouseY);
            case 2 -> drawCosmetics(ctx, mouseX, mouseY);
            default -> drawInterface(ctx, mouseX, mouseY);
        }
        mat.pop();

        if (popup != null) drawPopup(ctx, mouseX, mouseY);

        mat.pop();
        Gfx.alpha = 1f;
    }

    private void drawFrame(DrawContext ctx, int mx, int my) {
        Gfx.roundRect(ctx, px, py, PW, PH, 14, 0xF01C1C1E);
        Gfx.roundRect(ctx, px, py, SIDEBAR, PH, 14, 0xF0141416);
        ctx.fill(px + SIDEBAR - 14, py, px + SIDEBAR, py + PH, Gfx.a(0xF0141416));
        ctx.fill(px + SIDEBAR, py + 12, px + SIDEBAR + 1, py + PH - 12, Gfx.a(0xFF2C2C2E));

        // naglowek klienta
        Gfx.text(ctx, textRenderer, ClientInfo.LOGO + " " + ClientInfo.NAME, px + 14, py + 14, WHITE, true);
        text(ctx, "Version " + ClientInfo.VERSION, px + 14, py + 26, GREY);

        text(ctx, "FEATURES", px + 14, py + 46, 0xFF636366);
        text(ctx, "OTHER", px + 14, py + 138, 0xFF636366);

        // animowana "pigulka" zaznaczenia
        Gfx.roundRect(ctx, px + 8, Math.round(pillY), SIDEBAR - 16, 22, 8, BLUE);

        for (int i = 0; i < TABS.length; i++) {
            Tab t = TABS[i];
            int y = tabY(i);
            boolean hover = mx >= px + 8 && mx < px + SIDEBAR - 8 && my >= y && my < y + 22;
            float hv = approach("tab" + i, hover && i != tab ? 1f : 0f);
            if (i != tab) Gfx.roundRect(ctx, px + 8, y, SIDEBAR - 16, 22, 8, Gfx.lerpColor(0x00FFFFFF, 0x1EFFFFFF, hv));
            Gfx.roundRect(ctx, px + 14, y + 4, 14, 14, 4, t.color());
            int gw = textRenderer.getWidth(t.glyph());
            text(ctx, t.glyph(), px + 14 + (14 - gw) / 2, y + 7, WHITE);
            text(ctx, t.name(), px + 34, y + 7, i == tab ? WHITE : 0xFFD1D1D6);
        }

        // uzytkownik
        String user = client != null ? client.getSession().getUsername() : "player";
        Gfx.roundRect(ctx, px + 12, py + PH - 32, 20, 20, 10, BLUE);
        String letter = user.isEmpty() ? "?" : user.substring(0, 1).toUpperCase();
        text(ctx, letter, px + 12 + (20 - textRenderer.getWidth(letter)) / 2, py + PH - 26, WHITE);
        text(ctx, textRenderer.trimToWidth(user, SIDEBAR - 52), px + 38, py + PH - 26, WHITE);

        // tytul zawartosci
        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(cx(), py + 14, 0);
        mat.scale(1.4f, 1.4f, 1f);
        Gfx.text(ctx, textRenderer, TABS[tab].name(), 0, 0, WHITE, true);
        mat.pop();
        text(ctx, TABS[tab].subtitle(), cx(), py + 33, GREY);

        if (tab <= 1) text(ctx, "Click to toggle  -  right-click for settings", cx(), py + PH - 16, 0xFF636366);
    }

    private void drawCards(DrawContext ctx, int mx, int my) {
        List<Module> list = list();
        maxScroll = Math.max(0, list.size() * CARD_STEP - 6 - (bottom() - top()));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        ctx.enableScissor(cx() - 2, top(), cx() + cw() + 2, bottom());
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int y = cardY(i);
            if (y + CARD_H < top() || y > bottom()) continue;
            boolean hover = popup == null && mx >= cx() && mx < cx() + cw()
                    && my >= Math.max(y, top()) && my < Math.min(y + CARD_H, bottom());
            float hv = approach(m.name + "#h", hover ? 1f : 0f);
            Gfx.roundRect(ctx, cx(), y, cw(), CARD_H, 10, Gfx.lerpColor(CARD, CARD_HOVER, hv));
            text(ctx, m.name, cx() + 12, y + 8, WHITE);
            text(ctx, textRenderer.trimToWidth(m.description, cw() - 24 - 40), cx() + 12, y + 21, GREY);
            if (m.key > 0) {
                String kn = keyName(m.key);
                text(ctx, kn, cx() + cw() - 48 - textRenderer.getWidth(kn), y + 14, 0xFF636366);
            }
            Gfx.iosSwitch(ctx, cx() + cw() - 42, y + 10, approach(m, m.enabled ? 1f : 0f));
        }
        ctx.disableScissor();
    }

    private static final int WING_W = 70, WING_H = 92, WING_GAP = 8;

    private int wingX(int i) { return cx() + i * (WING_W + WING_GAP); }
    private int wingY() { return top() + 16; }

    private void drawCosmetics(DrawContext ctx, int mx, int my) {
        text(ctx, "Wings", cx(), top(), GREY);
        WingType[] types = WingType.values();
        for (int i = 0; i < types.length; i++) {
            WingType t = types[i];
            int x = wingX(i), y = wingY();
            boolean selected = Config.wing() == t;
            boolean hover = popup == null && mx >= x && mx < x + WING_W && my >= y && my < y + WING_H;
            float hv = approach("wing" + i, hover ? 1f : 0f);
            Gfx.roundRect(ctx, x, y, WING_W, WING_H, 12, Gfx.lerpColor(CARD, CARD_HOVER, hv));
            if (selected) {
                Gfx.border(ctx, x, y, WING_W, WING_H, BLUE);
                Gfx.border(ctx, x + 1, y + 1, WING_W - 2, WING_H - 2, BLUE);
            }
            if (t == WingType.NONE) {
                text(ctx, "-", x + WING_W / 2 - 2, y + 32, 0xFF636366);
            } else {
                var m = ctx.getMatrices();
                m.push();
                m.translate(x + 11, y + 10, 0);
                m.scale(1.5f, 1.5f, 1f);
                ctx.drawTexture(RenderLayer::getGuiTextured, t.texture, 0, 0, 0f, 0f, 32, 32, 32, 32);
                m.pop();
            }
            int lw = textRenderer.getWidth(t.label);
            text(ctx, t.label, x + (WING_W - lw) / 2, y + WING_H - 15, selected ? WHITE : 0xFFD1D1D6);
        }
        text(ctx, "More cosmetics (headbands, gloves, capes) are coming soon.", cx(), py + PH - 40, 0xFF636366);
    }

    private int openBtnX() { return cx() + cw() - 70; }

    private void drawInterface(DrawContext ctx, int mx, int my) {
        Gfx.roundRect(ctx, cx(), top(), cw(), 56, 12, CARD);
        text(ctx, "HUD Editor", cx() + 14, top() + 12, WHITE);
        text(ctx, "Drag your visuals anywhere on the screen", cx() + 14, top() + 28, GREY);
        boolean hover = popup == null && mx >= openBtnX() && mx < openBtnX() + 56 && my >= top() + 18 && my < top() + 38;
        float hv = approach("openEditor", hover ? 1f : 0f);
        Gfx.roundRect(ctx, openBtnX(), top() + 18, 56, 20, 10, Gfx.lerpColor(BLUE, 0xFF3D9BFF, hv));
        String o = "Open";
        text(ctx, o, openBtnX() + (56 - textRenderer.getWidth(o)) / 2, top() + 24, WHITE);

        text(ctx, "Tip: you can also right-click in chat and choose \"Edit HUD positions\".",
                cx(), top() + 68, 0xFF636366);
    }

    // ---------------- popup z ustawieniami ----------------

    private record PRow(int kind, Setting s, int y, int h) {}

    private int popH() {
        int h = 36 + 26 + 10;
        for (Setting s : popup.settings) h += s instanceof NumberSetting ? 32 : 26;
        return h;
    }

    private int popX() { return px + (PW - POPW) / 2; }
    private int popY() { return py + (PH - popH()) / 2; }

    private List<PRow> popupRows() {
        List<PRow> out = new ArrayList<>();
        int y = popY() + 36;
        out.add(new PRow(0, null, y, 26));
        y += 26;
        for (Setting s : popup.settings) {
            boolean num = s instanceof NumberSetting;
            int h = num ? 32 : 26;
            out.add(new PRow(num ? 2 : 1, s, y, h));
            y += h;
        }
        return out;
    }

    private void drawPopup(DrawContext ctx, int mx, int my) {
        var mat = ctx.getMatrices();
        float pt = Math.min(1f, popupT);
        Gfx.alpha = openT * pt;
        Gfx.roundRect(ctx, px, py, PW, PH, 14, 0x99000000);

        float s = 0.92f + 0.08f * pt;
        float ccx = popX() + POPW / 2f, ccy = popY() + popH() / 2f;
        mat.push();
        mat.translate(ccx, ccy, 0);
        mat.scale(s, s, 1f);
        mat.translate(-ccx, -ccy, 0);

        int x = popX(), y = popY(), w = POPW;
        Gfx.roundRect(ctx, x, y, w, popH(), 14, 0xFF2C2C2E);
        Gfx.text(ctx, textRenderer, popup.name, x + 14, y + 13, WHITE, true);
        String done = "Done";
        text(ctx, done, x + w - 14 - textRenderer.getWidth(done), y + 13, BLUE);
        ctx.fill(x + 12, y + 32, x + w - 12, y + 33, Gfx.a(0xFF3A3A3C));

        for (PRow r : popupRows()) {
            switch (r.kind()) {
                case 0 -> {
                    text(ctx, "Keybind", x + 14, r.y() + 9, WHITE);
                    int bx = x + w - 14 - 84;
                    Gfx.roundRect(ctx, bx, r.y() + 4, 84, 18, 9, listening ? BLUE : 0xFF3A3A3C);
                    String label = listening ? "Press a key..." : keyName(popup.key);
                    label = textRenderer.trimToWidth(label, 78);
                    text(ctx, label, bx + (84 - textRenderer.getWidth(label)) / 2, r.y() + 9, WHITE);
                }
                case 1 -> {
                    BoolSetting b = (BoolSetting) r.s();
                    text(ctx, b.name, x + 14, r.y() + 9, WHITE);
                    Gfx.iosSwitch(ctx, x + w - 14 - 30, r.y() + 5, approach(b, b.value ? 1f : 0f));
                }
                default -> {
                    NumberSetting n = (NumberSetting) r.s();
                    text(ctx, n.name, x + 14, r.y() + 5, WHITE);
                    String v = n.display();
                    text(ctx, v, x + w - 14 - textRenderer.getWidth(v), r.y() + 5, GREY);
                    int x0 = x + 14, x1 = x + w - 14, ty = r.y() + 20;
                    float frac = (n.value - n.min) / (n.max - n.min);
                    Gfx.roundRect(ctx, x0, ty, x1 - x0, 4, 2, 0xFF48484A);
                    int fw = Math.round((x1 - x0) * frac);
                    if (fw > 0) Gfx.roundRect(ctx, x0, ty, Math.max(4, fw), 4, 2, BLUE);
                    Gfx.roundRect(ctx, x0 + fw - 5, ty - 3, 10, 10, 5, WHITE);
                }
            }
        }
        mat.pop();
    }

    private void closePopup() {
        popup = null;
        listening = false;
        dragging = null;
        ModuleManager.save();
    }

    private void setSlider(double mx) {
        if (dragging == null) return;
        int x0 = popX() + 14, x1 = popX() + POPW - 14;
        float frac = (float) Math.max(0, Math.min(1, (mx - x0) / (x1 - x0)));
        dragging.fromFloat(dragging.min + frac * (dragging.max - dragging.min));
    }

    // ---------------- input ----------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (popup != null) {
            int x = popX(), y = popY(), w = POPW, h = popH();
            if (mx < x || mx >= x + w || my < y || my >= y + h) {
                closePopup();
                return true;
            }
            if (button != 0) return true;
            if (my < y + 30 && mx >= x + w - 14 - textRenderer.getWidth("Done") - 6) {
                closePopup();
                return true;
            }
            for (PRow r : popupRows()) {
                if (my < r.y() || my >= r.y() + r.h()) continue;
                if (r.kind() == 0) {
                    if (mx >= x + w - 14 - 84) listening = true;
                } else if (r.kind() == 1) {
                    BoolSetting b = (BoolSetting) r.s();
                    b.value = !b.value;
                    ModuleManager.save();
                } else {
                    dragging = (NumberSetting) r.s();
                    setSlider(mx);
                }
                break;
            }
            return true;
        }

        // sidebar
        if (button == 0) {
            for (int i = 0; i < TABS.length; i++) {
                int y = tabY(i);
                if (mx >= px + 8 && mx < px + SIDEBAR - 8 && my >= y && my < y + 22) {
                    if (tab != i) {
                        tab = i;
                        scroll = 0;
                        contentT = 0f;
                    }
                    return true;
                }
            }
        }

        if (tab <= 1) {
            List<Module> list = list();
            for (int i = 0; i < list.size(); i++) {
                int y = cardY(i);
                if (mx >= cx() && mx < cx() + cw() && my >= Math.max(y, top()) && my < Math.min(y + CARD_H, bottom())) {
                    Module m = list.get(i);
                    if (button == 0) {
                        m.toggle();
                        ModuleManager.save();
                    } else if (button == 1) {
                        popup = m;
                        popupT = 0f;
                        listening = false;
                    }
                    return true;
                }
            }
        } else if (tab == 2 && button == 0) {
            WingType[] types = WingType.values();
            for (int i = 0; i < types.length; i++) {
                int x = wingX(i), y = wingY();
                if (mx >= x && mx < x + WING_W && my >= y && my < y + WING_H) {
                    Config.data.wings = types[i].name();
                    Config.save();
                    DynamicIsland.push("Wings", types[i].label, 1500);
                    return true;
                }
            }
        } else if (tab == 3 && button == 0) {
            if (mx >= openBtnX() && mx < openBtnX() + 56 && my >= top() + 18 && my < top() + 38) {
                client.setScreen(new HudEditorScreen(this));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            setSlider(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) {
            dragging = null;
            ModuleManager.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (popup == null && tab <= 1) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (float) vertical * 16f));
            return true;
        }
        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (popup != null) {
            if (listening) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    // anuluj, nic nie zmieniaj
                } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                    popup.key = -1;
                    ModuleManager.save();
                } else {
                    popup.key = keyCode;
                    ModuleManager.save();
                    DynamicIsland.push("Bind", popup.name + " = " + keyName(keyCode), 1500);
                }
                listening = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closePopup();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
