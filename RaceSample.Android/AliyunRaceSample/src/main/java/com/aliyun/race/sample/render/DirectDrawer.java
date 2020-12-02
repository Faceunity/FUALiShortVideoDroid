package com.aliyun.race.sample.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

/**
 * 不使用美颜SDK时的渲染
 * */
public class DirectDrawer {
    private final String mVertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = vPosition;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String mFragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";

    private FloatBuffer mVertexBuffer, mTextureVerticesBuffer;
    private ShortBuffer mDrawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mBeautyIndex = 1;

    private short mDrawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private static final int COORDS_PER_VERTEX = 2;

    private final int mVertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float mSquareCoords[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };

    static float mTextureVertices[] = {
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    };

    public void setBeautyIndex(int index){
        if(mBeautyIndex == index){
            return;
        }
        mBeautyIndex = index;
        if(mBeautyIndex == 1){
            mTextureVertices = new float[] {
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            };

        } else if(mBeautyIndex == 0){
            mTextureVertices = new float[] {
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
            };
        }
        mTextureVerticesBuffer = allocBuffer(mTextureVertices);
    }


    private int texture;

    public DirectDrawer()
    {

        //顶点坐标
        mVertexBuffer = allocBuffer(mSquareCoords);
        //顶点绘制顺序
        ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);
        //纹理坐标
        mTextureVerticesBuffer = allocBuffer(mTextureVertices);
        //编译着色器
        int vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        glCheck();
        GLES20.glAttachShader(mProgram, fragmentShader);
        glCheck();
        GLES20.glLinkProgram(mProgram);
        glCheck();
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

    public void draw(int texture)
    {
        this.texture = texture;
        GLES20.glUseProgram(mProgram);
        glCheck();
        //使用纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        glCheck();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        glCheck();
        //顶点位置
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glCheck();
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        glCheck();
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        glCheck();
        //纹理坐标
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        glCheck();
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        glCheck();
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mTextureVerticesBuffer);
        glCheck();
        //绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        glCheck();
        //结束
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        glCheck();
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        glCheck();
    }

    //编译着色器
    private  int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        glCheck();
        GLES20.glShaderSource(shader, shaderCode);
        glCheck();
        GLES20.glCompileShader(shader);
        glCheck();
        return shader;
    }

}
