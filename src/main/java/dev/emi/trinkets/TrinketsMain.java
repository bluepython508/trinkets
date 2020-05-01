package dev.emi.trinkets;

import dev.emi.trinkets.api.ITrinket;
import dev.emi.trinkets.api.PlayerTrinketComponent;
import dev.emi.trinkets.api.SlotGroups;
import dev.emi.trinkets.api.Slots;
import dev.emi.trinkets.api.TrinketSlots;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketInventory;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class TrinketsMain implements ModInitializer {
	public static final Identifier ITEM_EQUIPPED_CHANGE_PACKET = new Identifier("trinkets", "item_equipped_change_packet");
	@Override
	public void onInitialize() {
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(TrinketsApi.TRINKETS, new PlayerTrinketComponent(player)));
		EntityComponents.setRespawnCopyStrategy(TrinketsApi.TRINKETS, RespawnCopyStrategy.INVENTORY);
		TrinketSlots.addSlot(SlotGroups.CHEST, Slots.CAPE, new Identifier("trinkets", "textures/item/empty_trinket_slot_cape.png"), (slot, stack) -> {
			if (!(stack.getItem() instanceof ITrinket)) {
				return stack.getItem() == Items.ELYTRA;
			}
			return ((ITrinket) stack.getItem()).canWearInSlot(slot.getSlotGroup().getName(), slot.getName());
		});
		ServerSidePacketRegistry.INSTANCE.register(ITEM_EQUIPPED_CHANGE_PACKET, TrinketInventory.EQUIP_STACK_HANDLER);
		//Slots used for testing
//		TrinketSlots.addSlot("head", "mask", new Identifier("trinkets", "textures/item/empty_trinket_slot_mask.png"));
//		TrinketSlots.addSlot("chest", "necklace", new Identifier("trinkets", "textures/item/empty_trinket_slot_necklace.png"));
//		TrinketSlots.addSlot("legs", "belt", new Identifier("trinkets", "textures/item/empty_trinket_slot_belt.png"));
//		TrinketSlots.addSlot("feet", "aglet", new Identifier("trinkets", "textures/item/empty_trinket_slot_aglet.png"));
//		TrinketSlots.addSlot("hand", "ring", new Identifier("trinkets", "textures/item/empty_trinket_slot_ring.png"));
//		TrinketSlots.addSlot("hand", "gloves", new Identifier("trinkets", "textures/item/empty_trinket_slot_gloves.png"));
//		TrinketSlots.addSlot("offhand", "ring", new Identifier("trinkets", "textures/item/empty_trinket_slot_ring.png"));
	}
}