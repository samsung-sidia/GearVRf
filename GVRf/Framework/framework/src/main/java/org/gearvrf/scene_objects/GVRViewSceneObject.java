/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.scene_objects;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.gearvrf.GVRApplication;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.ITouchEvents;
import org.gearvrf.IViewEvents;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.shaders.GVROESConvolutionShader;
import org.gearvrf.utility.Log;

/**
 * This class represents a {@linkplain GVRSceneObject Scene object} that shows a {@link View}
 * into the scene with an arbitrarily complex geometry.
 */
public class GVRViewSceneObject extends GVRSceneObject {
    private View mView;
    private final RootViewGroup mRootViewGroup;
    private final IViewEvents mEventsListener;
    private GestureDetector mGestureDetector = null;
    private final Object mLock;

    /**
     * Create a new {@link GVRViewSceneObject} instance with its corresponding {@link View}
     * inflated from specified xml resource.
     * Android {@link View} will be rendered as {@link GVRTexture} of a default
     * {@linkplain GVRMesh quad} created internally based on width and height of the {@link View}:
     *     GVRMesh.createQuad(getWidth() / Math.max(getWidth(), getHeight()),
     *                           getHeight() / Math.max(getWidth(), getHeight()));
     *
     * Android {@link View} must be inflated at UI thread so
     * see {@link #GVRViewSceneObject(GVRContext, int, IViewEvents)} whether you want
     * to be notified or {@link GVRViewSceneObject#waitFor()} to wait for the instance be ready.
     *
     * See {@link GVRMesh#createQuad(float, float)}, {@link IViewEvents}
     *     and {@link GVRViewSceneObject#waitFor()}
     *
     * @param gvrContext current {@link GVRContext}.
     * @param viewId The resource ID to be inflated. See {@link LayoutInflater}.
     */
    public GVRViewSceneObject(GVRContext gvrContext, int viewId) {
        this(gvrContext, null, viewId, null, null);
    }

    /**
     * Create a new {@link GVRViewSceneObject} instance with its corresponding {@link View}.
     * Android {@link View} will be rendered as {@link GVRTexture} of a default
     * {@linkplain GVRMesh quad} created internally based on width and height of the {@link View}:
     *     GVRMesh.createQuad(getWidth() / Math.max(getWidth(), getHeight()),
     *                           getHeight() / Math.max(getWidth(), getHeight()));
     *
     * Android {@link View} must be handled at UI thread so
     * see {@link #GVRViewSceneObject(GVRContext, int, IViewEvents)} whether you want
     * to be notified or {@link GVRViewSceneObject#waitFor()} to wait for the instance be ready.
     *
     * See {@link GVRMesh#createQuad(float, float)}, {@link IViewEvents}
     *     and {@link GVRViewSceneObject#waitFor()}
     *
     * @param gvrContext current {@link GVRContext}.
     * @param view The {@link View} to be shown.
     */
    public GVRViewSceneObject(GVRContext gvrContext, View view) {
        this(gvrContext, view, View.NO_ID, null, null);
    }

    /**
     * Create a new {@link GVRViewSceneObject} instance with its corresponding {@link View}
     * inflated from specified xml resource.
     * Android {@link View} will be rendered as {@link GVRTexture} of an arbitrarily complex geometry.
     *
     * @param gvrContext current {@link GVRContext}.
     * @param viewId The resource ID to inflate. See {@link LayoutInflater}.
     * @param mesh a {@link GVRMesh} - see
     *            {@link GVRContext#getAssetLoader()#loadMesh(org.gearvrf.GVRAndroidResource)} and
     *            {@link GVRMesh#createQuad(float, float)}
     */
    public GVRViewSceneObject(GVRContext gvrContext, int viewId, GVRMesh mesh) {
        this(gvrContext, null, viewId, null, mesh);
    }

    /**
     * Create a new {@link GVRViewSceneObject} instance with its corresponding {@link View}
     * inflated from specified xml resource and notifies its listener when the instance
     * has been ready.
     *
     * @param gvrContext current {@link GVRContext}.
     * @param viewId The resource ID to inflate. See {@link LayoutInflater}.
     * @param eventsListener Listener to be notified after the view has been inflated.
     */
    public GVRViewSceneObject(GVRContext gvrContext, int viewId, IViewEvents eventsListener) {
        this(gvrContext, null, viewId, eventsListener, null);
    }

