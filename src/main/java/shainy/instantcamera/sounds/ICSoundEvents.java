package shainy.instantcamera.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shainy.instantcamera.InstantCamera;

public class ICSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, InstantCamera.MODID);

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(InstantCamera.MODID, name)));
    }

    public static final RegistryObject<SoundEvent> INSTANT_CAMERA_FAIL = registerSoundEvent("item.instant_camera.fail");
    public static final RegistryObject<SoundEvent> INSTANT_CAMERA_SNAP = registerSoundEvent("item.instant_camera.snap");
    public static final RegistryObject<SoundEvent> INSTANT_CAMERA_USE = registerSoundEvent("item.instant_camera.use");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
