package org.gearvrf.physics;

import android.view.MotionEvent;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import java.util.EnumSet;

class PhysicsDrag {
    private static final String TAG = PhysicsDrag.class.getSimpleName();

    private static final float CURSOR_DEPTH = -7.0f;
    private static final float COLLIDER_HALF_EXT_X = 1f;
    private static final float COLLIDER_HALF_EXT_Y = 1f;
    private static final float COLLIDER_HALF_EXT_Z = 1f;

    private final Object mLock = new Object();

    private boolean mActive;
    private GVRSceneObject mCursor;
    private GVRSceneObject mDragger;
    private GVRCursorController mController = null;

    private final EnumSet<GVRPicker.EventOptions> mEventOptions = EnumSet.of(
            GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
            GVRPicker.EventOptions.SEND_TO_LISTENERS);

    PhysicsDrag(GVRContext gvrContext) {
        mActive = false;
        mCursor = null;

        mDragger = new GVRSceneObject(gvrContext);
        mDragger.setName("Physics_Dragger");
        GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
        collider.setHalfExtents(COLLIDER_HALF_EXT_X, COLLIDER_HALF_EXT_Y, COLLIDER_HALF_EXT_Z);
        mDragger.attachComponent(collider);

        gvrContext.getInputManager().selectController(new GVRInputManager.ICursorControllerSelectListener() {
            @Override
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mTouchHandler);
                }

                synchronized (mLock) {
                    mController = newController;
                    if (mActive) {
                        prepareController();
                    }
                }
            }
        });
    }

    private void prepareController() {
        mController.addPickEventListener(mTouchHandler);

        if (mCursor != null) {
            mController.setCursor(mCursor);
        }

        mController.setCursorDepth(CURSOR_DEPTH);
        mController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
        mController.getPicker().setEventOptions(mEventOptions);
    }

    void setActive(boolean active) {
        if (active != mActive) {
            synchronized (mLock) {
                if (active) {
                    prepareController();
                } else {
                    mController.removePickEventListener(mTouchHandler);
                }

                mActive = active;
            }
        }
    }

    void setDraggingCursor(GVRSceneObject cursor) {
        mCursor = cursor;
        synchronized (mLock) {
            if (mController != null) {
                mController.setCursor(mCursor);
            }
        }
    }

    private ITouchEvents mTouchHandler = new ITouchEvents() {
        private GVRSceneObject mMovingObject = null;

        @Override
        public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            if (mMovingObject == null) {
                GVRRigidBody rigidBody = (GVRRigidBody)sceneObject.getComponent(GVRRigidBody.getComponentType());
                if (rigidBody != null) {
                    float[] pos = pickedObject.getHitLocation();
                    pos[0] += sceneObject.getTransform().getPositionX();
                    pos[1] += sceneObject.getTransform().getPositionY();
                    pos[2] += sceneObject.getTransform().getPositionZ();

                    mDragger.getTransform().setPosition(pos[0], pos[1], pos[2]);

                    if (mController.startDrag(mDragger) && rigidBody.startDragging(mDragger)) {
                        mMovingObject = sceneObject;
                    }
                }
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            if (mMovingObject != null) {
                GVRRigidBody rigidBody = (GVRRigidBody)mMovingObject.getComponent(GVRRigidBody.getComponentType());
                if (rigidBody != null && rigidBody.stopDragging()) {
                    mMovingObject = null;
                    mController.stopDrag();
                }
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onMotionOutside(GVRPicker picker, MotionEvent motionEvent) {

        }
    };
}
