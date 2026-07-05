package com.example.examplemod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(VampireRing.MODID)
public class VampireRing {
    public static final String MODID = "vampirering";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final DeferredHolder<Item, VampireRingItem> VAMPIRE_RING =
            ITEMS.register("vampire_ring", () -> new VampireRingItem(new Item.Properties()));

    public VampireRing(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }
                },
                VAMPIRE_RING.get()
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(VAMPIRE_RING.get());
        }
    }

    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            float damageDealt = event.getNewDamage();
            boolean hasRingEquipped = CuriosApi
                    .getCuriosInventory(player)
                    .map(handler -> handler.findFirstCurio(itemStack -> itemStack.getItem() instanceof VampireRingItem).isPresent())
                    .orElse(false);

            if (!hasRingEquipped) {
                hasRingEquipped = player.getMainHandItem().getItem() instanceof VampireRingItem ||
                        player.getOffhandItem().getItem() instanceof VampireRingItem;
            }

            if (hasRingEquipped) {
                float lifestealAmount = damageDealt * 0.3f;
                player.heal(lifestealAmount);
                LOGGER.info("Vampirerinh Lifesteal for: " + lifestealAmount);
            }
        }
    }
}