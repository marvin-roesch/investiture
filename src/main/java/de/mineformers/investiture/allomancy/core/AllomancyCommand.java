package de.mineformers.investiture.allomancy.core;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * ${JDOC}
 */
public class AllomancyCommand extends CommandBase
{
    private static final String ACTION_ADD = "grant";
    private static final String ACTION_REMOVE = "take";
    private static final String ACTION_ALL = "mistborn";

    @Override
    public String getCommandName()
    {
        return Allomancy.DOMAIN;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return Allomancy.DOMAIN + ".commands.manage.usage";
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        switch (args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, ACTION_ADD, ACTION_REMOVE, ACTION_ALL);
            case 2:
                if (!ACTION_ALL.equalsIgnoreCase(args[0]))
                    return getListOfStringsMatchingLastWord(args, AllomancyAPIImpl.INSTANCE.getMistingNames());
            case 3:
                if (ACTION_ALL.equalsIgnoreCase(args[0]) && args.length == 3)
                    return super.getTabCompletionOptions(server, sender, args, pos);
                return getListOfStringsMatchingLastWord(args, server.getAllUsernames());
        }
        return super.getTabCompletionOptions(server, sender, args, pos);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return args.length > 2 && index == 1;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw new WrongUsageException(getCommandUsage(sender));
        String action = args[0];
        switch (action)
        {
            case ACTION_ADD:
            case ACTION_REMOVE:
                handleSingle(server, sender, action, Arrays.copyOfRange(args, 1, args.length));
                break;
            case ACTION_ALL:
                handleAll(server, sender, action, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private void handleSingle(MinecraftServer server, ICommandSender sender, String action, String[] args) throws CommandException
    {
        EntityPlayer player = args.length > 1 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
        Optional<Allomancer> allomancer = AllomancyAPIImpl.INSTANCE.toAllomancer(player);
        if (!allomancer.isPresent())
            throw new CommandException(Allomancy.DOMAIN + ".commands.manage.no_allomancer");
        String powerName = args[0];
        Optional<Class<? extends Misting>> power = AllomancyAPIImpl.INSTANCE.getMistingType(powerName);
        if (!power.isPresent())
            throw new CommandException(Allomancy.DOMAIN + ".commands.manage.invalid_power");
        switch (action)
        {
            case ACTION_ADD:
                allomancer.get().grantPower(power.get());
                break;
            case ACTION_REMOVE:
                allomancer.get().takePower(power.get());
                break;
        }
    }

    private void handleAll(MinecraftServer server, ICommandSender sender, String action, String[] args) throws CommandException
    {
        EntityPlayer player = args.length > 0 ? getPlayer(server, sender, args[0]) : getCommandSenderAsPlayer(sender);
        Optional<Allomancer> allomancer = AllomancyAPIImpl.INSTANCE.toAllomancer(player);
        if (!allomancer.isPresent())
            throw new CommandException(Allomancy.DOMAIN + ".commands.manage.no_allomancer");
        Metals.BASE_METALS.forEach(m -> allomancer.get().grantPower(m.mistingType()));
        sender.addChatMessage(new TextComponentTranslation(Allomancy.DOMAIN + ".commands.manage.granted_all", sender.getDisplayName()));
    }
}
