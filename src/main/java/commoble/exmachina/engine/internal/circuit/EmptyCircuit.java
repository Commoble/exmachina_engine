package commoble.exmachina.engine.internal.circuit;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import commoble.exmachina.engine.api.Circuit;
import commoble.exmachina.engine.api.StateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public enum EmptyCircuit implements Circuit
{
	INSTANCE;

	@Override
	public double getPowerSuppliedTo(BlockPos pos)
	{
		return 0;
	}

	@Override
	public double getCurrent()
	{
		return 0;
	}

	@Override
	public void markNeedingDynamicUpdate()
	{
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public Map<BlockPos, Pair<BlockState, StateComponent>> components()
	{
		return Map.of();
	}
	
}