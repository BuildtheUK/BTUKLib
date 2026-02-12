package org.btuk.minecraft.texteditorbooks;

import org.bukkit.inventory.meta.BookMeta;

public interface BookCloseAction {

    /**
     * Performs the action on book close. You may want to unregister the book close listener and remove the book
     * within this.
     * @param oldBookMeta The previous metadata of the book just closed.
     * @param newBookMeta The new metadata of the book just closed.
     * @param textEditorBookListener A reference to the book listener itself which calls this. Enables unregistering to be called
     * @param szNewContent The combined content of all pages in the new book. This is always provided for convenience
     * @return Whether to save the text in the book
     */
    boolean runBookClose(BookMeta oldBookMeta, BookMeta newBookMeta, TextEditorBookListener textEditorBookListener, String szNewContent);

    /**
     * Performs the action on book sign. You may want to unregister the book close listener and remove the book
     * within this.
     * @param oldBookMeta The previous metadata of the book just signed.
     * @param newBookMeta The new metadata of the book just signed.
     * @param textEditorBookListener A reference to the book listener itself which calls this. Enables unregistering to be called
     * @param szNewContent The combined content of all pages in the new book. This is always provided for convenience
     * @return Whether to save the text in the book
     */
    boolean runBookSign(BookMeta oldBookMeta, BookMeta newBookMeta, TextEditorBookListener textEditorBookListener, String szNewContent);

    /**
     * Performs the actions post saving and closing. Use this if you require actions to be performed based on the content
     * of the book after close
     */
    void runPostClose();
}
