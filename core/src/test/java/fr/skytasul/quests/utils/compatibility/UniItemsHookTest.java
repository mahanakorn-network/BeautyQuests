package fr.skytasul.quests.utils.compatibility;

import io.github.projectunified.uniitem.api.ItemKey;
import io.github.projectunified.uniitem.api.ItemProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UniItemsHookTest {

	@BeforeEach
	void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void refreshesProviderItemAndPreservesConfiguredAmount() {
		ItemProvider provider = mock(ItemProvider.class);
		ItemKey key = new ItemKey("itemsadder", "example:ruby");
		ItemStack saved = new ItemStack(Material.PAPER, 7);
		ItemStack current = new ItemStack(Material.DIAMOND);

		when(provider.key(saved)).thenReturn(key);
		when(provider.normalize(key)).thenReturn(key);
		when(provider.item(key)).thenReturn(current);

		UniItemsHook hook = new UniItemsHook(provider);
		ItemStack refreshed = hook.refresh(saved);

		assertEquals(Material.DIAMOND, refreshed.getType());
		assertEquals(7, refreshed.getAmount());
		assertEquals(1, current.getAmount(), "the provider-owned template must not be mutated");
	}

	@Test
	void fallsBackToACloneWhenItemIsNotProviderBacked() {
		ItemProvider provider = mock(ItemProvider.class);
		ItemStack saved = new ItemStack(Material.STONE, 3);
		when(provider.key(saved)).thenReturn(null);

		ItemStack refreshed = new UniItemsHook(provider).refresh(saved);

		assertEquals(saved, refreshed);
		assertNotSame(saved, refreshed);
	}

	@Test
	void comparesStableProviderKeys() {
		ItemProvider provider = mock(ItemProvider.class);
		ItemStack oldDefinition = new ItemStack(Material.PAPER);
		ItemStack newDefinition = new ItemStack(Material.DIAMOND);
		ItemStack differentItem = new ItemStack(Material.EMERALD);
		ItemKey shared = new ItemKey("mmoitems", "SWORD:CUTLASS");
		ItemKey different = new ItemKey("mmoitems", "SWORD:RAPIER");

		when(provider.key(oldDefinition)).thenReturn(shared);
		when(provider.key(newDefinition)).thenReturn(shared);
		when(provider.key(differentItem)).thenReturn(different);
		when(provider.normalize(any(ItemKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UniItemsHook hook = new UniItemsHook(provider);
		assertTrue(hook.isSimilar(oldDefinition, newDefinition));
		assertFalse(hook.isSimilar(oldDefinition, differentItem));
	}
}
