package lycanite.lycanitesmobs.demonmobs.entity;

import java.util.HashMap;

import lycanite.lycanitesmobs.DropRate;
import lycanite.lycanitesmobs.demonmobs.DemonMobs;
import lycanite.lycanitesmobs.entity.EntityCreatureBase;
import lycanite.lycanitesmobs.entity.ai.EntityAIAttackMelee;
import lycanite.lycanitesmobs.entity.ai.EntityAILookIdle;
import lycanite.lycanitesmobs.entity.ai.EntityAISwimming;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetAttack;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetRevenge;
import lycanite.lycanitesmobs.entity.ai.EntityAIWander;
import lycanite.lycanitesmobs.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityTrite extends EntityCreatureBase implements IMob {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityTrite(World par1World) {
        super(par1World);
        
        // Setup:
        this.entityName = "Trite";
        this.mod = DemonMobs.instance;
        this.attribute = EnumCreatureAttribute.UNDEAD;
        this.experience = 2;
        this.hasAttackSound = true;
        
        this.eggName = "DemonEgg";
        
        this.setWidth = 0.9F;
        this.setHeight = 0.6F;
        this.setupMob();
        
        // AI Tasks:
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this).setRate(10));
        this.tasks.addTask(6, new EntityAIWander(this).setSpeed(1.0D).setPauseRate(30));
        this.tasks.addTask(10, new EntityAIWatchClosest(this).setTargetClass(EntityPlayer.class));
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.targetTasks.addTask(0, new EntityAITargetRevenge(this));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(EntityPlayer.class));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(EntityVillager.class));
        this.targetTasks.addTask(3, new EntityAITargetAttack(this).setTargetClass(EntityChicken.class));
        
        // Drops:
        this.drops.add(new DropRate(Item.silk.itemID, 1).setMinAmount(1).setMaxAmount(3));
        this.drops.add(new DropRate(Item.spiderEye.itemID, 0.5F).setMaxAmount(2));
        this.drops.add(new DropRate(Item.fermentedSpiderEye.itemID, 0.25F));
    }
    
    // ========== Stats ==========
	@Override
	protected void applyEntityAttributes() {
		HashMap<String, Double> baseAttributes = new HashMap<String, Double>();
		baseAttributes.put("maxHealth", 10D);
		baseAttributes.put("movementSpeed", 0.36D);
		baseAttributes.put("knockbackResistance", 0.0D);
		baseAttributes.put("followRange", 16D);
		baseAttributes.put("attackDamage", 1D);
        super.applyEntityAttributes(baseAttributes);
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        // Leap:
        if(this.hasAttackTarget() && this.onGround && !this.worldObj.isRemote && this.rand.nextInt(10) == 0)
        	this.leap(6.0F, 0.6D, this.getAttackTarget());
    }
    
    
    // ==================================================
   	//                      Attacks
   	// ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttackClass(Class targetClass) {
    	if(targetClass.isAssignableFrom(EntityAsmodi.class))
    		return false;
        return super.canAttackClass(targetClass);
    }
    
    // ========== Melee Attack ==========
    @Override
    public boolean meleeAttack(Entity target, double damageScale) {
    	if(!super.meleeAttack(target, damageScale))
    		return false;
    	
    	// Wither:
        if(target instanceof EntityLivingBase) {
            byte effectSeconds = 5;
            if(this.worldObj.difficultySetting > 1)
                if (this.worldObj.difficultySetting == 2)
                	effectSeconds = 7;
                else if (this.worldObj.difficultySetting == 3)
                	effectSeconds = 10;
            if(effectSeconds > 0)
                ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.wither.id, effectSeconds * 20, 0));
        }
        
        return true;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    // ========== Movement ==========
    @Override
    public boolean canClimb() { return true; }
    
    
    // ==================================================
  	//                     Immunities
  	// ==================================================
    // ========== Damage ==========
    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect) {
        if(par1PotionEffect.getPotionID() == Potion.wither.id) return false;
        return super.isPotionApplicable(par1PotionEffect);
    }
    
    @Override
    public boolean canBurn() { return false; }
    
    // ========== Environmental ==========
    @Override
    public boolean webproof() { return true; }
}