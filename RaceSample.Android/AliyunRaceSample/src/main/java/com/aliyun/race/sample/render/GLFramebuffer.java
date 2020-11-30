package com.aliyun.race.sample.render;

import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

public class GLFramebuffer {
    private int[] mFramebuffer = new int[1];
    private int[] mTexture = new int[1];

    public GLFramebuffer(int width, int height) {
        glGenFramebuffers(1, mFramebuffer, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer[0]);
        glGenTextures(1, mTexture, 0);
        glBindTexture(GL_TEXTURE_2D, mTexture[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        glCheck();
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mTexture[0], 0);
    }

    public int getFramebuffer() {
        return mFramebuffer[0];
    }

    public int getTexture() {
        return mTexture[0];
    }

    public void destroy() {
        if (mFramebuffer[0] > 0) {
            glDeleteFramebuffers(1, mFramebuffer, 0);
        }
        if (mTexture[0] > 0) {
            glDeleteTextures(1, mTexture, 0);
        }
    }
}
