//
// Created by c.bozzetto on 3/19/2018.
//

#ifndef EXTENSIONS_BULLET_FILELOADER_H
#define EXTENSIONS_BULLET_FILELOADER_H

#include "../physics_common.h"
#include "../physics_loader.h"

class btBulletWorldImporter;

namespace gvr{
class BulletFileLoader : public PhysicsLoader
{
public:
    BulletFileLoader(char *buffer, size_t length, bool ignoreUpAxis);
    virtual ~BulletFileLoader();

    PhysicsRigidBody* getNextRigidBody();

    const char* getRigidBodyName(PhysicsRigidBody *body) const;

    PhysicsConstraint* getNextConstraint();

    PhysicsRigidBody* getConstraintBodyA(PhysicsConstraint *constraint);

    PhysicsRigidBody* getConstraintBodyB(PhysicsConstraint *constraint);

private:
    btBulletWorldImporter *mImporter;
    int mCurrRigidBody;
    int mCurrConstraint;
};

}

#endif //EXTENSIONS_BULLET_FILELOADER_H
