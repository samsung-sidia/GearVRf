package org.gearvrf.physics;

import android.content.res.AssetManager;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Created by c.bozzetto on 3/23/2018.
 */

public class GVRPhysicsLoader {

    static {
        System.loadLibrary("gvrf-physics");
    }

    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, GVRSceneObject sceneRoot) {
        long loader = NativePhysics3DLoader.ctor(fileName, gvrContext.getActivity().getAssets());

        long nativeRigidBody;
        while ((nativeRigidBody = NativePhysics3DLoader.getNextRigidBody(loader)) != 0) {
            String name = NativePhysics3DLoader.getRigidBodyName(loader, nativeRigidBody);
            GVRSceneObject sceneObject = sceneRoot.getSceneObjectByName(name);
            if (sceneObject != null) {
                GVRRigidBody rigidBody = new GVRRigidBody(gvrContext, nativeRigidBody);
                sceneObject.attachComponent(rigidBody);
            }
        }

//        long nativeConstraint;
//        while ((nativeConstraint = NativePhysics3DLoader.getNextConstraint(loader)) != 0) {
//
//        }
    }

}

class NativePhysics3DLoader {
    static native long ctor(String file_name, AssetManager assetManager);

    static native long getNextRigidBody(long loader);

    static native String getRigidBodyName(long loader, long rigid_body);

    static native long getNextConstraint(long loader);
}