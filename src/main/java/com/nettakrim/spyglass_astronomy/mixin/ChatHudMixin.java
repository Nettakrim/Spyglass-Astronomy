package com.nettakrim.spyglass_astronomy.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(at = @At("TAIL"), method = "addMessage(Lnet/minecraft/text/Text;IIZ)V")
    public void onChat(Text messageText, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {
        String message = messageText.getString();
        int sgaIndex = message.indexOf("sga:");
        if (sgaIndex == -1) return;
        
        String data = message.substring(sgaIndex+4);
        int firstIndex = data.indexOf("|");
        if (firstIndex == -1) return;
        int secondIndex = data.indexOf("|", firstIndex+1);
        data = data.substring(0, secondIndex == -1 ? firstIndex : secondIndex);
        if (data.charAt(1) != '_') return;


        switch (data.charAt(0)) {
            case 'c':
                //constellation shared with sga:c_Name|AAAA|
                if (secondIndex == -1) return;
                String constellationName = data.substring(2, firstIndex);
                String constellationData = data.substring(firstIndex+1, secondIndex);
                
                Text constellationText = SpyglassAstronomyCommands.getClickHere(
                    "commands.share.receive.constellation",
                    "/sga:admin add constellation "+constellationData+" "+constellationName,
                    true,
                    constellationName
                );
                SpyglassAstronomyClient.sayText(constellationText, true);
                break;
            case 's':
                //star shared with sga:s_Name|index|
                if (secondIndex == -1) return;
                String starName = data.substring(2, firstIndex);
                int starIndex = -1;
                try {
                    starIndex = Integer.parseInt(data.substring(firstIndex+1, secondIndex));
                } catch (Exception e) {
                    break;
                }

                Text starText = SpyglassAstronomyCommands.getClickHere(
                    "commands.share.receive.star",
                    "/sga:admin rename star "+Integer.toString(starIndex)+" "+starName,
                    true,
                    starName
                );
                SpyglassAstronomyClient.sayText(starText, true);
                break;
            case 'p':
                //planets shared with sga:p_Name|index|
                if (secondIndex == -1) return;
                String orbitingBodyName = data.substring(2, firstIndex);
                int orbitingBodyIndex = -1;
                try {
                    orbitingBodyIndex = Integer.parseInt(data.substring(firstIndex+1, secondIndex));
                } catch (Exception e) {
                    break;
                }

                if (orbitingBodyIndex >= SpyglassAstronomyClient.orbitingBodies.size()) break;

                OrbitingBody orbitingBody = SpyglassAstronomyClient.orbitingBodies.get(orbitingBodyIndex);

                Text orbitingBodyText = SpyglassAstronomyCommands.getClickHere(
                    "commands.share.receive."+(orbitingBody.isPlanet ? "planet" : "comet"),
                    "/sga:admin rename planet "+Integer.toString(orbitingBodyIndex)+" "+orbitingBodyName,
                    true,
                    orbitingBodyName
                );
                SpyglassAstronomyClient.sayText(orbitingBodyText, true);
                break;           
        }
    }
}
