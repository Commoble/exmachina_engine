package commoble.exmachina.engine.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import commoble.exmachina.engine.api.Circuit;
import commoble.exmachina.engine.api.CircuitManager;
import commoble.exmachina.engine.api.StateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelCircuitManager extends SavedData implements CircuitManager
{
	public static final String ID = "exmachina/circuit_manager";
	
	private Map<BlockPos, Circuit> circuitMap = new HashMap<>();
	private final ServerLevel level;
	private int lastKnownGeneration = 0;
	
	public static LevelCircuitManager getOrCreate(ServerLevel level)
	{
		return level.getDataStorage().computeIfAbsent(tag -> new LevelCircuitManager(level), () -> new LevelCircuitManager(level), ID);
	}
	
	public LevelCircuitManager(ServerLevel level)
	{
		this.level = level;
	}
	
	@Override
	public Circuit getCircuit(BlockPos pos)
	{
		// if data has been reloaded, dump the circuit map
		int actualGeneration = ComponentBaker.get().generation();
		if (this.lastKnownGeneration != actualGeneration)
		{
			this.lastKnownGeneration = actualGeneration;
			this.circuitMap = new HashMap<>();
		}
		
		Circuit existingCircuit = this.circuitMap.getOrDefault(pos, EmptyCircuit.INSTANCE);
		if (existingCircuit.isPresent())
		{
			return existingCircuit;
		}
		else // try to build new circuit here if possible
		{
			Circuit builtCircuit = CircuitBuilder.attemptToBuildCircuitFrom(this.level, pos);
			// if we built a valid circuit, keep track of where it is
			if (builtCircuit.isPresent())
			{
				for (var posInCircuit : builtCircuit.components().keySet())
				{
					this.circuitMap.put(posInCircuit, builtCircuit);
				}
			}
			return builtCircuit;
		}
	}

	@Override
	public void onBlockUpdate(BlockState newState, BlockPos updatedPos)
	{
		List<BlockPos> positionsToRemove = new ArrayList<>(); // no two circuit instances *should* share any blockpos
		
		// first, if the block was in an extant circuit, mark the position for removal
		Circuit circuit = this.getCircuit(updatedPos);
		if (circuit.isPresent())
		{
			var components = circuit.components();
			var cachedComponent = components.get(updatedPos);
			if (cachedComponent != null && cachedComponent.getLeft() != newState)
			{
				for (BlockPos posInOldCircuit : components.keySet())
				{
					positionsToRemove.add(posInOldCircuit);
				}
			}
		}
		
		// then, if the new block is connectable, we check those connections,
		// and, for any circuit extant at those positions,
		// if the circuit
			// A) does not contain the updated position, and
			// B) has a mutual connection to the updated position from the connected position
		// then we mark the circuit for removal
			// if the circuit didn't connect to the old blockstate, and doesn't connect to the new one, we don't worry about it
			// if the circuit did connect to the old blockstate,
				// if the old blockstate changed, we handled it above
				// if the old blockstate didn't change, we ignore it and rely on the block's developer to
				// call a circuit invalidation if necessary
		StateComponent newComponent = ComponentBaker.get().getComponent(newState, this.level.registryAccess());
		if (newComponent.isPresent())
		{
			Set<BlockPos> newConnections = newComponent.connector().connectedPositions(this.level, updatedPos);
			for (BlockPos newConnectionPos : newConnections)
			{
				Circuit circuitToConnectTo = this.getCircuit(newConnectionPos);
				if (circuitToConnectTo.isPresent())
				{
					var componentsToConnectTo = circuitToConnectTo.components();
					// if updated block connects to an extant circuit that doesn't contain the updated block
					if (!componentsToConnectTo.containsKey(updatedPos))
					{
						var componentToConnectTo = componentsToConnectTo.get(newConnectionPos);
						// and if the extant circuit can mutually connect to the new block
						if (componentToConnectTo != null && componentToConnectTo.getRight().connector().connectedPositions(this.level, newConnectionPos).contains(updatedPos))
						{
							// mark the circuit for invalidation and removal from the manager
							// circuit won't be in the set of circuits to remove yet because it would have been invalidated already
							for (BlockPos pos : componentsToConnectTo.keySet())
							{
								positionsToRemove.add(pos);
							}
						}
					}
				}
			}
		}
		
		// then we remove any invalidated circuits from the map
		// (or the map will just get increasingly large until the world unloads)
		positionsToRemove.forEach(this.circuitMap::remove);
	}

	@Override
	public void invalidateCircuit(BlockPos pos)
	{
		Circuit circuit = this.circuitMap.getOrDefault(pos, Circuit.empty());
		if (circuit.isPresent())
		{
			for (BlockPos extantPos : circuit.components().keySet())
			{
				this.circuitMap.remove(extantPos);
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag)
	{
		// noop, data is transient
		return tag;
	}
}
