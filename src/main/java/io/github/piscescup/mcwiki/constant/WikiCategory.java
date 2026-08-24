package io.github.piscescup.mcwiki.constant;


import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a category displayed by the in-game Minecraft Wiki browser.
 *
 * <p>Each category has a unique identifier used by commands, a translation
 * key used to display its localized name, and an item stack used as its
 * icon in the category selection screen.</p>
 *
 * <p>For example, the {@code block} category can be selected using:</p>
 *
 * <pre>{@code
 * /mc-wiki block
 * /mc-wiki block diamond_ore
 * }</pre>
 *
 * @author Ren YuanTong
 * @since 1.0.0
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki">Minecraft Wiki</a>
 */
public enum WikiCategory {

    /**
     * The villager trading category.
     *
     * @see <a href="https://minecraft.wiki/w/Trading">Trading</a>
     */
    TRADING(
        "trading",
        "gui.mc_wiki.category.trading",
        new ItemStack(Items.EMERALD)
    ),

    /**
     * The potion brewing category.
     *
     * @see <a href="https://minecraft.wiki/w/Brewing">Brewing</a>
     */
    BREWING(
        "brewing",
        "gui.mc_wiki.category.brewing",
        new ItemStack(Items.POTION)
    ),

    /**
     * The enchanting category.
     *
     * @see <a href="https://minecraft.wiki/w/Enchanting">Enchanting</a>
     */
    ENCHANTING(
        "enchanting",
        "gui.mc_wiki.category.enchanting",
        new ItemStack(Items.ENCHANTING_TABLE)
    ),

    /**
     * The mob category.
     *
     * @see <a href="https://minecraft.wiki/w/Mob">Mob</a>
     */
    MOB(
        "mob",
        "gui.mc_wiki.category.mob",
        new ItemStack(Items.SLIME_BLOCK)
    ),

    /**
     * The block category.
     *
     * @see <a href="https://minecraft.wiki/w/Block">Block</a>
     */
    BLOCK(
        "block",
        "gui.mc_wiki.category.block",
        new ItemStack(Items.BRICKS)
    ),

    /**
     * The item category.
     *
     * @see <a href="https://minecraft.wiki/w/Item">Item</a>
     */
    ITEM(
        "item",
        "gui.mc_wiki.category.item",
        new ItemStack(Items.DIAMOND_PICKAXE)
    ),

    /**
     * The biome category.
     *
     * @see <a href="https://minecraft.wiki/w/Biome">Biome</a>
     */
    BIOME(
        "biome",
        "gui.mc_wiki.category.biome",
        new ItemStack(Items.CHERRY_LEAVES)
    ),

    /**
     * The status effect category.
     *
     * @see <a href="https://minecraft.wiki/w/Effect">Effect</a>
     */
    EFFECT(
        "effect",
        "gui.mc_wiki.category.effect",
        new ItemStack(Items.SLIME_BALL)
    ),

    /**
     * The crafting category.
     *
     * @see <a href="https://minecraft.wiki/w/Crafting">Crafting</a>
     */
    CRAFTING(
        "crafting",
        "gui.mc_wiki.category.crafting",
        new ItemStack(Items.CRAFTING_TABLE)
    ),

    /**
     * The furnace smelting category.
     *
     * @see <a href="https://minecraft.wiki/w/Smelting">Smelting</a>
     */
    SMELTING(
        "smelting",
        "gui.mc_wiki.category.smelting",
        new ItemStack(Items.FURNACE)
    ),

    /**
     * The smithing category.
     *
     * @see <a href="https://minecraft.wiki/w/Smithing">Smithing</a>
     */
    SMITHING(
        "smithing",
        "gui.mc_wiki.category.smithing",
        new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    ),

    /**
     * The generated structure category.
     *
     * @see <a href="https://minecraft.wiki/w/Structure">Structure</a>
     */
    STRUCTURE(
        "structure",
        "gui.mc_wiki.category.structure",
        new ItemStack(Items.SANDSTONE)
    ),

    /**
     * The redstone mechanics category.
     *
     * @see <a href="https://minecraft.wiki/w/Redstone_circuit">Redstone circuit</a>
     */
    REDSTONE(
        "redstone",
        "gui.mc_wiki.category.redstone",
        new ItemStack(Items.REDSTONE)
    ),

    /**
     * The command category.
     *
     * @see <a href="https://minecraft.wiki/w/Commands">Commands</a>
     */
    COMMAND(
        "command",
        "gui.mc_wiki.category.command",
        new ItemStack(Items.COMMAND_BLOCK)
    ),

    /**
     * The Minecraft version history category.
     *
     * @see <a href="https://minecraft.wiki/w/Java_Edition_version_history">Java Edition version history</a>
     */
    VERSION(
        "version",
        "gui.mc_wiki.category.version",
        new ItemStack(Items.BEDROCK)
    ),

    /**
     * The tutorial category.
     *
     * @see <a href="https://minecraft.wiki/w/Tutorials">Tutorials</a>
     */
    TUTORIAL(
        "tutorial",
        "gui.mc_wiki.category.tutorial",
        new ItemStack(Items.LECTERN)
    );

    /**
     * The identifier used to select this category in commands.
     */
    @NotNull
    private final String id;

    /**
     * The translation key used to display the localized category name.
     */
    @NotNull
    private final String translationKey;

    /**
     * The item stack used as the category icon.
     *
     * <p>This stack is retained internally and must not be exposed directly
     * because {@link ItemStack} is mutable.</p>
     */
    @NotNull
    private final ItemStack icon;

    /**
     * Creates a Wiki category.
     *
     * @param id             the identifier used by commands
     * @param translationKey the translation key for the category name
     * @param icon           the item stack displayed as the category icon
     */
    WikiCategory(
        @NotNull String id,
        @NotNull String translationKey,
        @NotNull ItemStack icon
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.icon = icon.copy();
    }

    /**
     * Returns the identifier used to select this category in commands.
     *
     * @return the category identifier
     */
    @NotNull
    public String id() {
        return this.id;
    }

    /**
     * Returns the translation key used to display this category.
     *
     * @return the category translation key
     */
    @NotNull
    public String translationKey() {
        return this.translationKey;
    }

    /**
     * Returns a copy of the item stack used as this category's icon.
     *
     * <p>A copy is returned to prevent callers from modifying the icon
     * retained by this enum constant.</p>
     *
     * @return a copy of the category icon
     */
    @NotNull
    public ItemStack icon() {
        return this.icon.copy();
    }
}