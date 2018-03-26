//
// Created by c.bozzetto on 3/22/2018.
//

#ifndef EXTENSIONS_PHYSICS_LOADER_H
#define EXTENSIONS_PHYSICS_LOADER_H

#include <cstddef>

namespace gvr {

class PhysicsWorld;
class PhysicsRigidBody;
class PhysicsConstraint;

class PhysicsLoader
{
public:
    PhysicsLoader(char *buffer, size_t length) { }
    virtual ~PhysicsLoader() { }

    virtual PhysicsRigidBody* getNextRigidBody() = 0;

    virtual const char* getRigidBodyName(PhysicsRigidBody *body) const = 0;

    virtual PhysicsConstraint* getNextConstraint() = 0;
};

}

#endif //EXTENSIONS_PHYSICS_LOADER_H
