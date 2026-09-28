package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.editor.parsers.HookedItemParser;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.compatibility.UniItemsHook;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

// TODO refactor to use QuestOptionItem
public class OptionQuestItem extends QuestOption<ItemStack> {

	@Override
	public void setValue(ItemStack value) {
		if (value == null || value.getType() == Material.AIR) {
			value = XMaterial.BOOK.parseItem();
		}else {
			value = ItemUtils.clearVisibleAttributes(value.clone());
		}
		super.setValue(value);
	}

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(ConfigurationSection config, String key) {
		if (config.isItemStack(key)) {
			setValue(config.getItemStack(key));
			return;
		}

		String value = config.getString(key);
		ItemStack providerItem = UniItemsHook.get().build(value);
		setValue(providerItem == null ? XMaterial.valueOf(value).parseItem() : providerItem);
	}

	@Override
	public ItemStack cloneValue(ItemStack value) {
		return value.clone();
	}

	@Override
	public @Nullable String getValueString() {
		return getValue() == null ? null : Utils.getStringFromItemStack(getValue(), "", false);
	}

	private String[] getLore() {
		String description = formatDescription(Lang.customMaterialLore.toString());
		if (!hasCustomValue()) return new String[] { description, "", Lang.defaultValue.toString() };
		return new String[] { description };
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.nameAndLore(getValue().clone(), Lang.customMaterial.toString(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		if (event.hasCursor()) {
			ItemStack item = event.getCursor();
			QuestUtils.runSync(() -> {
				setValue(item);
				event.getPlayer().setItemOnCursor(null);
			});
		} else {
			HookedItemParser parser = new HookedItemParser();
			QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(event.getPlayer(),
					parser,
					event::reopen, item -> {
						setValue(item);
						event.reopen();
					}).addReset(() -> {
						resetValue();
						event.reopen();
					}, "null")
					.setInitialString(parser.serialize(getValue()))
					.setIndication(Lang.QUEST_MATERIAL.toString())
					.build().start();
		}
	}

}
