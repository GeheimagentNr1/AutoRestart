package de.geheimagentnr1.auto_restart.elements.gametests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

@EventBusSubscriber( modid = "auto_restart", bus = EventBusSubscriber.Bus.MOD )
public class AutoRestartGameTests {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(
        BuiltInRegistries.TEST_FUNCTION,
        "auto_restart"
    );

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOD_LOADS = TEST_FUNCTIONS.register(
        "mod_loads_successfully",
        () -> AutoRestartGameTests::modLoadsSuccessfully
    );

    public static void modLoadsSuccessfully( GameTestHelper helper ) {

        helper.succeed();
    }

    @SubscribeEvent
    public static void registerTests( RegisterGameTestsEvent event ) {

        event.register( MOD_LOADS.get() );
    }
}
