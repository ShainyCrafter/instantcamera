package shainy.instantcamera;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import shainy.instantcamera.item.ICItems;
import shainy.instantcamera.network.ICPacketHandler;
import shainy.instantcamera.network.TakePicturePacket;
import shainy.instantcamera.sounds.ICSoundEvents;
import shainy.instantcamera.util.MathUtils;

@Mod(InstantCamera.MODID)
public class InstantCamera {
    public static final String MODID = "instantcamera";

    @OnlyIn(Dist.CLIENT)
    private static int takingPictures = 0;

    @OnlyIn(Dist.CLIENT)
    private static boolean hideGui;

    public InstantCamera() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::init);
        ICSoundEvents.register(modEventBus);
        ICItems.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void init(final FMLCommonSetupEvent event) {
        ICPacketHandler.init();
    }

    @OnlyIn(Dist.CLIENT)
    public static void takePicture() {
        if (takingPictures++ > 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        hideGui = minecraft.options.hideGui;
        minecraft.options.hideGui = true;
        minecraft.gameRenderer.setRenderBlockOutline(false);
        minecraft.setScreen(null);
    }

    @SubscribeEvent
    public void onRenderTickEvent(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || takingPictures <= 0) return;

        Minecraft minecraft = Minecraft.getInstance();
        byte[] picture = new byte[16384];

        try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            int width = image.getWidth();
            int height = image.getHeight();
            int size = Math.min(width, height);
            int offsetX = (width - size) / 2;
            int offsetY = (height - size) / 2;

            NativeImage resizedImage = new NativeImage(image.format(), 128, 128, true);
            image.resizeSubRectTo(offsetX, offsetY, size, size, resizedImage);

            for (int y = 0; y < 128; y++)
                for (int x = 0; x < 128; x++) {
                    int rgba = resizedImage.getPixelRGBA(x, y);

                    int red = (rgba & 0xFF0000) >> 16;
                    int green = (rgba & 0xFF00) >> 8;
                    int blue = (rgba & 0xFF);

                    double minDifference = 195076;
                    MaterialColor similarColor = MaterialColor.NONE;
                    MaterialColor.Brightness similarBrightness = MaterialColor.Brightness.NORMAL;

                    LOOP:
                    for (int c = 0; c < 64; c++) {
                        MaterialColor color = MaterialColor.byId(c);
                        if (color == MaterialColor.NONE) continue;
                        for (int b = 0; b < 4; b++) {
                            MaterialColor.Brightness brightness = MaterialColor.Brightness.byId(b);

                            int rgb = color.calculateRGBColor(brightness);
                            double difference = Math.pow(red - ((rgb & 0xFF0000) >> 16), 2) + Math.pow(green - ((rgb & 0xFF00) >> 8), 2) + Math.pow(blue - (rgb & 0xFF), 2);

                            if (difference < minDifference) {
                                minDifference = difference;
                                similarColor = color;
                                similarBrightness = brightness;
                                if (difference == 0) break LOOP;
                            }
                        }
                    }

                    picture[x + y * 128] = similarColor.getPackedId(similarBrightness);

                    int rgb = similarColor.calculateRGBColor(similarBrightness);
                    int diffRed = red - ((rgb & 0xFF0000) >> 16);
                    int diffGreen = green - ((rgb & 0xFF00) >> 8);
                    int diffBlue = blue - (rgb & 0xFF);

                    double multiplier = 0.4375;

                    if (x < 127) {
                        rgba = resizedImage.getPixelRGBA(x + 1, y);
                        resizedImage.setPixelRGBA(x + 1, y, 0xFF000000 | MathUtils.clamp(((rgba & 0xFF0000) >> 16) + (int) (diffRed * multiplier), 0, 255) << 16 | MathUtils.clamp(((rgba & 0xFF00) >> 8) + (int) (diffGreen * multiplier), 0, 255) << 8 | MathUtils.clamp((rgba & 0xFF) + (int) (diffBlue * multiplier), 0, 255));
                    }
                    if (y < 127) {
                        multiplier = 0.3125;
                        rgba = resizedImage.getPixelRGBA(x, y + 1);
                        resizedImage.setPixelRGBA(x, y + 1, 0xFF000000 | MathUtils.clamp(((rgba & 0xFF0000) >> 16) + (int) (diffRed * multiplier), 0, 255) << 16 | MathUtils.clamp(((rgba & 0xFF00) >> 8) + (int) (diffGreen * multiplier), 0, 255) << 8 | MathUtils.clamp((rgba & 0xFF) + (int) (diffBlue * multiplier), 0, 255));
                        if (x > 0) {
                            multiplier = 0.1875;
                            rgba = resizedImage.getPixelRGBA(x - 1, y + 1);
                            resizedImage.setPixelRGBA(x - 1, y + 1, 0xFF000000 | MathUtils.clamp(((rgba & 0xFF0000) >> 16) + (int) (diffRed * multiplier), 0, 255) << 16 | MathUtils.clamp(((rgba & 0xFF00) >> 8) + (int) (diffGreen * multiplier), 0, 255) << 8 | MathUtils.clamp((rgba & 0xFF) + (int) (diffBlue * multiplier), 0, 255));
                        }
                        if (x < 127) {
                            multiplier = 0.0625;
                            rgba = resizedImage.getPixelRGBA(x + 1, y + 1);
                            resizedImage.setPixelRGBA(x + 1, y + 1, 0xFF000000 | MathUtils.clamp(((rgba & 0xFF0000) >> 16) + (int) (diffRed * multiplier), 0, 255) << 16 | MathUtils.clamp(((rgba & 0xFF00) >> 8) + (int) (diffGreen * multiplier), 0, 255) << 8 | MathUtils.clamp((rgba & 0xFF) + (int) (diffBlue * multiplier), 0, 255));
                        }
                    }
                }
        } finally {
            minecraft.options.hideGui = hideGui;
            minecraft.gameRenderer.setRenderBlockOutline(true);
        }

        ICPacketHandler.CHANNEL.sendToServer(new TakePicturePacket(picture));

        takingPictures--;
    }
}