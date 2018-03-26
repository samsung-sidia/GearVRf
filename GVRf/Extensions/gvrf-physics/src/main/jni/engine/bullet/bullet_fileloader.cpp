//
// Created by c.bozzetto on 3/19/2018.
//

#include <Android/asset_manager.h>

#include <btBulletCollisionCommon.h>
#include <btBulletDynamicsCommon.h>
#include <Serialize/BulletWorldImporter/btBulletWorldImporter.h>
#include <BulletDynamics/ConstraintSolver/btConstraintSolver.h>
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>

#include "bullet_fileloader.h"
#include "bullet_world.h"
#include "bullet_rigidbody.h"

namespace gvr {

static btBulletWorldImporter *parse_buffer(char *buf, int len, btDynamicsWorld *world) {
    btBulletWorldImporter *importer = new btBulletWorldImporter(world);
    importer->loadFileFromMemory(buf, len);

    return importer;
}

static void completeRigidBodies(btBulletWorldImporter *importer)
{
    for (int i = 0; i < importer->getNumRigidBodies(); i++)
    {
        btRigidBody *rb = reinterpret_cast<btRigidBody*>(importer->getRigidBodyByIndex(i));

        // btRigidBody userPointer will point to the newly created BulletRigidBody
        BulletRigidBody *brb = new BulletRigidBody(rb);
    }
}

static void completeConstraints(btBulletWorldImporter *importer)
{

}

BulletFileLoader::BulletFileLoader(char *buffer, size_t length) :
    PhysicsLoader(buffer, length), mCurrRigidBody(0), mCurrConstraint(0)
{
    mImporter = parse_buffer(buffer, length, nullptr);
    completeRigidBodies(mImporter);
    completeConstraints(mImporter);
}

BulletFileLoader::~BulletFileLoader()
{
    delete mImporter;
}

PhysicsRigidBody* BulletFileLoader::getNextRigidBody()
{
    PhysicsRigidBody *ret = nullptr;

    if (mCurrRigidBody < mImporter->getNumRigidBodies())
    {
        void *brb = mImporter->getRigidBodyByIndex(mCurrRigidBody)->getUserPointer();
        ret = reinterpret_cast<BulletRigidBody*>(brb);
        ++mCurrRigidBody;
    }

    return ret;
}

const char* BulletFileLoader::getRigidBodyName(PhysicsRigidBody *body) const
{
    btRigidBody *rb = reinterpret_cast<BulletRigidBody*>(body)->getRigidBody();

    return mImporter->getNameForPointer(rb);
}

PhysicsConstraint* BulletFileLoader::getNextConstraint()
{
    PhysicsConstraint *ret = nullptr;

    if (mCurrConstraint < mImporter->getNumConstraints())
    {
        void *phc = mImporter->getConstraintByIndex(mCurrConstraint)->getUserConstraintPtr();

        ret = reinterpret_cast<PhysicsConstraint*>(phc);
        ++mCurrConstraint;
    }

    return ret;
}

}