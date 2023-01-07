package com.nettakrim.spyglass_astronomy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import com.nettakrim.spyglass_astronomy.mixin.BiomeAccessAccessor;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;

public class SpaceDataManager {
    public static final int SAVE_FORMAT = 0;

    private long starSeed;
    private long planetSeed;

    private File data = null;

    public ArrayList<StarData> starDatas;

    public SpaceDataManager(ClientWorld world) {
        //https://github.com/Johni0702/bobby/blob/d2024a2d63c63d0bccf2eafcab17dd7bf9d26710/src/main/java/de/johni0702/minecraft/bobby/FakeChunkManager.java#L86
        long seedHash = ((BiomeAccessAccessor) world.getBiomeAccess()).getSeed();
        boolean useDefault = true;
        Path storagePath = SpyglassAstronomyClient.client.runDirectory
                .toPath()
                .resolve(".spyglass_astronomy")
                .resolve(getCurrentWorldOrServerName());
        
        try {
            Files.createDirectories(storagePath);
            data = new File(storagePath.toString()+"/"+seedHash + ".txt");
            if (!data.createNewFile()) {
                useDefault = !loadData();
            }
        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to create .spyglass_astronomy directory");
        }

        if (useDefault) {
            starSeed = seedHash;
            planetSeed = seedHash;
            saveData();
        }
    }

    public boolean loadData() {
        try {
            Scanner scanner = new Scanner(data);
            int stage = 0;
            Decoder decoder = Base64.getDecoder();
            int starIndex = 0;
            starDatas = new ArrayList<StarData>();
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();
                if (s.equals("---")) {
                    stage++;
                    continue;
                }
                switch (stage) {
                    case 0:
                        //format
                        break;
                    case 1:
                        String[] seeds =  s.split(" ");
                        if (seeds.length == 1) {
                            starSeed = Long.parseLong(s);
                            planetSeed = starSeed;
                        } else {
                            starSeed = Long.parseLong(seeds[0]);
                            planetSeed = Long.parseLong(seeds[1]);                         
                        }
                        break;
                    case 2:
                        Constellation constellation = new Constellation();
                        String[] parts = s.split(" \\| ");
                        constellation.name = parts[0];
                        String lines = parts[1];
                        for (int x = 0; x < lines.length(); x+=5) {
                            constellation.addLine(decodeStarLine(decoder, lines.substring(x, x+5)));
                        }
                        SpyglassAstronomyClient.constellations.add(constellation);
                        break;
                    case 3:
                        int split = s.indexOf(' ');
                        starIndex += Integer.parseInt(s.substring(0, split));
                        String name = s.substring(split+1);
                        starDatas.add(new StarData(starIndex, name));
                        break;
                }
            }           
            scanner.close();
            return true;
        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to load data");
        }
        return false;
    }

    public void saveData() {
        try {
            FileWriter writer = new FileWriter(data);
            StringBuilder s = new StringBuilder("Spyglass Astronomy - Format: "+SAVE_FORMAT);
            s.append("\n---\n");
            s.append(starSeed);
            if (planetSeed != starSeed) {
                s.append(' ');
                s.append(planetSeed);
            }
            s.append("\n---");
            Encoder encoder = Base64.getEncoder();
            for (Constellation constellation : SpyglassAstronomyClient.constellations) {
                s.append('\n');
                s.append(constellation.name+" | ");
                for (StarLine line : constellation.getLines()) {
                    s.append(encodeStarLine(encoder, line.getStars()));
                }
            }
            s.append("\n---");
            int lastIndex = 0;
            for (Star star : SpyglassAstronomyClient.stars) {
                if (star.name != null) {
                    s.append('\n');
                    s.append(Integer.toString(star.index-lastIndex)+" "+star.name);
                    lastIndex = star.index;
                }
            }            
            s.append("\n---");

            writer.write(s.toString());
            writer.close();
        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to save data");
        }
    }

    public String encodeStarLine(Encoder encoder, Star[] stars) {
        int starA = stars[0].index;
        int starB = stars[1].index;
        int combined = starA + (starB << 12);
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(combined);
        byte[] array = bb.array();
        return encoder.encodeToString(array).substring(1,6);
    }

    public StarLine decodeStarLine(Decoder decoder, String s) {
        byte[] array = decoder.decode("A"+s+"==");
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.put(array);
        bb.rewind();
        int combined = bb.getInt();
        int starB = combined >> 12;
        int starA = combined - (starB << 12);
        return new StarLine(starA, starB, false);
    }

    public long getStarSeed() {
        return starSeed;
    }

    public long getPlanetSeed() {
        return planetSeed;
    }

    private static String getCurrentWorldOrServerName() {
        //https://github.com/Johni0702/bobby/blob/d2024a2d63c63d0bccf2eafcab17dd7bf9d26710/src/main/java/de/johni0702/minecraft/bobby/FakeChunkManager.java#L342
        IntegratedServer integratedServer = SpyglassAstronomyClient.client.getServer();
        if (integratedServer != null) {
            return integratedServer.getSaveProperties().getLevelName();
        }

        // Needs to be before the ServerInfo because that one will contain a random IP
        if (SpyglassAstronomyClient.client.isConnectedToRealms()) {
            return "realms";
        }

        ServerInfo serverInfo = SpyglassAstronomyClient.client.getCurrentServerEntry();
        if (serverInfo != null) {
            return serverInfo.address.replace(':', '_');
        }

        return "unknown";
    }

    public void loadStarDatas() {
        if (starDatas == null) return;
        for (StarData starData : starDatas) {
            SpyglassAstronomyClient.stars.get(starData.index).name = starData.name;
        }
        starDatas = null;
    }

    public class StarData {
        public int index;
        public String name;

        public StarData(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }
}
