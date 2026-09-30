package fr.skytasul.quests.stages;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

class StageCancellationPolicyTest {

	@Test
	void questProgressListenersIgnoreCancelledActions() throws Exception {
		assertIgnoresCancelled(StageBucket.class, "onBucketFill", PlayerBucketFillEvent.class);
		assertIgnoresCancelled(StageEatDrink.class, "onItemConsume", PlayerItemConsumeEvent.class);
		assertIgnoresCancelled(StageBreed.class, "onBreed", EntityBreedEvent.class);
		assertIgnoresCancelled(StageBreed.class, "onEntityChangeBlock", EntityChangeBlockEvent.class);
		assertIgnoresCancelled(StageTame.class, "onTame", EntityTameEvent.class);
		assertIgnoresCancelled(StageChat.class, "onTchat", AsyncPlayerChatEvent.class);
		assertIgnoresCancelled(StageChat.class, "onCommand", PlayerCommandPreprocessEvent.class);
		assertIgnoresCancelled(StageInteractBlock.class, "onInteract", PlayerInteractEvent.class);
		assertIgnoresCancelled(StageInteractLocation.class, "onInteract", PlayerInteractEvent.class);
		assertIgnoresCancelled(StageLocation.class, "onPlayerMove", PlayerMoveEvent.class);
	}

	private void assertIgnoresCancelled(Class<?> stageClass, String methodName, Class<? extends Event> eventClass)
			throws NoSuchMethodException {
		Method method = stageClass.getMethod(methodName, eventClass);
		EventHandler handler = method.getAnnotation(EventHandler.class);
		assertTrue(handler != null && handler.ignoreCancelled(),
				() -> stageClass.getSimpleName() + "." + methodName + " must ignore cancelled events");
	}

}
