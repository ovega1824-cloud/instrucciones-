package com.ciber.instrucciones.background;

import com.ciber.instrucciones.background.packet.OpenMenuPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Anade /ins backgraund (y el alias /ins background) al comando /ins original.
 * Brigadier junta los dos registros del literal "ins", asi que los subcomandos
 * originales (show, hide, set, image, timer, reset) siguen funcionando igual.
 */
@Mod.EventBusSubscriber(modid = "instrucciones")
public final class BackgroundCommand {
    private BackgroundCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        for (String literal : new String[] {"backgraund", "background"}) {
            dispatcher.register(Commands.literal("ins")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.literal(literal).executes(BackgroundCommand::openMenu)));
        }
    }

    private static int openMenu(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        BackgroundNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenMenuPacket());
        return 1;
    }
}
