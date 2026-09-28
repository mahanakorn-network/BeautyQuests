package fr.skytasul.quests.editor.parsers;

import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.utils.compatibility.UniItemsHook;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Accepts either a vanilla material or a UniItems provider key. */
public final class HookedItemParser implements AbstractParser<ItemStack> {

	private final MaterialParser materialParser = new MaterialParser(true, false);

	@Override
	public @NotNull ItemStack parse(@NotNull String input) throws ParsingError {
		ItemStack hooked = UniItemsHook.get().build(input);
		if (hooked != null)
			return hooked;
		return materialParser.parse(input).parseItem();
	}

	@Override
	public @Nullable String serialize(@NotNull ItemStack value) {
		String providerId = UniItemsHook.get().identify(value);
		return providerId == null ? value.getType().name() : providerId;
	}
}
