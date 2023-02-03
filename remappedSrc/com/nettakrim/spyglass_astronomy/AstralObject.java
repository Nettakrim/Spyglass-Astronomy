package com.nettakrim.spyglass_astronomy;

public class AstralObject {
    public final Star star;
    public final OrbitingBody orbitingBody;
    public final boolean isStar;

    public AstralObject(Star star) {
        this.star = star;
        this.orbitingBody = null;
        this.isStar = true;
    }

    public AstralObject(OrbitingBody orbitingBody) {
        this.star = null;
        this.orbitingBody = orbitingBody;
        this.isStar = false;
    }

    public void select() {
        if (isStar) star.select();
        else orbitingBody.select();
    }

    public static boolean isNull(AstralObject astralObject) {
        if (astralObject == null) return true;
        if (astralObject.isStar) {
            if (astralObject.star == null) return true;
        } else {
            if (astralObject.orbitingBody == null) return true;
        }
        return false;
    }
}
