package fr.skytasul.quests.utils.compatibility;

import io.github.projectunified.uniitem.all.AllItemProvider;
import io.github.projectunified.uniitem.api.ItemKey;
import io.github.projectunified.uniitem.api.ItemProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Central item-provider bridge for BeautyQuests.
 *
 * <p>Quest data remains backwards-compatible Bukkit {@link ItemStack} data. UniItems reads the
 * provider identity already stored by the source plugin, allowing BeautyQuests to compare by a
 * stable provider key and rebuild the current item definition when an item is displayed or given.
 */
public final class UniItemsHook {

	private static final UniItemsHook INSTANCE = new UniItemsHook(new AllItemProvider());

	private final ItemProvider provider;

	UniItemsHook(@NotNull ItemProvider provider) {
		this.provider = Objects.requireNonNull(provider, "provider");
	}

	public static @NotNull UniItemsHook get() {
		return INSTANCE;
	}

	/**
	 * Returns the normalized UniItems key stored by the source item plugin.
	 */
	public @Nullable String identify(@Nullable ItemStack item) {
		if (item == null || item.getType().isAir())
			return null;

		try {
			ItemKey key = provider.key(item);
			return key == null ? null : provider.normalize(key).toString();
		} catch (Throwable ignored) {
			// An absent or incompatible optional provider must never break quest loading.
			return null;
		}
	}

	/**
	 * Builds an item directly from a UniItems provider key.
	 */
	public @Nullable ItemStack build(@Nullable String id) {
		return build(id, null);
	}

	/**
	 * Builds an item directly from a UniItems provider key with optional player context.
	 */
	public @Nullable ItemStack build(@Nullable String id, @Nullable Player player) {
		if (id == null || id.isBlank())
			return null;

		try {
			ItemKey key = provider.normalize(ItemKey.fromString(id.trim()));
			ItemStack item = player == null ? provider.item(key) : provider.item(key, player);
			return item == null || item.getType().isAir() ? null : item;
		} catch (Throwable ignored) {
			return null;
		}
	}

	/**
	 * Rebuilds the current provider definition while retaining the configured stack amount.
	 * A clone of the saved item is returned when it is vanilla or the provider cannot resolve it.
	 */
	public @NotNull ItemStack refresh(@NotNull ItemStack savedItem) {
		return refresh(savedItem, null);
	}

	public @NotNull ItemStack refresh(@NotNull ItemStack savedItem, @Nullable Player player) {
		ItemStack fallback = savedItem.clone();
		String id = identify(savedItem);
		if (id == null)
			return fallback;

		ItemStack current = build(id, player);
		if (current == null)
			return fallback;

		current = current.clone();
		current.setAmount(savedItem.getAmount());
		return current;
	}

	public @NotNull List<ItemStack> refreshAll(@NotNull List<ItemStack> savedItems, @Nullable Player player) {
		List<ItemStack> refreshed = new ArrayList<>(savedItems.size());
		for (ItemStack item : savedItems) {
			if (item != null)
				refreshed.add(refresh(item, player));
		}
		return refreshed;
	}

	/**
	 * Compares only provider identities. Returning false lets the regular Bukkit comparisons run.
	 */
	public boolean isSimilar(@NotNull ItemStack first, @NotNull ItemStack second) {
		String firstId = identify(first);
		return firstId != null && firstId.equals(identify(second));
	}

	public @NotNull List<String> availableTypes() {
		try {
			return List.copyOf(provider.availableTypes());
		} catch (Throwable ignored) {
			return List.of();
		}
	}
}
