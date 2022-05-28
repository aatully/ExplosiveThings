package me.modsmeus.explosivethings;

import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

@Mod(Explosivethings.MOD_ID)
public class Explosivethings {
    public static final String MOD_ID = "explosivethings";
    public static final int Explosiveness = 5;
    Logger logger = LogManager.getLogger(MOD_ID);

    public Explosivethings() {
        logger.info("Explosive Things Loaded!");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer()) {
            this.handleDurability(event.player.getHeldItemMainhand(), event.player.world, event.player.getPosition());
            this.handleDurability(event.player.getHeldItemOffhand(), event.player.world, event.player.getPosition());
            event.player.inventory.armorInventory.forEach((slot) -> {
                this.handleDurability(slot.getStack(), event.player.world, event.player.getPosition());
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().getHeldItemMainhand().isDamageable()) {
            int lucky = ThreadLocalRandom.current().nextInt(0, 100);
            if (lucky <= 15) {
                event.getPlayer().world.createExplosion(null, event.getPlayer().getPosX(), event.getPlayer().getPosY(), event.getPlayer().getPosZ(), Explosiveness, Explosion.Mode.DESTROY);
            }
        }
    }

    public void handleDurability(ItemStack stack, World world, BlockPos pos) {
        if (stack.getMaxDamage() > 0) {
            CompoundNBT tags = stack.getTag();
            if (tags.contains("ExplosionDuration")) {
                CompoundNBT updateTag = stack.getTag();

                int Explosive = updateTag.getInt("ExplosionDuration");
                int difference = stack.getDamage();

                updateTag.putInt("ExplosionDuration", (Explosive - difference));

                stack.setTag(updateTag);
                stack.setDamage(0);

                if ((Explosive - difference) <= 0) {
                    stack.setCount(0);
                    world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), Explosiveness, Explosion.Mode.DESTROY);
                }
            } else {
                CompoundNBT newTag = stack.getTag();

                int randomNum = ThreadLocalRandom.current().nextInt(0, stack.getItem().getMaxDamage(stack));

                newTag.putInt("ExplosionDuration", randomNum);
                stack.setTag(newTag);
            }
        }
    }
}
