/***************************************************************************
 *  Collider made from a capsule.
 ***************************************************************************/

#include "capsule_collider.h"

namespace gvr {
    ColliderData CapsuleCollider::isHit(SceneObject* owner, const float sphere[]) {
        return nullptr;
    }

    ColliderData CapsuleCollider::isHit(SceneObject *owner, const glm::vec3 &rayStart,
                                        const glm::vec3 &rayDir) {
        return nullptr;
    }
}
