package shainy.instantcamera.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TakePicturePacket {
    public byte[] data;

    public TakePicturePacket(byte[] data) {
        this.data = data;
    }

    public TakePicturePacket(FriendlyByteBuf buffer) {
        this.data = buffer.readByteArray();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByteArray(this.data);
    }

    static class Handler {
        public static void onMessage(TakePicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender != null) {
                    Vec3 position = sender.position();
                    MapItemSavedData data = MapItemSavedData.createFresh(position.x, position.z, (byte) 0, false, false, sender.level.dimension()).locked();
                    data.colors = packet.data;
                    int id = sender.level.getFreeMapId();
                    sender.level.setMapData(MapItem.makeKey(id), data);

                    ItemStack item = new ItemStack(Items.FILLED_MAP);
                    item.getOrCreateTag().putInt("map", id);
                    sender.addItem(item);

                    if (!item.isEmpty()) {
                        sender.level.addFreshEntity(new ItemEntity(sender.level, position.x, position.y, position.z, item));
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
