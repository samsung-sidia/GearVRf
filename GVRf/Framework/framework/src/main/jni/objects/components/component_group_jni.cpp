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


/***************************************************************************
 * JNI
 ***************************************************************************/

#include "component_group.h"

#include "util/gvr_jni.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr {
extern "C" {
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeComponentGroup_addChildComponent(JNIEnv * env,
            jobject obj, jlong jgroup, jlong jcomponent);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeComponentGroup_removeChildComponent(
            JNIEnv * env, jobject obj, jlong jgroup,
            jlong jcomponent);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeComponentGroup_addChildComponent(JNIEnv * env,
        jobject obj, jlong jgroup, jlong jcomponent) {
    ComponentGroup* group = reinterpret_cast<ComponentGroup*>(jgroup);
    Component* component = reinterpret_cast<Component*>(jcomponent);
    group->addChildComponent(component);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeComponentGroup_removeChildComponent(JNIEnv * env,
        jobject obj, jlong jgroup, jlong jcomponent) {
    ComponentGroup* group = reinterpret_cast<ComponentGroup*>(jgroup);
    Component* component = reinterpret_cast<Component*>(jcomponent);
    group->removeChildComponent(component);
}
}
