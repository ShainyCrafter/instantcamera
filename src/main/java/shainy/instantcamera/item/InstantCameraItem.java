package shainy.instantcamera.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import shainy.instantcamera.InstantCamera;
import shainy.instantcamera.sounds.ICSoundEvents;

public class InstantCameraItem extends Item {
    public InstantCameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.playSound(ICSoundEvents.INSTANT_CAMERA_USE.get());
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity entity, int charge) {
        if (entity instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                ItemStack paper = player.getOffhandItem();
                LABEL:
                if (!paper.is(Items.PAPER)) {
                    paper = player.getMainHandItem();
                    if (!paper.is(Items.PAPER)) {
                        Inventory inventory = player.getInventory();
                        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                            paper = inventory.getItem(slot);
                            if (paper.is(Items.PAPER)) break LABEL;
                        }

                        Vec3 motion = player.getLookAngle().normalize().scale(0.075);
                        Vec3 position = player.getEyePosition();
                        level.addParticle(ParticleTypes.SMOKE, position.x, position.y, position.z, motion.x, motion.y, motion.z);
                        player.playSound(ICSoundEvents.INSTANT_CAMERA_FAIL.get());

                        return;
                    }
                }
                paper.shrink(1);
            }

            player.playSound(ICSoundEvents.INSTANT_CAMERA_SNAP.get());
            player.getCooldowns().addCooldown(ICItems.INSTANT_CAMERA.get(), 20);

            if (level.isClientSide) InstantCamera.takePicture();
        }
    }
}
