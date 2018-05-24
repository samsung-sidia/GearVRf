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
 * Things which can be attached to a scene object.
 ***************************************************************************/

#ifndef COMPONENT_H_
#define COMPONENT_H_

#include <memory>
#include "objects/hybrid_object.h"
#include "component_types.h"

namespace gvr {
class SceneObject;
class Scene;

class Component: public HybridObject {
public:
    Component();
    explicit Component(long long type);
    explicit Component(SceneObject* owner_object);
    explicit Component(long long type, SceneObject* owner_object);
    virtual ~Component();

    Component* parent() const;
    void set_parent(Component* parent);

    SceneObject* owner_object() const;

    virtual void set_owner_object(SceneObject* owner_object);
    virtual void onAddedToScene(Scene* scene) { }
    virtual void onRemovedFromScene(Scene* scene) { }
    virtual void onAttach(SceneObject* owner) { }
    virtual void onDetach(SceneObject* owner) { }

    long long getType() const;
    bool enabled() const;
    virtual void set_enable(bool enable);

private:
    Component(const Component& component) = delete;
    Component(Component&& component) = delete;
    Component& operator=(const Component& component) = delete;
    Component& operator=(Component&& component) = delete;

protected:
    Component *parent_;
    SceneObject* owner_object_;
    bool         enabled_;

private:
    long long    type_;

};

}

#endif

