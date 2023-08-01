package commoble.exmachina.engine.api;

import org.jetbrains.annotations.NotNull;

import commoble.exmachina.engine.api.Connector.BlockConnector;
import commoble.exmachina.engine.api.StaticProperty.BakedStaticProperty;
import commoble.exmachina.engine.api.content.NoneDynamicProperty;

public record BlockComponent(
	@NotNull BlockConnector connector,
	@NotNull BakedStaticProperty staticLoad,
	@NotNull BakedStaticProperty staticSource,
	@NotNull DynamicProperty dynamicLoad,
	@NotNull DynamicProperty dynamicSource)
{
	public static final BlockComponent EMPTY = new BlockComponent(
		BlockConnector.EMPTY,
		BakedStaticProperty.EMPTY,
		BakedStaticProperty.EMPTY,
		NoneDynamicProperty.INSTANCE,
		NoneDynamicProperty.INSTANCE);
	
	public boolean isPresent()
	{
		return this != EMPTY;
	}
}
