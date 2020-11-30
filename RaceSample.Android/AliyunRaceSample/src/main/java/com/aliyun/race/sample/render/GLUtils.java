package com.aliyun.race.sample.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.*;

public class GLUtils {

    public static void glCheck()
    {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
//            throw new AssertionError("RACE GL Error " + error);
        }
    }

    public static int getTextureOES() {
        int[] texture = new int[1];
        glGenTextures(1, texture, 0);
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glCheck();
        return texture[0];
    }

    public static FloatBuffer getPosition() {
        return allocateBuffer(VERTEX);
    }

    public static FloatBuffer getPositionInverted() {
         float[] position = new float[] {
                 -1.F,    1.F,
                 1.F,    1.F,
                 -1.F,   -1.F,
                 1.F,   -1.F,
        };
        return allocateBuffer(position);
    }

    public static FloatBuffer getCoordinate() {
        return allocateBuffer(TEXTURE_COORDINATE);
    }

    public static FloatBuffer allocateBuffer(float[] a)
    {
        ByteBuffer mbb=ByteBuffer.allocateDirect(a.length * 4);
        mbb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer=mbb.asFloatBuffer();
        floatBuffer.put(a);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static void viewport(int displayWidth, int displayHeight, int bufferWidth, int bufferHeight) {
        if (bufferWidth > 0 && bufferHeight > 0) {
            float ar = bufferHeight / (float) bufferWidth;
            if (displayWidth / (float) displayHeight > ar) {
                int scaleHeight = displayWidth * bufferWidth / bufferHeight;
                int offset = (displayHeight - scaleHeight) / 2;
                glViewport(0, offset, displayWidth, scaleHeight);
            } else {
                int scaleWidth = displayHeight * bufferHeight / bufferWidth;
                int offset = (displayWidth - scaleWidth) / 2;
                glViewport(offset, 0, scaleWidth, displayHeight);
            }
        } else {
            glViewport(0, 0, displayWidth, displayHeight);
        }
    }

    public static final float[] VERTEX = new float[] {
            -1, -1,
            1, -1,
            -1,  1,
            1,  1
    };

    public static final float[] TEXTURE_COORDINATE = new float[] {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
}
