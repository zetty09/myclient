package com.myclient.ui;

import com.myclient.ClientInfo;
import com.myclient.Config;
import com.myclient.cosmetics.CapeManager;
import com.myclient.cosmetics.FistType;
import com.myclient.cosmetics.HeadbandType;
import com.myclient.cosmetics.WingType;
import com.myclient.modules.BoolSetting;
import com.myclient.modules.Module;
import com.myclient.modules.ModuleManager;
import com.myclient.modules.ModeSetting;
import com.myclient.modules.NumberSetting;
import com.myclient.modules.Setting;
import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Menu klienta w stylu iOS: sidebar z ikonkami, karty z przelacznikami, prawy klik = ustawienia. */
public class MainMenuScreen extends Screen {
    private static final int PW = 480, PH = 292, SIDEBAR = 122;
    private static final int WHITE = 0xFFFFFFFF, GREY = 0xFF8E8E93, DIM = 0xFF636366;
    private static final int POPW = 244;
    private static final int CARD_H = 36, CARD_STEP = 42;

    private record Tab(String name, net.minecraft.util.Identifier icon, int color, String subtitle) {}

    private static final Tab[] TABS = {
            new Tab("Modules", Icons.MODULES, 0xFFFF9F0A, "Gameplay features"),
            new Tab("Visuals", Icons.VISUALS, 0xFFBF5AF2, "Widgets shown on your screen"),
            new Tab("Cosmetics", Icons.COSMETICS, 0xFFFF375F, "Customize your look"),
            new Tab("User Interface", Icons.INTERFACE, 0xFF0A84FF, "Move and edit your HUD"),
    };

    private record Sub(String name, net.minecraft.util.Identifier icon) {}

    private static final Sub[] SUBS = {
            new Sub("Wings", Icons.WINGS),
            new Sub("Headbands", Icons.HEADBAND),
            new Sub("Fists", Icons.FISTS),
            new Sub("Capes", Icons.CAPE),
    };

    private int px, py;
    private int tab = 0, sub = 0;
    private float scroll = 0, maxScroll = 0;
    private int capeScroll = 0;
    private float openT = 0, contentT = 0, popupT = 0, pillY = -1;
    private float dt = 0.016f;
    private long lastNs = System.nanoTime();
    private final Map<Object, Float> anim = new HashMap<>();

    private Module popup;
    private boolean listening;
    private NumberSetting dragging;

    private TextFieldWidget nameField;
    private TextFieldWidget searchField;
    private boolean closing;
    private int lastMx, lastMy;
    private List<String> capeList = List.of();
    private long capeListAt = 0;

    public MainMenuScreen() {
        super(Text.literal(ClientInfo.NAME));
    }

    @Override
    protected void init() {
        px = (width - PW) / 2;
        py = (height - PH) / 2;
        nameField = new TextFieldWidget(textRenderer, cx(), 0, 150, 18, Text.literal("Nickname"));
        nameField.setMaxLength(16);
        nameField.setPlaceholder(Text.literal("Player nickname"));
        nameField.visible = false;
        addDrawableChild(nameField);

        searchField = new TextFieldWidget(textRenderer, cx() + cw() - 130, py + 12, 130, 18, Text.literal("Search"));
        searchField.setMaxLength(24);
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setChangedListener(t -> scroll = 0);
        addDrawableChild(searchField);
    }

    // ---------------- uklad ----------------

    private int cx() { return px + SIDEBAR + 16; }
    private int cw() { return PW - SIDEBAR - 32; }
    private int top() { return py + 50; }
    private int bottom() { return py + PH - 26; }
    private int tabY(int i) { return i < 3 ? py + 58 + i * 24 : py + 150; }

