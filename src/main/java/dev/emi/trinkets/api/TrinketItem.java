package dev.emi.trinkets.api;

import java.util.List;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class TrinketItem extends Item implements Trinket {
	public static final DispenserBehavior TRINKET_DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos pos = pointer.getBlockPos().offset((Direction) pointer.getBlockState().get(DispenserBlock.FACING));
			List<LivingEntity> entities = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.Equipable(stack)));
			if (!entities.isEmpty()) {
				LivingEntity entity = entities.get(0);
				if (entity instanceof PlayerEntity) {
					TrinketComponent comp = TrinketsApi.getTrinketComponent((PlayerEntity)entity);
					ItemStack test = stack.copy().split(1);
					if (comp.equip(test)) {
						stack.decrement(1);
						return stack;
					}
				}
			}
			return super.dispenseSilently(pointer, stack);
		}
	};

	public TrinketItem(Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, TRINKET_DISPENSER_BEHAVIOR);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		//Overriding the use method to equip the trinket when it's used
		return Trinket.equipTrinket(player, hand);
	}
}