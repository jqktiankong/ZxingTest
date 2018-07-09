/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final long ANIMATION_DELAY = 0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private static final int NORMAL_POINT_SIZE = 1;

    private CameraManager cameraManager;
    private final Paint paint;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    private Bitmap frameBitmap;
    private int lineY = 0;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        frameColor = resources.getColor(R.color.frame);
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;


    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 画蓝色扫描框
        paint.setStrokeWidth(NORMAL_POINT_SIZE);
        paint.setColor(frameColor);
        canvas.drawLine(frame.left, frame.top, frame.right, frame.top, paint);
        canvas.drawLine(frame.right, frame.top, frame.right, frame.bottom, paint);
        canvas.drawLine(frame.right, frame.bottom, frame.left, frame.bottom, paint);
        canvas.drawLine(frame.left, frame.bottom, frame.left, frame.top, paint);
        // 加载.9图
        frameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_scan);
        NinePatch ninePatch = new NinePatch(frameBitmap, frameBitmap.getNinePatchChunk(), null);
        RectF rectF = new RectF(frame.left, frame.top, frame.right, frame.bottom);
        ninePatch.draw(canvas, rectF);
        // 画扫描横线
        paint.setColor(frameColor);
        paint.setStrokeWidth(POINT_SIZE);
        canvas.drawLine(frame.left + 20, frame.top + lineY, frame.right - 20, frame.top + lineY, paint);
        lineY += 2;
        lineY = lineY >= frame.bottom - frame.top ? 0 : lineY;
        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
    }

    public void drawViewfinder() {
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }
}
