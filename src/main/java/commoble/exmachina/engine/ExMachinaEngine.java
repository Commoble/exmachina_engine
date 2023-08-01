package commoble.exmachina.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.databuddy.config.ConfigHelper;
import commoble.exmachina.engine.api.CircuitComponent;
import commoble.exmachina.engine.api.CircuitManager;
import commoble.exmachina.engine.api.ExMachinaRegistries;
import commoble.exmachina.engine.api.content.AllDirectionsConnector;
import commoble.exmachina.engine.api.content.BlockStateConnector;
import commoble.exmachina.engine.api.content.BlockStateProperty;
import commoble.exmachina.engine.api.content.ConstantProperty;
import commoble.exmachina.engine.api.content.DirectionsConnector;
import commoble.exmachina.engine.api.content.NoneDynamicProperty;
import commoble.exmachina.engine.api.content.UnionConnector;
import commoble.exmachina.engine.circuit.ComponentBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

@Mod(Names.ENGINE_MODID)
public class ExMachinaEngine
{
	public static final Logger LOGGER = LogManager.getLogger();
	private static ExMachinaEngine instance;
	public static ExMachinaEngine get() { return instance; }
	
	public final CommonConfig commonConfig;
	
	// forge constructs this during modloading
	public ExMachinaEngine()
	{
		instance = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		var connectors = exMachinaDefreg(modBus, ExMachinaRegistries.CONNECTOR_TYPE);
		var staticProperties = exMachinaDefreg(modBus, ExMachinaRegistries.STATIC_PROPERTY_TYPE);
		var dynamicProperties = exMachinaDefreg(modBus, ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE);
		
		connectors.register(Names.ALL_DIRECTIONS, () -> AllDirectionsConnector.CODEC);
		connectors.register(Names.DIRECTIONS, () -> DirectionsConnector.CODEC);
		connectors.register(Names.BLOCKSTATE, () -> BlockStateConnector.CODEC);
		connectors.register(Names.UNION, () -> UnionConnector.CODEC);
		staticProperties.register(Names.CONSTANT, () -> ConstantProperty.CODEC);
		staticProperties.register(Names.BLOCKSTATE, () -> BlockStateProperty.CODEC);
		dynamicProperties.register(Names.NONE, () -> NoneDynamicProperty.CODEC);
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onRegisterDataPackRegistries);
		
		// subscribe events to forge bus -- server init and in-game events
		forgeBus.addListener(this::onServerStarting);
		forgeBus.addListener(this::onServerStopping);
		forgeBus.addListener(this::onNeighborNotify);
		
		this.commonConfig = ConfigHelper.register(ModConfig.Type.SERVER, CommonConfig::create);
	}
	
	private void onRegisterDataPackRegistries(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(ExMachinaRegistries.CIRCUIT_COMPONENT, CircuitComponent.CODEC);
	}
	
	private void onServerStarting(ServerStartingEvent event)
	{
		ComponentBaker.get().preBake(event.getServer().registryAccess());
	}
	
	private void onServerStopping(ServerStoppingEvent event)
	{
		ComponentBaker.get().clear();
	}
	
	private void onNeighborNotify(NeighborNotifyEvent event)
	{
		// called when a block update occurs at a given position (including when a blockstate change occurs at that position)
		// if the blockstate changed, the event's given state is the new blockstate
		LevelAccessor level = event.getLevel();
		
		if (level instanceof ServerLevel serverLevel)
		{
			BlockState newState = event.getState();
			BlockPos pos = event.getPos();
			CircuitManager.get(serverLevel).onBlockUpdate(newState, pos);
		}
	}
	
	public static ResourceLocation exMachinaRl(String name)
	{
		return new ResourceLocation(Names.EXMACHINA_MODID, name);
	}
	
	public static <T> DeferredRegister<T> exMachinaDefreg(IEventBus modBus, ResourceKey<Registry<T>> key)
	{
		var defreg = DeferredRegister.create(key, Names.EXMACHINA_MODID);
		defreg.makeRegistry(() -> new RegistryBuilder<T>()
			.hasTags());
		defreg.register(modBus);
		return defreg;
	}
}
