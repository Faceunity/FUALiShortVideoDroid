package com.aliyun.svideo.editor.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class BasicRenderer {
    private int mProgram;

    private float[] mVertex = new float[] {
            -1, -1,
             1, -1,
            -1,  1,
             1,  1
    };

    private float[] mCoord = new float[] {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;

    public BasicRenderer()
    {
        mProgram = createProgram();
        mVertexBuffer = allocBuffer(mVertex);
        mCoordBuffer = allocBuffer(mCoord);
    }

    public void draw(int texture)
    {
//        Log.i("RACE", "draw texture " + texture);
        glUseProgram(mProgram);
        int vertexLocation = glGetAttribLocation(mProgram, "a_position");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 0, mVertexBuffer);
        int coordLocation = glGetAttribLocation(mProgram, "a_texcoord");
        glEnableVertexAttribArray(coordLocation);
        glVertexAttribPointer(coordLocation, 2, GL_FLOAT, false, 0, mCoordBuffer);
        int textureLocation = glGetUniformLocation(mProgram, "RACE_Tex0");
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(textureLocation, 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void destroy() {
        glDeleteProgram(mProgram);
    }

    private int createProgram()
    {
        mProgram = glCreateProgram();
        int vShader = compileShader(GL_VERTEX_SHADER, mVertexShader);
        int fShader = compileShader(GL_FRAGMENT_SHADER, mFragShader);
        glAttachShader(mProgram, vShader);
        glAttachShader(mProgram, fShader);
        glLinkProgram(mProgram);
        glDeleteShader(vShader);
        glDeleteShader(fShader);
        return mProgram;
    }

    private int compileShader(int type, String source)
    {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        return shader;
    }

    private void check()
    {
        for(int error; (error = glGetError()) != GL_NO_ERROR;)
        {
            Log.e("RACE", "gl error " + error);
            throw new AssertionError();
        }
    }

    private FloatBuffer allocBuffer(float[] a)
    {
        ByteBuffer mbb=ByteBuffer.allocateDirect(a.length * 4);
        mbb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer=mbb.asFloatBuffer();
        floatBuffer.put(a);
        floatBuffer.position(0);
        return floatBuffer;
    }

    private String mVertexShader = "attribute vec3 a_position;\n" +
            "    attribute vec2 a_texcoord;\n" +
            "    \n" +
            "    varying vec2 v_texcoord;\n" +
            "    void main()\n" +
            "    {\n" +
            "        gl_Position = vec4(a_position.xyz, 1.0);\n" +
            "        v_texcoord  = a_texcoord;\n" +
            "    }";
    private String mFragShader = "#ifdef GL_ES\n" +
            "    precision mediump float;\n" +
            "#endif\n" +
            "    uniform sampler2D RACE_Tex0;\n" +
            "    varying vec2 v_texcoord;\n" +
            "    void main()\n" +
            "    {\n" +
            "        gl_FragColor = texture2D(RACE_Tex0, v_texcoord);\n" +
            "    }";
}
