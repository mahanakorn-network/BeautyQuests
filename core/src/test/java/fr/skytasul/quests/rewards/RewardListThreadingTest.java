package fr.skytasul.quests.rewards;

import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static fr.skytasul.quests.test.TestUtils.waitForEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.rewards.RewardList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag("integration")
class RewardListThreadingTest {

	private ServerMock server;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		loadPlugin();
	}

	@Test
	void syncRewardAfterWaitIsQueuedOnMainThread() throws Exception {
		var joinWaiter = waitForEvent(QuesterJoinEvent.class);
		var player = server.addPlayer();
		var quester = joinWaiter.assertFired(1000).getQuester();
		var rewards = new RewardList(List.of(new WaitReward(null, 1), new XPReward(null, 7)));

		var result = rewards.giveRewards(quester);
		assertTrue(rewards.isInAsyncReward(quester));

		server.getScheduler().waitAsyncTasksFinished();
		assertEquals(0, player.getTotalExperience(), "XP reward must not run on the async wait thread");

		server.getScheduler().performOneTick();
		result.get(1, TimeUnit.SECONDS);

		assertEquals(7, player.getTotalExperience());
		assertFalse(rewards.isInAsyncReward(quester));
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

}
