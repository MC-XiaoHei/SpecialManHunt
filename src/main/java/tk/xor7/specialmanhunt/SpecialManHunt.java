package tk.xor7.specialmanhunt;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xor7studio.util.Xor7IO;
import xor7studio.util.Xor7Runnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpecialManHunt implements ModInitializer {
    public static MinecraftServer server;
    public ServerPlayerEntity speedRunner;
    public Set<ServerPlayerEntity> hunters;
    public Vec3d overWorldPos,netherPos,theEndPos;
    public Team hunter;
    public boolean inWorld(@NotNull ServerPlayerEntity player, RegistryKey<World> world){
        return player.getWorld() == server.getWorld(world);
    }
    public void refreshCompass(@NotNull ServerPlayerEntity player){
        PlayerInventory inv=player.getInventory();
        Vec3d pos=getRunnerPos(player);
        if(pos==null) return;
        ItemStack item=new ItemStack(Items.COMPASS,1);
        NbtCompound nbt = new NbtCompound(),
                    nbtPos = new NbtCompound();
        String world="";
        if(inWorld(player,World.OVERWORLD))world="overworld";
        else if(inWorld(player,World.NETHER))world="the_nether";
        else if(inWorld(player,World.END))world="the_end";
        else player.sendMessage(Text.literal("?????????????????????"));
        nbtPos.putInt("X", (int) pos.x);
        nbtPos.putInt("Y", (int) pos.y);
        nbtPos.putInt("Z", (int) pos.z);
        nbt.putString("xot7tag","ManHuntCompass");
        nbt.putString("LodestoneDimension",world);
        nbt.put("LodestonePos",nbtPos);
        nbt.putBoolean("LodestoneTracked",false);
        item.setNbt(nbt);
        boolean flag=false;
        for(int i=0;i<inv.size();i++){
            ItemStack tmp=inv.getStack(i);
            NbtCompound compound=tmp.getNbt();
            if(compound!=null &&
               compound.contains("xot7tag") &&
               compound.getString("xot7tag").equals("ManHuntCompass")){
                item.setCount(1);
                if(flag) inv.removeStack(i);
                else inv.setStack(i,item);
                flag=true;
            }
        }
        if(!flag) player.giveItemStack(item);
        else player.sendMessage(Text.literal("??????????????????????????????????????????"));
    }
    public Vec3d getRunnerPos(@NotNull ServerPlayerEntity player){
        Vec3d pos=null;
        if(inWorld(player,World.OVERWORLD) && overWorldPos!=null)pos=overWorldPos;
        else if(inWorld(player,World.NETHER) && netherPos!=null)pos=netherPos;
        else if(inWorld(player,World.END) && theEndPos!=null)pos=theEndPos;
        else player.sendMessage(Text.literal("???????????????????????????????????????????????????"));
        return pos;
    }
    public void reload(){
        speedRunner=null;
        hunters=new HashSet<>();
        overWorldPos=netherPos=theEndPos=null;
    }
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("runner")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    server=context.getSource().getServer();
                                    ServerPlayerEntity tmp=server.getPlayerManager().getPlayer(name);
                                    if(tmp==null)
                                        Objects.requireNonNull(context.getSource().getPlayer()).sendMessage(Text.literal("?????????????????????"));
                                    speedRunner=tmp;
                                    CopyOnWriteArrayList<net.minecraft.server.network.ServerPlayerEntity> list= new CopyOnWriteArrayList<>(server.getPlayerManager().getPlayerList());
                                    for(ServerPlayerEntity player:list)
                                        player.sendMessage(Text.literal("??????"+ speedRunner.getName().getString()+"??????????????????"));
                                    return 1;
                                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("hunter")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    server=context.getSource().getServer();
                                    ServerPlayerEntity tmp=server.getPlayerManager().getPlayer(name);
                                    if(tmp==null)
                                        Objects.requireNonNull(context.getSource().getPlayer()).sendMessage(Text.literal("?????????????????????"));
                                    hunters.add(tmp);
                                    if(hunter==null){
                                        hunter=server.getScoreboard().addTeam("hunter");
                                        hunter.setPrefix(Text.literal("[??????]"));
                                    }
                                    server.getScoreboard().addPlayerToTeam(tmp.getEntityName(),hunter);
                                    CopyOnWriteArrayList<net.minecraft.server.network.ServerPlayerEntity> list= new CopyOnWriteArrayList<>(server.getPlayerManager().getPlayerList());
                                    for(ServerPlayerEntity player:list)
                                        player.sendMessage(Text.literal("??????"+ tmp.getName().getString()+"???????????????"));
                                    return 1;
                                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("remove")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    server=context.getSource().getServer();
                                    ServerPlayerEntity tmp=server.getPlayerManager().getPlayer(name);
                                    if(tmp==null)
                                        Objects.requireNonNull(context.getSource().getPlayer()).sendMessage(Text.literal("?????????????????????"));
                                    hunters.add(tmp);
                                    server.getScoreboard().removePlayerFromTeam(tmp.getEntityName(),hunter);
                                    CopyOnWriteArrayList<net.minecraft.server.network.ServerPlayerEntity> list= new CopyOnWriteArrayList<>(server.getPlayerManager().getPlayerList());
                                    for(ServerPlayerEntity player:list)
                                        player.sendMessage(Text.literal("??????"+ Objects.requireNonNull(tmp).getName().getString()+"??????????????????"));
                                    return 1;
                                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("reloadGame")
                        .requires(source -> source.hasPermissionLevel(1))
                                .executes(context -> {
                                    server=context.getSource().getServer();
                                    reload();
                                    return 1;
                                })));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("compass")
                        .executes(context -> {
                            server=context.getSource().getServer();
                            ServerPlayerEntity player=context.getSource().getPlayer();
                            if(player==null) context.getSource().sendMessage(Text.literal("?????????????????????????????????"));
                            else {
                                player.sendMessage(Text.literal("?????????????????????"));
                                refreshCompass(player);
                            }
                            return 1;
                        })));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            NbtCompound compound=player.getStackInHand(hand).getNbt();
            if(!player.isSpectator() &&
                    compound!=null &&
                    compound.contains("xot7tag") &&
                    compound.getString("xot7tag").equals("ManHuntCompass"))
                refreshCompass((ServerPlayerEntity) player);
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        reload();
        Xor7IO.modId="Xor7SpecialManhunt";
        new Xor7Runnable(){
            @Override
            public void run() {
                if(speedRunner==null) return;
                if(inWorld(speedRunner,World.OVERWORLD))overWorldPos=speedRunner.getPos();
                else if(inWorld(speedRunner,World.NETHER))netherPos=speedRunner.getPos();
                else if(inWorld(speedRunner,World.END))theEndPos=speedRunner.getPos();
            }
        }.start(100);
    }
}
