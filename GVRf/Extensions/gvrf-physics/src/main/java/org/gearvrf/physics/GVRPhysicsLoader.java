package org.gearvrf.physics;

import android.content.res.AssetManager;
import android.util.ArrayMap;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Created by c.bozzetto on 3/23/2018.
 */

public class GVRPhysicsLoader {

    static private String TAG = GVRPhysicsLoader.class.getSimpleName();

    static {
        System.loadLibrary("gvrf-physics");
    }

    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, GVRSceneObject sceneRoot) {
        long loader = NativePhysics3DLoader.ctor(fileName, gvrContext.getActivity().getAssets());

        ArrayMap<Long, GVRSceneObject> rbObjects = new ArrayMap<>();

        long nativeRigidBody;
        while ((nativeRigidBody = NativePhysics3DLoader.getNextRigidBody(loader)) != 0) {
            String name = NativePhysics3DLoader.getRigidBodyName(loader, nativeRigidBody);
            Log.d(TAG, "Found rigid body " + nativeRigidBody + " name: '" + name + "'");
            GVRSceneObject sceneObject = sceneRoot.getSceneObjectByName(name);
            if (sceneObject != null) {
                Log.d(TAG, "Found scene object '" + name + "'");
                GVRRigidBody rigidBody = new GVRRigidBody(gvrContext, nativeRigidBody);
                sceneObject.attachComponent(rigidBody);
                rbObjects.put(nativeRigidBody, sceneObject);
            }
        }

        long nativeConstraint;
        while ((nativeConstraint = NativePhysics3DLoader.getNextConstraint(loader)) != 0) {
            nativeRigidBody = NativePhysics3DLoader.getConstraintBodyA(loader, nativeConstraint);
            Log.d(TAG, "Found constraint " + nativeConstraint + " bodyA: " + nativeRigidBody);
            GVRSceneObject sceneObject = rbObjects.get(nativeRigidBody);

            if (sceneObject == null) {
                // There is no scene object to own this constraint
                // TODO: what to do with this constraint?
                Log.d(TAG, "Did not found scene object for this constraint :(");
                continue;
            }

            Log.d(TAG, "Scene object to own this constraint: '" + sceneObject.getName() + '"');

            int constraintType = Native3DConstraint.getConstraintType(nativeConstraint);
            GVRConstraint constraint = null;
            if (constraintType == GVRConstraint.fixedConstraintId) {
                Log.d(TAG, "Is fixed constraint");
            } else if (constraintType == GVRConstraint.point2pointConstraintId) {
                Log.d(TAG, "Is point-to-point constraint");
                constraint = new GVRPoint2PointConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.sliderConstraintId) {
                Log.d(TAG, "Is slider constraint");
            } else if (constraintType == GVRConstraint.hingeConstraintId) {
                Log.d(TAG, "Is hinge constraint");
                constraint = new GVRHingeConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.coneTwistConstraintId) {
                Log.d(TAG, "Is cone twist constraint");
            } else if (constraintType == GVRConstraint.genericConstraintId) {
                Log.d(TAG, "Is generic (6 DoF) constraint");
            }

            if (constraint != null) {
                sceneObject.attachComponent(constraint);
                Log.d(TAG, "Constraint attached to scene object");
            }
        }
    }

}

class NativePhysics3DLoader {
    static native long ctor(String file_name, AssetManager assetManager);

    static native long getNextRigidBody(long loader);

    static native String getRigidBodyName(long loader, long rigid_body);

    static native long getNextConstraint(long loader);

    static native long getConstraintBodyA(long loader, long constraint);
}