/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder;


import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;


import com.aliyun.demo.recorder.util.gles.GlUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class OpenGLTest {
    private static final String TAG = "OpenGLTest";
    String kGPUImageTextureShaderString =
            "attribute vec4 position;\n" +
                    "attribute vec4 inputTextureCoordinate;\n" +
                    "varying vec2 texture;\n" +
                    "void main(void) {\n" +
                    "gl_Position = position;\n" +
                    "texture = inputTextureCoordinate.xy;\n" +
                    "}";

    //    String kGPUImageTextureFragmentString =
//"#extension GL_OES_EGL_image_external : require\n"+
//        "precision mediump float;" +
//        "varying vec2 textureCoordinate;\n" +
//        "uniform samplerExternalOES inputImageTexture;\n" +
//        " \n" +
//        "void main() {" +
//        "  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n" +
//        "}";
    String kGPUImageTextureFragmentString =
//            "#extension GL_OES_EGL_image_external : require\n" +
                    "varying highp vec2 texture;\n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    "void main() {\n" +
                    "highp vec4 textureColor = texture2D(inputImageTexture, texture);\n" +
                    "highp float v = textureColor.r * 0.5 + textureColor.g * 0.3 + textureColor.b * 0.4;\n" +
                    "gl_FragColor = textureColor.bgra;\n" +
                    "}";
//String kGPUImageTextureFragmentString =
//                "varying highp vec2 texture;\n" +
//                "void main() {\n" +
//                "gl_FragColor = vec4(0.2, 0.8, 0.4, 1.0);\n" +
//                "}";


    int _vertexShader;
    int _fragmentShader;

    int _positionSlot;
    int _coordsOutSlot;
    int _textureSlot;

    private boolean isInitialized = false;

    int program;
    int[] _tmp_fb = new int[1024 * 768 * 3 / 2];
    int[] _tmp_tid = new int[1];
//    int _tmp_w;
//    int _tmp_h;
    IntBuffer present_viewport = IntBuffer.allocate(4);
    IntBuffer present_fb = IntBuffer.allocate(1);


    void tmp_init(int textureWidth, int textureHeight)

    {
        GLES20.glGenFramebuffers(1, _tmp_fb, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        GLES20.glGenTextures(1, _tmp_tid, 0);
        GlUtil.checkGlError("glGenTextures");
        if (_tmp_tid[0] > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _tmp_tid[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, present_fb);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _tmp_fb[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _tmp_tid[0], 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, present_fb.get(0));
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, present_fb.get(0));
    }

//void tmp_delete()
//
//    {
//        if (GLES20.GLES20.glIsFramebuffer(_tmp_fb)) {
//            GLES20.glDeleteFramebuffers(1, & _tmp_fb);
//            _tmp_fb = 0;
//        }
//        if (GLES20.glIsTexture(_tmp_tid)) {
//            GLES20.glDeleteTextures(1, & _tmp_tid);
//            _tmp_tid = 0;
//        }
//    }



    public int renderWithTexture(int txtId, int textureWidth, int textureHeight, float[] matrix) {
        if(!isInitialized) {
            tmp_init(textureWidth, textureHeight);
            setupBuffers();
            isInitialized = true;
        }
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, present_fb);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, present_viewport);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _tmp_fb[0]);

        GLES20.glViewport(0, 0, textureWidth, textureHeight);

        GLES20.glClearColor(1, 1, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(program);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, txtId); //0x8D65


        GLES20.glUniform1i(_textureSlot, 0);


//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        float positionCoords[] = {
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
        };
        float textureCoords[] = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };

        GLES20.glEnableVertexAttribArray(_positionSlot);
        GLES20.glEnableVertexAttribArray(_coordsOutSlot);
        GLES20.glVertexAttribPointer(_positionSlot, 3, GLES20.GL_FLOAT, false, 0, bufferUtil(positionCoords));
        GLES20.glVertexAttribPointer(_coordsOutSlot, 2, GLES20.GL_FLOAT, false, 0, bufferUtil(textureCoords));
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glVertexAttribPointer(_positionSlot, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(_coordsOutSlot, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glUseProgram(0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, present_fb.get(0));
        GLES20.glViewport(present_viewport.get(0), present_viewport.get(1), present_viewport.get(2), present_viewport.get(3));


        return _tmp_tid[0];
    }

    void setupBuffers() {
        _vertexShader = compileShaderWithSource(kGPUImageTextureShaderString, GLES20.GL_VERTEX_SHADER);
        _fragmentShader = compileShaderWithSource(kGPUImageTextureFragmentString, GLES20.GL_FRAGMENT_SHADER);

        program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, _vertexShader);
        GLES20.glAttachShader(program, _fragmentShader);

        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        GLES20.glUseProgram(program);


        _positionSlot = GLES20.glGetAttribLocation(program, "position");
        _coordsOutSlot = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
        _textureSlot = GLES20.glGetUniformLocation(program, "inputImageTexture");

        GLES20.glEnableVertexAttribArray(_positionSlot);
        GLES20.glEnableVertexAttribArray(_coordsOutSlot);

        GLES20.glUseProgram(0);
//    GLES20.glUniform1i(_textureSlot, 0);
    }

    int compileShaderWithSource(String str, int shaderType)

    {


        int shader = GLES20.glCreateShader(shaderType);

        GLES20.glShaderSource(shader, str);

        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;

    }

    public Buffer bufferUtil(float[] arr) {
        FloatBuffer mBuffer;
        //先初始化buffer,数组的长度*4,因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());

        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);

        return mBuffer;
    }
}
