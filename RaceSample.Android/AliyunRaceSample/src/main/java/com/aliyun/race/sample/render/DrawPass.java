package com.aliyun.race.sample.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

public class DrawPass {
    private GLProgram mProgram;
    private int mBeautyIndex = 1;
    private int mFaceIndex = 1;


    private float[] mVertex = new float[] {
            -1, -1,
             1, -1,
            -1,  1,
             1,  1
    };

    private float[] mCoord = new float[] {
            0, 1,
            0, 0,
            1, 1,
            1, 0
    };


    public void setBeautyIndex(int index){
        if(mBeautyIndex == index){
          return;
        }
        mCoordBuffer = null;
        mBeautyIndex = index;
        if(mBeautyIndex == 1){
            mCoord = new float[] {
                    0, 1,
                    0, 0,
                    1, 1,
                    1, 0
            };

        } else if(mBeautyIndex == 0){
            mCoord = new float[] {
                    1, 1,
                    1, 0,
                    0, 1,
                    0, 0
            };
        }
        mCoordBuffer = allocBuffer(mCoord);
    }
    public void setFaceIndex(int index){
        if(mFaceIndex == index){
          return;
        }
        mCoordBuffer = null;
        mFaceIndex = index;
        if(mFaceIndex == 1){
            mCoord = new float[] {
                    0, 0,
                    0, 1,
                    1,0,
                    1, 1,
            };

        } else if(mFaceIndex == 0){
            mCoord = new float[] {
                    1,0,
                    1, 1,
                    0, 0,
                    0, 1,
            };
        }
        mCoordBuffer = allocBuffer(mCoord);
    }

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;

    public DrawPass()
    {
        mBeautyIndex = -1;
        mFaceIndex = -1;
        mProgram = new GLProgram();
        mProgram.init(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        mVertexBuffer = allocBuffer(mVertex);
        mCoordBuffer = allocBuffer(mCoord);
    }


    public void draw(int texture)
    {
        int program = mProgram.getProgram();
        glUseProgram(program);
        glCheck();
        int vertexLocation = glGetAttribLocation(program, "a_position");
        glCheck();
        glEnableVertexAttribArray(vertexLocation);
        glCheck();
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 0, mVertexBuffer);
        glCheck();
        int coordLocation = glGetAttribLocation(program, "a_texcoord");
        glCheck();
        glEnableVertexAttribArray(coordLocation);
        glCheck();
        glVertexAttribPointer(coordLocation, 2, GL_FLOAT, false, 0, mCoordBuffer);
        glCheck();
        int textureLocation = glGetUniformLocation(program, "RACE_Tex0");
        glCheck();
        glActiveTexture(GL_TEXTURE0);
        glCheck();
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(textureLocation, 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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

    private static final String VERTEX_SHADER_SOURCE = "attribute vec3 a_position;\n" +
            "    attribute vec2 a_texcoord;\n" +
            "    \n" +
            "    varying vec2 v_texcoord;\n" +
            "    void main()\n" +
            "    {\n" +
            "        gl_Position = vec4(a_position.xyz, 1.0);\n" +
            "        v_texcoord  = a_texcoord;\n" +
            "    }";
    private static final String FRAGMENT_SHADER_SOURCE = "#ifdef GL_ES\n" +
            "    precision mediump float;\n" +
            "#endif\n" +
            "    uniform sampler2D RACE_Tex0;\n" +
            "    varying vec2 v_texcoord;\n" +
            "    void main()\n" +
            "    {\n" +
            "        gl_FragColor = texture2D(RACE_Tex0, v_texcoord);\n" +
            "    }";
}
