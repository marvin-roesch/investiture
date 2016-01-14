package de.mineformers.investiture.client.renderer;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import scala.io.Source;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.*;

/**
 * Wraps OpenGL's shader system in an object oriented API.
 * A shader will only take effect when it is supported by the GPU.
 */
public class Shader
{
    private final TObjectIntMap<String> varLocations = new TObjectIntHashMap<>();
    private int lastProgram;
    private boolean initialised;
    private boolean supported;
    private final int program;

    /**
     * Create a new shader.
     * This should not be called before the OpenGL context is set up.
     * The locations of the provided shaders has to be of the format <code>domain:name</code> and they will be searched for at
     * <code>/assets/{domain}/shaders/{name}.[vert/frag]</code>.
     *
     * @param vertex   the location of the vertex shader to use in this program, null if there is no vertex shader
     * @param fragment the location of the fragment shader to use in this program, null if there is no fragment shader
     */
    public Shader(ResourceLocation vertex, ResourceLocation fragment)
    {
        program = glCreateProgram();
        if (vertex != null) {
            addShader("/assets/" + vertex.getResourceDomain() + "/shaders/" + vertex.getResourcePath() + ".vert", GL_VERTEX_SHADER);
        }
        if (fragment != null) {
            addShader("/assets/" + fragment.getResourceDomain() + "/shaders/" + fragment.getResourcePath() + ".frag", GL_FRAGMENT_SHADER);
        }
    }

    /**
     * Initialises this shader program, i.e. uploading it to the GPU and linking it.
     * Calling this method is not required, but it can save some time when it's called before any performance-dependant operation.
     */
    public void init()
    {
        glLinkProgram(program);
        glValidateProgram(program);
        initialised = true;
        // If the program was successfully compiled, we know it must be supported
        supported = glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE;
    }

    /**
     * Activates this shader program, if it is supported.
     * Callers must not take care of storing previously enabled shaders.
     */
    public void activate()
    {
        if (!initialised) init();
        if (supported) {
            lastProgram = glGetInteger(GL_CURRENT_PROGRAM);
            glUseProgram(program);
        }
    }

    /**
     * Deactivates this shader program and restores the previous one.
     */
    public void deactivate()
    {
        if (!initialised) init();
        if (supported) glUseProgram(lastProgram);
    }

    /**
     * Adds a shader to this program.
     *
     * @param source the location of the shader source file
     * @param type   the type of the shader, either {@link org.lwjgl.opengl.GL20#GL_VERTEX_SHADER GL_VERTEX_SHADER}
     *               or {@link org.lwjgl.opengl.GL20#GL_FRAGMENT_SHADER GL_FRAGMENT_SHADER}
     */
    private void addShader(String source, int type)
    {
        // Only allow this operation before the shader was initialised
        if (!initialised) {
            try {
                glAttachShader(program, createShader(source, type));
            } catch (Exception e) {
                // Catch exceptions if shaders are not supported, no way around this
                e.printStackTrace();
            }
        }
    }

    /**
     * Compiles a shader.
     *
     * @param source the location of the shader source file
     * @param type   the type of the shader, either {@link org.lwjgl.opengl.GL20#GL_VERTEX_SHADER GL_VERTEX_SHADER}
     *               or {@link org.lwjgl.opengl.GL20#GL_FRAGMENT_SHADER GL_FRAGMENT_SHADER}
     *
     * @return the ID of the shader
     */
    private int createShader(String source, int type)
    {
        int shader = 0;
        try {
            shader = glCreateShader(type);
            if (shader == 0) return 0;
            glShaderSource(shader, Source.fromInputStream(Shader.class.getResourceAsStream(source), "UTF-8").mkString());
            glCompileShader(shader);
            return shader;
        } catch (Exception e) {
            // Catch exceptions if shaders are not supported, no way around this
            glDeleteShader(shader);
            throw e;
        }
    }

    /**
     * @param name the name of the uniform to look up
     *
     * @return OpenGL's internal ID of the given uniform
     */
    public int getUniformLocation(String name)
    {
        if (!varLocations.containsKey(name)) varLocations.put(name, glGetUniformLocation(program, name));
        return varLocations.get(name);
    }

    /**
     * Sets up to 4 uniform integers with the same name declared in the shader.
     * This should also be used for setting a <code>sampler2D</code> uniform.
     *
     * @param name   the name of the uniform
     * @param values the integer values to set, the length of the vararg array should match the size of the uniform in the shader
     */
    public void setUniformInt(String name, int... values)
    {
        if (supported) {
            int location = getUniformLocation(name);
            switch (values.length) {
                case 1:
                    glUniform1i(location, values[0]);
                    break;
                case 2:
                    glUniform2i(location, values[0], values[1]);
                    break;
                case 3:
                    glUniform3i(location, values[0], values[1], values[2]);
                    break;
                case 4:
                    glUniform4i(location, values[0], values[1], values[2], values[3]);
                    break;
            }
        }
    }

    /**
     * Sets up to 4 uniform floats (including n-dimensional vectors) with the same name declared in the shader.
     *
     * @param name   the name of the uniform
     * @param values the float values to set, the length of the vararg array should match the size of the uniform in the shader
     */
    public void setUniformFloat(String name, float... values)
    {
        if (supported) {
            int location = getUniformLocation(name);
            switch (values.length) {
                case 1:
                    glUniform1f(location, values[0]);
                    break;
                case 2:
                    glUniform2f(location, values[0], values[1]);
                    break;
                case 3:
                    glUniform3f(location, values[0], values[1], values[2]);
                    break;
                case 4:
                    glUniform4f(location, values[0], values[1], values[2], values[3]);
                    break;
            }
        }
    }

    /**
     * Sets a <code>vec3</code> uniform declared in the shader with a given name.
     *
     * @param name   the name of the uniform
     * @param vector the value of the vector
     */
    public void setUniform(String name, Vec3 vector)
    {
        setUniformFloat(name, (float) vector.xCoord, (float) vector.yCoord, (float) vector.zCoord);
    }

    /**
     * Sets a <code>bool</code> uniform declared in the shader with a given name.
     *
     * @param name  the name of the uniform
     * @param value the boolean value to use
     */
    public void setUniformBool(String name, boolean value)
    {
        setUniformInt(name, value ? 1 : 0);
    }
}
