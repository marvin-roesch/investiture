package de.mineformers.investiture.client.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import javax.vecmath.Vector4f;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods dealing with Minecraft's and Forge's model system.
 */
public class Modeling
{
    private static final Function<ResourceLocation, TextureAtlasSprite> TEXTURE_GETTER =
        res -> res != null ? Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString())
                           : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("missingno");

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will have flipped UVs (helpful with Blender's default export options) and will contain all groups from the OBJ file.
     * There will be no texture replacement.
     *
     * @param resource the location of the model to load
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IBakedModel loadModel(ResourceLocation resource)
    {
        return loadModel(resource, ImmutableMap.of());
    }

    /**
     * Tries to load and bake an OBJ model using Forge's facilities, substituting it for the "missing" model if any errors occur.
     * The baked model will have flipped UVs (helpful with Blender's default export options) and will contain all groups from the OBJ file.
     *
     * @param resource the location of the model to load
     * @param textures a map from texture variables (starting with '#') in the model to the locations of the textures to use
     * @return the baked model if there was no error while trying to load it, substituting it with the missing model otherwise
     */
    public static IBakedModel loadModel(ResourceLocation resource, Map<String, ResourceLocation> textures)
    {
        return loadModel(resource, textures, ImmutableMap.of("flip-v", "true"));
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
    public static IBakedModel loadModel(ResourceLocation resource,
                                        Map<String, ResourceLocation> textures,
                                        List<String> visibleGroups)
    {
        return loadModel(resource, textures, visibleGroups, ImmutableMap.of("flip-v", "true"));
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
    public static IBakedModel loadModel(ResourceLocation resource,
                                        Map<String, ResourceLocation> textures,
                                        ImmutableMap<String, String> customData)
    {
        return loadModel(resource, textures, ImmutableList.of(OBJModel.Group.ALL), customData);
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
    public static IBakedModel loadModel(ResourceLocation resource,
                                        Map<String, ResourceLocation> textures,
                                        List<String> visibleGroups,
                                        ImmutableMap<String, String> customData)
    {
        try
        {
            IModel model = ModelLoaderRegistry.getModel(resource);
            if (model instanceof IRetexturableModel)
            {
                model = ((IRetexturableModel) model).retexture(FluentIterable.from(textures.keySet())
                                                                             .toMap(k -> textures.get(k).toString()));
            }
            if (model instanceof IModelCustomData)
            {
                model = ((IModelCustomData) model).process(customData);
            }
            return model.bake(new OBJModel.OBJState(visibleGroups, true), Attributes.DEFAULT_BAKED_FORMAT, TEXTURE_GETTER);
        }
        catch (Exception e)
        {
            Investiture.log().error("Failed loading OBJ model '%s'", resource.toString(), e);
        }
        return ModelLoaderRegistry.getMissingModel().bake(part -> Optional.absent(), Attributes.DEFAULT_BAKED_FORMAT, TEXTURE_GETTER);
    }

    public static BakedQuad scale(VertexFormat format, BakedQuad quad, Vec3d scale)
    {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        IVertexConsumer cons = new VertexTransformer(builder)
        {
            @Override
            public void put(int element, float... data)
            {
                VertexFormatElement el = format.getElement(element);
                switch (el.getUsage())
                {
                    case POSITION:
                        float[] newData = new float[4];
                        Vector4f vec = new Vector4f(data);
                        vec.set((float) scale.xCoord * vec.x, (float) scale.yCoord * vec.y, (float) scale.zCoord * vec.z, vec.w);
                        vec.get(newData);
                        parent.put(element, newData);
                        break;
                    default:
                        parent.put(element, data);
                        break;
                }
            }
        };
        quad.pipe(cons);
        return builder.build();
    }
}
