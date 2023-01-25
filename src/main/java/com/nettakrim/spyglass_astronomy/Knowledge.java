package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

public class Knowledge {
    public enum Level {
        NOVICE,
        ADEPT,
        EXPERT,
        MASTER
    }

    private Level starKnowledge;
    private Level orbitKnowledge;
    private boolean bypass;
    private int planets;
    private int comets;

    public void updateStarKnowledge(ArrayList<Constellation> constellations, ArrayList<Star> stars) {
        int namedStars = 0;
        for (Star star : stars) {
            if (star.name != null) {
                namedStars++;
            }
        }
        if (constellations.size() >= 20 && namedStars >= 10) {
            starKnowledge = Level.MASTER;
            return;
        }
        if (constellations.size() >= 10 && namedStars >= 5) {
            starKnowledge = Level.EXPERT;
            return;
        }
        if (constellations.size() >= 5) {
            starKnowledge = Level.ADEPT;
            return;
        }
        starKnowledge = Level.NOVICE;
    }

    public String getInstructionsToNextStarKnowledgeStage() {
        switch (starKnowledge) {
            case NOVICE:
                return "\nDraw 5 Constellations to learn more";
            case ADEPT:
                return "\nDraw 10 Constellations and Name 5 Stars to learn more";
            case EXPERT:
                return "\nDraw 20 Constellations and Name 10 Stars to learn more";
            default:
                return "";
        }
    }

    public void updateOrbitKnowledge(ArrayList<OrbitingBody> orbitingBodies, int planets, int comets) {
        int namedPlanets = 0;
        int namedComets = 0;
        this.planets = planets;
        this.comets = comets;
        for (OrbitingBody orbitingBody : orbitingBodies) {
            if (orbitingBody.name != null) {
                if (orbitingBody.isPlanet) namedPlanets++;
                else namedComets++;
            }
        }
        if (namedPlanets == planets && namedComets >= comets-1) {
            orbitKnowledge = Level.MASTER;
            return;
        }
        if (namedPlanets >= planets-2 && namedComets >= 2) {
            orbitKnowledge = Level.EXPERT;
            return;
        }
        if (namedPlanets >= planets/2 || namedComets >= 1) {
            orbitKnowledge = Level.ADEPT;
            return;
        }
        orbitKnowledge = Level.NOVICE;
        return;
    }

    public String getInstructionsToNextOrbitKnowledgeStage() {
        switch (orbitKnowledge) {
            case NOVICE:
                return String.format("\nName %d Planets or 1 Comet to learn more",planets/2);
            case ADEPT:
                return String.format("\nName %d Planets and 2 Comets to learn more",planets-2);
            case EXPERT:
                return String.format("\nName %d Planets and %d Comets to learn more",planets, comets-1);
            default:
                return "";
        }
    }

    public boolean starKnowledgeAtleast(Level level) {
        return bypass || knowledgeAtleast(starKnowledge, level);
    }

    public boolean orbitKnowledgeAtleast(Level level) {
        return bypass || knowledgeAtleast(orbitKnowledge, level);
    }

    private boolean knowledgeAtleast(Level a, Level b) {
        //b <= a 
        if (b == a) return true;
        if (a == Level.MASTER) return true;
        if (b == Level.NOVICE) return true;
        return a == Level.EXPERT && b == Level.ADEPT;
    }

    public boolean bypassKnowledge() {
        bypass = !bypass;
        return bypass;
    }
}
