package org.btuk.minecraft.gui;

public interface GuiFactory<T> {

    Class<T> getGuiClass();

    Gui create(GuiOpenContext context);
}