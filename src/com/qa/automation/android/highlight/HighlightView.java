package com.qa.automation.android.highlight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.qa.automation.android.find.ViewFetcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Highlight view.
 */
public class HighlightView {
    private static String LOG_TAG = "HighlightView";
    private static ShapeDrawable shape = null;
    private static Activity currActivity = null;
    private static ArrayList<Activity> highlightedActivityList = new ArrayList<>();
    private static HashMap<View, Drawable> highlightedViewDrawableMap = new HashMap<>();
    private static HashMap<Activity, View> highlightedActivityViewMap = new HashMap<>();
    private static ViewFetcher viewFetcher = new ViewFetcher();

    static {
        // Create a border programmatically
        shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(15);
    }

    /**
     * Remove highlighted activity.
     *
     * @param activity the activity
     */
    public static void removeHighlightedActivity(Activity activity) {
        if (highlightedActivityList.indexOf(activity) > -1) {
            if (highlightedActivityViewMap.get(activity) != null) {
                View v = highlightedActivityViewMap.get(activity);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    v.setBackgroundDrawable(highlightedViewDrawableMap.get(v));
                } else {
                    v.setBackground(highlightedViewDrawableMap.get(v));
                }
                v.invalidate();
            }
        }
    }

    /**
     * Highlight.
     *
     * @param activity  the activity
     * @param decorView the decor view
     */
    public static void highlight(Activity activity, View decorView) {
        if (activity == null || !viewFetcher.isDecorView(decorView)) {
            return;
        }
        currActivity = activity;
        highlightedActivityList.add(currActivity);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackground(v);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        break;
                    }
                }
                return false;
            }
        };
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackground(v);
            }
        };

        ArrayList<View> allViews = new ArrayList<View>();
        try {
            viewFetcher.addChildren(allViews, (ViewGroup) decorView, false);
        } catch (Exception ignored) {
        }
        for (View view : allViews) {
            if (!(view instanceof ViewGroup)) {
                boolean flag = false;
                if (!hasTouchListener(view)) {
                    view.setOnTouchListener(touchListener);
                    flag = true;
                }
                if (!hasClickListener(view) && !flag) {
                    view.setOnClickListener(clickListener);
                }
            }
        }
    }

    private static void setBackground(View v) {
        if (v.getBackground() != shape) {
            highlightedViewDrawableMap.put(v, v.getBackground());
            if (highlightedActivityViewMap.get(currActivity) == null) {
                highlightedActivityViewMap.put(currActivity, v);
            } else {
                View preView = highlightedActivityViewMap.get(currActivity);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    preView.setBackgroundDrawable(highlightedViewDrawableMap.get(preView));
                } else {
                    preView.setBackground(highlightedViewDrawableMap.get(preView));
                }
                highlightedActivityViewMap.put(currActivity, v);
            }
            // Assign the created border to view
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                v.setBackgroundDrawable(shape);
            } else {
                v.setBackground(shape);
            }
            v.invalidate();
        }
    }

    private static boolean hasTouchListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
                if (listenerInfoObject == null) {
                    return false;
                }
            }

            // get the field mOnTouchListener, that holds the listener and cast it to a listener
            Field touchListenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnTouchListener");
            if (touchListenerField != null && listenerInfoObject != null) {
                touchListenerField.setAccessible(true);
                View.OnTouchListener touchListener = (View.OnTouchListener) touchListenerField.get(listenerInfoObject);
                return touchListener != null;
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "hasTouchListener exception:" + e.getMessage());
        }
        return true;
    }

    private static boolean hasClickListener(View view) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        Object listenerInfoObject = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
                listenerInfoObject = listenerInfoField.get(view);
                if (listenerInfoObject == null) {
                    return false;
                }
            }

            // get the field mOnClickListener, that holds the listener and cast it to a listener
            Field clickListenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnClickListener");
            if (clickListenerField != null && listenerInfoObject != null) {
                clickListenerField.setAccessible(true);
                View.OnClickListener clickListener = (View.OnClickListener) clickListenerField.get(listenerInfoObject);
                return clickListener != null;
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "hasClickListener exception:" + e.getMessage());
        }
        return true;
    }

}
