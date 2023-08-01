package commoble.exmachina.engine.api.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.engine.ExMachinaEngine;
import commoble.exmachina.engine.Names;
import commoble.exmachina.engine.api.ExMachinaRegistries;
import commoble.exmachina.engine.api.StaticProperty;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * StaticProperty representing a constant value for all blockstates.
<pre>
{
	"type": "exmachina:constant",
	"value": 5.0
}
</pre>
 */
public record ConstantProperty(double value) implements StaticProperty
{
	public static final ResourceKey<Codec<? extends StaticProperty>> KEY = ResourceKey.create(ExMachinaRegistries.STATIC_PROPERTY_TYPE, ExMachinaEngine.exMachinaRl(Names.CONSTANT));
	public static final Codec<ConstantProperty> CODEC = Codec.DOUBLE.fieldOf("value").xmap(ConstantProperty::of, ConstantProperty::value).codec();

	private static final ConstantProperty ZERO = new ConstantProperty(0D);
	
	public static ConstantProperty zero()
	{
		return ZERO;
	}

	public static ConstantProperty of(double value)
	{
		return value == 0D ? ZERO : new ConstantProperty(value);
	}
	
	@Override
	public DataResult<BakedStaticProperty> bake(Block block)
	{
		return DataResult.success(this::getValue);
	}
	
	public double getValue(BlockState state)
	{
		return this.value;
	}
	
	@Override
	public boolean isPresent()
	{
		return this.value != 0D;
	}

	@Override
	public Codec<? extends StaticProperty> codec()
	{
		return CODEC;
	}
}
