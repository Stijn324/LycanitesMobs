package lycanite.lycanitesmobs.api.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import lycanite.lycanitesmobs.AssetManager;
import lycanite.lycanitesmobs.LycanitesMobs;
import lycanite.lycanitesmobs.ObjectManager;
import lycanite.lycanitesmobs.api.ILycaniteMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCustomSpawnEgg extends Item {
	public ILycaniteMod mod;
	public String itemName = "CustomSpawnEgg";
	public String texturePath = "customspawn";
    
	// ==================================================
	//                    Constructor
	// ==================================================
    public ItemCustomSpawnEgg(int itemID) {
        super(itemID - 256);
        this.setHasSubtypes(true);
        setCreativeTab(LycanitesMobs.creativeTab);
        setUnlocalizedName("CustomSpawnEgg");
    }
    
	// ==================================================
	//                  Get Display Name
	// ==================================================
    @Override
    public String getItemDisplayName(ItemStack par1ItemStack) {
        String s = ("" + StatCollector.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
        String s1 = ObjectManager.entityLists.get(this.mod.getDomain()).getStringFromID(par1ItemStack.getItemDamage());

        if (s1 != null)
            s = s + " " + StatCollector.translateToLocal("entity." + s1 + ".name");

        return s;
    }
    
    
	// ==================================================
	//                   Get Egg Color
	// ==================================================
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        EntityEggInfo entityegginfo = (EntityEggInfo)ObjectManager.entityLists.get(this.mod.getDomain()).entityEggs.get(Integer.valueOf(par1ItemStack.getItemDamage()));
        return entityegginfo != null ? (par2 == 0 ? entityegginfo.primaryColor : entityegginfo.secondaryColor) : 16777215;
    }
    
    
	// ==================================================
	//                     Item Use
	// ==================================================
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int blockID = world.getBlockId(x, y, z);
        
        // Edit Spawner:
        if(blockID == Block.mobSpawner.blockID) {
        	TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        	if(tileEntity != null && tileEntity instanceof TileEntityMobSpawner) {
        		TileEntityMobSpawner spawner = (TileEntityMobSpawner)tileEntity;
        		spawner.getSpawnerLogic().setMobID(ObjectManager.entityLists.get(this.mod.getDomain()).getStringFromID(itemStack.getItemDamage()));
        	}
        }
        
        // Spawn Mob:
        else if(!world.isRemote) {
	        x += Facing.offsetsXForSide[side];
	        y += Facing.offsetsYForSide[side];
	        z += Facing.offsetsZForSide[side];
	        double d0 = 0.0D;
	        
	        if(side == 1 && Block.blocksList[blockID] != null && Block.blocksList[blockID].getRenderType() == 11)
	            d0 = 0.5D;
	        
	        Entity entity = spawnCreature(world, itemStack.getItemDamage(), (double)x + 0.5D, (double)y + d0, (double)z + 0.5D);
	        
	        if(entity != null) {
	            if(entity instanceof EntityLivingBase && itemStack.hasDisplayName())
	                ((EntityLiving)entity).setCustomNameTag(itemStack.getDisplayName());
	
	            if(!player.capabilities.isCreativeMode)
	                --itemStack.stackSize;
	        }
        }

        return true;
    }
    
    
	// ==================================================
	//                   On Right Click
	// ==================================================
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if(par2World.isRemote)
            return par1ItemStack;
        else {
            MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);

            if(movingobjectposition == null)
                return par1ItemStack;
            else
                if(movingobjectposition.typeOfHit == EnumMovingObjectType.TILE) {
                    int i = movingobjectposition.blockX;
                    int j = movingobjectposition.blockY;
                    int k = movingobjectposition.blockZ;

                    if(!par2World.canMineBlock(par3EntityPlayer, i, j, k))
                        return par1ItemStack;

                    if(!par3EntityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, par1ItemStack))
                        return par1ItemStack;

                    if(par2World.getBlockMaterial(i, j, k) == Material.water) {
                        Entity entity = spawnCreature(par2World, par1ItemStack.getItemDamage(), (double)i, (double)j, (double)k);

                        if(entity != null)
                            if(entity instanceof EntityLivingBase && par1ItemStack.hasDisplayName())
                                ((EntityLiving)entity).setCustomNameTag(par1ItemStack.getDisplayName());

                            if(!par3EntityPlayer.capabilities.isCreativeMode)
                                --par1ItemStack.stackSize;
                    }
                }

                return par1ItemStack;
        }
    }
    
    
	// ==================================================
	//                   Spawn Creature
	// ==================================================
    public Entity spawnCreature(World par0World, int par1, double par2, double par4, double par6) {
        if(!ObjectManager.entityLists.get(this.mod.getDomain()).entityEggs.containsKey(Integer.valueOf(par1)))
            return null;
        else {
            Entity entity = null;

            for(int j = 0; j < 1; ++j) {
                entity = ObjectManager.entityLists.get(this.mod.getDomain()).createEntityByID(par1, par0World);

                if(entity != null && entity instanceof EntityLivingBase) {
                    EntityLiving entityliving = (EntityLiving)entity;
                    entity.setLocationAndAngles(par2, par4, par6, MathHelper.wrapAngleTo180_float(par0World.rand.nextFloat() * 360.0F), 0.0F);
                    entityliving.rotationYawHead = entityliving.rotationYaw;
                    entityliving.renderYawOffset = entityliving.rotationYaw;
                    entityliving.onSpawnWithEgg((EntityLivingData)null);
                    par0World.spawnEntityInWorld(entity);
                    entityliving.playLivingSound();
                }
            }

            return entity;
        }
    }
    
    
	// ==================================================
	//                      Visuals
	// ==================================================
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }
    
    // ========== Get Icon ==========
    @SideOnly(Side.CLIENT)
    public Icon getIconFromDamageForRenderPass(int par1, int par2) {
        return par2 > 0 ? AssetManager.getIcon(this.itemName + "_overlay") : AssetManager.getIcon(this.itemName);
    }
    
    // ========== Register Icon ==========
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister) {
    	AssetManager.addIcon(this.itemName, this.mod.getDomain(), texturePath, iconRegister);
    	AssetManager.addIcon(this.itemName + "_overlay", this.mod.getDomain(), texturePath + "_overlay", iconRegister);
    }
    
    
	// ==================================================
	//                   Get Sub Items
	// ==================================================
    @SideOnly(Side.CLIENT)
    public void getSubItems(int itemID, CreativeTabs creativeTabs, List par3List) {
    	if(this.mod == null || !ObjectManager.entityLists.containsKey(this.mod.getDomain()))
    		return;
    	
    	HashMap entityEggs = ObjectManager.entityLists.get(this.mod.getDomain()).entityEggs;
    	if(entityEggs.size() <= 0)
    		return;
    	
        Iterator iterator = entityEggs.values().iterator();
        while(iterator.hasNext()) {
            EntityEggInfo entityegginfo = (EntityEggInfo)iterator.next();
            par3List.add(new ItemStack(itemID, 1, entityegginfo.spawnedID));
        }
    }
}