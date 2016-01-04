package de.mineformers.investiture.client.renderer;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.Vec3;
import scala.io.Source;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.*;

/**
 * Shader
 *
 * @author PaleoCrafter
 */
public class Shader
{
    private final TObjectIntMap<String> varLocations = new TObjectIntHashMap<>();
    private int lastProgram;
    private boolean initialised;
    private boolean supported;
    private final int program;

    public Shader(String vertex, String fragment)
    {
        program = glCreateProgram();
        if (vertex != null)
            addShader(vertex, GL_VERTEX_SHADER);
        if (fragment != null)
            addShader(fragment, GL_FRAGMENT_SHADER);
    }

    public void init()
    {
        glLinkProgram(program);
        glValidateProgram(program);
        initialised = true;
        supported = glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE;
    }

    public void activate()
    {
        if (!initialised)
            init();
        if (supported)
        {
            lastProgram = glGetInteger(GL_CURRENT_PROGRAM);
            glUseProgram(program);
        }
    }

    public void deactivate()
    {
        if (!initialised)
            init();
        if (supported)
            glUseProgram(lastProgram);
    }

    private void addShader(String source, int type)
    {
        if (!initialised)
        {
            try
            {
                glAttachShader(program, createShader(source, type));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private int createShader(String source, int type)
    {
        int shader = 0;
        try
        {
            shader = glCreateShader(type);
            if (shader == 0)
                return 0;
            glShaderSource(shader, Source.fromInputStream(
                Shader.class.getResourceAsStream(source), "UTF-8").mkString());
            glCompileShader(shader);
            return shader;
        }
        catch (Exception e)
        {
            glDeleteShader(shader);
            throw e;
        }
    }

    public int getUniformLocation(String name)
    {
        if (!varLocations.containsKey(name))
            varLocations.put(name, glGetUniformLocation(program, name));
        return varLocations.get(name);
    }

    public void setUniformInt(String name, int... values)
    {
        if (supported)
        {
            int location = getUniformLocation(name);
            switch (values.length)
            {
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

    public void setUniformFloat(String name, float... values)
    {
        if (supported)
        {
            int location = getUniformLocation(name);
            switch (values.length)
            {
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

    public void setUniform(String name, Vec3 vector)
    {
        setUniformFloat(name, (float) vector.xCoord, (float) vector.yCoord, (float) vector.zCoord);
    }

    public void setUniformBool(String name, boolean value)
    {
        setUniformInt(name, value ? 1 : 0);
    }
}
