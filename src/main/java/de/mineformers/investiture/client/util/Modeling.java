package de.mineformers.investiture.client.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods dealing with Minecraft's and Forge's model system.
 */
public class Modeling
{
    private static final Function<ResourceLocation, TextureAtlasSprite> TEXTURE_GETTER = res -> Minecraft.getMinecraft().getTextureMapBlocks()
                                                                                                         .getAtlasSprite(res.toString());

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will have flipped UVs (helpful with Blender's default export options) and will contain all groups from the OBJ file.
     * There will be no texture replacement.
     *
     * @param resource the location of the model to load
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IFlexibleBakedModel loadOBJModel(ResourceLocation resource)
    {
        return loadOBJModel(resource, ImmutableMap.of());
    }

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will have flipped UVs (helpful with Blender's default export options) and will contain all groups from the OBJ file.
     *
     * @param resource the location of the model to load
     * @param textures a map from texture variables (starting with '#') in the model to the locations of the textures to use
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IFlexibleBakedModel loadOBJModel(ResourceLocation resource, Map<String, ResourceLocation> textures)
    {
        return loadOBJModel(resource, textures, ImmutableMap.of("flip-v", "true"));
    }

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will contain all groups from the OBJ file.
     *
     * @param resource      the location of the model to load
     * @param textures      a map from texture variables (starting with '#') in the model to the locations of the textures to use
     * @param visibleGroups the groups in the OBJ file to show in the baked model
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IFlexibleBakedModel loadOBJModel(ResourceLocation resource,
                                                   Map<String, ResourceLocation> textures,
                                                   List<String> visibleGroups)
    {
        return loadOBJModel(resource, textures, visibleGroups, ImmutableMap.of("flip-v", "true"));
    }

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will contain all groups from the OBJ file.
     *
     * @param resource   the location of the model to load
     * @param textures   a map from texture variables (starting with '#') in the model to the locations of the textures to use
     * @param customData the custom data to pass to the OBJ loader
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IFlexibleBakedModel loadOBJModel(ResourceLocation resource,
                                                   Map<String, ResourceLocation> textures,
                                                   ImmutableMap<String, String> customData)
    {
        return loadOBJModel(resource, textures, ImmutableList.of(OBJModel.Group.ALL), customData);
    }

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     *
     * @param resource      the location of the model to load
     * @param textures      a map from texture variables (starting with '#') in the model to the locations of the textures to use
     * @param visibleGroups the groups in the OBJ file to show in the baked model
     * @param customData    the custom data to pass to the OBJ loader
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IFlexibleBakedModel loadOBJModel(ResourceLocation resource,
                                                   Map<String, ResourceLocation> textures,
                                                   List<String> visibleGroups,
                                                   ImmutableMap<String, String> customData)
    {
        try
        {
            IModel iModel = ModelLoaderRegistry.getModel(resource);
            if (iModel instanceof OBJModel)
            {
                OBJModel obj = (OBJModel) iModel;
                IModel model = ((OBJModel) obj.retexture(FluentIterable.from(textures.keySet())
                                                                       .toMap(k -> textures.get(k).toString()))).process(customData);
                return model.bake(new OBJModel.OBJState(visibleGroups, true), Attributes.DEFAULT_BAKED_FORMAT, TEXTURE_GETTER);
            }
        }
        catch (IOException e)
        {
            Investiture.log().error("Failed loading OBJ model '%s'", resource.toString(), e);
        }
        return ModelLoaderRegistry.getMissingModel().bake(part -> Optional.absent(), Attributes.DEFAULT_BAKED_FORMAT, TEXTURE_GETTER);
    }
}
