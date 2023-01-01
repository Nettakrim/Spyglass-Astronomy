package com.nettakrim.spyglass_astronomy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private File data = null;

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
            if (data.createNewFile()) {
                saveData();
            } else {
                useDefault = !loadData();
            }
        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to create .spyglass_astronomy directory");
        }

        if (useDefault) {
            starSeed = seedHash;
        }
    }

    public boolean loadData() {
        try {
            Scanner scanner = new Scanner(data);
            int stage = 0;
            Decoder decoder = Base64.getDecoder();
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
                        starSeed = Long.parseLong(s);
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
}
