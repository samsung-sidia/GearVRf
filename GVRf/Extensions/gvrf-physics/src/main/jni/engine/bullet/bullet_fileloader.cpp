//
// Created by c.bozzetto on 3/19/2018.
//

#include <Android/asset_manager.h>

#include <btBulletCollisionCommon.h>
#include <btBulletDynamicsCommon.h>
#include <Serialize/BulletWorldImporter/btBulletWorldImporter.h>
#include <BulletDynamics/ConstraintSolver/btConstraintSolver.h>
#include <BulletDynamics/ConstraintSolver/btConeTwistConstraint.h>
#include <BulletDynamics/ConstraintSolver/btFixedConstraint.h>
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>
#include <BulletDynamics/ConstraintSolver/btHingeConstraint.h>
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
#include <BulletDynamics/ConstraintSolver/btSliderConstraint.h>
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>

#include "bullet_fileloader.h"
#include "bullet_world.h"
#include "bullet_rigidbody.h"
#include "bullet_conetwistconstraint.h"
#include "bullet_fixedconstraint.h"
#include "bullet_generic6dofconstraint.h"
#include "bullet_hingeconstraint.h"
#include "bullet_point2pointconstraint.h"
#include "bullet_sliderconstraint.h"

static char tag[] = "BulletLoaderN";

namespace gvr {

static btBulletWorldImporter *parse_buffer(char *buf, int len, btDynamicsWorld *world) {
    btBulletWorldImporter *importer = new btBulletWorldImporter(world);
    importer->loadFileFromMemory(buf, len);

    return importer;
}

static void createBulletRigidBodies(btBulletWorldImporter *importer)
{
    for (int i = 0; i < importer->getNumRigidBodies(); i++)
    {
        btRigidBody *rb = reinterpret_cast<btRigidBody*>(importer->getRigidBodyByIndex(i));

        // btRigidBody userPointer will point to the newly created BulletRigidBody
        BulletRigidBody *brb = new BulletRigidBody(rb);
    }
}

static void createBulletConstraints(btBulletWorldImporter *importer)
{
    for (int i = 0; i < importer->getNumConstraints(); i++)
    {
        btTypedConstraint *constraint = importer->getConstraintByIndex(i);

        __android_log_print(ANDROID_LOG_DEBUG, tag, "new constraint: %p (type=%i)", constraint, (int)constraint->getConstraintType());

        if (constraint->getConstraintType() == btTypedConstraintType::POINT2POINT_CONSTRAINT_TYPE)
        {
            btPoint2PointConstraint *p2p = static_cast<btPoint2PointConstraint*>(constraint);

            // Constraint userPointer will point to newly created BulletPoint2PointConstraint
            BulletPoint2PointConstraint *bp2p = new BulletPoint2PointConstraint(p2p);

            __android_log_print(ANDROID_LOG_DEBUG, tag, "Created point-to-point constraint");

            // Adapting pivot to GVRf coordinates system
            btVector3 pivot = p2p->getPivotInA();
            float t = pivot.getZ();
            pivot.setZ(pivot.getY());
            pivot.setY(t);
            p2p->setPivotA(pivot);

            pivot = p2p->getPivotInB();
            t = pivot.getZ();
            pivot.setZ(pivot.getY());
            pivot.setY(t);
            p2p->setPivotB(pivot);
        }
    }

}

BulletFileLoader::BulletFileLoader(char *buffer, size_t length) :
    PhysicsLoader(buffer, length), mCurrRigidBody(0), mCurrConstraint(0)
{
    mImporter = parse_buffer(buffer, length, nullptr);
    createBulletRigidBodies(mImporter);

    createBulletConstraints(mImporter);
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

        ret = static_cast<PhysicsConstraint*>(phc);
        __android_log_print(ANDROID_LOG_DEBUG, tag, "Sending constraint %i [%p / %p]",
            mCurrConstraint, phc, mImporter->getConstraintByIndex(mCurrConstraint));

        ++mCurrConstraint;
    }

    return ret;
}

PhysicsRigidBody* BulletFileLoader::getConstraintBodyA(PhysicsConstraint *constraint)
{
    btTypedConstraint *btc = static_cast<btTypedConstraint*>(constraint->getUnderlying());
    btRigidBody *rbA = &btc->getRigidBodyA();
    return static_cast<PhysicsRigidBody*>(rbA->getUserPointer());
}

}