    /**
     * Create a new {@link GVRViewSceneObject} instance with its corresponding {@link View}
     * inflated from specified xml resource with an arbitrarily complex geometry and
     * notifies its listener when the instance has been ready.
     *
     * @param gvrContext current {@link GVRContext}.
     * @param viewId The resource ID to inflate. See {@link LayoutInflater}.
     * @param eventsListener Listener to be notified after the view has been inflated.
     * @param mesh a {@link GVRMesh} - see
     *            {@link GVRContext#getAssetLoader()#loadMesh(org.gearvrf.GVRAndroidResource)} and
     *            {@link GVRContext#createQuad(float, float)}
     */
    public GVRViewSceneObject(GVRContext gvrContext, int viewId,
                                   IViewEvents eventsListener, GVRMesh mesh) {
        this(gvrContext, null, viewId, eventsListener, mesh);
    }

    /**
     * Shows {@link View} in a 2D, rectangular {@linkplain GVRViewSceneObject scene object.}
     *
     * @param gvrContext current {@link GVRContext}
     * @param view The {@link View} to be shown.
     * @param width the rectangle's width
     * @param height the rectangle's height
     */
    public GVRViewSceneObject(GVRContext gvrContext, View view,
                              float width, float height) {
        this(gvrContext, view,
                GVRMesh.createQuad(gvrContext, "float3 a_position float2 a_texcoord",
                        width, height));
    }

    /**
     * Shows any {@link View} into the {@linkplain GVRViewSceneObject scene object} with an
     * arbitrarily complex geometry.
     * 
     * @param gvrContext current {@link GVRContext}
     * @param view The {@link View} to be shown.
     * @param mesh a {@link GVRMesh} - see
     *            {@link GVRContext#getAssetLoader()#loadMesh(org.gearvrf.GVRAndroidResource)} and
     *            {@link GVRContext#createQuad(float, float)}
     */
    public GVRViewSceneObject(GVRContext gvrContext, View view, GVRMesh mesh) {
        this(gvrContext, view, View.NO_ID, null, mesh);
    }

