package fr.bletrazer.mailbox.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
/**
 * Constructeur d ' ItemStack
 * @author Bletrazer
 *
 */
public class ItemStackBuilder {
	
	private Material material = Material.STONE;
	private String name = " ";
	private String loreFormat = "§7";
	private List<String> lore = new ArrayList<>();
	private Map<Enchantment, Integer> enchantments = new HashMap<>();
	private List<ItemFlag> flags = new ArrayList<>();
	private Integer stackSize = 1;
	
	public ItemStackBuilder() {
		
	}
	
	public ItemStackBuilder(Material mat) {
		this.setMaterial(mat);
	}
	
	
	public ItemStack build() {
		ItemStack res = new ItemStack(this.getMaterial());
		res.setAmount(this.getStackSize());
		ItemMeta meta = res.getItemMeta();
		meta.setLore(this.getLore());
		meta.setDisplayName(this.getName());
		
		for(Entry<Enchantment, Integer> entry : this.getEnchantements().entrySet()) {
			meta.addEnchant(entry.getKey(), entry.getValue(), true);
		}
		
		for(ItemFlag flag : this.getFlags()) {
			meta.addItemFlags(flag);
		}
		
		res.setItemMeta(meta);
		
		return res;
	}

	public Material getMaterial() {
		return material;
	}

	public ItemStackBuilder setMaterial(Material material) {
		this.material = material;
		return this;
	}

	public String getName() {
		return name;
	}

	public ItemStackBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public List<String> getLore() {
		return lore;
	}

	public ItemStackBuilder setLore(List<String> lore) {
		for(String str : lore) {
			this.addLore(this.getLoreFormat() + str);
			
		}
		return this;
	}
	
	public ItemStackBuilder addLore(String str) {
		this.getLore().add(this.getLoreFormat() + str);
		return this;
	}
	
	public ItemStackBuilder addLores(List<String> list) {
		for(String str : list) {
			this.addLore(str);
			
		}
		
		return this;
	}
	
	private List<String> format(String str, Integer size){
		Integer t = (int) Math.ceil((str.length() / size)) +1;
	    StringBuilder[] temp = new StringBuilder[t];
	    Integer index = 0;
	    
	    for(String word : str.split(" ")) {
	    	if(temp[index] == null) {
	    		temp[index] = new StringBuilder(this.getLoreFormat());
	    	}
	    	
	    	temp[index].append(word + " ");
	    	
	    	if(temp[index].toString().length() >= size) {
	    		index++;
	    		
	    	}
	    }
		
	    List<String> res = Arrays.asList(temp).stream().filter(e -> e!=null).map(StringBuilder::toString).collect(Collectors.toList());
	    return res;
	}
	
	public ItemStackBuilder setAutoFormatingLore(String str, Integer size) {
	    this.setLore(format(str, size) );
		return this;
	}
	
	public ItemStackBuilder addAutoFormatingLore(String str, Integer size) {
	    this.getLore().addAll(format(str, size) );
		return this;
	}
	
	public ItemStackBuilder addAutoFormatingLores(List<String> list, Integer size) {
		for(String str : list) {
			this.getLore().addAll(format(str, size) );
		}
		return this;
	}

	public Map<Enchantment, Integer> getEnchantements() {
		return enchantments;
	}

	public ItemStackBuilder setEnchantments(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
		return this;
	}
	
	public ItemStackBuilder enchant(Enchantment enchantement, Integer power) {
		this.getEnchantements().put(enchantement, power);
		return this;
	}

	public List<ItemFlag> getFlags() {
		return flags;
	}

	public ItemStackBuilder setFlags(List<ItemFlag> flags) {
		this.flags = flags;
		return this;
	}
	
	public ItemStackBuilder addFlag(ItemFlag flag) {
		this.getFlags().add(flag);
		return this;
	}

	public String getLoreFormat() {
		return loreFormat;
	}

	public ItemStackBuilder setLoreFormat(String loreFormat) {
		this.loreFormat = loreFormat;
		return this;
	}

	public Integer getStackSize() {
		return stackSize;
	}

	public ItemStackBuilder setStackSize(Integer stackSize, Boolean canBeUnderOne) {
		if(!canBeUnderOne && stackSize < 1) {
			this.stackSize = 1;
		} else {
			this.stackSize = stackSize;
		}
		return this;
	}
}
