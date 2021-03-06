package com.LandOfGlendria.HunkleberryGeneral;

import java.text.DateFormat;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HGMessageManagement {

	private static final Logger log = Logger.getLogger("Minecraft");
	private Plugin plugin;
	private HGCommandDAO commandDAO;

	public HGMessageManagement(Plugin plugin,HGCommandDAO commandDAO) {
		this.plugin = plugin;
		this.commandDAO = commandDAO;
	}
	
	public String encodeString(String unsafe) throws UnsupportedEncodingException {
		return URLEncoder.encode(unsafe,HGStatics.UTF8);
	}
	
	public String decodeString(String unsafe) throws UnsupportedEncodingException {
		return URLDecoder.decode(unsafe,HGStatics.UTF8);
	}

	public String[] messageSegmenter(String message, int notUSed) {
		char chars[] = message.toCharArray();
		int defaultWidth = HGStatics.MAX_MESSAGE_LENGTH.intValue();
		int minLength = Math.abs(defaultWidth / 2);
		int lastChar = chars.length - 1;
		int lookingAtChar = 0;
		int lastGood = 0;
		int visibleCount = 0;
		int startingAt = 0;
		int messageCount = 0;
		int lastPossible = 0;
		String messages[] = new String[lastChar / minLength + 1];
		String lastColor = new String();
		while (lookingAtChar < lastChar) {
			startingAt = lookingAtChar;
			visibleCount = 0;
			lastPossible = lookingAtChar;
			lastGood = lookingAtChar;
			StringBuffer currentMessage = new StringBuffer();
			currentMessage.append(lastColor);
			while (visibleCount < defaultWidth + 1 && lookingAtChar < lastChar + 1) {
				switch (chars[lookingAtChar]) {
				case 32: // ' '
					lastGood = lookingAtChar;
					visibleCount++;
					lookingAtChar++;
					break;

				case 44: // ','
					lastGood = lookingAtChar;
					visibleCount++;
					lookingAtChar++;
					break;

				case 45: // '-'
					lastGood = lookingAtChar;
					visibleCount++;
					lookingAtChar++;
					break;

				case 46: // '.'
					lastGood = lookingAtChar;
					visibleCount++;
					lookingAtChar++;
					break;

				case 167:
					if (chars.length > lookingAtChar + 2) {
						lastColor = String.copyValueOf(chars, lookingAtChar, 2);
						lookingAtChar += 2;
						break;
					}

				default:
					visibleCount++;
					lastPossible = lookingAtChar;
					lookingAtChar++;
					break;
				}
			}
			if (lookingAtChar >= lastChar) {
				lastGood = lastChar;
				lastPossible = lastChar;
			}
			if (visibleCount > minLength) {
				currentMessage.append(chars, startingAt, (lastGood - startingAt) + 1);
				lookingAtChar = lastGood + 1;
			} else {
				currentMessage.append(chars, startingAt, (lastPossible - startingAt) + 1);
				lookingAtChar = lastPossible + 1;
			}
			messages[messageCount] = currentMessage.toString();
			messageCount++;
		}
		return messages;
	}

	public void sendSegmented(Player player, String message) {
		if (message.length() <= HGStatics.MAX_MESSAGE_LENGTH.intValue()) {
			player.sendMessage(message);
		} else {
			String lines[] = messageSegmenter(message, 0);
			String as[];
			int j = (as = lines).length;
			for (int i = 0; i < j; i++) {
				String line = as[i];
				if (line != null) {
					player.sendMessage(line);
				}
			}

		}
	}

	public String stripColor(String message) {
		String clear = message.replaceAll("("+HGStatics.COLOR_IND+".)", "");
		return clear;
	}

	public void sendNegativeMessage(Player player, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append(HGStatics.ERROR_COLOR);
		sb.append(message);
		sendSegmented(player, sb.toString());
	}

	public void sendPositiveMessage(Player player, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append(HGStatics.POSITIVE_COLOR);
		sb.append(message);
		sendSegmented(player, sb.toString());
	}

	public String formatInvalidArgs(String arg, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("Invalid Argument [");
		sb.append(arg);
		sb.append("]: ");
		sb.append(message);
		sb.append(".");
		return sb.toString();
	}

	public String formatInvalidArgs(int arg, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("Invalid Argument [");
		sb.append((new Integer(arg)).toString());
		sb.append("]: ");
		sb.append(message);
		sb.append(".");
		return sb.toString();
	}

	public void sendColorUsage(Player player, String command, String arguments) {
		StringBuffer sb = new StringBuffer();
		sb.append("Usage: ");
		sb.append(HGStatics.COMMAND_COLOR);
		sb.append("/");
		sb.append(command);
		sb.append(" ");
		sb.append(HGStatics.ARGUMENTS_COLOR);
		sb.append(arguments);
		sendSegmented(player, sb.toString());
	}

	public void sendColorErrorUsage(Player player, String error, String command, String arguments, String helpCommand) {
		sendSegmented(player, (new StringBuilder(String.valueOf(HGStatics.ERROR_COLOR))).append(error).toString());
		StringBuffer sb = new StringBuffer();
		sb.append("Usage: ");
		sb.append(HGStatics.COMMAND_COLOR);
		sb.append("/");
		sb.append(command);
		sb.append(" ");
		sb.append(HGStatics.ARGUMENTS_COLOR);
		sb.append(arguments);
		sb.append(HGStatics.NO_COLOR);
		sb.append(" See ");
		sb.append(HGStatics.WARNING_COLOR);
		sb.append("/");
		sb.append(helpCommand);
		sb.append(" ");
		sb.append(command);
		sb.append(HGStatics.NO_COLOR);
		sb.append(" for more information.");
		sendSegmented(player, sb.toString());
	}

	public void sendCommandList(Player player) {
		StringBuffer sb = new StringBuffer();
		boolean allowedToUse = true;
		for (HGCommand command : HGCommand.values()) {
			if (!player.isOp() && commandDAO.getOpsOnly(command).booleanValue()) {
				if (HGConfig.permissions == null) {
					allowedToUse = false;
				} else if (!HGConfig.permissions.has(player, commandDAO.getPermissions(command))) {
					allowedToUse = false;
				}
			}
			if (allowedToUse) {
				sb.append(HGStatics.NO_COLOR);
				sb.append("[");
				sb.append(HGStatics.WARNING_COLOR);
				sb.append("/");
				sb.append(commandDAO.getCommand(command));
				sb.append(HGStatics.NO_COLOR);
				sb.append("] ");
			}
		}
		sendPositiveMessage(player,sb.toString());
		return;
	}
	
	public void sendColorHelp(Player player, HGCommand command, boolean allowed) {
		StringBuffer sb = new StringBuffer();
		sb.append("Usage: ");
		sb.append(HGStatics.COMMAND_COLOR);
		sb.append("/");
		sb.append(commandDAO.getCommand(command));
		sb.append(" ");
		sb.append(HGStatics.ARGUMENTS_COLOR);
		sb.append(commandDAO.getCommandArgs(command));
		sb.append(" ");
		sb.append(HGStatics.NO_COLOR);
		sb.append(commandDAO.getCommandUsage(command));
		sb.append(" Settings: ");
		sb.append(HGStatics.WARNING_COLOR);
		sb.append("[opsOnly=");
		sb.append(commandDAO.getOpsOnly(command).toString());
		sb.append("] [serverAllowed=");
		sb.append(commandDAO.getServerAllowed(command).toString());
		sb.append("] [permissions=");
		sb.append(commandDAO.getPermissions(command));
		sb.append("] ");
		if (commandDAO.getServerAllowed(command).booleanValue()) {
			if (allowed) {
				sb.append(HGStatics.POSITIVE_COLOR);
				sb.append("Usable.");
			} else {
				sb.append(HGStatics.ERROR_COLOR);
				sb.append("Not usable.");
			}
		} else {
			sb.append(HGStatics.ERROR_COLOR);
			sb.append("Not usable.");
		}

		sendSegmented(player, sb.toString());
	}

	public String concatinateRemainingArgs(String stringArray[], int index) {
		int indexCount = 0;
		String message = new String("");
		String as[];
		int j = (as = stringArray).length;
		for (int i = 0; i < j; i++) {
			String cat = as[i];
			if (indexCount > index) {
				message = message.concat(" ");
			}
			if (indexCount >= index) {
				message = message.concat(cat);
			}
			indexCount++;
		}

		return message;
	}

	public String sendColorList(Player player) {
		StringBuffer sb = new StringBuffer();
		int indexCount = 0;	
		for (ChatColor color : ChatColor.values()) {
			sb.append(color);
			sb.append("[");
			sb.append(indexCount);
			sb.append("] ");
			indexCount++;
		}
		sendPositiveMessage(player,sb.toString());
		return null;
	}

	public String getPlayerList(World world,int qualifier) {
		Player[] players = plugin.getServer().getOnlinePlayers();
		StringBuilder sb = new StringBuilder();
		sb.append(HGStatics.NO_COLOR);
		for (Player player : players) {
			if (world != null && !player.getWorld().equals(world)) {
				continue;
			}
			sb.append("[");
			if (player.isOp()){
				sb.append(HGStatics.ERROR_COLOR);
			} else {
				sb.append(HGStatics.POSITIVE_COLOR);
			}
			sb.append(player.getName());
			if (qualifier == 1) {
				if (!(player.getName().equals(player.getDisplayName()))) {
					sb.append("/");
					sb.append(player.getDisplayName());
				}
				sb.append("/");
				sb.append(player.getWorld().getName());
			}
			sb.append(HGStatics.NO_COLOR);
			sb.append("] ");
		}
		return sb.toString();
	}

	public String getWorldList(Player player) {
		ArrayList<World> worlds = (ArrayList<World>) plugin.getServer().getWorlds();
		StringBuffer sb = new StringBuffer();
		for (Iterator<World> iterator = worlds.iterator(); iterator.hasNext(); sb.append("] ")) {
			World world = (World) iterator.next();
			sb.append(HGStatics.NO_COLOR);
			sb.append("[");
			if (world.getEnvironment() == org.bukkit.World.Environment.NORMAL) {
				sb.append(HGStatics.POSITIVE_COLOR);
			} else {
				sb.append(HGStatics.ERROR_COLOR);
			}
			sb.append(world.getName());
			sb.append(HGStatics.NO_COLOR);
		}
		return sb.toString();
	}

	public String getMaterialList(String material) {
		StringBuilder sb = new StringBuilder();
		for (Material mat : Material.values()) {
			if (mat.toString().toLowerCase().matches(".*("+material.toLowerCase()+").*")) {
				sb.append("[");
				sb.append(HGStatics.WARNING_COLOR);
				sb.append(mat.toString());
				sb.append(HGStatics.NO_COLOR);
				sb.append("] ");
			}
		}
		return sb.toString();
	}

	public String getLocationList(HashSet<HGLocation> locations) {
		StringBuilder sb = new StringBuilder();
		for (HGLocation location : locations) {
			sb.append(HGStatics.NO_COLOR);
			sb.append("[");
			sb.append(HGStatics.WARNING_COLOR);
			sb.append(location.locationName);
			sb.append(HGStatics.NO_COLOR);
			sb.append("(");
			sb.append(location.worldName);
			sb.append(")] ");
		}
		return sb.toString();
	}

	public void info(String message) {
		log.info((new StringBuilder(String.valueOf(HGStatics.LOG_PREFIX))).append(message).toString());
	}

	public void warn(String message) {
		log.info((new StringBuilder(String.valueOf(HGStatics.LOG_PREFIX))).append(message).toString());
	}

	public void severe(String message) {
		log.info((new StringBuilder(String.valueOf(HGStatics.LOG_PREFIX))).append(message).toString());
	}

	public void parseMotdForPlayer(Player player,String motd) {
		//just a series of replaces, I guess
		int beginToken = -1;
		int endToken = -1;
		String[] lines = motd.split(HGStatics.MOTD_EOL);
		for (String line : lines) {
			int initialToken = 0;
			StringBuilder newline = new StringBuilder(line);
			while (true) {
				try {
					initialToken = findToken(newline,HGStatics.MOTD_SPECIAL_CHARS,0);
					String testString = newline.toString();
					if (initialToken == -1) {
						break; //to next line
					}

					//one of these for each token
					//colors
					newline = replaceBasic(newline,HGStatics.MOTD_AQUA ,ChatColor.AQUA .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_BLACK ,ChatColor.BLACK .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_BLUE ,ChatColor.BLUE .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_AQUA ,ChatColor.DARK_AQUA .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_BLUE ,ChatColor.DARK_BLUE .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_GRAY ,ChatColor.DARK_GRAY .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_GREEN ,ChatColor.DARK_GREEN .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_PURPLE ,ChatColor.DARK_PURPLE .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_DARK_RED ,ChatColor.DARK_RED .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_GOLD ,ChatColor.GOLD .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_GRAY ,ChatColor.GRAY .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_GREEN ,ChatColor.GREEN .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_LIGHT_PURPLE ,ChatColor.LIGHT_PURPLE .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_RED ,ChatColor.RED .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_WHITE ,ChatColor.WHITE .toString());
					newline = replaceBasic(newline,HGStatics.MOTD_YELLOW ,ChatColor.YELLOW .toString());

					//server
					newline = replaceBasic(newline,HGStatics.MOTD_NUMP,String.valueOf(plugin.getServer().getOnlinePlayers().length));
					newline = replaceBasic(newline,HGStatics.MOTD_MAXP,String.valueOf(plugin.getServer().getMaxPlayers()));
					newline = replaceBasic(newline,HGStatics.MOTD_WORLDS,getWorldList(player));
					newline = replaceBasic(newline,HGStatics.MOTD_DATE,
							DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()));
					newline = replaceBasic(newline,HGStatics.MOTD_PPL,getPlayerList((World)null,0));

					//player
					newline = replaceBasic(newline,HGStatics.MOTD_NAME,player.getName());
					newline = replaceBasic(newline,HGStatics.MOTD_ALIAS,player.getDisplayName());
					newline = replaceBasic(newline,HGStatics.MOTD_ADDR,player.getAddress().toString());

					//more complex tokens

					//set item in hand
					for (String specialChar : HGStatics.MOTD_SPECIAL_CHARS) {
						if ((beginToken = findToken(newline,(specialChar + HGStatics.MOTD_HOLD),0)) != -1) {
							if ((endToken = findToken(newline,specialChar,beginToken + 1)) != -1) {
								String material = newline.substring(beginToken + (specialChar + HGStatics.MOTD_HOLD).length(),endToken);
								HGInventoryManagement.setItemInHand(player, material, 1);
								newline.delete(beginToken, endToken+1);
							} else {
								warn("Error in " + HGStatics.MOTD_FILE + " Unable to find end token for " + 
										HGStatics.MOTD_HOLD + "in this line: " + line);
							}
						}
						//teleport
						if ((beginToken = findToken(newline,(specialChar + HGStatics.MOTD_PORT),0)) != -1) {
							if ((endToken = findToken(newline,specialChar,beginToken + 1)) != -1) {
								String location = newline.substring(beginToken + (specialChar + HGStatics.MOTD_PORT).length(),endToken);
								//get world,x,y,z
								String[] elements = location.split(",");
								if (elements.length != 4){
									warn("Not enough info to teleport from MOTD, please revise.[worldname,x,y,z]");
								}
								double x = 0;
								double y = 0;
								double z = 0;
								World world = plugin.getServer().getWorld(elements[0]);
								try {
									x = Double.valueOf(elements[1]);
									y = Double.valueOf(elements[2]);
									z = Double.valueOf(elements[3]);
								} catch (NumberFormatException e) {
									warn("Not enough info to teleport from MOTD, please revise.[worldname,x,y,z]");
									break;
								}
								if (world != null) {
									player.teleport(new Location(world, x, y, z));
								}
								newline.delete(beginToken, endToken+1);
							} else {
								warn("Error in " + HGStatics.MOTD_FILE + " Unable to find end token for " + 
										HGStatics.MOTD_PORT + "in this line: " + line);
							}
						}
					}
					if (testString.equals(newline.toString())) {
						//no change, nextline
						break;
					}

				} catch (Exception e) {
					info("exception: " +newline.toString());

					return;
				}
			}// while
			sendSegmented(player,newline.toString());
		} // no more lines
	}

	public int findToken(StringBuilder line,String[] tokens,int startIndex) {
		int found = -1;
		for (String token : tokens) {
			if (line.indexOf(token,startIndex) != -1) {
				found = findToken(line,token,startIndex);
			}
		}
		return found;
	}
	
	public int findToken(StringBuilder line,String token,int startIndex) {
		return line.indexOf(token,startIndex);
	}

	//replaces basic this for that tokens
	public StringBuilder replaceBasic(StringBuilder newline,String token, String replacement) throws Exception {
		int beginToken = -1;
		int endToken = -1;
		for (String specialChar : HGStatics.MOTD_SPECIAL_CHARS) {
			if ((beginToken = findToken(newline,(specialChar + token),0)) != -1) {
				if ((endToken = findToken(newline,specialChar,beginToken + 1)) != -1) {
					//replace token with info
					newline.replace(beginToken, endToken+1, replacement);
				} else {
					warn("Error in " + HGStatics.MOTD_FILE + " Unable to find end token for " + 
							token + "in this line: " + newline);
					throw new Exception("parse error");
				}
			} //token not found
		}
		return newline;
	}
}
