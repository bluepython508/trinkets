package dev.emi.trinkets.api;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

/**
 * Trinkets should extend this interface to be usable in trinket slots
 */
public interface ITrinket {
	
	/**
	 * @return Whether the provided slot is valid for this item
	 */
	public boolean canWearInSlot(String group, String slot);

	/**
	 * Called once per tick while being worn by a player
	 */
	public default void tick(PlayerEntity player, ItemStack stack) {
	}

	/**
	 * @return Whether the itemstack can be inserted into a slot
	 */
	public default boolean canInsert(ItemStack stack) {
		return true;
	}

	/**
	 * @return Whether the itemstack can be removed from a slot
	 */
	public default boolean canTake(ItemStack stack) {
		return true;
	}

	/**
	 * Called when equipped by a player (Only called on client side)
	 */
	public default void onEquip(PlayerEntity player, ItemStack stack) {
	}

	/**
	 * Called when unequipped by a player (Only called on client side)
	 */
	public default void onUnequip(PlayerEntity player, ItemStack stack) {
	}

	/**
	 * Called when equipped by a player (Only called on server side)
	 */
	public default void onEquipServer(PlayerEntity player, ItemStack stack) {
	}

	/**
	 * Called when unequipped by a player (Only called on server side)
	 */
	public default void onUnequipServer(PlayerEntity player, ItemStack stack) {
	}

	/**
	 * Called on equip and unequip to get modifiers provided by the trinket in specific slots
	 * @param group	Slot group to get modifiers for
	 * @param slot	Slot to get modifiers for
	 * @param uuid	UUID Trinkets should use for the provide slot when constructing EAMs
	 * @param stack	Item stack to get modifiers for
	 */
	public default Multimap<String, EntityAttributeModifier> getTrinketModifiers(String group, String slot, UUID uuid, ItemStack stack){
		return HashMultimap.create();
	}

	/**
	 * Called to render the trinket
	 * @param slot The {@code group:slot} structured slot the trinket is being rendered in
	 * @see {@link #translateToFace(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 * @see {@link #translateToChest(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 * @see {@link #translateToRightArm(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 * @see {@link #translateToLeftArm(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 * @see {@link #translateToRightLeg(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 * @see {@link #translateToLeftLeg(MatrixStack, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public default void render(String slot, MatrixStack matrixStack, VertexConsumerProvider vertexConsumer, int light, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
	}
	
	//Helper stuff for creating trinkets that interact with vanilla behavior properly
	public static final DispenserBehavior TRINKET_DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			ItemStack newStack = dispenseTrinket(pointer, stack);
			return newStack.isEmpty() ? super.dispenseSilently(pointer, stack) : newStack;
		}
	};

	public static ItemStack dispenseTrinket(BlockPointer pointer, ItemStack stack) {
		BlockPos pos = pointer.getBlockPos().offset((Direction) pointer.getBlockState().get(DispenserBlock.FACING));
		List<LivingEntity> entities = pointer.getWorld().getEntities(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.CanPickup(stack)));
		if (entities.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			LivingEntity entity = (LivingEntity) entities.get(0);
			if(entity instanceof PlayerEntity) {
				TrinketComponent comp = TrinketsApi.getTrinketComponent((PlayerEntity) entity);
				if(comp.equip(stack)) {
					stack.setCount(0);
				}
			}
			return stack;
		}
	}

	public static TypedActionResult<ItemStack> equipTrinket(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		TrinketComponent comp = TrinketsApi.getTrinketComponent(player);
		if (comp.equip(stack)) {
			stack.setCount(0);
			return new TypedActionResult<ItemStack>(ActionResult.SUCCESS, stack);
		} else {
			return new TypedActionResult<ItemStack>(ActionResult.FAIL, stack);
		}
	}

	//Helper stuff for rendering
	/**
	 * Translates the rendering context to the center of the player's face, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToFace(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSwimmingPose() || player.isFallFlying()) {
			matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(model.head.roll));
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(headYaw));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-45.0F));
		} else {
			if (player.isInSneakingPose() && !model.riding) {
				matrixStack.translate(0.0F, 0.25F, 0.0F);
			}
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(headYaw));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(headPitch));
		}
		matrixStack.translate(0.0F, -0.25F, -0.3F);
	}
	/**
	 * Translates the rendering context to the center of the player's chest/torso segment, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToChest(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSneakingPose() && !model.riding && !player.isSwimming()) {
			matrixStack.translate(0.0F, 0.2F, 0.0F);
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(model.torso.pitch * 57.5F));
		}
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.torso.yaw * 57.5F));
		matrixStack.translate(0.0F, 0.4F, -0.16F);
	}
	/**
	 * Translates the rendering context to the center of the bottom of the player's right arm, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToRightArm(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSneakingPose() && !model.riding && !player.isSwimming()) {
			matrixStack.translate(0.0F, 0.2F, 0.0F);
		}
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.torso.yaw * 57.5F));
		matrixStack.translate(-0.3125F, 0.15625F, 0.0F);
		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(model.rightArm.roll * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.rightArm.yaw * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(model.rightArm.pitch * 57.5F));
		matrixStack.translate(-0.0625F, 0.625F, 0.0F);
	}
	/**
	 * Translates the rendering context to the center of the bottom of the player's left arm, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToLeftArm(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSneakingPose() && !model.riding && !player.isSwimming()) {
			matrixStack.translate(0.0F, 0.2F, 0.0F);
		}
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.torso.yaw * 57.5F));
		matrixStack.translate(0.3125F, 0.15625F, 0.0F);
		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(model.leftArm.roll * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.leftArm.yaw * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(model.leftArm.pitch * 57.5F));
		matrixStack.translate(0.0625F, 0.625F, 0.0F);
	}
	/**
	 * Translates the rendering context to the center of the bottom of the player's right leg, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToRightLeg(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSneakingPose() && !model.riding && !player.isSwimming()) {
			matrixStack.translate(0.0F, 0.0F, 0.25F);
		}
		matrixStack.translate(-0.125F, 0.75F, 0.0F);
		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(model.rightLeg.roll * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.rightLeg.yaw * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(model.rightLeg.pitch * 57.5F));
		matrixStack.translate(0.0F, 0.75F, 0.0F);
	}
	/**
	 * Translates the rendering context to the center of the bottom of the player's left leg, parameters should be passed from {@link #render(String, MatrixStack, VertexConsumerProvider, int, PlayerEntityModel, AbstractClientPlayerEntity, float, float)}
	 */
	public static void translateToLeftLeg(MatrixStack matrixStack, PlayerEntityModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player, float headYaw, float headPitch) {
		if (player.isInSneakingPose() && !model.riding && !player.isSwimming()) {
			matrixStack.translate(0.0F, 0.0F, 0.25F);
		}
		matrixStack.translate(0.125F, 0.75F, 0.0F);
		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(model.leftLeg.roll * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(model.leftLeg.yaw * 57.5F));
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(model.leftLeg.pitch * 57.5F));
		matrixStack.translate(0.0F, 0.75F, 0.0F);
	}
}