    private GVRViewSceneObject(GVRContext gvrContext, final View view, final int viewId,
                               IViewEvents eventsListener, GVRMesh mesh) {
        super(gvrContext, mesh);
        final GVRApplication application = gvrContext.getApplication();

        mEventsListener = eventsListener;
        mLock = new Object();

        mRootViewGroup = new RootViewGroup(application, this);

        application.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (viewId != View.NO_ID) {
                    addView(application, viewId);
                } else {
                    addView(application, view);
                }
            }
        });
    }

    // UI thread
    private void addView(GVRApplication application, int viewId) {
        addView(application, View.inflate(application.getActivity(), viewId, null));
    }

    // UI thread
    private void addView(GVRApplication application, View view) {
        if (view == null) {
            throw new IllegalArgumentException("Android view cannot be null.");
        } else if (view.getParent() != null) {
            // To keep compatibility with GVRView
            ((ViewGroup)view.getParent()).removeView(view);
        }

        mView = view;

        mRootViewGroup.addView(mView);
        mRootViewGroup.startRendering();
        getEventReceiver().addListener(mRootViewGroup);

        // To fix invalidate issue at S6/Note5
        application.getFullScreenView().invalidate();
    }

    public RootViewGroup getRootView() { return mRootViewGroup; }

    public void setGestureDetector(GestureDetector gestureDetector)
    {
        mGestureDetector = gestureDetector;
    }

    GestureDetector getGestureDetector() { return mGestureDetector; }

    @Override
    protected void onNewParentObject(GVRSceneObject parent) {
        super.onNewParentObject(parent);
        final Activity activity = getGVRContext().getActivity();

        synchronized (mLock) {
            if (getRenderData() != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Do call View#onDraw().
                        mRootViewGroup.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    protected void onRemoveParentObject(GVRSceneObject parent) {
        super.onRemoveParentObject(parent);
        final Activity activity = getGVRContext().getActivity();

        synchronized (mLock) {
            if (getRenderData() != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Don't call View#onDraw() anymore.
                        mRootViewGroup.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    /**
     * @return The instance of inflated view
     */
    public View getView() {
        return mView;
    }

    /**
     * @see {@link View#postInvalidate()}
     */
    public void invalidate() {
        mRootViewGroup.postInvalidate();
    }

    /**
     * @see {@link View#findFocus()}
     */
    public View findFocus() {
        return mRootViewGroup.findFocus();
    }

    /**
     * Set the focused child view by its id
     * @param id id of child view
     */
    public void setFocusedView(int id) {
        mRootViewGroup.setCurrentFocusedView(mRootViewGroup.findViewById(id));
    }

    /**
     * @see {@link View#requestFocus()}
     */
    public void setFocusedView(View view) {
        mRootViewGroup.setCurrentFocusedView(view);
    }

    /**
     * @see {@link View#findViewById(int)}
     */
    public View findViewById(int id) {
        return mRootViewGroup.findViewById(id);
    }

    /**
     * @see {@link View#findViewWithTag(Object)}
     */
    public View findViewWithTag(Object tag) {
        return mRootViewGroup.findViewWithTag(tag);
    }

    /**
     * Will lock current thread until the {@link GVRRenderData} of this instance has been created.
     * {@link GVRViewSceneObject} instance will be ready after the corresponding
     * {@link View} has been inflated/configured at UI thread.
     * Don't call this at UI thread.
     */
    public void waitFor() {
        // Skip for UI thread because the Android View already will be handled by that.
        if (Looper.getMainLooper() == Looper.myLooper()) {
            // Going to deadlock!
            throw new UnsupportedOperationException("GVRSVewSceneObject#waitFor() cannot lock UI thread.");
        }

        synchronized (mLock) {
            if (getRenderData() == null) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Set the default size of the texture buffers. You can call this to reduce the buffer size
     * of views with anti-aliasing issue. The default size is 512px and the maximum value
     * should be 1024px to avoid anti-aliasing issues.
     *
     * @param size buffer size.
     */
    public void setTextureBufferSize(int size) {
        synchronized (mLock) {
            mRootViewGroup.setTextureBufferSize(size);
        }
    }

    /**
     * To set initial properties before attach the view to the View three.
     * Called at UI thread.
     */
    protected void onInitView() {
        if (mEventsListener != null) {
            mEventsListener.onInitView(this, mView);
        }
    }

    /**
     * To set initial properties before start rendering {@link GVRViewSceneObject};
     * Called at Framework thread.
     */
    protected void onStartRendering() {
        if (mEventsListener != null) {
            mEventsListener.onStartRendering(this, mView);
        }
    }

    private static class SoftInputController extends Handler implements ActionMode.Callback,
            TextView.OnEditorActionListener, View.OnTouchListener {
        final static int max_timeout = 2000;
        Activity mActivity;
        GVRViewSceneObject mSceneObject;
        long mLastUpTime = 0;

        public SoftInputController(Activity activity, GVRViewSceneObject sceneObject) {
            super(activity.getMainLooper());
            mActivity = activity;
            mSceneObject = sceneObject;
        }

        public void startListener(View view) {
            view.setOnTouchListener(this);

            if (view instanceof TextView) {
                TextView tv  = (TextView) view;
                tv.setLongClickable(false);
                tv.setTextIsSelectable(false);
                tv.setOnEditorActionListener(this);
                tv.setCustomSelectionActionModeCallback(this);
            }

            // TODO: Fix WebView
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            View view = (View) msg.obj;
            InputMethodManager keyboard = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (!keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0)
                 && msg.what * msg.arg1 <= max_timeout) {
                Message msg2 = new Message();
                msg2.obj = view;
                msg2.what = msg.what++;
                msg2.arg1 = 10;

                sendMessageDelayed(msg2, msg2.arg1);
            } else {
                removeMessages(msg.what);
                Log.d(mSceneObject.getClass().getSimpleName(), "hideSoftInputFromWindow done by "
                        + view.toString());
            }
        }

        public void hideSoftInput(View view, int delay) {
            Message msg = new Message();
            msg.obj = view;
            msg.what = 0;
            msg.arg1 = delay;

            sendMessageDelayed(msg, delay);
        }

        public static void setCursorVisible(View view, boolean visible) {
            if (view instanceof TextView) {
                ((TextView)view).setCursorVisible(visible);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if ((v instanceof TextView)  &&
                ((event.getDownTime() - mLastUpTime  <= ViewConfiguration.getDoubleTapTimeout()) ||
                 (event.getEventTime() - event.getDownTime()) >= ViewConfiguration.getLongPressTimeout())) {
                Log.w(mSceneObject.getClass().getSimpleName(),
                        "Double tap/long press disabled to avoid popups!!!");
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mLastUpTime = event.getEventTime();
                }
                // FIXME: Improve it to avoid  blue balloon of the cursor.
                Log.d("PICKER EVENT CANCELED", "onTouchEvent action=%d button=%d x=%f y=%f",
                      event.getAction(), event.getButtonState(), event.getX(), event.getY());
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mLastUpTime = event.getEventTime();

                hideSoftInput(v, 10);
            }
            if (mSceneObject.getGestureDetector() != null)
            {
                mSceneObject.getGestureDetector().onTouchEvent(event);
            }
            return false;
        }
    }

    /**
     * Internal class to draw the Android view into canvas at UI thread and
     * update the GL texture of the scene object at GL thread.
     *
     * This is the root view to overwrite the default canvas of the view by the
     * canvas of the texture attached to the scene object.
     */
    protected static class RootViewGroup extends FrameLayout implements ITouchEvents {
        final GVRContext mGVRContext;
        final GVRViewSceneObject mSceneObject;
        Surface mSurface;
        SurfaceTexture mSurfaceTexture;

        float mHitX;
        float mHitY;
        float mActionDownX;
        float mActionDownY;
        GVRSceneObject mSelected = null;
        SoftInputController mSoftInputController;
        int mTextureBufferSize = 512;

        public RootViewGroup(GVRApplication application, GVRViewSceneObject sceneObject) {
            super(application.getActivity());

            setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            mGVRContext = application.getGVRContext();
            mSceneObject = sceneObject;

            // To optimization
            setWillNotDraw(true);

            mSoftInputController = new SoftInputController(application.getActivity(), sceneObject);


            // To block Android's popups
            // setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        }

        @Override
        public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
            /* FIXME: This method was deprecated in API level 26
              Use onDescendantInvalidated(View, View) instead.
             */
            postInvalidate();

            return super.invalidateChildInParent(location, dirty);
        }

        @Override
        public void onDescendantInvalidated(View child, View target) {
            super.onDescendantInvalidated(child, target);
            // To fix the issue of not redrawing the children after its invalidation.

            postInvalidate();
        }

        public void dispatchPickerInputEvent(final MotionEvent e, final float x, final float y) {
            final MotionEvent enew = MotionEvent.obtain(e);
            enew.setLocation(x, y);

            mGVRContext.getActivity().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    RootViewGroup.super.dispatchTouchEvent(enew);
                    enew.recycle();
                }
            });
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // Doesn't handle default touch from screen.
            return false;
        }

        private void dispatchPickerHoverEvent(final MotionEvent event) {
            mGVRContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RootViewGroup.super.dispatchHoverEvent(event);
                    event.recycle();
                }
            });
        }

        public void setCurrentFocusedView(View view) {
            view.requestFocus();
        }

        private void setChildrenInputController(ViewGroup viewGroup) {
            int count = viewGroup.getChildCount();

            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);

                if (view instanceof ViewGroup) {
                    setChildrenInputController((ViewGroup) view);
                }

                mSoftInputController.startListener(view);
            }
        }

        // Set the default size of the image buffers.
        // Call this after render data is ready
        public void setTextureBufferSize(int size) {
            if (mSurfaceTexture != null) {
                final GVRRenderData rdata = mSceneObject.getRenderData();
                final GVRMaterial material = rdata.getMaterial();
                final float frameWidth = getWidth();
                final float frameHeight = getHeight();
                final float viewSize = Math.max(frameWidth, frameHeight);
                final float quadWidth = frameWidth / viewSize;
                final float quadHeight = frameHeight / viewSize;
                final float bufferWidth = quadWidth * size;
                final float bufferHeight = quadHeight * size;

                mSurfaceTexture.setDefaultBufferSize((int) bufferWidth, (int) bufferHeight);

                material.setFloat("texelWidth", 1.0f / bufferWidth);
                material.setFloat("texelHeight", 1.0f / bufferHeight);
            }

            mTextureBufferSize = size;
        }

        @Override
        // Android UI thread
        protected void dispatchDraw(Canvas canvas) {
            // Canvas attached to GVRViewSceneObject to draw on
            Canvas attachedCanvas = mSurface.lockCanvas(null);
            // Clear the canvas
            attachedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            attachedCanvas.scale(attachedCanvas.getWidth() / (float) canvas.getWidth(),
                    attachedCanvas.getHeight() / (float) canvas.getHeight());
            // draw the view to provided canvas
            super.dispatchDraw(attachedCanvas);

            mSurface.unlockCanvasAndPost(attachedCanvas);
        }

        // UI Thread
        public void startRendering() {
            setChildrenInputController(this);

            mSceneObject.onInitView();

            // To just set the layout's dimensions but don't call draw(...) after it
            setVisibility(INVISIBLE);

            /**
             * To be notified when when the layout gets ready.
             * So after that create render data and material
             * to the scene object.
             */
            getViewTreeObserver().addOnGlobalLayoutListener (
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            RootViewGroup.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            onLayoutReady();
                        }
                    });

            mGVRContext.getApplication().registerView(this);
        }

        private void onLayoutReady() {
            synchronized (mSceneObject.mLock) {
                createRenderData();

                onRenderDataReady();

                mSceneObject.mLock.notify();
            }
        }

        private void onRenderDataReady() {
            mGVRContext.runOnTheFrameworkThread(new Runnable() {
                @Override
                public void run() {
                    // Because GVRTexture:getId() may cause deadlock at UI thread!
                    createSurfaceTexture();

                    mSceneObject.onStartRendering();

                    // FIXME: This may cause some black texture at first frames
                    mGVRContext.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onViewVisible();
                        }
                    });
                }
            });
        }

        private void onViewVisible() {
            // To call draw(...) after renderData has been created
            if (mSceneObject.getParent() != null) {
                setVisibility(VISIBLE);
            }

            /**
             * To adjust the default buffer size of the surface texture according to
             * changes of layout's size.
             */
            getViewTreeObserver().addOnGlobalLayoutListener (
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            onLayoutChanged();
                        }
                    });
        }

        private void onLayoutChanged() {
            setTextureBufferSize(mTextureBufferSize);
        }

        private void createRenderData() {
            final GVRTexture texture = new GVRExternalTexture(mGVRContext);
            final GVRMaterial material = new GVRMaterial(mGVRContext,
                    new GVRShaderId(GVROESConvolutionShader.class));
            final GVRCollider collider;

            if (mSceneObject.getRenderData() == null) {
                final float frameWidth = getWidth();
                final float frameHeight = getHeight();
                final float viewSize = Math.max(frameWidth, frameHeight);
                final float quadWidth = frameWidth / viewSize;
                final float quadHeight = frameHeight / viewSize;
                final GVRRenderData renderData = new GVRRenderData(mGVRContext);
                renderData.setMesh(
                        GVRMesh.createQuad(mGVRContext,
                                "float3 a_position float2 a_texcoord",
                                quadWidth, quadHeight));
                mSceneObject.attachComponent(renderData);
            }

            collider = new GVRMeshCollider(mGVRContext, mSceneObject.getRenderData().getMesh(),true);
            material.setMainTexture(texture);

            mSceneObject.getRenderData().setMaterial(material);
            mSceneObject.attachComponent(collider);
        }

        private void createSurfaceTexture() {
            final GVRRenderData rdata = mSceneObject.getRenderData();

            // FIXME: GVRTexture:getId() may cause deadlock at UI thread!
            mSurfaceTexture = new SurfaceTexture(rdata.getMaterial().getMainTexture().getId());
            mSurface = new Surface(mSurfaceTexture);
            mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                Runnable onFrameAvailableGLCallback = new Runnable() {
                    @Override
                    public void run() {
                        mSurfaceTexture.updateTexImage();
                    }
                };

                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mGVRContext.runOnGlThread(onFrameAvailableGLCallback);
                }
            });

            setTextureBufferSize(mTextureBufferSize);
        }

        private MotionEvent createMotionEvent(GVRPicker.GVRPickedObject pickInfo, int action) {
            float hitX = pickInfo.textureCoords[0] * getWidth();
            float hitY = pickInfo.textureCoords[1] * getHeight();
            long now = SystemClock.uptimeMillis();

            final MotionEvent event = MotionEvent.obtain(now, now, action, hitX, hitY,0);
            // Set source to touchscreen to make hover works fine
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            return event;
        }

        @Override
        public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo) {
            // If motionEvent is null, it is a hover action
            if (pickInfo.motionEvent == null) {
                MotionEvent event = createMotionEvent(pickInfo, MotionEvent.ACTION_HOVER_ENTER);
                dispatchPickerHoverEvent(event);
            }
        }

        @Override
        public void onExit(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo) {
            // If motionEvent is null, it is a hover action
            if (pickInfo.motionEvent == null) {
                MotionEvent event = createMotionEvent(pickInfo, MotionEvent.ACTION_HOVER_EXIT);
                dispatchPickerHoverEvent(event);
            } else if (sceneObject == mSelected) {
                mSelected = null;
                onDrag(pickInfo);
            }
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo) {
            if ((mSelected == null) && (pickInfo.motionEvent != null)) {
                final MotionEvent event = pickInfo.motionEvent;
                final float[] texCoords = pickInfo.getTextureCoords();

                mHitX = texCoords[0] * getWidth();
                mHitY = texCoords[1] * getHeight();
                mActionDownX = event.getRawX() - getLeft();
                mActionDownY = event.getRawY() - getTop();
                mSelected = sceneObject;
                dispatchPickerInputEvent(event, mHitX, mHitY);
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo) {
            final MotionEvent event = pickInfo.motionEvent;

            // If motionEvent is null, it is a hover action
            if (event == null) {
                MotionEvent e = createMotionEvent(pickInfo, MotionEvent.ACTION_HOVER_MOVE);
                dispatchPickerHoverEvent(e);
            } else if ((sceneObject == mSelected && event.getAction() == MotionEvent.ACTION_MOVE)) {
                onDrag(pickInfo);
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
            if (mSelected != null)
            {
                onDrag(pickInfo);
                mSelected = null;
            }
        }

        public void onDrag(GVRPicker.GVRPickedObject pickInfo)
        {
            if ((pickInfo.motionEvent != null) && (pickInfo.hitObject == mSelected))
            {
                final MotionEvent event = pickInfo.motionEvent;
                final float[] texCoords = pickInfo.getTextureCoords();
                float x = event.getRawX() - getTop();
                float y = event.getRawY() - getLeft();

                /*
                 * When we get events from the Gear controller we replace the location
                 * with the current hit point since the pointer coordinates in
                 * these events are all zero.
                 */
                if ((pickInfo.getPicker().getController().getControllerType() == GVRControllerType.CONTROLLER) &&
                    (event.getButtonState() == MotionEvent.BUTTON_SECONDARY))
                {
                    x = texCoords[0] * getWidth();
                    y = texCoords[1] * getHeight();
                }
                /*
                 * The pointer values in other events are not with respect to the view.
                 * Here we make the event location relative to the hit point where
                 * the button went down.
                 */
                else
                {
                    x += mHitX - mActionDownX;
                    y += mHitY - mActionDownY;
                }
                dispatchPickerInputEvent(event, x, y);
            }
        }

        @Override
        public void onMotionOutside(GVRPicker picker, MotionEvent event)
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                    break;
                default:
                    return;
            }
            dispatchPickerInputEvent(event, event.getX(), event.getY());
        }
    }
}
