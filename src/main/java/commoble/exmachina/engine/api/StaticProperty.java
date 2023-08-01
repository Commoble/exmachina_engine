package commoble.exmachina.engine.api;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.engine.api.content.ConstantProperty;
import commoble.exmachina.engine.util.CodecHelper;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Static Properties are assigned to blocks as part of their circuit component definition.
 * They can only vary by blockstate, but are more efficient to calculate for large circuits.
 * 
 * Subcodecs of StaticProperty can be registered to {@link ExMachinaRegistries.STATIC_PROPERTY_TYPE}.
<pre>
{
	"type": "modid:registered_type_id",
	// the rest of the subtype fields
}
</pre>

 * StaticProperties can also be inlined as a constant, e.g. "static_load": 5.0
 */
public interface StaticProperty
{
	public static final Codec<StaticProperty> CODEC = new ExtraCodecs.EitherCodec<>(
			Codec.DOUBLE.xmap(ConstantProperty::of, ConstantProperty::value),
			CodecHelper.dispatch(ExMachinaRegistries.STATIC_PROPERTY_TYPE, StaticProperty::codec))
		.xmap(
			either -> either.map(Function.identity(), Function.identity()),
			p -> p instanceof ConstantProperty c ? Either.left(c) : Either.right(p));
	
	/**
	 * @param block Block this StaticProperty is associated with.
	 * @return DataResult success holding a state-to-double function if block is valid, error result otherwise
	 */
	public abstract DataResult<BakedStaticProperty> bake(Block block);
	
	/**
	 * Returns whether blocks with this property have this property.
	 * If false, blocks with this property will be ignored when calculating the total value (saves us from having to add 100 zeros together).
	 * 
	 * Consider using {@link ConstantPropery.ZERO} instead of overriding.
	 * 
	 * @return Whether blocks with this property can provide a value to the circuit
	 */
	default boolean isPresent()
	{
		return true;
	}
	
	/**
	 * {@return Codec registered to {@link ExMachinaRegistries.STATIC_PROPERTY_TYPE}.}
	 */
	public abstract Codec<? extends StaticProperty> codec();
	
	@FunctionalInterface
	public static interface BakedStaticProperty
	{
		public static final BakedStaticProperty EMPTY = BakedStaticProperty::zero;
		
		public abstract double getValue(BlockState state);
		
		private static double zero(BlockState state)
		{
			return 0D;
		}
		
		default boolean isPresent()
		{
			return this != BakedStaticProperty.EMPTY;
		}
	}
}
