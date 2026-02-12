package org.btuk.minecraft.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtils {

    private static final Pattern PATTERN = Pattern.compile("%s");

    public static Component title(String message) {
        return Component.text(message, NamedTextColor.AQUA, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false);
    }

    public static Component line(String message) {
        return colouredText(NamedTextColor.WHITE, message);
    }

    public static Component greyText(String message) {
        return colouredText(NamedTextColor.GRAY, message);
    }

    public static Component error(String message) {
        return Component.text(message, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Create an error message with vars.
     * The colour of the message is RED, with the vars highlighted with DARK_RED.
     *
     * @param message the message, using %s as placeholder for the vars.
     * @param vars the vars to add to the placeholders, must equal the number of placeholder symbols.
     * @return the {@link Component} with the message, or null if the number of vars is incorrect.
     */
    public static Component error(String message, String... vars) {
        return varMessage(NamedTextColor.RED, NamedTextColor.DARK_RED, message, vars);
    }

    public static Component success(String message) {
        return colouredText(NamedTextColor.GREEN, message);
    }

    /**
     * Create a success message with vars.
     * The colour of the message is GREEN, with the vars highlighted with DARK_AQUA.
     *
     * @param message the message, using %s as placeholder for the vars.
     * @param vars the vars to add to the placeholders, must equal the number of placeholder symbols.
     * @return the {@link Component} with the message, or null if the number of vars is incorrect.
     */
    public static Component success(String message, String... vars) {
        return varMessage(NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, message, vars);
    }

    private static Component varMessage(NamedTextColor textColour, NamedTextColor varColour, String message, String... vars) {
        Component component = Component.empty();
        // Find the number of vars needed.
        int lastIdx = 0;
        int count = 0;
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            int idx = matcher.start();
            if (idx != lastIdx) {
                component = component.append(colouredText(textColour, message.substring(lastIdx, idx)));
            }
            if (count < vars.length) {
                component = component.append(colouredText(varColour, vars[count]));
                count++;
            }
            lastIdx = idx + 2;
        }
        // At the remaining text if exists.
        if (lastIdx < message.length()) {
            component = component.append(colouredText(textColour, message.substring(lastIdx)));
        }
        return component;
    }

    private static Component colouredText(NamedTextColor colour, String message) {
        return Component.text(message, colour)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, false);
    }
}
