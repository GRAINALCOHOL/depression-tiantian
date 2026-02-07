package grainalcohol.dtt.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.mental.MentalHealthStatus;
import net.depression.client.ClientMentalIllness;
import net.depression.client.ClientMentalStatus;
import net.depression.client.DepressionClient;
import net.depression.mental.MentalStatus;
import net.depression.network.CloseEyePacket;
import net.depression.server.Registry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class DTTCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            triggerCommand(dispatcher);
        });
    }

    private static void triggerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("depression")
                .then(literal("trigger")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(literal("close_eyes")
                                .executes(context -> triggerCloseEyes(context, false))
                                .then(literal("force")
                                        .executes(context -> triggerCloseEyes(context, true))
                                )
                        )
                )
                .then(literal("check")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(literal("combat_status").executes(DTTCommand::checkCombatStatus))
                        .then(literal("is_close_eyes").executes(DTTCommand::checkIsCloseEyes))
                )
        );
    }

    private static int checkIsCloseEyes(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        boolean isCloseEyes = DepressionClient.clientMentalStatus.mentalIllness.isCloseEye;
        player.sendMessage(Text.literal(isCloseEyes ? "Eyes Closed" : "Eyes Open"));

        return 1;
    }

    private static int checkCombatStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        player.sendMessage(Text.literal(mentalStatus.combatCountdown > 0 ? "In Combat" : "Not in Combat"));

        return 1;
    }

    private static int triggerCloseEyes(CommandContext<ServerCommandSource> context, boolean force) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        if (!force) {
            if (player.isCreative() || player.isSpectator()) {
                source.sendError(Text.translatable("commands.dtt.trigger.close_eyes.no_effect"));
                return 0;
            }
            if (!MentalHealthStatus.from(player).isDepressed()) {
                source.sendError(Text.translatable("commands.dtt.trigger.close_eyes.not_depressed"));
                return 0;
            }
        }

        CloseEyePacket.sendToPlayer(player);
        SymptomEvent.CLOSE_EYES_EVENT.invoker().onCloseEyes(player, false);
        return 1;
    }
}
