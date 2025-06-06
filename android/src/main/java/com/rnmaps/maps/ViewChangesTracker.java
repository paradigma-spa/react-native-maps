package com.rnmaps.maps;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;

public class ViewChangesTracker {

  private static ViewChangesTracker instance;
  private final Handler handler;
  private final LinkedList<MapMarker> markers = new LinkedList<>();
  private boolean hasScheduledFrame = false;
  private final Runnable updateRunnable;
  private final long fps = 40;

  private ViewChangesTracker() {
    handler = new Handler(Looper.getMainLooper());
    updateRunnable = new Runnable() {
      @Override
      public void run() {        
        update();

        if (!markers.isEmpty()) {
          handler.postDelayed(updateRunnable, fps);
        } else {
          hasScheduledFrame = false;
        }
      }
    };
  }

  static ViewChangesTracker getInstance() {
    if (instance == null) {
      synchronized (ViewChangesTracker.class) {
        instance = new ViewChangesTracker();
      }
    }

    return instance;
  }

  public void addMarker(MapMarker marker) {
    markers.add(marker);

    if (!hasScheduledFrame) {
      hasScheduledFrame = true;
      handler.postDelayed(updateRunnable, fps);
    }
  }

  public void removeMarker(MapMarker marker) {
    markers.remove(marker);
  }

  public boolean containsMarker(MapMarker marker) {
    return markers.contains(marker);
  }

  private final LinkedList<MapMarker> markersToRemove = new LinkedList<>();

  public void update() {
    for (MapMarker marker : markers) {
      if (!marker.updateCustomForTracking()) {
        markersToRemove.add(marker);
      }
    }

    // Remove markers that are not active anymore
    if (!markersToRemove.isEmpty()) {
      markers.removeAll(markersToRemove);
      markersToRemove.clear();
    }
  }

}
