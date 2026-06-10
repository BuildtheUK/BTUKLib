package org.btuk.minecraft.gui;

@FunctionalInterface
public interface GuiFactory {

    Gui create(GuiOpenContext context);
}