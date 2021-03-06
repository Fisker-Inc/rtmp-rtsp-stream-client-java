package com.pedro.encoder.utils.gl;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Pair;

/**
 * Created by pedro on 22/03/19.
 */

public class SizeCalculator {

  public static void calculateViewPort(boolean keepAspectRatio, int mode, int previewWidth,
      int previewHeight, int streamWidth, int streamHeight) {
    Pair<Point, Point> pair = getViewport(keepAspectRatio, mode, previewWidth, previewHeight, streamWidth, streamHeight);
    GLES20.glViewport(pair.first.x, pair.first.y, pair.second.x, pair.second.y);
  }

  public static Pair<Point, Point> getViewport(boolean keepAspectRatio, int mode, int previewWidth,
      int previewHeight, int streamWidth, int streamHeight) {
    if (keepAspectRatio) {
      if (previewWidth > previewHeight) { //landscape
        if (mode == 0 || mode == 2) { //adjust
          int realWidth = previewHeight * streamWidth / streamHeight;
          return new Pair<>(new Point((previewWidth - realWidth) / 2, 0),
              new Point(realWidth, previewHeight));
        } else { //fill
          int realHeight = previewWidth * streamHeight / streamWidth;
          int yCrop = Math.abs((realHeight - previewWidth) / 2);
          return new Pair<>(new Point(0, -yCrop), new Point(previewWidth, realHeight));
        }
      } else { //portrait
        if (mode == 0 || mode == 2) { //adjust
          int realHeight = previewWidth * streamHeight / streamWidth;
          return new Pair<>(new Point(0, (previewHeight - realHeight) / 2), new Point(previewWidth, realHeight));
        } else { //fill
          int realWidth = previewHeight * streamWidth / streamHeight;
          int xCrop = Math.abs((realWidth - previewWidth) / 2);
          return new Pair<>(new Point(-xCrop, 0), new Point(realWidth, previewHeight));
        }
      }
    } else {
      return new Pair<>(new Point(0, 0), new Point(previewWidth, previewHeight));
    }
  }

  public static void processMatrix(int rotation, int width, int height, boolean isPreview,
      boolean isPortrait, boolean flipStreamHorizontal, boolean flipStreamVertical, int mode,
      float[] MVPMatrix) {
    PointF scale;
    if (mode == 2 || mode == 3) { // stream rotation is enabled
      scale = getScale(rotation, width, height, isPortrait, isPreview);
      if (!isPreview && !isPortrait) rotation += 90;
    } else {
      scale = new PointF(1f, 1f);
    }
    if (!isPreview) {
      float xFlip = flipStreamHorizontal ? -1f : 1f;
      float yFlip = flipStreamVertical ? -1f : 1f;
      scale = new PointF(scale.x * xFlip, scale.y * yFlip);
    }
    updateMatrix(rotation, scale, MVPMatrix);
  }

  private static void updateMatrix(int rotation, PointF scale, float[] MVPMatrix) {
    Matrix.setIdentityM(MVPMatrix, 0);
    Matrix.scaleM(MVPMatrix, 0, scale.x, scale.y, 1f);
    Matrix.rotateM(MVPMatrix, 0, rotation, 0f, 0f, -1f);
  }

  private static PointF getScale(int rotation, int width, int height, boolean isPortrait,
      boolean isPreview) {
    float scaleX = 1f;
    float scaleY = 1f;
    if (!isPreview) {
      if (isPortrait && rotation != 0 && rotation != 180) { //portrait
        final float adjustedWidth = width * (width / (float) height);
        scaleY = adjustedWidth / height;
      } else if (!isPortrait && rotation != 90 && rotation != 270) { //landscape
        final float adjustedWidth = height * (height / (float) width);
        scaleX = adjustedWidth / width;
      }
    }
    return new PointF(scaleX, scaleY);
  }
}