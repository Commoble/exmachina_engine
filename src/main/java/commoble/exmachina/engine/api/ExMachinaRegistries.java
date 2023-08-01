package commoble.exmachina.engine.api;

import com.mojang.serialization.Codec;

import commoble.exmachina.engine.ExMachinaEngine;
import commoble.exmachina.engine.Names;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registry ResourceKeys for Ex Machina's static registries
 */
public class ExMachinaRegistries
{
	// static registries
	public static final ResourceKey<Registry<Codec<? extends Connector>>> CONNECTOR_TYPE = ResourceKey.createRegistryKey(ExMachinaEngine.exMachinaRl(Names.CONNECTOR_TYPE));
	public static final ResourceKey<Registry<Codec<? extends StaticProperty>>> STATIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachinaEngine.exMachinaRl(Names.STATIC_PROPERTY_TYPE));
	public static final ResourceKey<Registry<Codec<? extends DynamicProperty>>> DYNAMIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachinaEngine.exMachinaRl(Names.DYNAMIC_PROPERTY_TYPE));
	
	// dynamic registries
	public static final ResourceKey<Registry<CircuitComponent>> CIRCUIT_COMPONENT = ResourceKey.createRegistryKey(ExMachinaEngine.exMachinaRl(Names.CIRCUIT_COMPONENT));
}
