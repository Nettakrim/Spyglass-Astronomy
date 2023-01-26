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
        if (constellations.size() >= 20 && namedStars >= 8) {
            starKnowledge = Level.MASTER;
            return;
        }
        if (constellations.size() >= 10 && namedStars >= 3) {
            starKnowledge = Level.EXPERT;
            return;
        }
        if (constellations.size() >= 5) {
            starKnowledge = Level.ADEPT;
            return;
        }
        starKnowledge = Level.NOVICE;
    }

    public String getInstructionsToStarKnowledgeStage(int stage) {
        switch (stage) {
            case 1:
                return "\nDraw 5 Constellations to learn more";
            case 2:
                return "\nDraw 10 Constellations and Name 3 Stars to learn more";
            case 3:
                return "\nDraw 20 Constellations and Name 8 Stars to learn more";
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
        if (namedPlanets >= planets-1 && namedComets >= comets-1) {
            orbitKnowledge = Level.MASTER;
            return;
        }
        if (namedPlanets >= planets/3*2 && namedComets >= 2) {
            orbitKnowledge = Level.EXPERT;
            return;
        }
        if (namedPlanets >= planets/3 || namedComets >= 1) {
            orbitKnowledge = Level.ADEPT;
            return;
        }
        orbitKnowledge = Level.NOVICE;
        return;
    }

    public String getInstructionsToOrbitKnowledgeStage(int stage) {
        switch (stage) {
            case 1:
                return String.format("\nName %d Planets or 1 Comet to learn more",planets/3);
            case 2:
                return String.format("\nName %d Planets and 2 Comets to learn more",planets/3*2);
            case 3:
                return String.format("\nName %d Planets and %d Comets to learn more",planets-1, comets-1);
            default:
                return "";
        }
    }

    private void updateFlags(Level level, int[] flags, int index) {
        int current = -1;
        switch (level) {
            case NOVICE:
                current = 0;
                break;
            case ADEPT:
                current = 1;
                break;
            case EXPERT:
                current = 2;
                break;
            case MASTER:
                current = 3;
                break;
        }
        if (flags[index] == -1) flags[index] = current;
        else flags[index] = Math.min(flags[index], current);
    }

    public String getKnowledgeInstructions(int[] flags) {
        return getInstructionsToStarKnowledgeStage(flags[0])+getInstructionsToOrbitKnowledgeStage(flags[1]);
    }

    public boolean starKnowledgeAtleast(Level level, int[] flags) {
        boolean isAtleast = bypass || knowledgeAtleast(starKnowledge, level);
        if (!isAtleast) {
            updateFlags(level, flags, 0);
        }
        return isAtleast;
    }

    public boolean orbitKnowledgeAtleast(Level level, int[] flags) {
        boolean isAtleast = bypass || knowledgeAtleast(orbitKnowledge, level);
        if (!isAtleast) {
            updateFlags(level, flags, 1);
        }
        return isAtleast;
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
