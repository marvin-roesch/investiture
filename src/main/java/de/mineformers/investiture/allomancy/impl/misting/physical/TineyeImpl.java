package de.mineformers.investiture.allomancy.impl.misting.physical;

import com.google.common.base.Throwables;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.AllomancyConfig;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.physical.Tineye;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import de.mineformers.investiture.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * ${JDOC}
 */
public class TineyeImpl extends AbstractMisting implements Tineye, ITickable
{
    private static final MethodHandle UNDERLYING_INTEGER_MAP_GETTER;
    private static final MethodHandle REGISTRY_OBJECTS_GETTER;
    private static final MethodHandle IDENTITY_MAP_GETTER;
    private static final MethodHandle OBJECT_LIST_GETTER;
    private static final MethodHandle AVAILABILITY_MAP_GETTER;

    static
    {
        UNDERLYING_INTEGER_MAP_GETTER = Reflection.getterHandle(RegistryNamespaced.class)
                                                  .mcpName("underlyingIntegerMap")
                                                  .srgName("field_148759_a")
                                                  .build();
        REGISTRY_OBJECTS_GETTER = Reflection.getterHandle(RegistrySimple.class)
                                            .mcpName("registryObjects")
                                            .srgName("field_82596_a")
                                            .build();
        IDENTITY_MAP_GETTER = Reflection.getterHandle(ObjectIntIdentityMap.class)
                                        .mcpName("identityMap")
                                        .srgName("field_148749_a")
                                        .build();
        OBJECT_LIST_GETTER = Reflection.getterHandle(ObjectIntIdentityMap.class)
                                       .mcpName("objectList")
                                       .srgName("field_148748_b")
                                       .build();
        AVAILABILITY_MAP_GETTER = Reflection.getterHandle(FMLControlledNamespacedRegistry.class)
                                            .mcpName("availabilityMap")
                                            .build();
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static IntIdentityHashBiMap<Potion> underlyingIntegerMap()
    {
        try
        {
            return (IntIdentityHashBiMap<Potion>) UNDERLYING_INTEGER_MAP_GETTER.bindTo(GameData.getPotionRegistry()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, Potion> registryObjects()
    {
        try
        {
            return (Map<ResourceLocation, Potion>) REGISTRY_OBJECTS_GETTER.bindTo(GameData.getPotionRegistry()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static IntIdentityHashBiMap<Potion> identityMap()
    {
        try
        {
            return (IntIdentityHashBiMap<Potion>) IDENTITY_MAP_GETTER.bindTo(underlyingIntegerMap()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static List<Potion> objectList()
    {
        try
        {
            return (List<Potion>) OBJECT_LIST_GETTER.bindTo(underlyingIntegerMap()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static BitSet availabilityMap()
    {
        try
        {
            return (BitSet) AVAILABILITY_MAP_GETTER.bindTo(GameData.getPotionRegistry()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    public static void init()
    {
        Potion oldNightVision = MobEffects.NIGHT_VISION;
        registryObjects().remove(new ResourceLocation("night_vision"));
//        identityMap().remove(oldNightVision);
        objectList().remove(oldNightVision);
        objectList().add(16, null);
        availabilityMap().clear(16);
        class NightVisionWrapper extends Potion
        {
            private NightVisionWrapper()
            {
                super(oldNightVision.isBadEffect(), oldNightVision.getLiquidColor());
                setPotionName(oldNightVision.getName());
                int x = oldNightVision.getStatusIconIndex() & 0b111;
                int y = oldNightVision.getStatusIconIndex() >> 3;
                setIconIndex(x, y);
            }

            @Override
            public void performEffect(EntityLivingBase entity, int amplifier)
            {
                oldNightVision.performEffect(entity, amplifier);
            }

            @Override
            public void affectEntity(Entity throwable, Entity thrower, EntityLivingBase target, int amplifier, double strength)
            {
                oldNightVision.affectEntity(throwable, thrower, target, amplifier, strength);
            }

            @Override
            public boolean isInstant()
            {
                return oldNightVision.isInstant();
            }

            @Override
            public boolean isReady(int duration, int amplifier)
            {
                return oldNightVision.isReady(duration, amplifier);
            }

            @Override
            public boolean shouldRender(PotionEffect effect)
            {
                EntityPlayer player = Investiture.proxy.localPlayer();
                if (player == null)
                    return oldNightVision.shouldRender(effect);
                boolean active = AllomancyAPIImpl.INSTANCE.toAllomancer(player).map(a -> a.activePowers().contains(Tineye.class)).orElse(false);
                return !active && oldNightVision.shouldRender(effect);
            }

            @Override
            public boolean shouldRenderInvText(PotionEffect effect)
            {
                return oldNightVision.shouldRenderInvText(effect);
            }

            @Override
            @SideOnly(Side.CLIENT)
            public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc)
            {
                oldNightVision.renderInventoryEffect(x, y, effect, mc);
            }
        }
        Reflection.setFinalField(MobEffects.class, null, "nightVision", "field_76439_r", new NightVisionWrapper());
    }

    @Inject
    private Entity entity;
    private float prevFOV;

    @Override
    public void startBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            living.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Short.MAX_VALUE, 0, false, false));
        }
        if (AllomancyConfig.mistings.tineye.fovEnabled && entity instanceof EntityPlayer && entity == Investiture.proxy.localPlayer())
        {
            prevFOV = Investiture.proxy.getFOV((EntityPlayer) entity);
            Investiture.proxy.animateFOV(prevFOV + AllomancyConfig.mistings.tineye.fovIncrease, 10);
        }
    }

    @Override
    public void update()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (!living.isPotionActive(MobEffects.NIGHT_VISION))
            {
                living.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Short.MAX_VALUE, 0, false, false));
            }
        }
    }

    @Override
    public void stopBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            living.removePotionEffect(MobEffects.NIGHT_VISION);
        }
        if (AllomancyConfig.mistings.tineye.fovEnabled && entity instanceof EntityPlayer && entity == Investiture.proxy.localPlayer())
        {
            Investiture.proxy.animateFOV(prevFOV, 10);
        }
    }

    public static class EventHandler
    {
        private float normalFOV = -1;
        private int lastDirection = 0;

        @SubscribeEvent
        public void onMouse(MouseEvent event)
        {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null || !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(player).filter(a -> a.activePowers().contains(Tineye.class)).ifPresent(a -> {
                if (event.getDwheel() > 0 && (Minecraft.getMinecraft().gameSettings.fovSetting == normalFOV || normalFOV == -1)  && lastDirection <= 0)
                {
                    normalFOV = Investiture.proxy.getFOV(player);
                    Investiture.proxy.animateFOV(AllomancyConfig.mistings.tineye.fovZoom, 10);
                    lastDirection = 1;
                    event.setCanceled(true);
                }
                else if (event.getDwheel() < 0 && normalFOV != -1 && lastDirection >= 0)
                {
                    Investiture.proxy.animateFOV(normalFOV, 10);
                    normalFOV = -1;
                    lastDirection = -1;
                    event.setCanceled(true);
                }
            });
        }

        @SubscribeEvent
        public void onSound(PlaySoundEvent event)
        {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null)
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(player).filter(a -> a.activePowers().contains(Tineye.class)).ifPresent(a -> {
                ISound previousResult = event.getResultSound();
                if (previousResult instanceof ITickableSound)
                {
                    event.setResultSound(new ITickableSound()
                    {
                        @Override
                        public boolean isDonePlaying()
                        {
                            return ((ITickableSound) previousResult).isDonePlaying();
                        }

                        @Override
                        public void update()
                        {
                            ((ITickableSound) previousResult).update();
                        }

                        @Override
                        public ResourceLocation getSoundLocation()
                        {
                            return previousResult.getSoundLocation();
                        }

                        @Override
                        public boolean canRepeat()
                        {
                            return previousResult.canRepeat();
                        }

                        @Override
                        public int getRepeatDelay()
                        {
                            return previousResult.getRepeatDelay();
                        }

                        @Override
                        public float getVolume()
                        {
                            return previousResult.getVolume() * 10f;
                        }

                        @Override
                        public float getPitch()
                        {
                            return previousResult.getPitch();
                        }

                        @Override
                        public float getXPosF()
                        {
                            return previousResult.getXPosF();
                        }

                        @Override
                        public float getYPosF()
                        {
                            return previousResult.getYPosF();
                        }

                        @Override
                        public float getZPosF()
                        {
                            return previousResult.getZPosF();
                        }

                        @Override
                        public AttenuationType getAttenuationType()
                        {
                            return previousResult.getAttenuationType();
                        }

                        @Override
                        public SoundEventAccessor createAccessor(SoundHandler soundHandler)
                        {
                            return previousResult.createAccessor(soundHandler);
                        }

                        @Override
                        public Sound getSound()
                        {
                            return previousResult.getSound();
                        }

                        @Override
                        public SoundCategory getCategory()
                        {
                            return previousResult.getCategory();
                        }
                    });
                }
                else
                {
                    event.setResultSound(new ISound()
                    {
                        @Override
                        public ResourceLocation getSoundLocation()
                        {
                            return previousResult.getSoundLocation();
                        }

                        @Override
                        public boolean canRepeat()
                        {
                            return previousResult.canRepeat();
                        }

                        @Override
                        public int getRepeatDelay()
                        {
                            return previousResult.getRepeatDelay();
                        }

                        @Override
                        public float getVolume()
                        {
                            return previousResult.getVolume() * 10f;
                        }

                        @Override
                        public float getPitch()
                        {
                            return previousResult.getPitch();
                        }

                        @Override
                        public float getXPosF()
                        {
                            return previousResult.getXPosF();
                        }

                        @Override
                        public float getYPosF()
                        {
                            return previousResult.getYPosF();
                        }

                        @Override
                        public float getZPosF()
                        {
                            return previousResult.getZPosF();
                        }

                        @Override
                        public AttenuationType getAttenuationType()
                        {
                            return previousResult.getAttenuationType();
                        }

                        @Override
                        public SoundEventAccessor createAccessor(SoundHandler soundHandler)
                        {
                            return previousResult.createAccessor(soundHandler);
                        }

                        @Override
                        public Sound getSound()
                        {
                            return previousResult.getSound();
                        }

                        @Override
                        public SoundCategory getCategory()
                        {
                            return previousResult.getCategory();
                        }
                    });
                }
            });
        }
    }
}
