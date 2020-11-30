package com.aliyun.race.sample.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

public class DrawCamera {
    private GLProgram mProgram;
    private int[] mTextureY = new int[] {0};
    private int[] mTextureCbCr = new int[] {0};
    private FloatBuffer mPosition;
    private FloatBuffer mTextureCoordinate;

    public DrawCamera() {
        mProgram = new GLProgram();
        mProgram.init(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        mPosition = GLUtils.getPositionInverted();
        mTextureCoordinate = GLUtils.getCoordinate();
    }

    public void draw(byte[] cameraNV21Byte, int width, int height) {
        if (cameraNV21Byte == null || width <= 0 || height <= 0) {
            return;
        }
        glCheck();
        ByteBuffer bufferY = ByteBuffer.allocateDirect(width * height);
        bufferY.order(ByteOrder.nativeOrder());
        bufferY.put(cameraNV21Byte, 0, width * height);
        bufferY.position(0);
        if (mTextureY[0]  <= 0) {
            glGenTextures(1, mTextureY, 0);
            glBindTexture(GL_TEXTURE_2D, mTextureY[0]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufferY);
            glCheck();
        }
        else {
            glCheck();
            glBindTexture(GL_TEXTURE_2D, mTextureY[0]);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufferY);
            glCheck();
        }
        ByteBuffer bufferCbCr = ByteBuffer.allocateDirect(width * height / 2);
        bufferCbCr.order(ByteOrder.nativeOrder());
        bufferCbCr.put(cameraNV21Byte, width * height , width * height / 2);
        bufferCbCr.position(0);
        if (mTextureCbCr[0]  <= 0) {
            glGenTextures(1, mTextureCbCr, 0);
            glBindTexture(GL_TEXTURE_2D, mTextureCbCr[0]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, bufferCbCr);
            glCheck();
        }
        else {
            glBindTexture(GL_TEXTURE_2D, mTextureCbCr[0]);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width / 2, height / 2, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, bufferCbCr);
            glCheck();
        }
        int program = mProgram.getProgram();
        glUseProgram(program);
        glCheck();
        int vertexLocation = glGetAttribLocation(program, "a_position");
        glEnableVertexAttribArray(vertexLocation);
        glCheck();
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 0, mPosition);
        glCheck();
        int coordinateLocation = glGetAttribLocation(program, "a_texcoord");
        glEnableVertexAttribArray(coordinateLocation);
        glCheck();
        glVertexAttribPointer(coordinateLocation, 2, GL_FLOAT, false, 0, mTextureCoordinate);
        int textureYLocation = glGetUniformLocation(program, "RACE_Tex0");
        glCheck();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureY[0]);
        glUniform1i(textureYLocation, 0);
        glCheck();
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, mTextureCbCr[0]);
        int textureCbCrLocation = glGetUniformLocation(program, "RACE_Tex1");
        glUniform1i(textureCbCrLocation, 1);
        glCheck();
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glCheck();
    }

    public void destroy() {
        if (mProgram != null) {
            mProgram.destroy();
        }
        if (mTextureY[0] > 0) {
            glDeleteTextures(1, mTextureY, 0);
        }
        if (mTextureCbCr[0] > 0) {
            glDeleteTextures(1, mTextureCbCr, 0);
        }
    }

    private static final String VERTEX_SHADER_SOURCE =
            "attribute vec2 a_position;\n" +
            "    attribute vec2 a_texcoord;\n" +
            "    \n" +
            "    varying vec2 v_texcoord;\n" +
            "    void main()\n" +
            "    {\n" +
            "        gl_Position = vec4(a_position.xy, 0, 1.0);\n" +
            "        v_texcoord  = a_texcoord;\n" +
            "    }";

    private static final String FRAGMENT_SHADER_SOURCE =
            "#ifdef GL_ES                                       \n" +
            "precision highp float;                             \n" +
            "#endif                                             \n" +

            "varying vec2 v_texcoord;                           \n" +
            "uniform sampler2D RACE_Tex0;                       \n" +
            "uniform sampler2D RACE_Tex1;                       \n" +

            "void main (void){                                  \n" +
            "   float r, g, b, y, u, v;                         \n" +
            "   y = texture2D(RACE_Tex0, v_texcoord).r;         \n" +
            "   u = texture2D(RACE_Tex1, v_texcoord).a - 0.5;   \n" +
            "   v = texture2D(RACE_Tex1, v_texcoord).r - 0.5;   \n" +
            "   r = y + 1.13983 * v;                            \n" +
            "   g = y - 0.39465 * u - 0.58060 * v;              \n" +
            "   b = y + 2.03211 * u;                            \n" +
            "   gl_FragColor = vec4(r, g, b, 1.0);              \n" +
            "}                                                  \n";
}
