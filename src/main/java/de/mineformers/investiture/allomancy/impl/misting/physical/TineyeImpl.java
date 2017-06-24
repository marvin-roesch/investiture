package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.AllomancyConfig;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.physical.Tineye;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Optional;

/**
 * ${JDOC}
 */
public class TineyeImpl extends AbstractMisting implements Tineye, ITickable
{
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
        if (entity instanceof EntityPlayer && entity == Investiture.proxy.localPlayer())
        {
            prevFOV = Investiture.proxy.getFOV((EntityPlayer) entity);
            if (AllomancyConfig.mistings.tineye.fovEnabled)
            {
                Investiture.proxy.animateFOV(prevFOV + AllomancyConfig.mistings.tineye.fovIncrease, 10);
            }
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
        if (entity instanceof EntityPlayer && entity == Investiture.proxy.localPlayer())
        {
            Investiture.proxy.animateFOV(prevFOV, 10);
        }
    }

    @SideOnly(Side.CLIENT)
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
            Optional<Allomancer> allomancer = AllomancyAPIImpl.INSTANCE.toAllomancer(player)
                                                                       .filter(a -> a.activePowers().contains(Tineye.class));
            allomancer.ifPresent(a ->
                                 {
                                     if (event.getDwheel() > 0 && (Minecraft
                                         .getMinecraft().gameSettings.fovSetting == normalFOV || normalFOV == -1) && lastDirection <= 0)
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
            Optional<Allomancer> allomancer = AllomancyAPIImpl.INSTANCE.toAllomancer(player)
                                                                       .filter(a -> a.activePowers().contains(Tineye.class));
            allomancer.ifPresent(a ->
                                 {
                                     ISound previousResult = event.getResultSound();
                                     if (previousResult instanceof ITickableSound)
                                     {
                                         event.setResultSound(
                                             new ITickableSound()
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
