package commoble.exmachina.engine.internal.util;

import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigHelper
{
	public static <T> T register(
		final ModConfig.Type configType,
		final Function<ForgeConfigSpec.Builder, T> configFactory)
	{
		final ModLoadingContext modContext = ModLoadingContext.get();
		final org.apache.commons.lang3.tuple.Pair<T, ForgeConfigSpec> entry = new ForgeConfigSpec.Builder()
			.configure(configFactory);
		final T config = entry.getLeft();
		final ForgeConfigSpec spec = entry.getRight();
		modContext.registerConfig(configType,spec);
		
		return config;
	}
}
