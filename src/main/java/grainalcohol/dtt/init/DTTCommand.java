package grainalcohol.dtt.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.mental.MentalHealthStatus;
import net.depression.network.CloseEyePacket;
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
        );
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
        MentalIllnessEvent.CLOSE_EYES_EVENT.invoker().onCloseEyes(player, false);
        return 1;
    }
}
