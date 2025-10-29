package com.example.matrix_events.mvc;

import java.util.ArrayList;
import java.util.List;

public abstract class Model {
    private List<View> views = new ArrayList<>();
    public void addView(View v) {
        views.add(v);
    }
    public void removeView(View v) {
        views.remove(v);
    }
    public void notifyViews() {
        for (View v : views) {
            v.update();
        }
    }
}
