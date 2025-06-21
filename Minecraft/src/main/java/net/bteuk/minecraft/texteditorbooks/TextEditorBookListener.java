package net.bteuk.minecraft.texteditorbooks;

import lombok.Getter;
import net.bteuk.minecraft.gui.Gui;
import net.bteuk.minecraft.misc.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A type of listener which handles text edits made through books
 */
public class TextEditorBookListener implements Listener {

    /**
     * A reference to the instance of the plugin
     */
    private final JavaPlugin plugin;

    /**
     * A reference to the interface object which defines the intended behaviour on book close
     */
    private final BookCloseAction bookCloseAction;

    /**
     * A reference to the Player
     */
    private final Player player;

    /**
     * A reference to parent GUI
     */
    private final Gui parentGUI;

    /**
     * Stores the current book meta for the book
     */
    private final WritableBookMeta editableBookData;

    /**
     * The book item stack
     */
    @Getter
    private final ItemStack book;

    /**
     * Whether the book as been edited
     */
    @Getter
    private boolean edited;

    /**
     * Constructs the object, gets the book ready
     *
     * @param plugin          A reference to the instance of the TeachingTutorials plugin
     * @param player          A reference to the Player
     * @param szBookTitle     The intended title for the book
     * @param bookCloseAction The action to perform on book close
     */
    public TextEditorBookListener(JavaPlugin plugin, Player player, Gui parentGUI, String szBookTitle, BookCloseAction bookCloseAction, String... initialValue) {

        this.plugin = plugin;
        this.bookCloseAction = bookCloseAction;
        this.player = player;
        this.parentGUI = parentGUI;

        //Creates the book
        this.book = new ItemStack(Material.WRITABLE_BOOK, 1);

        //Extracts a reference to the book meta, and sets the title and initial value
        BookMeta bookMeta = (BookMeta) this.book.getItemMeta();
        bookMeta.setTitle(szBookTitle);
        bookMeta.displayName(Component.text(szBookTitle).decoration(TextDecoration.ITALIC, false));

        //Adds the initial value if it not blank
        if (initialValue.length > 0)
            bookMeta.addPages(Component.text(initialValue[0]));

        //Adds the meta of the book back in
        this.book.setItemMeta(bookMeta);

        //Stores the writable book meta for later comparison
        editableBookData = (WritableBookMeta) this.book.getItemMeta();
    }

    /**
     * Gives the player the book, closes the current inventory and registers the listeners with the server's event listeners
     *
     * @param szBookName     The human name of the book used when notifying a player which slot the book has been added to
     */
    public void startEdit(String szBookName) {
        //Gives the player the book item

        //Closes the current inventory
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        boolean bPlayerHasItem = false;

        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) != null)
                if (Objects.equals(player.getInventory().getItem(i), this.book)) {
                    bPlayerHasItem = true;
                    player.getInventory().setHeldItemSlot(i);
                }
        }

        if (!bPlayerHasItem)
            PlayerUtils.giveItem(player, this.book, szBookName);

        //Registers the book close listener
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregisters the listeners with the server's event listeners
     */
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Detects when a player closes a book after editing it, checks whether it is the relevant player and relevant book,
     * then detects the changes and stores the information in the book.
     * <p>Will then run the custom book close logic inserted into this listener at construction</p>
     *
     * @param event
     */
    @EventHandler
    public void BookCloseEvent(PlayerEditBookEvent event) {
        //Check the player
        if (!event.getPlayer().equals(player))
            return;

        //Check the current item in hand for equivalence to the book of this listener
        if (!event.getPlayer().getInventory().getItemInMainHand().equals(this.book)) {
            return;
        }

        //Check the old book meta against the old book meta stored here
        if (!event.getPreviousBookMeta().equals(editableBookData)) {
            return;
        }

        event.setCancelled(true);

        //Extracts the new content from the book
        StringBuilder szNewContent = new StringBuilder();
        List<Component> pages = event.getNewBookMeta().pages();
        List<String> pagesString = pages.stream().map(page -> PlainTextComponentSerializer.plainText().serialize(page)).toList();
        if (!pages.isEmpty()) {
            for (Component page : pages) {
                szNewContent.append(((TextComponent) page).content()).append(" ");
            }
            // Removes the end space, the space after the last page is added in the loop but then needs to be removed
            szNewContent = new StringBuilder(szNewContent.substring(0, szNewContent.length() - 1));
        }

        // Check if the player has edited this book.
        if (hasChanged(pagesString)) {
            edited = true;
        }

        //Performs the predefined instructions upon book close, or sign
        boolean bSaveAnswers;
        if (event.isSigning())
            bSaveAnswers = bookCloseAction.runBookClose(event.getPreviousBookMeta(), event.getNewBookMeta(), this, szNewContent.toString());
        else
            bSaveAnswers = bookCloseAction.runBookSign(event.getPreviousBookMeta(), event.getNewBookMeta(), this, szNewContent.toString());

        if (bSaveAnswers) {
            //Saves the instructions in the book
            editableBookData.setPages(pagesString);
            getBook().setItemMeta(editableBookData);
        }

        bookCloseAction.runPostClose();
    }

    @EventHandler
    public void bookDestroyed(ItemDespawnEvent event) {
        if (event.getEntity().getItemStack().equals(this.book))
            unregister();
    }

    @EventHandler
    public void bookTouched(InventoryClickEvent event) {
        if (event.getCurrentItem() != null)
            if (event.getCurrentItem().equals(this.book)) {
                plugin.getLogger().log(Level.INFO, "Book touched, cancelling");
                event.setCancelled(true);

                //Closing the inv will cancel the copying/dragging process. We then want to reopen.
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        parentGUI.open(player);
                    }
                }, 1);
            }
    }

    @EventHandler
    public void bookDragged(InventoryDragEvent event) {
        if (event.getOldCursor().equals(this.book))
            event.setCancelled(true);
    }

    @EventHandler
    public void bookMoved(InventoryMoveItemEvent event) {
        if (event.getItem().equals(this.book))
            event.setCancelled(true);
    }

    @EventHandler
    public void bookDropped(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().equals(this.book))
            event.setCancelled(true);
    }

    public List<String> getBookPages() {
        return editableBookData.getPages().stream().map(String::trim).collect(Collectors.toList());
    }

    /**
     * Compares the current content of the book with the list of pages provided, ignoring leading and trailing whitespace
     *
     * @param newPages The list of pages to which you want to compare the book's current list of pages
     * @return Whether the content is different
     */
    public boolean hasChanged(List<String> newPages) {
        String previousBookContent = String.join("", getBookPages());
        String currentBookContent = String.join("", newPages);

        return !Objects.equals(previousBookContent, currentBookContent);
    }
}
