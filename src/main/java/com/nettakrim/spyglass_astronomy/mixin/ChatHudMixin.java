package com.nettakrim.spyglass_astronomy.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    //@Inject(at = @At("TAIL"), method = "addMessage(Lnet/minecraft/text/Text;IIZ)V")
    //public void onChat(Text message, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {
    //mixin to addToMessageHistory instead of addMessage to avoid changes between 1.19 and 1.19.1
    @Inject(at = @At("TAIL"), method = "addToMessageHistory(Ljava/lang/String;)V")
    public void onChat(String message, CallbackInfo ci) {
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
                    String.format("[Spyglass Astronomy] |/[Click Here]| to add Constellation \"%s\"", constellationName),
                    "/sga:admin add constellation "+constellationData+" "+constellationName,
                    true
                );
                SpyglassAstronomyClient.say(constellationText, true);
                break;
            case 's':
                //star shared with sga:s_Name|index|
                if (secondIndex == -1) return;
                String starName = data.substring(2, firstIndex);
                int starIndex = Integer.parseInt(data.substring(firstIndex+1, secondIndex));

                Text starText = SpyglassAstronomyCommands.getClickHere(
                    String.format("[Spyglass Astronomy] |/[Click Here]| to add Star \"%s\"", starName),
                    "/sga:admin rename star "+Integer.toString(starIndex)+" "+starName,
                    true
                );
                SpyglassAstronomyClient.say(starText, true);
                break;
            case 'p':
                //planets shared with sga:p_Name|index|
                if (secondIndex == -1) return;
                String orbitingBodyName = data.substring(2, firstIndex);
                int orbitingBodyIndex = Integer.parseInt(data.substring(firstIndex+1, secondIndex));

                Text orbitingBodyText = SpyglassAstronomyCommands.getClickHere(
                    String.format("[Spyglass Astronomy] |/[Click Here]| to add Planet \"%s\"", orbitingBodyName),
                    "/sga:admin rename planet "+Integer.toString(orbitingBodyIndex)+" "+orbitingBodyName,
                    true
                );
                SpyglassAstronomyClient.say(orbitingBodyText, true);
                break;           
        }
    }
}
