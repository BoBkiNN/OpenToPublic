package xyz.bobkinn_.opentopublic;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.SelectorText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class OpenToPublicCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> openToPublicCommand = CommandManager.literal("opentopublic")
                .then(CommandManager.literal("owner")
                        .executes(context -> {
                            // Execute the 'owner' subcommand
                            GameProfile owner = OpenToPublic.getWorldOwner();
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (owner == null){
                                TranslatableText text = new TranslatableText("opentopublic.notstarted");
                                text.formatted(Formatting.RED);
                                player.sendMessage(text, false);
                                return 1;
                            }
                            SelectorText selector = new SelectorText(owner.getName());
                            selector.formatted(Formatting.GOLD);
                            TranslatableText text = new TranslatableText("opentopublic.ownercmd", selector);
                            text.formatted(Formatting.YELLOW);
                            context.getSource().getPlayer().sendMessage(text, false);
                            return 1;
                        }))
                .then(CommandManager.literal("setport")
                        .then(CommandManager.argument("port", IntegerArgumentType.integer(0, 65535))
                                .executes(context -> {
                                    // Execute the 'setport' subcommand
                                    int port = IntegerArgumentType.getInteger(context, "port");
                                    context.getSource().getPlayer().sendMessage(new LiteralText("Setport subcommand: " + port), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("setpublic")
                        .then(CommandManager.argument("isPublic", BoolArgumentType.bool())
                                .executes(context -> {
                                    // Execute the 'setpublic' subcommand
                                    boolean isPublic = BoolArgumentType.getBool(context, "isPublic");
                                    context.getSource().getPlayer().sendMessage(new LiteralText("Setpublic subcommand: " + isPublic), false);
                                    return 1;
                                })));
        dispatcher.register(openToPublicCommand);
    }
}

