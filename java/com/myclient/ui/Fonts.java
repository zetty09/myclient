package com.myclient.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Czcionki klienta. "Arial" to Liberation Sans (licencja OFL, metrycznie identyczna z Arialem);
 * prawdziwego Ariala nie mozna dolaczyc do moda. Pliki: assets/myclient/font/.
 */
public final class Fonts {
    public static final String[] NAMES = {"Default", "Arial", "Arial Bold", "Mono"};

    private Fonts() {}

    private static Identifier id(String name) {
        return switch (name) {
            case "Arial" -> Identifier.of("myclient", "arial");
            case "Arial Bold" -> Identifier.of("myclient", "arial_bold");
            case "Mono" -> Identifier.of("myclient", "mono");
            default -> null;
        };
    }

    public static Text styled(String s, String font) {
        Identifier id = font == null ? null : id(font);
        return id == null ? Text.literal(s) : Text.literal(s).setStyle(Style.EMPTY.withFont(id));
    }

    /** width z czcionka jako drugim argumentem (wygodne w menu). */
    public static int wf(TextRenderer tr, String font, String s) { return width(tr, s, font); }

    public static String tf(TextRenderer tr, String font, String s, int maxWidth) { return trim(tr, s, maxWidth, font); }

    public static int width(TextRenderer tr, String s, String font) {
        return tr.getWidth(styled(s, font));
    }

    /** Przycina tekst, tak by miescil sie w podanej szerokosci. */
    public static String trim(TextRenderer tr, String s, int maxWidth, String font) {
        if (s == null) return "";
        if (width(tr, s, font) <= maxWidth) return s;
        int n = s.length();
        while (n > 0 && width(tr, s.substring(0, n), font) > maxWidth) n--;
        return s.substring(0, n);
    }

    public static void draw(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color, boolean shadow, String font) {
        ctx.drawText(tr, styled(s, font), x, y, color, shadow);
    }
}
