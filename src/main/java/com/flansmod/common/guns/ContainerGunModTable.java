package com.flansmod.common.guns;

import com.flansmod.common.FlansMod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ContainerGunModTable extends Container
{
	private InventoryGunModTable inventory;
	public InventoryPlayer playerInv;
	public World world;

	public ContainerGunModTable(InventoryPlayer i, World w)
	{
		playerInv = i;
		inventory = new InventoryGunModTable();
		world = w;

		//Gun slot
		SlotGun gunSlot = new SlotGun(inventory, 0, 80, 110, null);
		addSlotToContainer(gunSlot);

		//Attachment Slots
		/*addSlotToContainer(new SlotGun(inventory, 1, 54, 110, gunSlot));
		addSlotToContainer(new SlotGun(inventory, 2, 80, 84, gunSlot));
		addSlotToContainer(new SlotGun(inventory, 3, 106, 110, gunSlot));
		addSlotToContainer(new SlotGun(inventory, 4, 80, 136, gunSlot));

		for(int row = 0; row < 4; row++)
		{
			for(int col = 0; col < 2; col++)
			{
				addSlotToContainer(new SlotGun(inventory, 5 + row * 2 + col, 10 + col * 18, 83 + row * 18, gunSlot));
			}
		}*/
		//Attachment Slots
		for(int k = 0; k < 8; k++)
		{
			addSlotToContainer(new SlotGun(inventory, k + 1, 17 + (k * 18), 89, gunSlot));
		}

		//Generic Attachment Slots
		for(int col = 0; col < 8; col++)
		{
			addSlotToContainer(new SlotGun(inventory, 9 + col, 17 + (col * 18), 115 + (col * 18), gunSlot));
		}

		//Main inventory slots
		for(int row = 0; row < 3; row++)
		{
			for(int col = 0; col < 9; col++)
			{
				addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 176+ row * 18));
			}

		}
		//Quickbar slots
		for(int col = 0; col < 9; col++)
		{
			addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 234));
		}
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if(inventory.getStackInSlot(0) != null)
			player.dropItem(inventory.getStackInSlot(0), false);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = ItemStack.EMPTY.copy();
		Slot currentSlot = inventorySlots.get(slotID);

		Slot gunSlot = inventorySlots.get(0);

		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			// gun slot, 4 attach slots and 8 generics
			if(slotID >= 17)
			{
				if(slotStack.getItem() instanceof ItemGun && !gunSlot.getHasStack())
				{
					gunSlot.putStack(slotStack);
					currentSlot.putStack(ItemStack.EMPTY.copy());
				}
				if(slotStack.getItem() instanceof ItemAttachment)
				{
					for(int i = 1; i < 12; i++)
					{
						Slot attachmentSlot = inventorySlots.get(i);
						if(!attachmentSlot.getHasStack() && attachmentSlot.isItemValid(slotStack))
						{
							attachmentSlot.putStack(slotStack);
							currentSlot.putStack(ItemStack.EMPTY.copy());
							break;
						}
					}
				}
				return ItemStack.EMPTY.copy();
			}
			else
			{
				if(!mergeItemStack(slotStack, 17, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY.copy();
				}
			}

			if(slotStack.getCount() == 0)
			{
				currentSlot.putStack(ItemStack.EMPTY.copy());
			}
			else
			{
				currentSlot.onSlotChanged();
			}

			if(slotStack.getCount() == stack.getCount())
			{
				return ItemStack.EMPTY.copy();
			}

			currentSlot.onTake(player, slotStack);
		}

		return stack;
	}

	public void pressButton(boolean paint, boolean left)
	{
		//Nope.
	}

	public void clickPaintjob(int i)
	{
		ItemStack gunStack = inventory.getStackInSlot(0);
		if(gunStack != null && gunStack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)gunStack.getItem()).GetType();
			clickPaintjob(gunType.getPaintjob(i));
		}
	}

	public void clickPaintjob(Paintjob paintjob)
	{
		ItemStack gunStack = inventory.getStackInSlot(0);
		if(gunStack != null && gunStack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)gunStack.getItem()).GetType();
			int numDyes = paintjob.dyesNeeded.length;

			boolean legendary = false;
			for(int n = 0; n < numDyes; n++)
			{
				if(paintjob.dyesNeeded[n].getItem() == FlansMod.rainbowPaintcan)
				{
					legendary = true;
				}
			}

			if(!playerInv.player.capabilities.isCreativeMode)
			{
				//Calculate which dyes we have in our inventory
				for(int n = 0; n < numDyes; n++)
				{
					int amountNeeded = paintjob.dyesNeeded[n].getCount();
					boolean lookingForRainbow = paintjob.dyesNeeded[n].getItem() == FlansMod.rainbowPaintcan;
					for(int s = 0; s < playerInv.getSizeInventory(); s++)
					{
						ItemStack stack = playerInv.getStackInSlot(s);
						if(lookingForRainbow)
						{
							if(stack.getItem() == FlansMod.rainbowPaintcan)
								amountNeeded -= stack.getCount();
						}
						else
						{
							if(stack != null && stack.getItem() == Items.DYE && stack.getItemDamage() == paintjob.dyesNeeded[n].getItemDamage())
								amountNeeded -= stack.getCount();
						}
					}
					//We don't have enough of this dye
					if(amountNeeded > 0)
						return;
				}

				for(int n = 0; n < numDyes; n++)
				{
					int amountNeeded = paintjob.dyesNeeded[n].getCount();
					for(int s = 0; s < playerInv.getSizeInventory(); s++)
					{
						if(amountNeeded <= 0)
							continue;
						ItemStack stack = playerInv.getStackInSlot(s);
						boolean lookingForRainbow = paintjob.dyesNeeded[n].getItem() == FlansMod.rainbowPaintcan;
						if(lookingForRainbow)
						{
							if(stack.getItem() == FlansMod.rainbowPaintcan)
							{
								ItemStack consumed = playerInv.decrStackSize(s, amountNeeded);
								amountNeeded -= stack.getCount();
							}
						}
						else
						{
							if(stack != null && stack.getItem() == Items.DYE && stack.getItemDamage() == paintjob.dyesNeeded[n].getItemDamage())
							{
								ItemStack consumed = playerInv.decrStackSize(s, amountNeeded);
								amountNeeded -= consumed.getCount();
							}
						}
					}
				}
			}

			//Paint the gun. This line is only reached if the player is in creative or they have had their dyes taken already
			//gunStack.getTagCompound().setString("Paint", paintjob.iconName);
			gunStack.setItemDamage(paintjob.ID);
			if(legendary)
			{
				if(!gunStack.hasTagCompound())
				{
					gunStack.setTagCompound(new NBTTagCompound());
				}
				if(!gunStack.getTagCompound().hasKey("display"))
				{
					gunStack.getTagCompound().setTag("display", new NBTTagCompound());
				}
				if(!gunStack.getTagCompound().getCompoundTag("display").hasKey("Name"))
				{
					gunStack.getTagCompound().getCompoundTag("display").setString("Name", "\u00a7e" + playerInv.player.getName() + "'s " + gunStack.getDisplayName());
				}
				gunStack.getTagCompound().setString("LegendaryCrafter", playerInv.player.getName());
			}
		}
	}
}
