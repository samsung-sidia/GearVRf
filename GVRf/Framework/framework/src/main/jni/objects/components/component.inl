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

#ifndef COMPONENT_INL_
#define COMPONENT_INL_

namespace gvr {
inline Component::Component() :
        HybridObject(), type_(0), parent_(0), owner_object_(0), enabled_(true) {
}

inline Component::Component(long long type) :
        HybridObject(), type_(type), parent_(0), owner_object_(0), enabled_(true) {
}

inline Component::Component(SceneObject* owner_object) :
    type_(0),
    parent_(0),
    enabled_(true),
    owner_object_(owner_object) {
}

inline Component::Component(long long type, SceneObject* owner_object) :
        type_(type),
        parent_(0),
        enabled_(true),
        owner_object_(owner_object) {
}

inline Component::~Component() {
    parent_ = nullptr;
    owner_object_ = nullptr;
}

inline Component *Component::parent() const {
    return parent_;
}

inline void Component::set_parent(Component *parent) {
    parent_ = parent;
}

inline SceneObject *Component::owner_object() const {
    if (nullptr != parent_ && nullptr == owner_object_)
        return parent_->owner_object();

    return owner_object_;
}

inline void Component::set_owner_object(SceneObject *owner_object) {
    if (owner_object_) {
        onDetach(owner_object_);
    }
    owner_object_ = owner_object;
    if (owner_object) {
        onAttach(owner_object);
    }
}

inline long long Component::getType() const {
    return type_;
}

inline bool Component::enabled() const {
    return enabled_;
}

inline void Component::set_enable(bool enable) {
    enabled_ = enable;
}

}
#endif

