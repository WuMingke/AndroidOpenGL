package com.erkuai.androidopengl;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glv = (GLSurfaceView) findViewById(R.id.glv);

        //设置OpenGL版本
        glv.setEGLContextClientVersion(2);
        //设置渲染器
        glv.setRenderer(new MyRenderer());
        //设置渲染模式为连续模式（会以60fps刷新）
        glv.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


    }
}
