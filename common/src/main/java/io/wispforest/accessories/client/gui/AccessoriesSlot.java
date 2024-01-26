package io.wispforest.accessories.client.gui;

import com.mojang.datafixers.util.Pair;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.SlotReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AccessoriesSlot extends Slot {

    public final boolean isCosmetic;

    public final LivingEntity entity;
    public final AccessoriesContainer container;

    public AccessoriesSlot(LivingEntity entity, AccessoriesContainer container, boolean isCosmetic, int slot, int x, int y) {
        super(isCosmetic ? container.getCosmeticAccessories() : container.getAccessories(), slot, x, y);

        this.isCosmetic = isCosmetic;
        this.container = container;
        this.entity = entity;
    }

    @Override
    public void set(ItemStack stack) {
        var prevStack = this.getItem();

        super.set(stack);

        if(isCosmetic) {
            var reference = new SlotReference(container.getSlotName(), entity, getContainerSlot());

            var api = AccessoriesAPI.instance();

            api.getAccessory(prevStack)
                    .ifPresent(prevAccessory1 -> prevAccessory1.onUnequip(prevStack, reference));

            api.getAccessory(stack)
                    .ifPresent(accessory1 -> accessory1.onEquip(stack, new SlotReference(container.getSlotName(), entity, getContainerSlot())));
        }

        container.markChanged();
        container.update();
    }

    @Override
    public int getMaxStackSize() {
        // TODO: API TO LIMIT IDK
        return super.getMaxStackSize();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(isCosmetic) return true;

        return AccessoriesAPI.instance().canInsertIntoSlot(entity, new SlotReference(container.getSlotName(), entity, getContainerSlot()), stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        if(isCosmetic) return true;

        var stack = this.getItem();
        var accessory = AccessoriesAPI.instance().getAccessory(stack);

        return accessory.map(value -> value.canUnequip(stack, new SlotReference(container.getSlotName(), entity, getContainerSlot())))
                .orElseGet(() -> super.mayPickup(player));
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        var slotType = container.slotType();

        // Thanks to mojang you can not access the GUI atlas from this call and you must use Atlases from ModelManager.
        // )::::::::::::::::::::::::::::::
        return slotType.map(type -> new Pair<>(new ResourceLocation("textures/atlas/blocks.png"), type.icon()))
                .orElse(null);
    }

    @Override
    public boolean allowModification(Player player) {
        return true;
    }
}