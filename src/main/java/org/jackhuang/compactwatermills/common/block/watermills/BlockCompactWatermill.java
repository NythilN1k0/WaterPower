package org.jackhuang.compactwatermills.common.block.watermills;

import java.util.ArrayList;

import gregtech.api.interfaces.IDebugableBlock;

import org.jackhuang.compactwatermills.InternalName;
import org.jackhuang.compactwatermills.common.block.BlockMeta;
import org.jackhuang.compactwatermills.common.block.BlockMultiID;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public class BlockCompactWatermill extends BlockMeta
	implements IDebugableBlock {
	
	public BlockCompactWatermill() {
		super(InternalName.cptBlockCompactWatermill, Material.iron, ItemCompactWaterMill.class);
		
		GameRegistry.registerTileEntity(TileEntityWatermill.class,
				"cptwtrml.watermill");		
	}
	
	@Override
	protected int getTextureIndex(IBlockAccess iBlockAccess, int x, int y, int z, int meta) {
		TileEntity tTileEntity = iBlockAccess.getTileEntity(x, y, z);
		if(tTileEntity instanceof TileEntityWatermill) {
			if(((TileEntityWatermill)tTileEntity).getType() == null) return meta;
			return ((TileEntityWatermill)tTileEntity).getType().ordinal();
		}
		return meta;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int var2) {
		return WaterType.makeTileEntity(0);
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return WaterType.makeTileEntity(metadata);
	}
	
	@Override
	protected String getTextureFolder(int index) {
		return "watermill";
	}

	@Override
	protected int maxMetaData() {
		return WaterType.values().length;
	}

	@Override
	public ArrayList<String> getDebugInfo(EntityPlayer aPlayer, int aX, int aY,
			int aZ, int aLogLevel) {
		ArrayList<String> al = new ArrayList<String>();
		TileEntity tileEntity = aPlayer.worldObj.getTileEntity(aX, aY, aZ);
		if(tileEntity instanceof TileEntityWatermill) {
			TileEntityWatermill te = (TileEntityWatermill) tileEntity;
			if(te.getType() == null)
				al.add("Type: null");
			else
				al.add("Type: " + te.getType().name());
			al.add("Output: " + te.getOfferedEnergy());
			al.add("Range: " + te.getRange());
		} else {
			al.add("Not a watermill tile entity.");
		}
		return al;
	}
	
	
	
}
