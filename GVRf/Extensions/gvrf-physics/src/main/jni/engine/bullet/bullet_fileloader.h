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
    BulletFileLoader(char *buffer, size_t length);
    virtual ~BulletFileLoader();

    virtual PhysicsRigidBody* getNextRigidBody();

    virtual const char* getRigidBodyName(PhysicsRigidBody *body) const;

    virtual PhysicsConstraint* getNextConstraint();

private:
    btBulletWorldImporter *mImporter;
    int mCurrRigidBody;
    int mCurrConstraint;
};

}

#endif //EXTENSIONS_BULLET_FILELOADER_H
