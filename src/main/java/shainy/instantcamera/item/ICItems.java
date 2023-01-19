package shainy.instantcamera.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shainy.instantcamera.InstantCamera;

public class ICItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, InstantCamera.MODID);
    public static final RegistryObject<Item> INSTANT_CAMERA = ITEMS.register("instant_camera", () -> new InstantCameraItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
