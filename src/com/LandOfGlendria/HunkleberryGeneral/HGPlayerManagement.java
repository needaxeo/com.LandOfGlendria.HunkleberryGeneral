package com.LandOfGlendria.HunkleberryGeneral;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class HGPlayerManagement
{

	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft");
	private HGMessageManagement msg;

	public HGPlayerManagement(HGMessageManagement msg)
	{
		this.msg = msg;
	}

	public String healPlayer(Player player)
	{
		player.setHealth(HGStatics.MAX_HEALTH.intValue());
		return null;
	}

	public String setDisplayName(Player player, String name, String colorArray)
	{
		StringBuffer displayName = new StringBuffer((new StringBuilder(" ")).append(HGStatics.NO_COLOR).append(name).toString());
		int color = 0;
		int index = 0;
		int cumulative = 3;
		if(colorArray != null)
		{
			String as[];
			int j = (as = colorArray.split("/")).length;
			for(int i = 0; i < j; i++)
			{
				String indexAndColor = as[i];
				if(indexAndColor != null)
				{
					String pair[] = indexAndColor.split(",");
					if(pair.length == 2)
					{
						try
						{
							index = Integer.parseInt(pair[0]);
							if(index < 1 || index > name.length())
							{
								return msg.formatInvalidArgs(index, "Index out of bounds");
							}
						}
						catch(NumberFormatException e)
						{
							return msg.formatInvalidArgs(pair[0], "Invalid index");
						}
						try
						{
							color = Integer.parseInt(pair[1]);
						}
						catch(NumberFormatException e)
						{
							return msg.formatInvalidArgs(pair[1], "Invalid color");
						}
						if(ChatColor.getByCode(color) != null)
						{
							displayName.insert((index + cumulative) - 1, ChatColor.getByCode(color));
							cumulative += 2;
						} else
						{
							return msg.formatInvalidArgs(color, "Invalid color");
						}
					} else
					{
						return msg.formatInvalidArgs(indexAndColor, "Invalid index.color pair");
					}
				}
			}

		}
		displayName.append(HGStatics.NO_COLOR);
		displayName.append(" ");
		player.setDisplayName(displayName.toString());
		msg.sendPositiveMessage(player, (new StringBuilder("Set display name to ")).append(displayName.toString()).toString());
		return null;
	}

	public Block getFloor(Block block)
	{
		Block testingBlock = block;
		Location loc = null;
		int blockHeight = (int)block.getLocation().getY();
		for(int height = blockHeight; height > 3; height--)
		{
			loc = testingBlock.getLocation();
			loc.setY(height);
			testingBlock = block.getWorld().getBlockAt(loc);
			if(testingBlock.getFace(BlockFace.DOWN).getTypeId() != Material.AIR.getId())
			{
				return testingBlock.getFace(BlockFace.UP);
			}
		}

		return null;
	}

	public String teleport(Player player, double x, double y, double z)
	{
		player.teleportTo(new Location(player.getWorld(), x, y, z));
		return null;
	}

	public String getLocation(Player player)
	{
		Location loc = player.getLocation();
		msg.sendPositiveMessage(player, (new StringBuilder("Loc: ")).append(loc.getX()).append(",").append(loc.getY()).append(",").append(loc.getZ()).append(").").toString());
		return null;
	}

	public String stratum(Player player, int strataToJump)
	{
		HashSet<Byte> blocks = new HashSet<Byte>();
		Material amaterial[];
		int strataCount = (amaterial = Material.values()).length;
		for(int i = 0; i < strataCount; i++)
		{
			Material mat = amaterial[i];
			blocks.add(Byte.valueOf((byte)mat.getId()));
		}

		List<Block> blockList = player.getLineOfSight(blocks, 250);
		Location loc = null;
		strataCount = 1;
		boolean solidFound = false;
		Iterator<Block> iterator = blockList.iterator();
		while(iterator.hasNext()) 
		{
			Block block = (Block)iterator.next();
			if(block.getY() < 3 || block.getY() > 123)
			{
				break;
			}
			if(solidFound && block.getTypeId() == Material.AIR.getId())
			{
				if(strataCount == strataToJump)
				{
					if(block.getFace(BlockFace.UP).getTypeId() != Material.AIR.getId())
					{
						continue;
					}
					loc = getFloor(block).getFace(BlockFace.DOWN).getLocation();
					if(loc != null)
					{
						break;
					}
				} else
				{
					solidFound = false;
					strataCount++;
				}
			} else
			if(block.getTypeId() != Material.AIR.getId() && block.getTypeId() != Material.LAVA.getId() && block.getTypeId() != Material.STATIONARY_LAVA.getId() && block.getType().isBlock())
			{
				solidFound = true;
			}
		}
		if(loc != null)
		{
			msg.sendPositiveMessage(player, "Air found, going there.");
			player.teleportTo(loc);
			return null;
		} else
		{
			return "Unable to find air.";
		}
	}
}
