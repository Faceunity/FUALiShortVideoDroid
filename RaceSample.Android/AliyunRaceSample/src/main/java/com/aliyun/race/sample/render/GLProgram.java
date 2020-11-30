package com.aliyun.race.sample.render;

import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

public class GLProgram {
    private static final String TAG = "RACE";
    private int mProgram;

     void init(String vertexShaderSource, String fragmentShaderSource) {
        mProgram = glCreateProgram();
        glCheck();
        int vShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
        glAttachShader(mProgram, vShader);
        glAttachShader(mProgram, fShader);
        glLinkProgram(mProgram);
        int[] status = new int[1];
         glValidateProgram(mProgram);
         glGetProgramiv(mProgram, GL_VALIDATE_STATUS, status, 0);
         if (status[0] == 0)
         {
             String info = glGetProgramInfoLog(mProgram);
             throw new AssertionError("RACE Invalid shader program: " + info);
         }
        glDeleteShader(vShader);
        glDeleteShader(fShader);
        glCheck();
    }

     int getProgram() {
        return mProgram;
    }

    void destroy() {
         glDeleteProgram(mProgram);
         glCheck();
    }
    private int compileShader(int type, String source)
    {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        int[] status = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0)
        {
            String info = glGetShaderInfoLog(shader);
            throw new AssertionError("RACE Error compiling shader source "+ source + " error info " + info);
        }
        return shader;
    }
}
