package shainy.instantcamera.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import shainy.instantcamera.InstantCamera;

public class ICPacketHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(InstantCamera.MODID, "channel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
        CHANNEL.registerMessage(0, TakePicturePacket.class, TakePicturePacket::encode, TakePicturePacket::new, TakePicturePacket.Handler::onMessage);
    }
}
