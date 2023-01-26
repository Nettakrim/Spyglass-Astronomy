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

import com.nettakrim.spyglass_astronomy.OrbitingBody.OrbitingBodyType;
import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;

import java.util.ArrayList;
import java.util.Stack;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

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

    private static Stack<Text> sayBuffer = new Stack<Text>();

    public static Knowledge knowledge;

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
        if (spaceDataManager != null) spaceDataManager.saveData();

        world = clientWorld;

        stars = new ArrayList<Star>();
        constellations = new ArrayList<Constellation>();
        orbitingBodies = new ArrayList<OrbitingBody>();

        spaceDataManager = new SpaceDataManager(clientWorld);

        generateSpace(false);

        knowledge = new Knowledge();
        updateKnowledge();
    }

    public static void generateSpace(boolean reset) {
        Random random = Random.create(0);
        generateStars(random, reset);
        generatePlanets(random, reset);

        spaceRenderingManager = new SpaceRenderingManager();
        spaceRenderingManager.updateSpace(0);
    }

    public static void generateStars(Random random, boolean reset) {
        if (random == null) {
            random = Random.create(spaceDataManager.getStarSeed());
        } else {
            random.setSeed(spaceDataManager.getStarSeed());
        }

        if (reset) {
            stars = new ArrayList<Star>();
            constellations = new ArrayList<Constellation>();
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

            float alphaRaw = random.nextFloat();
            float alpha = Math.max(MathHelper.sqrt(alphaRaw*sizeRaw),(2*sizeRaw-1.5f)/(alphaRaw+0.5f));
            alpha = (alpha + (alpha*alpha))/2;

            int [] color = generateRandomColor(random, 0.8f, 20, 0);

            float rotationSpeed = (random.nextFloat() * 2f)-1;
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

    public static void generatePlanets(Random random, boolean reset) {
        if (random == null) {
            random = Random.create(spaceDataManager.getPlanetSeed());
        } else {
            random.setSeed(spaceDataManager.getPlanetSeed());
        }
        //things with less importance and *could* change in the future and not be too bad like exact color that use their own random
        Random lowPriorityRandom = Random.create(spaceDataManager.getPlanetSeed());
        
        if (reset) {
            orbitingBodies = new ArrayList<OrbitingBody>();
        }

        IntTetrisBagRandom planetDesignRandom = new IntTetrisBagRandom(random, 3);
        IntTetrisBagRandom cometDesignRandom = new IntTetrisBagRandom(random, 2);

        //always atleast 5 planets, and always atleast 2 outer planets
        //between 1 and 3 inner planets
        int innerPlanets = random.nextInt(3)+1;
        //between 4 and 8 outer planets for 1 inner, between 2 and 8 for 3 inner
        int outerPlanets = random.nextBetween(5-innerPlanets, 8);

        int comets = random.nextBetween(4, 6);

        float yearLength = spaceDataManager.getYearLength();
        
        //earth will have a largely circular orbit
        earthOrbit = generateRandomOrbit(random, yearLength, 0.05f, 10f, true);
    
        starAngleMultiplier = ((yearLength + 1) / yearLength) * 360f;

        //inner planets are spaced rougly evenly, and rounded to 8ths of an earth year
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
            
            Orbit orbit = generateRandomOrbit(random, period, 0.1f, 20f, false);
            addRandomOrbitingBody(random, lowPriorityRandom, orbit, true, planetDesignRandom, type);
        }

        //outer planets rougly double in period each planet, further out planets will have slightly more irregular orbits
        float[] periodOffsets = new float[] {0.75f, 1f, 1f, 1.25f};
        for (float x = 0; x < outerPlanets; x++) {
            float period = (yearLength * (2 << ((int)x+1))) * periodOffsets[random.nextInt(periodOffsets.length)];
            float settingsMultiplier = (x/8)+1;
            Orbit orbit = generateRandomOrbit(random, period, Math.min(0.15f*settingsMultiplier,0.5f), Math.min(20f*settingsMultiplier,60f), false);
            
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

        //comets have a very high eccentrity, halley's for instance, has an eccentricity of 0.97
        //halleys comet also has a period of 76 years though, so the eccentricity and period of our comets is a bit lower on average
        for (int x = 0; x < comets; x++) {
            float periodRaw = random.nextFloat();
            float eccentricity = (random.nextFloat()*0.2f)+0.75f;

            float period = Math.max(Math.round(32 * (((eccentricity-0.25f)) + (8*periodRaw-4))), 2);
            period *= yearLength;

            float rotation = random.nextFloat() * 360;
            float inclination = (random.nextFloat() * 180) - 90;
            float timeOffset = random.nextFloat();
            Orbit orbit = new Orbit(period, eccentricity, rotation, inclination, timeOffset);
            addRandomOrbitingBody(random, lowPriorityRandom, orbit, false, cometDesignRandom, OrbitingBodyType.COMET);
        }

        spaceDataManager.loadOrbitingBodyDatas();
    }

    private static int[] generateRandomColor(Random random, float hueRange, float lightnessRange, int forceHue) {
        float offsetRange = 2*hueRange-2;
        float gradientPos = random.nextFloat();
        if (forceHue == -1) {
            gradientPos /= 2f;
        } else if (forceHue == 1) {
            gradientPos = 1 - gradientPos/2f;
        }

        return new int[]{
            (int)(Math.min(offsetRange * gradientPos - hueRange + 2f, 1f)*255),
            (int)(255 - (random.nextFloat() * lightnessRange)),
            (int)(Math.min(hueRange - offsetRange * gradientPos, 1f)*255)
        };
    }

    private static void addRandomOrbitingBody(Random random, Random lowPriorityRandom, Orbit orbit, boolean isPlanet, IntTetrisBagRandom decorationRandom, OrbitingBodyType type) {
        float size = random.nextFloat()+1;
        float albedo = (random.nextFloat()+1)/2;
        float rotationSpeed = random.nextFloat();
        if (rotationSpeed < 0.5f) rotationSpeed--;
        int decoration = decorationRandom.get();
        int forceMainHue = 0;
        if (type == OrbitingBodyType.ICEGIANT || type == OrbitingBodyType.ICEPLANET || type == OrbitingBodyType.OCEANPLANET) {
            forceMainHue = 1;
            albedo = (albedo+1)/2;
        } else {
            int forceNonIcyColor = lowPriorityRandom.nextBetween(0, 2);
            if ((forceNonIcyColor != 0 && type == OrbitingBodyType.TERRESTIAL || type == OrbitingBodyType.HABITABLE) || type == OrbitingBodyType.GASGIANT) {
                forceMainHue = -1;
            }
        }
        if (!isPlanet) {
            albedo /= 4;
            size = (size + 2)/12;
        }
        if (type == OrbitingBodyType.GASGIANT || type == OrbitingBodyType.ICEGIANT) {
            size *= 2;
        }
        int[] mainColor = generateRandomColor(random, 0.1f, 196, forceMainHue);
        int[] secondaryColor = generateRandomColor(lowPriorityRandom, 0.1f, 196, 0);
        orbitingBodies.add(new OrbitingBody(orbit, size, albedo, rotationSpeed, isPlanet, decoration, mainColor, secondaryColor, type));
    }

    private static Orbit generateRandomOrbit(Random random, float period, float maxEccentricity, float maxInclination, boolean isEarth) {
        float eccentricityRaw = random.nextFloat();
        float rotationRaw = random.nextFloat();
        float inclinationRaw = (random.nextFloat()*2)-1;

        float eccentricity = eccentricityRaw * maxEccentricity;
        float rotation = rotationRaw*360f;
        float inclination = (inclinationRaw*Math.abs(inclinationRaw))*maxInclination;

        float timeOffset = isEarth ? 0 : random.nextFloat();

        return new Orbit(period, eccentricity, rotation, inclination, timeOffset);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }

    public static float getStarAngle() {
        return getPositionInOrbit(starAngleMultiplier);
    }

    public static float getPositionInOrbit(float scale) {
        Long time = world.getTimeOfDay();
        return ((float)((time/24000)%earthOrbit.period) * scale) + (((time%24000)/24000.0f) * scale);
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

        while (!sayBuffer.empty()) {
            say(sayBuffer.pop(), false);
        }
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
                if (orbitingBody.isPlanet) {
                    sayActionBar(orbitingBody.name == null ? "Use /sga:name to name this Planet" : orbitingBody.name);
                }
            }
        }
    }

    public static void startUsingSpyglass() {
        editMode = 0;
        zoom = 0;
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
            }
            if (target.getLines().size() == 0) {
                say(String.format("Removed Constellation \"%s\"", target.name));
                constellations.remove(target);
            }
        } else {
            constellations.add(drawingConstellation);
        }
        selectConstellation(star, false);
        SpaceDataManager.makeChange();

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
        vector.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(getStarAngle()*-1));
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

    public static void say(Text text, boolean buffer) {
        if (buffer) {
            sayBuffer.add(text);
        } else {
            client.player.sendMessage(text);
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

    public static void updateKnowledge() {
        knowledge.updateStarKnowledge(constellations, stars);
        knowledge.updateOrbitKnowledge(orbitingBodies, orbitingBodies.size(), 0);
    }

    public static String getMoonPhaseName(int phase) {
        switch (phase) {
            case 0:
                return "▉ Full Moon";
            case 1:
                return "▜ Waning Gibbous";
            case 2:
                return "▐ Last Quarter";
            case 3:
                return " ] Waning Cresent";
            case 4:
                return "[] New Moon";
            case 5:
                return "[ Waxing Crescent";
            case 6:
                return "▌ First Quarter";
            case 7:
                return "▛ Waxing Gibbous";
            default:
                return "? Unkown";
        }
    }

    public static boolean isHoldingSpyglass() {
        if (!ready) return false;
        return client.player.getMainHandStack().isOf(Items.SPYGLASS) || client.player.getOffHandStack().isOf(Items.SPYGLASS);
    }

    public static void setStarCount(int count) {
        starCount = count;
    }

    public static int getStarCount() {
        return starCount;
    }
}