    private List<Module> list() {
        List<Module> all = ModuleManager.of(tab == 0 ? Module.Category.MODULE : Module.Category.VISUAL);
        String q = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) return all;
        List<Module> out = new ArrayList<>();
        for (Module m : all) {
            if (m.name.toLowerCase().contains(q) || m.description.toLowerCase().contains(q)) out.add(m);
        }
        return out;
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
        Gfx.textF(ctx, textRenderer, Theme.menuFont(), s, x, y, color, false);
    }

    // ---------------- render ----------------

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);

        long now = System.nanoTime();
        dt = Math.min((now - lastNs) / 1_000_000_000f, 0.05f);
        lastNs = now;
        lastMx = mouseX;
        lastMy = mouseY;
        var ui = ModuleManager.UI;
        float spd = ui != null && ui.enabled ? ui.speed.value : 1f;
        float k = 1f - (float) Math.exp(-dt * 12f * spd);
        if (closing) {
            openT += (0f - openT) * k * 1.3f;
            if (openT < 0.04f) {
                closing = false;
                openT = 0f;
                super.close();
                return;
            }
        } else {
            openT += (1f - openT) * k;
        }
        contentT += (1f - contentT) * k;
        popupT += ((popup != null ? 1f : 0f) - popupT) * k;
        float targetPill = tabY(tab);
        if (pillY < 0) pillY = targetPill;
        pillY += (targetPill - pillY) * k;

        if (now / 1_000_000L - capeListAt > 1000) {
            capeListAt = now / 1_000_000L;
            capeList = CapeManager.list();
        }
        // pole z nickiem tylko w Cosmetics -> Capes
        boolean fieldOn = tab == 2 && sub == 3 && popup == null;
        nameField.visible = fieldOn;
        nameField.setX(cx());
        nameField.setY(subContentY() + 112);
        if (!fieldOn) nameField.setFocused(false);
        boolean searchOn = tab <= 1 && popup == null && !closing;
        searchField.visible = searchOn;
        searchField.setX(cx() + cw() - 130);
        searchField.setY(py + 12);
        if (!searchOn) searchField.setFocused(false);

        var mat = ctx.getMatrices();
        String oa = ui != null && ui.enabled ? ui.open.get() : "Pop";
        String ca = ui != null && ui.enabled ? ui.close.get() : "Instant";
        float s = switch (oa) {
            case "Zoom" -> 0.7f + 0.3f * openT;
            case "Fade", "Slide up", "Drop" -> 1f;
            default -> 0.94f + 0.06f * openT;
        };
        if (closing && ca.equals("Shrink")) s = 0.8f + 0.2f * openT;
        float ty = switch (oa) {
            case "Slide up" -> (1f - openT) * 28f;
            case "Drop" -> -(1f - openT) * 36f;
            default -> 0f;
        };
        float ccx = px + PW / 2f, ccy = py + PH / 2f;
        mat.push();
        mat.translate(0, ty, 0);
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
        Gfx.roundRect(ctx, px, py, PW, PH, 14, Theme.panel());
        Gfx.roundRect(ctx, px, py, SIDEBAR, PH, 14, Theme.sidebar());
        ctx.fill(px + SIDEBAR - 14, py, px + SIDEBAR, py + PH, Gfx.a(Theme.sidebar()));
        ctx.fill(px + SIDEBAR, py + 12, px + SIDEBAR + 1, py + PH - 12, Gfx.a(0xFF2C2C2E));

        Gfx.textF(ctx, textRenderer, Theme.menuFont(), ClientInfo.LOGO + " " + ClientInfo.NAME, px + 14, py + 14, WHITE, true);
        text(ctx, "Version " + ClientInfo.VERSION, px + 14, py + 26, GREY);

        text(ctx, "FEATURES", px + 14, py + 46, DIM);
        text(ctx, "OTHER", px + 14, py + 138, DIM);

        // animowana "pigulka" zaznaczenia
        Gfx.roundRect(ctx, px + 8, Math.round(pillY), SIDEBAR - 16, 22, 8, Theme.accent());

        for (int i = 0; i < TABS.length; i++) {
            Tab t = TABS[i];
            int y = tabY(i);
            boolean hover = mx >= px + 8 && mx < px + SIDEBAR - 8 && my >= y && my < y + 22;
            float hv = approach("tab" + i, hover && i != tab ? 1f : 0f);
            if (i != tab) Gfx.roundRect(ctx, px + 8, y, SIDEBAR - 16, 22, 8, Gfx.lerpColor(0x00FFFFFF, 0x1EFFFFFF, hv));
            // ikonka: kolorowy kafelek 16x16 z bialym symbolem 8x8
            Gfx.roundRect(ctx, px + 13, y + 3, 16, 16, 5, t.color());
            Icons.draw(ctx, t.icon(), px + 15, y + 5, 12, WHITE);
            text(ctx, t.name(), px + 36, y + 7, i == tab ? WHITE : 0xFFD1D1D6);
        }

        String user = client != null ? client.getSession().getUsername() : "player";
        Gfx.roundRect(ctx, px + 12, py + PH - 32, 20, 20, 10, Theme.accent());
        String letter = user.isEmpty() ? "?" : user.substring(0, 1).toUpperCase();
        text(ctx, letter, px + 12 + (20 - Fonts.wf(textRenderer, Theme.menuFont(), letter)) / 2, py + PH - 26, WHITE);
        text(ctx, Fonts.tf(textRenderer, Theme.menuFont(), user, SIDEBAR - 52), px + 38, py + PH - 26, WHITE);

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(cx(), py + 14, 0);
        mat.scale(1.4f, 1.4f, 1f);
        Gfx.textF(ctx, textRenderer, Theme.menuFont(), TABS[tab].name(), 0, 0, WHITE, true);
        mat.pop();
        text(ctx, TABS[tab].subtitle(), cx(), py + 33, GREY);

        if (tab <= 1) text(ctx, "Click to toggle  -  right-click for settings", cx(), py + PH - 16, DIM);
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
            Gfx.roundRect(ctx, cx(), y, cw(), CARD_H, 10, Gfx.lerpColor(Theme.card(), Theme.hover(), hv));
            text(ctx, m.name, cx() + 12, y + 8, WHITE);
            text(ctx, Fonts.tf(textRenderer, Theme.menuFont(), m.description, cw() - 24 - 40), cx() + 12, y + 21, GREY);
            if (m.key > 0) {
                String kn = keyName(m.key);
                text(ctx, kn, cx() + cw() - 48 - Fonts.wf(textRenderer, Theme.menuFont(), kn), y + 14, DIM);
            }
            Gfx.iosSwitch(ctx, cx() + cw() - 42, y + 10, approach(m, m.enabled ? 1f : 0f));
        }
        ctx.disableScissor();
    }

    // ---------------- Cosmetics ----------------

    private static final int CARD_W = 70, CARD_GAP = 8, CAPE_ROWS = 5, CAPE_ROW_H = 18;

    private int subTabX(int i) {
        int x = cx();
        for (int k = 0; k < i; k++) x += subTabW(k) + 6;
        return x;
    }
    private int subTabW(int i) { return 7 + 12 + 5 + Fonts.wf(textRenderer, Theme.menuFont(), SUBS[i].name()) + 10; }
    private int subTabY() { return top(); }
    private int subContentY() { return top() + 28; }

    private int itemCount() {
        return switch (sub) {
            case 0 -> WingType.values().length;
            case 1 -> HeadbandType.values().length;
            default -> FistType.values().length;
        };
    }
    private int itemH() { return sub == 0 ? 92 : 60; }
    private int itemX(int i) { return cx() + (i % 4) * (CARD_W + CARD_GAP); }
    private int itemY(int i) { return subContentY() + (i / 4) * (itemH() + CARD_GAP); }

    private boolean itemSelected(int i) {
        return switch (sub) {
            case 0 -> Config.wing() == WingType.values()[i];
            case 1 -> Config.headband() == HeadbandType.values()[i];
            default -> Config.fists() == FistType.values()[i];
        };
    }
    private String itemLabel(int i) {
        return switch (sub) {
            case 0 -> WingType.values()[i].label;
            case 1 -> HeadbandType.values()[i].label;
            default -> FistType.values()[i].label;
        };
    }

    private void drawCosmetics(DrawContext ctx, int mx, int my) {
        // podzakladki z ikonkami
        for (int i = 0; i < SUBS.length; i++) {
            int x = subTabX(i), y = subTabY(), w = subTabW(i);
            boolean hover = popup == null && mx >= x && mx < x + w && my >= y && my < y + 18;
            float hv = approach("sub" + i, hover ? 1f : 0f);
            boolean sel = sub == i;
            float st = approach("subsel" + i, sel ? 1f : 0f);
            Gfx.roundRect(ctx, x, y, w, 18, 9, Gfx.lerpColor(Gfx.lerpColor(Theme.card(), Theme.hover(), hv), Theme.accent(), st));
            Icons.draw(ctx, SUBS[i].icon(), x + 7, y + 3, 12, sel ? WHITE : 0xFFD1D1D6);
            text(ctx, SUBS[i].name(), x + 7 + 12 + 5, y + 5, sel ? WHITE : 0xFFD1D1D6);
        }

        if (sub == 3) {
            drawCapes(ctx, mx, my);
            return;
        }
        for (int i = 0; i < itemCount(); i++) drawItem(ctx, i, mx, my);
        if (sub == 1 || sub == 2) {
            text(ctx, "Only you can see these cosmetics.", cx(), py + PH - 40, DIM);
        }
    }

    private void drawItem(DrawContext ctx, int i, int mx, int my) {
        int x = itemX(i), y = itemY(i), h = itemH();
        boolean selected = itemSelected(i);
        boolean hover = popup == null && mx >= x && mx < x + CARD_W && my >= y && my < y + h;
        float hv = approach("item" + sub + "_" + i, hover ? 1f : 0f);
        Gfx.roundRect(ctx, x, y, CARD_W, h, 12, Gfx.lerpColor(Theme.card(), Theme.hover(), hv));
        if (selected) {
            Gfx.border(ctx, x, y, CARD_W, h, Theme.accent());
            Gfx.border(ctx, x + 1, y + 1, CARD_W - 2, h - 2, Theme.accent());
        }

        if (sub == 0) {
            WingType t = WingType.values()[i];
            if (t == WingType.NONE) {
                text(ctx, "-", x + CARD_W / 2 - 2, y + 32, DIM);
            } else {
                var m = ctx.getMatrices();
                m.push();
                m.translate(x + 11, y + 10, 0);
                m.scale(1.5f, 1.5f, 1f);
                ctx.drawTexture(RenderLayer::getGuiTextured, t.texture, 0, 0, 0f, 0f, 32, 32, 32, 32);
                m.pop();
            }
        } else {
            int color = sub == 1 ? HeadbandType.values()[i].color() : FistType.values()[i].color();
            if (color == 0) {
                text(ctx, "-", x + CARD_W / 2 - 2, y + 14, DIM);
            } else if (sub == 1) {
                // glowa (skin-owy kafelek) z opaska, wezlem i koncami
                int hx = x + CARD_W / 2 - 15, hy = y + 6;
                Gfx.roundRect(ctx, hx, hy, 30, 30, 6, 0xFFE0B48C);
                Gfx.roundRect(ctx, hx, hy, 30, 9, 4, 0xFF5A3B24);
                Gfx.roundRect(ctx, hx - 2, hy + 10, 34, 8, 3, Gfx.lerpColor(color, 0xFF000000, 0.25f));
                Gfx.roundRect(ctx, hx - 2, hy + 10, 34, 5, 3, color);
                ctx.fill(hx + 6, hy + 21, hx + 8, hy + 23, 0xFF2B1B10);
                ctx.fill(hx + 22, hy + 21, hx + 24, hy + 23, 0xFF2B1B10);
                Gfx.roundRect(ctx, hx + 28, hy + 16, 6, 6, 3, Gfx.lerpColor(color, 0xFF000000, 0.2f));
                Gfx.roundRect(ctx, hx + 29, hy + 21, 4, 11, 2, color);
            } else {
                // rekawica: korpus, kciuk, mankiet i polysk
                int gx = x + CARD_W / 2 - 15, gy = y + 5;
                int dark = Gfx.lerpColor(color, 0xFF000000, 0.35f);
                Gfx.roundRect(ctx, gx, gy + 1, 30, 24, 11, dark);
                Gfx.roundRect(ctx, gx, gy, 30, 23, 11, color);
                Gfx.roundRect(ctx, gx - 6, gy + 9, 12, 11, 5, color);
                Gfx.roundRect(ctx, gx + 3, gy + 22, 24, 10, 3, Gfx.lerpColor(color, 0xFFFFFFFF, 0.55f));
                Gfx.roundRect(ctx, gx + 3, gy + 28, 24, 4, 2, dark);
                Gfx.roundRect(ctx, gx + 6, gy + 4, 9, 4, 2, Gfx.lerpColor(color, 0xFFFFFFFF, 0.45f));
            }
        }
        String label = itemLabel(i);
        int lw = Fonts.wf(textRenderer, Theme.menuFont(), label);
        text(ctx, label, x + (CARD_W - lw) / 2, y + h - 14, selected ? WHITE : 0xFFD1D1D6);
    }

    private int capeListY() { return subContentY(); }
    private int fetchBtnX() { return cx() + 156; }
    private int folderBtnX() { return cx() + 218; }
    private int btnY() { return subContentY() + 112; }

    private void drawCapes(DrawContext ctx, int mx, int my) {
        int rows = capeList.size() + 1;
        capeScroll = Math.max(0, Math.min(capeScroll, Math.max(0, rows - CAPE_ROWS)));

        Gfx.roundRect(ctx, cx(), capeListY(), cw(), CAPE_ROWS * CAPE_ROW_H + 4, 10, Theme.card());
        for (int r = 0; r < CAPE_ROWS; r++) {
            int idx = r + capeScroll;
            if (idx >= rows) break;
            int y = capeListY() + 2 + r * CAPE_ROW_H;
            String name = idx == 0 ? "None (default cape)" : capeList.get(idx - 1);
            boolean selected = idx == 0 ? Config.data.cape.isEmpty() : name.equals(Config.data.cape);
            boolean hover = popup == null && mx >= cx() && mx < cx() + cw() && my >= y && my < y + CAPE_ROW_H;
            float hv = approach("cape" + idx, hover ? 1f : 0f);
            if (hv > 0.02f) Gfx.roundRect(ctx, cx() + 3, y, cw() - 6, CAPE_ROW_H - 1, 7, Gfx.lerpColor(0x00FFFFFF, 0x26FFFFFF, hv));
            if (selected) Gfx.roundRect(ctx, cx() + 10, y + 6, 6, 6, 3, Theme.accent());
            text(ctx, Fonts.tf(textRenderer, Theme.menuFont(), name, cw() - 40), cx() + 22, y + 5, selected ? WHITE : 0xFFD1D1D6);
        }
        if (rows > CAPE_ROWS) text(ctx, "scroll for more", cx() + cw() - 70, capeListY() + CAPE_ROWS * CAPE_ROW_H + 8, DIM);

        int y0 = subContentY();
        text(ctx, "Copy a cape from a player (e.g. an anniversary cape)", cx(), y0 + 98, GREY);

        // przyciski (pole tekstowe rysuje sam widget)
        drawButton(ctx, "Download", fetchBtnX(), btnY(), 56, mx, my, Theme.accent());
        drawButton(ctx, "Open folder", folderBtnX(), btnY(), 76, mx, my, 0xFF3A3A3C);

        String st = CapeManager.status;
        if (st != null && !st.isEmpty()) text(ctx, st, cx(), y0 + 138, 0xFFFFD60A);
        text(ctx, "Own cape: put a 64x32 .png into the folder, then pick it above.", cx(), y0 + 152, DIM);
    }

    private void drawButton(DrawContext ctx, String label, int x, int y, int w, int mx, int my, int color) {
        boolean hover = popup == null && mx >= x && mx < x + w && my >= y && my < y + 18;
        float hv = approach("btn" + label, hover ? 1f : 0f);
        Gfx.roundRect(ctx, x, y, w, 18, 9, Gfx.lerpColor(color, 0xFF5A5A5E, hv * 0.35f));
        text(ctx, label, x + (w - Fonts.wf(textRenderer, Theme.menuFont(), label)) / 2, y + 5, WHITE);
    }

    // ---------------- User Interface ----------------

    private int openBtnX() { return cx() + cw() - 70; }

    private void drawInterface(DrawContext ctx, int mx, int my) {
        Gfx.roundRect(ctx, cx(), top(), cw(), 56, 12, Theme.card());
        text(ctx, "HUD Editor", cx() + 14, top() + 12, WHITE);
        text(ctx, "Drag your visuals anywhere on the screen", cx() + 14, top() + 28, GREY);
        boolean hover = popup == null && mx >= openBtnX() && mx < openBtnX() + 56 && my >= top() + 18 && my < top() + 38;
        float hv = approach("openEditor", hover ? 1f : 0f);
        Gfx.roundRect(ctx, openBtnX(), top() + 18, 56, 20, 10, Gfx.lerpColor(Theme.accent(), 0xFF3D9BFF, hv));
        String o = "Open";
        text(ctx, o, openBtnX() + (56 - Fonts.wf(textRenderer, Theme.menuFont(), o)) / 2, top() + 24, WHITE);

        text(ctx, "Tip: you can also right-click in chat and choose \"Edit HUD positions\".",
                cx(), top() + 68, DIM);
    }

    // ---------------- popup z ustawieniami ----------------

    private record PRow(int kind, Setting s, int y, int h) {}

    private List<Setting> shownSettings() {
        List<Setting> out = new ArrayList<>();
        for (Setting st : popup.settings) if (st.visibleWhen.getAsBoolean()) out.add(st);
        return out;
    }

    private int popContentH() {
        int h = 36 + 26 + 10;
        for (Setting st : shownSettings()) h += st instanceof NumberSetting ? 32 : 26;
        return h;
    }

    private int popH() { return Math.min(popContentH(), PH - 12); }

    private float popScroll = 0;

    private int popX() { return px + (PW - POPW) / 2; }
    private int popY() { return py + (PH - popH()) / 2; }

    private List<PRow> popupRows() {
        List<PRow> out = new ArrayList<>();
        int y = popY() + 36 - (int) popScroll;
        out.add(new PRow(0, null, y, 26));
        y += 26;
        for (Setting s : shownSettings()) {
            boolean num = s instanceof NumberSetting;
            int h = num ? 32 : 26;
            out.add(new PRow(num ? 2 : (s instanceof ModeSetting ? 3 : 1), s, y, h));
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
        Gfx.roundRect(ctx, x, y, w, popH(), 14, Theme.card());
        Gfx.textF(ctx, textRenderer, Theme.menuFont(), popup.name, x + 14, y + 13, WHITE, true);
        String done = "Done";
        text(ctx, done, x + w - 14 - Fonts.wf(textRenderer, Theme.menuFont(), done), y + 13, Theme.accent());
        ctx.fill(x + 12, y + 32, x + w - 12, y + 33, Gfx.a(0xFF3A3A3C));

        ctx.enableScissor(x, y + 34, x + w, y + popH() - 4);
        for (PRow r : popupRows()) {
            switch (r.kind()) {
                case 0 -> {
                    text(ctx, "Keybind", x + 14, r.y() + 9, WHITE);
                    int bx = x + w - 14 - 84;
                    Gfx.roundRect(ctx, bx, r.y() + 4, 84, 18, 9, listening ? Theme.accent() : 0xFF3A3A3C);
                    String label = listening ? "Press a key..." : keyName(popup.key);
                    label = Fonts.tf(textRenderer, Theme.menuFont(), label, 78);
                    text(ctx, label, bx + (84 - Fonts.wf(textRenderer, Theme.menuFont(), label)) / 2, r.y() + 9, WHITE);
                }
                case 1 -> {
                    BoolSetting b = (BoolSetting) r.s();
                    text(ctx, b.name, x + 14, r.y() + 9, WHITE);
                    Gfx.iosSwitch(ctx, x + w - 14 - 30, r.y() + 5, approach(b, b.value ? 1f : 0f));
                }
                case 3 -> {
                    ModeSetting ms = (ModeSetting) r.s();
                    text(ctx, ms.name, x + 14, r.y() + 9, WHITE);
                    String v = ms.get();
                    int bw = Fonts.wf(textRenderer, Theme.menuFont(), v) + 16;
                    int bx = x + w - 14 - bw;
                    Gfx.roundRect(ctx, bx, r.y() + 4, bw, 18, 9, 0xFF3A3A3C);
                    text(ctx, v, bx + 8, r.y() + 9, Theme.accent());
                }
                default -> {
                    NumberSetting n = (NumberSetting) r.s();
                    text(ctx, n.name, x + 14, r.y() + 5, WHITE);
                    String v = n.display();
                    text(ctx, v, x + w - 14 - Fonts.wf(textRenderer, Theme.menuFont(), v), r.y() + 5, GREY);
                    int x0 = x + 14, x1 = x + w - 14, ty = r.y() + 20;
                    float frac = (n.value - n.min) / (n.max - n.min);
                    Gfx.roundRect(ctx, x0, ty, x1 - x0, 4, 2, 0xFF48484A);
                    int fw = Math.round((x1 - x0) * frac);
                    if (fw > 0) Gfx.roundRect(ctx, x0, ty, Math.max(4, fw), 4, 2, Theme.accent());
                    Gfx.roundRect(ctx, x0 + fw - 5, ty - 3, 10, 10, 5, WHITE);
                }
            }
        }
        ctx.disableScissor();
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

    private void pick(int i) {
        switch (sub) {
            case 0 -> {
                Config.data.wings = WingType.values()[i].name();
                DynamicIsland.push("Wings", WingType.values()[i].label, 1500);
            }
            case 1 -> {
                Config.data.headband = HeadbandType.values()[i].name();
                DynamicIsland.push("Headband", HeadbandType.values()[i].label, 1500);
            }
            default -> {
                Config.data.fists = FistType.values()[i].name();
                DynamicIsland.push("Fists", FistType.values()[i].label, 1500);
            }
        }
        Config.save();
    }

    private void fetchCape() {
        CapeManager.fetch(nameField.getText().trim());
    }

    private void openCapeFolder() {
        try {
            Util.getOperatingSystem().open(CapeManager.dir().toFile());
        } catch (Throwable t) {
            CapeManager.status = "Folder: " + CapeManager.dir();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (closing) return true;
        if (popup != null) {
            int x = popX(), y = popY(), w = POPW, h = popH();
            if (mx < x || mx >= x + w || my < y || my >= y + h) {
                closePopup();
                return true;
            }
            if (button != 0) return true;
            if (my < y + 30 && mx >= x + w - 14 - Fonts.wf(textRenderer, Theme.menuFont(), "Done") - 6) {
                closePopup();
                return true;
            }
            if (my < y + 34 || my > y + h - 4) return true;
            for (PRow r : popupRows()) {
                if (my < r.y() || my >= r.y() + r.h()) continue;
                if (r.kind() == 0) {
                    if (mx >= x + w - 14 - 84) listening = true;
                } else if (r.kind() == 1) {
                    BoolSetting b = (BoolSetting) r.s();
                    b.value = !b.value;
                    ModuleManager.save();
                } else if (r.kind() == 3) {
                    ((ModeSetting) r.s()).next();
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
                        popScroll = 0;
                        popupT = 0f;
                        listening = false;
                    }
                    return true;
                }
            }
        } else if (tab == 2 && button == 0) {
            for (int i = 0; i < SUBS.length; i++) {
                if (mx >= subTabX(i) && mx < subTabX(i) + subTabW(i) && my >= subTabY() && my < subTabY() + 18) {
                    if (sub != i) {
                        sub = i;
                        contentT = 0.4f;
                    }
                    return true;
                }
            }
            if (sub < 3) {
                for (int i = 0; i < itemCount(); i++) {
                    int x = itemX(i), y = itemY(i);
                    if (mx >= x && mx < x + CARD_W && my >= y && my < y + itemH()) {
                        pick(i);
                        return true;
                    }
                }
            } else {
                int rows = capeList.size() + 1;
                for (int r = 0; r < CAPE_ROWS; r++) {
                    int idx = r + capeScroll;
                    if (idx >= rows) break;
                    int y = capeListY() + 2 + r * CAPE_ROW_H;
                    if (mx >= cx() && mx < cx() + cw() && my >= y && my < y + CAPE_ROW_H) {
                        Config.data.cape = idx == 0 ? "" : capeList.get(idx - 1);
                        Config.save();
                        DynamicIsland.push("Cape", idx == 0 ? "Default" : capeList.get(idx - 1), 1500);
                        return true;
                    }
                }
                if (mx >= fetchBtnX() && mx < fetchBtnX() + 56 && my >= btnY() && my < btnY() + 18) {
                    fetchCape();
                    return true;
                }
                if (mx >= folderBtnX() && mx < folderBtnX() + 76 && my >= btnY() && my < btnY() + 18) {
                    openCapeFolder();
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
        if (popup != null) {
            float max = Math.max(0, popContentH() - popH());
            popScroll = Math.max(0, Math.min(max, popScroll - (float) vertical * 14f));
            return true;
        }
        if (popup == null && tab <= 1) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (float) vertical * 16f));
            return true;
        }
        if (popup == null && tab == 2 && sub == 3) {
            capeScroll -= (int) Math.signum(vertical);
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
        if (nameField != null && nameField.isFocused() && nameField.visible
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            fetchCape();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (closing) return;
        var ui = ModuleManager.UI;
        String ca = ui != null && ui.enabled ? ui.close.get() : "Instant";
        switch (ca) {
            case "Fade", "Shrink" -> {
                closing = true;
                popup = null;
            }
            case "Glass break" -> {
                float ix = lastMx, iy = lastMy;
                if (ix < px || ix > px + PW || iy < py || iy > py + PH) {
                    ix = px + PW / 2f;
                    iy = py + PH / 2f;
                }
                int tintColor = ui.tint.get().equals("Clear") ? 0xFFDCEBFF : com.myclient.visuals.Colors.pick(ui.tint.get());
                com.myclient.visuals.GlassBreak.shatter(px, py, PW, PH, ix, iy, ui.glassTime.value, ui.cracks.index, tintColor);
                if (ui.sound.value && client != null) {
                    client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                            net.minecraft.sound.SoundEvents.BLOCK_GLASS_BREAK, 1.0f, 1.0f));
                }
                super.close();
            }
            default -> super.close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
