package com.nettakrim.spyglass_astronomy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;

import java.util.ArrayList;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class SpyglassAstronomyClient implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "spyglass_astronomy";
	public static final Logger LOGGER = LoggerFactory.getLogger("Spyglass Astronomy");

    public static boolean ready;

    private static final int starCount = 1024; //encoding will break at 4097, so stay at 4096 and below :)

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

    private static float starAngleMultiplier;

	@Override
	public void onInitializeClient() {
        client = MinecraftClient.getInstance();

        SpyglassAstronomyCommands.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {update();});
	}

    public static void saveSpace() {
        if (spaceDataManager != null) {
            spaceDataManager.saveData();
        }
    }

    public static void loadSpace(ClientWorld clientWorld) {
        world = clientWorld;

        stars = new ArrayList<>();
        constellations = new ArrayList<>();
        orbitingBodies = new ArrayList<OrbitingBody>();

        spaceDataManager = new SpaceDataManager(clientWorld);

        generateSpace();
    }

    public static void generateSpace() {
        Random random = Random.create(0);
        generateStars(random);
        generatePlanets(random);

        spaceRenderingManager = new SpaceRenderingManager();
        spaceRenderingManager.updateSpace(0);
    }

    public static void generateStars(Random random) {
        random.setSeed(spaceDataManager.getStarSeed());

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
            distance = MathHelper.fastInverseSqrt(distance);
            posX *= distance;
            posY *= distance;
            posZ *= distance;

            float sizeRaw = random.nextFloat();
            if (currentStars % 2 == 0) {
                float galaxyCloseness = (0.12f/(MathHelper.abs(posX)+0.1f))-0.2f;
                if (galaxyCloseness > 0) sizeRaw = (1-galaxyCloseness)*sizeRaw + galaxyCloseness*((sizeRaw*sizeRaw)/2);
            }
            float size = 0.15f + sizeRaw * 0.2f;
            float rotationSpeed = (random.nextFloat() * 2f)-1;

            float range = 0.8f;
            float offsetRange = 2*range-2;
            float gradientPos = random.nextFloat();
            float alphaRaw = random.nextFloat();
            float alpha = Math.max(MathHelper.sqrt(alphaRaw*sizeRaw),(2*sizeRaw-1.5f)/(alphaRaw+0.5f));
            alpha = (alpha + (alpha*alpha))/2;

            int[] color = new int[]{
                (int)(Math.min(offsetRange * gradientPos - range + 2f, 1f)*255),
                (int)(255 - random.nextFloat() * 20),
                (int)(Math.min(range - offsetRange * gradientPos, 1f)*255)
            };

            float twinkleSpeed = random.nextFloat()*0.025f+0.035f;

            stars.add(new Star(currentStars, posX, posY, posZ, size, rotationSpeed, color, alpha, twinkleSpeed));

            currentStars++;
        }

        ready = true;

        for (Constellation constellation : constellations) {
            constellation.initaliseStarLines();
        }

        spaceDataManager.loadStarDatas();
    }

    public static void generatePlanets(Random random) {
        random.setSeed(spaceDataManager.getPlanetSeed());

        //always atleast 5 planets, and always atleast 2 outer planets, inner planets not guaranteed
        //between 0 and 3 inner planets
        int innerPlanets = random.nextInt(4);
        //between 5 and 8 outer planets for 0 inner, between 2 and 8 for 3 inner
        int outerPlanets = random.nextBetween(5-innerPlanets, 8);

        //earth will often have a year of 4 lunar cycles (32 days, 10 realtime hours), but theres a chance to have some sligthly more irregular years
        //float[] yearTimesInLunarCycles = new float[] {4,4,4,4,4,4,4,4,4,4,4,4,3,3,3,3,5,5,5,5,3.5f,4.5f};
        //float yearLength = yearTimesInLunarCycles[random.nextInt(yearTimesInLunarCycles.length)]*8;
        float[] yearTimes = new float[] {8,8,8,8,8,8,8,8,8,8,8,8,6,6,6,6,10,10,10,10,7,9};
        float yearLength = yearTimes[random.nextInt(yearTimes.length)];

        //earth will have a largely circular orbit
        earthOrbit = generateRandomOrbit(random, yearLength, 0.05f, 20f);
    
        starAngleMultiplier = ((yearLength + 1) / yearLength) * 360f;

        //inner planets are spaced rougly evenly, and rounded to 8ths of an earth year
        float innerRoundAmount = 8;
        while (innerPlanets >= innerRoundAmount) innerRoundAmount *= 2;

        float innerDistanceRange = 1f/innerPlanets;
        float[] innerPlanetPeriods = new float[innerPlanets];
        for (float x = 0; x < innerPlanets; x++) {
            float minPeriod = (x/innerPlanets);
            float maxPeriod = (x/innerPlanets)+innerDistanceRange;
            float unRoundedPeriod = scale01Between(random.nextFloat(), minPeriod, maxPeriod);
            float period = (MathHelper.floor(unRoundedPeriod*(innerRoundAmount-1))+1)/innerRoundAmount;
            for (int y = 0; y < x; y++) {
                if (innerPlanetPeriods[y] == period) {
                    period += 1f/innerRoundAmount;
                }
            }
            innerPlanetPeriods[(int)x] = period;
            period *= yearLength;
            Orbit orbit = generateRandomOrbit(random, period, 0.1f, 20f);
            addRandomOrbitingBody(random, orbit);
        }

        //outer planets rougly double in period each planet, further out planets will have slightly more irregular orbits
        float[] periodOffsets = new float[] {0.75f, 1f, 1f, 1.25f};
        for (float x = 0; x < outerPlanets; x++) {
            float period = (yearLength * (2 << ((int)x+1))) * periodOffsets[random.nextInt(periodOffsets.length)];
            float settingsMultiplier = (x/8)+1;
            Orbit orbit = generateRandomOrbit(random, period, Math.min(0.15f*settingsMultiplier,0.5f), Math.min(20f*settingsMultiplier,60f));
            addRandomOrbitingBody(random, orbit);
        }

        spaceDataManager.loadOrbitingBodyDatas();
    }

    private static float scale01Between(float x, float valueAt0, float valueAt1) {
        return (1-x)*valueAt0 + x*valueAt1;
    }

    private static void addRandomOrbitingBody(Random random, Orbit orbit) {
        float size = random.nextFloat()+1;
        float albedo = (random.nextFloat()+1)/2;
        float rotationSpeed = random.nextFloat();
        if (rotationSpeed < 0.5f) rotationSpeed--;
        orbitingBodies.add(new OrbitingBody(orbit, size, albedo, rotationSpeed));
    }

    private static Orbit generateRandomOrbit(Random random, float period, float maxEccentricity, float maxInclination) {
        float eccentricityRaw = random.nextFloat();
        float rotationRaw = random.nextFloat();
        float inclinationRaw = (random.nextFloat()*2)-1;

        float eccentricity = eccentricityRaw * maxEccentricity;
        float rotation = rotationRaw*360f;
        float inclination = (inclinationRaw*Math.abs(inclinationRaw))*maxInclination;

        return new Orbit(period, eccentricity, rotation, inclination);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }

    public static float getStarAngleMultiplier() {
        return getPositionInOrbit(starAngleMultiplier);
    }

    public static float getPositionInOrbit(float scale) {
        Long time = world.getTimeOfDay();
        return ((float)((time/24000)%(long)earthOrbit.period) * scale) + (((time%24000)/24000.0f) * scale);
    }

    public static Long getDay() {
        Long time = world.getTimeOfDay();
        return time/24000;        
    }

    public static float getDayFraction() {
        Long time = world.getTimeOfDay();
        return ((time%24000)/24000.0f); 
    }

    public static void update() {
        if (!ready) return;
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

        if (spyglassing) {
            updateHover();
        }
        lastToggle = toggle;
    }

    private static void updateHover() {
        if (editMode != 0) {
            AstralObject astralObject;
            if (editMode == 2) {
                astralObject = getNearestAstralObjectToCursor();
            } else {
                Vec3f lookVector = getLookVector();
                rotateVectorToStarRotation(lookVector);
                astralObject = new AstralObject(getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ()));
            }
            if (AstralObject.isNull(astralObject)) return;
            if (astralObject.isStar) {
                Star star = astralObject.star;
                for (Constellation constellation : constellations) {
                    if (constellation.hasStar(star)) {
                        if (constellation.name.equals("Unnamed")) {
                            sayActionBar("Use /sga:name to name this Constellation!");
                        } else if (star.name == null) {
                            sayActionBar(constellation.name);
                        } else {
                            sayActionBar(constellation.name+" | "+star.name);
                        }
                        return;
                    }
                }

                if (editMode == 2) sayActionBar(star.name == null ? "Use /sga:name to name this Star" : star.name);
            } else {
                OrbitingBody orbitingBody = astralObject.orbitingBody;
                sayActionBar(orbitingBody.name == null ? "Use /sga:name to name this Planet" : orbitingBody.name);
            }
        }
    }

    public static void startUsingSpyglass() {
        editMode = 0;
    }

    public static void toggleEditMode() {
        editMode = (editMode+1)%3;
    }

    public static void selectStar() {
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());
        if (star == null) return;
        star.select();
    }

    public static void selectAstralObject() {
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        AstralObject astralObject = getNearestAstralObjectToCursor();
        if (astralObject == null) return;
        astralObject.select();        
    }

    public static void startDrawingConstellation() {
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());
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

        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());

        if(star == null || !drawingLine.finishDrawing(star)) {
            return;
        }

        Constellation target = null;
        int end = constellations.size();
        for (int i = 0; i < end; i++) {
            Constellation constellation = constellations.get(i);
            if (constellation.lineIntersects(drawingLine)) {
                if (target != null) {
                    for (StarLine line : constellation.getLines()) {
                        target.addLine(line);
                    }
                    say(String.format("New line merged two Constellations \"%s\" and \"%s\"", Constellation.selected.name, target.name));
                    constellations.remove(i);
                    break;
                }
                target = constellation;
            }
        }
        if (target != null) {
            Constellation potentialNew = target.addLineCanRemove(drawingLine);
            if (potentialNew != null) {
                say(String.format("Removing Line split Constellation \"%s\" into two", target.name));
                constellations.add(potentialNew);
            }
            if (target.getLines().size() == 0) constellations.remove(target);
        } else {
            constellations.add(drawingConstellation);
        }
        selectConstellation(star, false);

        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static void updateDrawingConstellation() {
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());
        if (star == null || drawingLine.hasStar(star.index)) {
            drawingLine.updateDrawing(new Vec3f(lookVector.getX() * 100, lookVector.getY() * 100, lookVector.getZ() * 100));
        } else {
            drawingLine.updateDrawing(star.getRenderedPosition());
        }
    }

    public static Vec3f getLookVector() {
        float pitch = client.player.getPitch() / 180 * MathHelper.PI;
        float yaw = client.player.getYaw() / 180 * MathHelper.PI;
        float x = -MathHelper.sin(yaw);
        float y = -MathHelper.sin(pitch);
        float z =  MathHelper.cos(yaw);
        float scale = MathHelper.cos(pitch);
        x *= scale;
        z *= scale;
        return new Vec3f(x, y, z);        
    }

    public static void rotateVectorToStarRotation(Vec3f vector) {
        vector.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        vector.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(getStarAngleMultiplier()*-1));
        vector.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45f));
    }

    public static void rotateVectorToOrbitingBodyRotation(Vec3f vector) {
        vector.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(SpyglassAstronomyClient.getPositionInOrbit(-360f)*(1-1/SpyglassAstronomyClient.earthOrbit.period)+180));
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

        Vec3f lookVector = getLookVector();
        Vec3f rotatedToBody = lookVector.copy();
        rotateVectorToOrbitingBodyRotation(rotatedToBody);
        float x = rotatedToBody.getX();
        float y = rotatedToBody.getY();
        float z = rotatedToBody.getZ();

        for (OrbitingBody orbitingBody : orbitingBodies) {
            Vec3f pos = orbitingBody.getPosition();
            float currentDistance = getSquaredDistance(x - pos.getX(), y - pos.getY(), z - pos.getZ());
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestOrbitingBody = orbitingBody;
            }
        }

        rotateVectorToStarRotation(lookVector);
        x = lookVector.getX();
        y = lookVector.getY();
        z = lookVector.getZ();

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

    public static void say(String message) {
        Text text = Text.of("[Spyglass Astronomy] "+message);
        client.player.sendMessage(text);
    }

    public static void longSay(String message) {
        Text text = Text.of("- [Spyglass Astronomy] -\n"+message);
        client.player.sendMessage(text);
    }

    public static void sayActionBar(String message) {
        Text text = Text.of(message);
        client.player.sendMessage(text, true);        
    }

    public static String getMoonPhaseName(int phase) {
        switch (phase) {
            case 0:
                return "Full Moon";
            case 1:
                return "Waning Gibbous";
            case 2:
                return "Last Quarter";
            case 3:
                return "Waning Cresent";
            case 4:
                return "New Moon";
            case 5:
                return "Waxing Crescent";
            case 6:
                return "First Quarter";
            case 7:
                return "Waxing Gibbous";
            default:
                return "Unkown";
        }
    }

    public static boolean isHoldingSpyglass() {
        if (!ready) return false;
        return client.player.getMainHandStack().isOf(Items.SPYGLASS) || client.player.getOffHandStack().isOf(Items.SPYGLASS);
    }
}
