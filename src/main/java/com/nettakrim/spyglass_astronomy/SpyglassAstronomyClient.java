package com.nettakrim.spyglass_astronomy;

import com.nettakrim.spyglass_astronomy.commands.admin_subcommands.StarCountCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.random.Random;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.spyglass_astronomy.OrbitingBody.OrbitingBodyType;
import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;

import java.util.ArrayList;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SpyglassAstronomyClient implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "spyglass_astronomy";
	public static final Logger LOGGER = LoggerFactory.getLogger("Spyglass Astronomy");

    public static boolean ready;

    private static int starCount = 1024; //encoding will break at 4096, so stay at 4095 and below :)

    public static MinecraftClient client;
    public static ClientWorld world;

    public static ArrayList<Star> stars;

    public static ArrayList<Constellation> constellations;

    public static SpaceRenderingManager spaceRenderingManager;

    public static int editMode;
    public static boolean isDrawingConstellation;
    private static StarLine drawingLine;
    public static Constellation drawingConstellation;

    public static SpaceDataManager spaceDataManager;

    private static boolean lastToggle = false;

    public static Orbit earthOrbit;
    public static ArrayList<OrbitingBody> orbitingBodies;

    public static float starAngleMultiplier;

    public static float zoom;

    public static Knowledge knowledge;

    public static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
    public static final TextColor nameTextColor = TextColor.fromRgb(0xB38EF3);
    public static final TextColor buttonTextColor = TextColor.fromRgb(0x41F384);

    private static boolean spyglassImprovementsIsLoaded;

	@Override
	public void onInitializeClient() {
        client = MinecraftClient.getInstance();

        SpyglassAstronomyCommands.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> update());

        spyglassImprovementsIsLoaded = FabricLoader.getInstance().isModLoaded("spyglass-improvements");
	}

    public static void saveSpace() {
        if (spaceDataManager != null) {
            spaceDataManager.saveData();
        }
        if (spaceRenderingManager != null) {
            spaceRenderingManager.saveData();
        }
    }

    public static void discardUnsavedChanges() {
        loadSpace(world, false);
    }

    public static void loadSpace(ClientWorld clientWorld, boolean allowSave) {
        if (spaceDataManager != null && allowSave) spaceDataManager.saveData();

        world = clientWorld;

        stars = new ArrayList<>();
        constellations = new ArrayList<>();
        orbitingBodies = new ArrayList<>();

        spaceDataManager = new SpaceDataManager(clientWorld);

        generateSpace(false);
        StarCountCommand.invalidatedConstellations.clear();

        knowledge = new Knowledge();
        updateKnowledge();
    }

    public static void generateSpace(boolean reset) {
        Random random = Random.create(0);
        generateStars(random, reset, reset);
        generatePlanets(random, reset);

        for (Constellation constellation : constellations) {
            constellation.initaliseStarLines();
        }

        spaceRenderingManager = new SpaceRenderingManager();
        spaceRenderingManager.updateSpace(0);
    }

    public static void generateStars(Random random, boolean resetStars, boolean resetConstellations) {
        if (random == null) {
            random = Random.create(spaceDataManager.getStarSeed());
        } else {
            random.setSeed(spaceDataManager.getStarSeed());
        }

        if (resetStars) {
            stars = new ArrayList<>();
        }
        if (resetConstellations) {
            constellations = new ArrayList<>();
            spaceRenderingManager.scheduleConstellationsUpdate();
        }
        int currentStars = 0;
        while (currentStars < starCount) {
            float posX = random.nextFloat() * 2.0f - 1.0f;
            float posY = random.nextFloat() * 2.0f - 1.0f;
            float posZ = random.nextFloat() * 2.0f - 1.0f;
            float galaxyBias = 0.75f;
            posX = (galaxyBias * posX * MathHelper.abs(posX))+((1-galaxyBias) * posX);

            //makes sure position is a uniform point in a sphere, then normalises position to the outside of a sphere
            float distance = posX * posX + posY * posY + posZ * posZ;
            if (!(distance < 1.0) || !(distance > 0.01)) continue;
            distance = MathHelper.inverseSqrt(distance);
            posX *= distance;
            posY *= distance;
            posZ *= distance;

            float sizeRaw = random.nextFloat();
            if (currentStars % 2 == 0) {
                float galaxyCloseness = (0.12f/(MathHelper.abs(posX)+0.1f))-0.2f;
                if (galaxyCloseness > 0) sizeRaw = (1-galaxyCloseness)*sizeRaw + galaxyCloseness*((sizeRaw*sizeRaw)/2);
            }
            float size = 0.15f + sizeRaw * 0.2f;

            float alphaRaw = random.nextFloat();
            float alpha = Math.max(MathHelper.sqrt(alphaRaw*sizeRaw),(2*sizeRaw-1.5f)/(alphaRaw+0.5f));
            alpha = (alpha + (alpha*alpha))/2;

            int [] color = generateRandomColor(random, 0.8f, 20, 16, 0, 2f);

            float rotationSpeed = (random.nextFloat() * 2f)-1;
            float twinkleSpeed = random.nextFloat()*0.025f+0.035f;

            stars.add(new Star(currentStars, posX, posY, posZ, size, rotationSpeed, color, alpha, twinkleSpeed));

            currentStars++;
        }

        ready = true;

        spaceDataManager.loadStarDatas();
    }

    public static void generatePlanets(Random random, boolean reset) {
        if (random == null) {
            random = Random.create(spaceDataManager.getPlanetSeed());
        } else {
            random.setSeed(spaceDataManager.getPlanetSeed());
        }
        //things with less importance and *could* change in the future and not be too bad like exact color that use their own random
        Random lowPriorityRandom = Random.create(spaceDataManager.getPlanetSeed());

        if (reset) {
            orbitingBodies = new ArrayList<>();
        }

        IntTetrisBagRandom planetDesignRandom = new IntTetrisBagRandom(random, 3);
        IntTetrisBagRandom cometDesignRandom = new IntTetrisBagRandom(random, 2);

        //always at least 5 planets, and always at least 2 outer planets
        //between 1 and 3 inner planets
        int innerPlanets = random.nextInt(3)+1;
        //between 4 and 8 outer planets for 1 inner, between 2 and 8 for 3 inner
        int outerPlanets = random.nextBetween(5-innerPlanets, 8);

        int comets = random.nextBetween(4, 6);

        float yearLength = spaceDataManager.getYearLength();

        //earth will have a largely circular orbit
        earthOrbit = generateRandomOrbit(random, yearLength, 0.05f, 10f, 5f, true);

        starAngleMultiplier = ((yearLength + 1) / yearLength) * 360f;

        //inner planets are spaced roughly evenly, and rounded to 8ths of an earth year
        float innerRoundAmount = 8;
        while (innerPlanets >= innerRoundAmount) innerRoundAmount *= 2;

        int otherHabitable = random.nextBetween(0, 8); //0 means first inner planet habitable, 1 means first outer, all else mean none

        float innerDistanceRange = 1f/innerPlanets;
        float[] innerPlanetPeriods = new float[innerPlanets];
        for (float x = 0; x < innerPlanets; x++) {
            float minPeriod = (x/innerPlanets);
            float maxPeriod = (x/innerPlanets)+innerDistanceRange;
            float rawUnRoundedPeriod = random.nextFloat();
            float unRoundedPeriod = (1-rawUnRoundedPeriod)*minPeriod + rawUnRoundedPeriod*maxPeriod;
            float period = (MathHelper.floor(unRoundedPeriod*(innerRoundAmount-1))+1)/innerRoundAmount;
            for (int y = 0; y < x; y++) {
                if (innerPlanetPeriods[y] == period) {
                    period += 1f/innerRoundAmount;
                }
            }
            if (period == 1) period -= 0.5f/innerRoundAmount;

            innerPlanetPeriods[(int)x] = period;
            period *= yearLength;

            OrbitingBodyType type;
            if (otherHabitable == 0 && x == innerPlanets) {
                type = OrbitingBodyType.HABITABLE;
            } else {
                int randomInnerType = lowPriorityRandom.nextBetween(0, 3);
                if (randomInnerType == 0 && x > innerPlanets/2) {
                    type = OrbitingBodyType.OCEANPLANET;
                } else {
                    type = OrbitingBodyType.TERRESTIAL;
                }
            }

            Orbit orbit = generateRandomOrbit(random, period, 0.1f, 20f, 10f, false);
            addRandomOrbitingBody(random, lowPriorityRandom, orbit, true, planetDesignRandom, type);
        }

        //outer planets roughly double in period each planet, further out planets will have slightly more irregular orbits
        float[] periodOffsets = new float[] {0.75f, 1f, 1f, 1.25f};
        for (float x = 0; x < outerPlanets; x++) {
            float period = (yearLength * (2 << ((int)x+1))) * periodOffsets[random.nextInt(periodOffsets.length)];
            float settingsMultiplier = (x/8)+1;
            Orbit orbit = generateRandomOrbit(random, period, Math.min(0.15f*settingsMultiplier,0.5f), Math.min(30f*settingsMultiplier,60f), Math.min(20f*settingsMultiplier,60f), false);

            OrbitingBodyType type;
            if (otherHabitable == 1 && x == innerPlanets) {
                type = OrbitingBodyType.HABITABLE;
            } else {
                int canBeTerrestial = lowPriorityRandom.nextBetween(0, 1);
                if (x <= 4 && canBeTerrestial == 0) {
                    int isIcy = x == 0 ? 1 : lowPriorityRandom.nextBetween(0, 1);
                    if (isIcy == 1) {
                        type = OrbitingBodyType.ICEPLANET;
                    } else {
                        type = OrbitingBodyType.TERRESTIAL;
                    }
                } else {
                    int isTerrestial = lowPriorityRandom.nextBetween(0, 3);
                    if (isTerrestial == 0) {
                        type = OrbitingBodyType.ICEPLANET;
                    } else if (x > outerPlanets/2) {
                        type = OrbitingBodyType.ICEGIANT;
                    } else {
                        type = OrbitingBodyType.GASGIANT;
                    }
                }
            }
            addRandomOrbitingBody(random, lowPriorityRandom, orbit, true, planetDesignRandom, type);
        }

        //comets have a very high eccentricity, halley's for instance, has an eccentricity of 0.97
        //halley's comet also has a period of 76 years though, so the eccentricity and period of our comets is a bit lower on average
        for (int x = 0; x < comets; x++) {
            float periodRaw = random.nextFloat();
            float eccentricity = (random.nextFloat()*0.2f)+0.75f;

            float period = Math.max(Math.round(32 * (((eccentricity-0.25f)) + (8*periodRaw-4))), 2);
            period *= yearLength;

            float rotation = random.nextFloat() * 360;
            float ascension = (random.nextFloat() * 180) - 90;
            float inclination = (random.nextFloat() * 180) - 90;
            float timeOffset = ((float)comets)/x;
            Orbit orbit = new Orbit(period, eccentricity, rotation, ascension, inclination, timeOffset);
            addRandomOrbitingBody(random, lowPriorityRandom, orbit, false, cometDesignRandom, OrbitingBodyType.COMET);
        }

        spaceDataManager.loadOrbitingBodyDatas();
    }

    private static int[] generateRandomColor(Random random, float hueRange, float lightnessRange, int saturationAmount, int forceHue, float forceHueAmount) {
        float offsetRange = 2*hueRange-2;
        float gradientPos = random.nextFloat();
        if (forceHue == -1) {
            gradientPos /= forceHueAmount;
        } else if (forceHue == 1) {
            gradientPos = 1 - gradientPos/forceHueAmount;
        }

        float colorRaw = random.nextFloat();
        float lightness = 255 - (colorRaw * lightnessRange);
        float saturationRaw = (colorRaw*256)%1;
        int saturation = (int)(saturationRaw*saturationRaw*saturationAmount);
        if (saturation-lightness > -96 || MathHelper.abs(gradientPos-0.5f) < 0.25f) {
            lightness = 255-((255-lightness)/2);
            saturation/=1.5f;
        }

        return new int[]{
            (int)(Math.min(offsetRange * gradientPos - hueRange + 2f, 1f)*(255-saturation)),
            (int)(lightness),
            (int)(Math.min(hueRange - offsetRange * gradientPos, 1f)*(255-saturation))
        };
    }

    private static void addRandomOrbitingBody(Random random, Random lowPriorityRandom, Orbit orbit, boolean isPlanet, IntTetrisBagRandom decorationRandom, OrbitingBodyType type) {
        float size = random.nextFloat()+1;
        float albedo = (random.nextFloat()+1)/2;
        float rotationSpeed = random.nextFloat();
        if (rotationSpeed < 0.5f) rotationSpeed--;
        int decoration = decorationRandom.get();
        int forceMainHue = 0;
        float hueForceAmount = 2f;
        if (type == OrbitingBodyType.ICEGIANT || type == OrbitingBodyType.ICEPLANET || type == OrbitingBodyType.OCEANPLANET) {
            forceMainHue = 1;
            albedo = (albedo+1)/2;
            hueForceAmount = type == OrbitingBodyType.OCEANPLANET ? 3.5f: 2.5f;
        } else {
            int forceNonIcyColor = lowPriorityRandom.nextBetween(0, 2);
            if ((forceNonIcyColor != 0 && type == OrbitingBodyType.TERRESTIAL || type == OrbitingBodyType.HABITABLE) || type == OrbitingBodyType.GASGIANT) {
                forceMainHue = -1;
                hueForceAmount = 3f;
            }
        }
        if (!isPlanet) {
            albedo /= 4;
            size = (size + 2)/12;
        } else if (type == OrbitingBodyType.GASGIANT || type == OrbitingBodyType.ICEGIANT) {
            size *= 2;
        }
        int[] mainColor = generateRandomColor(random, 0.1f, 196, 48, forceMainHue, hueForceAmount);
        int[] secondaryColor = generateRandomColor(lowPriorityRandom, 0.1f, 196, 64, 0, 2f);
        orbitingBodies.add(new OrbitingBody(orbit, size, albedo, rotationSpeed, isPlanet, decoration, mainColor, secondaryColor, type));
    }

    private static Orbit generateRandomOrbit(Random random, float period, float maxEccentricity, float maxAscension, float maxInclination, boolean isEarth) {
        float eccentricityRaw = random.nextFloat();
        float rotationRaw = random.nextFloat();
        float ascensionRaw = (random.nextFloat()*2)-1;
        float inclinationRaw = (random.nextFloat()*2)-1;

        float eccentricity = eccentricityRaw * maxEccentricity;
        float rotation = rotationRaw*360f;
        float ascension = (ascensionRaw*Math.abs(ascensionRaw))*maxAscension;
        float inclination = (inclinationRaw*Math.abs(inclinationRaw))*maxInclination;

        float timeOffset = isEarth ? 0 : random.nextFloat();

        return new Orbit(period, eccentricity, rotation, ascension, inclination, timeOffset);
    }

    public static float getStarAngle() {
        return getPositionInOrbit(starAngleMultiplier);
    }

    public static float getPositionInOrbit(float scale) {
        long time = world.getTimeOfDay();
        return (((time/24000)%earthOrbit.period) * scale) + (((time%24000)/24000.0f) * scale);
    }

    public static Long getDay() {
        long time = world.getTimeOfDay();
        return time/24000;
    }

    public static float getDayFraction() {
        long time = world.getTimeOfDay();
        return ((time%24000)/24000.0f);
    }

    public static void update() {
        if (!ready || client.player == null) return;
        boolean spyglassing = client.player.isUsingSpyglass();
        boolean toggle = client.options.pickItemKey.isPressed();

        if (spyglassing && toggle && !lastToggle) {
            toggleEditMode();
        }

        if (spyglassing && editMode == 1 && client.options.attackKey.isPressed()) {
            if (!isDrawingConstellation) SpyglassAstronomyClient.startDrawingConstellation();
        } else if (isDrawingConstellation) {
            stopDrawingConstellation();
        }

        if (spyglassing && editMode == 2 && client.options.attackKey.isPressed()) {
            selectAstralObject();
        }

        if (spyglassing && spaceRenderingManager.starsCurrentlyVisible()) {
            updateHover();
            if (isDrawingConstellation && spyglassImprovementsIsLoaded) updateDrawingConstellation();
        }

        if (!isHoldingSpyglass()) {
            editMode = 0;
        }

        lastToggle = toggle;
    }

    private static void updateHover() {
        if (editMode != 0) {
            AstralObject astralObject;
            if (editMode == 2) {
                astralObject = getNearestAstralObjectToCursor();
            } else {
                Vector3f lookVector = getLookVector();
                rotateVectorToStarRotation(lookVector);
                astralObject = new AstralObject(getNearestStar(lookVector.x, lookVector.y, lookVector.z));
            }
            if (AstralObject.isNull(astralObject)) return;
            if (astralObject.isStar) {
                Star star = astralObject.star;

                if (editMode == 2) {
                    if (star.isUnnamed()) {
                        if (star == Star.selected) {
                            sayActionBar("prompt.name.star");
                            return;
                        } else {
                            sayActionBar("prompt.unnamed.star");
                        }
                    }
                    else {
                        sayActionBar("prompt.star", star.name);
                    }
                }

                for (Constellation constellation : constellations) {
                    if (constellation.hasStar(star)) {
                        if (constellation.isUnnamed()) {
                            if (constellation == Constellation.selected) {
                                sayActionBar("prompt.name.constellation");
                            } else if (star.isUnnamed()) {
                                sayActionBar("prompt.unnamed.constellation");
                            } else {
                                sayActionBar("prompt.unnamed.constellationandstar", star.name);
                            }
                        } else if (star.isUnnamed()) {
                            sayActionBar("prompt.constellation", constellation.name);
                        } else {
                            sayActionBar("prompt.constellationandstar", constellation.name, star.name);
                        }
                        return;
                    }
                }
            } else {
                OrbitingBody orbitingBody = astralObject.orbitingBody;
                String type = orbitingBody.isPlanet ? "planet" : "comet";
                if (orbitingBody.isUnnamed()) {
                    if (orbitingBody == OrbitingBody.selected) {
                        sayActionBar("prompt.name."+type);
                    } else {
                        sayActionBar("prompt.unnamed."+type);
                    }
                }
                else sayActionBar("prompt."+type, orbitingBody.name);
            }
        }
    }

    public static void toggleEditMode() {
        editMode = (editMode+1)%3;
    }

    public static void selectAstralObject() {
        Vector3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        AstralObject astralObject = getNearestAstralObjectToCursor();
        if (astralObject == null) return;
        astralObject.select();
    }

    public static void startDrawingConstellation() {
        Vector3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.x, lookVector.y, lookVector.z);
        if (star == null) return;

        selectConstellation(star, true);

        drawingLine = new StarLine(star);
        drawingConstellation = new Constellation(drawingLine);

        isDrawingConstellation = true;

        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static float getSquaredDistance(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static void stopDrawingConstellation() {
        if (!isDrawingConstellation) return;
        isDrawingConstellation = false;

        Vector3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.x, lookVector.y, lookVector.z);

        if(star == null || !drawingLine.finishDrawing(star)) {
            return;
        }

        addStarLine(drawingLine, drawingConstellation, true, true);

        selectConstellation(star, false);
        SpaceDataManager.makeChange();

        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static void addStarLine(StarLine newLine, Constellation newConstellation, boolean canRemove, boolean sayFeedback) {
        Constellation target = null;
        int end = constellations.size();
        for (int i = 0; i < end; i++) {
            Constellation constellation = constellations.get(i);
            if (constellation.lineIntersects(newLine)) {
                if (target != null) {
                    for (StarLine line : constellation.getLines()) {
                        target.addLine(line);
                    }
                    if (sayFeedback) say("constellation.merge", target.name, constellation.name);
                    if (target.isUnnamed()) target.name = constellation.name;
                    constellations.remove(i);
                    break;
                }
                target = constellation;
            }
        }
        if (target != null) {
            if (canRemove) {
                Constellation potentialNew = target.addLineCanRemove(newLine);
                if (potentialNew != null) {
                    if (sayFeedback) say("constellation.split", target.name);
                    constellations.add(potentialNew);
                }
                if (target.getLines().size() == 0) {
                    if (sayFeedback) say("constellation.remove", target.name);
                    constellations.remove(target);
                }
            } else {
                target.addLine(newLine);
            }
        } else {
            constellations.add(newConstellation);
        }
    }

    public static void updateDrawingConstellation() {
        Vector3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.x, lookVector.y, lookVector.z);
        if (star == null || drawingLine.hasStar(star.index)) {
            drawingLine.updateDrawing(new Vector3f(lookVector.x * 100, lookVector.y * 100, lookVector.z * 100));
        } else {
            drawingLine.updateDrawing(star.getRenderedPosition());
        }
    }

    public static Vector3f getLookVector() {
        float pitch = client.player.getPitch() / 180 * MathHelper.PI;
        float yaw = client.player.getYaw() / 180 * MathHelper.PI;
        float x = -MathHelper.sin(yaw);
        float y = -MathHelper.sin(pitch);
        float z =  MathHelper.cos(yaw);
        float scale = MathHelper.cos(pitch);
        x *= scale;
        z *= scale;
        return new Vector3f(x, y, z);
    }

    public static void rotateVectorToStarRotation(Vector3f vector) {
        vector.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
        vector.rotate(RotationAxis.POSITIVE_X.rotationDegrees(getStarAngle()*-1));
        vector.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-45f));
    }

    public static void rotateVectorToOrbitingBodyRotation(Vector3f vector) {
        vector.rotate(RotationAxis.POSITIVE_Z.rotationDegrees(SpyglassAstronomyClient.getPositionInOrbit(-360f)*(1-1/SpyglassAstronomyClient.earthOrbit.period)+180));
    }

    public static Star getNearestStar(float x, float y, float z) {
        float nearestDistance = 5; //at most the nearest star can only be 2 units away, or 4 when squared
        Star nearestStar = null;

        for (Star star : stars) {
            float[] pos = star.getPosition();
            float currentDistance = getSquaredDistance(x - pos[0], y - pos[1], z - pos[2]);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestStar = star;
            }
        }

        if (nearestDistance > 0.0005f) return null;

        if (nearestStar.getCurrentNonTwinkledAlpha() < 0.1f) return null;

        return nearestStar;
    }

    public static AstralObject getNearestAstralObjectToCursor() {
        float nearestDistance = 5; //at most the nearest star can only be 2 units away, or 4 when squared
        Star nearestStar = null;
        OrbitingBody nearestOrbitingBody = null;
        boolean isStar = false;

        Vector3f lookVector = getLookVector();
        Vector3f rotatedToBody = new Vector3f(lookVector);
        rotateVectorToOrbitingBodyRotation(rotatedToBody);
        float x = rotatedToBody.x;
        float y = rotatedToBody.y;
        float z = rotatedToBody.z;

        for (OrbitingBody orbitingBody : orbitingBodies) {
            Vector3f pos = orbitingBody.getPosition();
            float currentDistance = getSquaredDistance(x - pos.x, y - pos.y, z - pos.z);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestOrbitingBody = orbitingBody;
            }
        }

        rotateVectorToStarRotation(lookVector);
        x = lookVector.x;
        y = lookVector.y;
        z = lookVector.z;

        for (Star star : stars) {
            float[] pos = star.getPosition();
            float currentDistance = getSquaredDistance(x - pos[0], y - pos[1], z - pos[2]);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestStar = star;
                isStar = true;
            }
        }

        if (nearestDistance > 0.0005f) return null;

        if (isStar) {
            if (nearestStar.getCurrentNonTwinkledAlpha() < 0.1f) return null;
            return new AstralObject(nearestStar);
        } else {
            if (nearestOrbitingBody.getCurrentNonTwinkledAlpha() < 0.1f) return null;
            return new AstralObject(nearestOrbitingBody);
        }
    }

    public static float getHeight() {
        if (client.player == null) return 128;
        return (float)client.player.getPos().y;
    }

    public static void selectConstellation(Star star, boolean clear) {
        Constellation oldSelected = Constellation.selected;
        Constellation.deselect();
        for (Constellation constellation : constellations) {
            if (constellation.hasStar(star)) {
                constellation.select();
                break;
            }
        }
        if (!clear && Constellation.selected == null && oldSelected != null) {
            oldSelected.select();
        }
    }

    private static void say(Text text) {
        client.player.sendMessage(text);
    }

    public static void say(String key, Object... args) {
        say(Text.translatable(MODID+".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Text.translatable(MODID+"."+key, args).setStyle(Style.EMPTY.withColor(textColor))));
    }

    public static void sayText(Text text) {
        say(Text.translatable(MODID+".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text));
    }

    public static void longSay(Text text) {
        client.player.sendMessage(Text.translatable(MODID+".longsay").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text));
    }

    public static void sayActionBar(String key, Object... args) {
        client.player.sendMessage(Text.translatable(MODID+"."+key, args), true);
    }

    public static void updateKnowledge() {
        knowledge.updateStarKnowledge(constellations, stars);
        int planets = 0;
        int comets = 0;
        for (OrbitingBody orbitingBody : orbitingBodies) {
            if (orbitingBody.isPlanet) planets++;
            else comets++;
        }
        knowledge.updateOrbitKnowledge(orbitingBodies, planets, comets);
    }

    public static boolean isHoldingSpyglass() {
        if (!ready || client.player == null) return false;
        return client.player.getMainHandStack().isOf(Items.SPYGLASS) || client.player.getOffHandStack().isOf(Items.SPYGLASS);
    }

    public static void setStarCount(int count) {
        starCount = count;
    }

    public static int getStarCount() {
        return starCount;
    }
}
