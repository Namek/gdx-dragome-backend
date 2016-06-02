/*-------------------------------------------------------
 * This file was automatically generated by XpeCodeGen
 *
 * Dont make changes to this file
 *-------------------------------------------------------*/
package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;

/** @author xpenatan */
public class btCollisionShape extends BulletBase {

    /**
	 * 
	 * @param value A size 4 array for index 0,1,2 (center), and 4 (radius) 
	 */
    public void getBoundingSphere(float[] value) {
		checkPointer();
		Object center =  com.badlogic.gdx.physics.bullet.linearmath.btVector3.btVector3_1.jsObj;
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("var radius;this.$$$jsObj.getBoundingSphere(vec,radius);value[0]=center.x();value[1]=center.y();value[2]=center.z();value[3]=radius;",this);
    }

    public float getAngularMotionDisc() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalFloat("this.$$$jsObj.getAngularMotionDisc();",this);
    }

    public boolean isPolyhedral() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isPolyhedral();",this);
    }

    public boolean isConvex2d() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isConvex2d();",this);
    }

    public boolean isConvex() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isConvex();",this);
    }

    public boolean isNonMoving() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isNonMoving();",this);
    }

    public boolean isConcave() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isConcave();",this);
    }

    public boolean isCompound() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isCompound();",this);
    }

    public boolean isSoftBody() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isSoftBody();",this);
    }

    public boolean isInfinite() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalBoolean("this.$$$jsObj.isInfinite();",this);
    }

    public void setLocalScaling(Vector3 scaling) {
		checkPointer();
		float x = scaling.x;
		float y = scaling.y;
		float z = scaling.z;
		Object vec =  com.badlogic.gdx.physics.bullet.linearmath.btVector3.btVector3_1.jsObj;
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("vec.setValue(x,y,z);this.$$$jsObj.setLocalScaling(vec);",this);
    }

    public void getLocalScaling(Vector3 out) {
		checkPointer();
		float x=0,y=0,z=0;
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("var vec=this.$$$jsObj.getLocalScaling();x=vec.x();y=vec.y();z=vec.z();",this);
		out.set(x,y,z);
    }

    public void calculateLocalInertia(float mass, Vector3 inertia) {
		checkPointer();
		float x=0,y=0,z=0;
		Object vec3 =  com.badlogic.gdx.physics.bullet.linearmath.btVector3.btVector3_1.jsObj;
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("this.$$$jsObj.calculateLocalInertia(mass,vec3);x=vec3.x();y=vec3.y();z=vec3.z();",this);
		inertia.set(x,y,z);
    }

    public int getShapeType() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalInt("this.$$$jsObj.getShapeType();",this);
    }

    public void getAnisotropicRollingFrictionDirection(Vector3 out) {
		checkPointer();
		float x=0,y=0,z=0;
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("var vec=this.$$$jsObj.getAnisotropicRollingFrictionDirection();x=vec.x();y=vec.y();z=vec.z();",this);
		out.set(x,y,z);
    }

    public float getMargin() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalFloat("this.$$$jsObj.getMargin();",this);
    }

    public int getUserIndex() {
		checkPointer();
		return com.dragome.commons.javascript.ScriptHelper.evalInt("this.$$$jsObj.getUserIndex();",this);
    }

    public void setUserIndex(int index) {
		checkPointer();
		com.dragome.commons.javascript.ScriptHelper.evalNoResult("this.$$$jsObj.setUserIndex(index);",this);
    }
}