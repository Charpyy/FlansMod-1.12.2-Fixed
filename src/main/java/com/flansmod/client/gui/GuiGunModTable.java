package com.flansmod.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.flansmod.client.ClientProxy;
import com.flansmod.client.model.CustomItemRenderType;
import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.ContainerGunModTable;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.Paintjob;
import com.flansmod.common.network.PacketGunPaint;

public class GuiGunModTable extends GuiContainer
{
	private static final ResourceLocation texture = new ResourceLocation("flansmod", "gui/gunTable.png");
	private static final Random rand = new Random();
	private Paintjob hoveringOver = null;
	private int mouseX, mouseY;
	private InventoryPlayer inventory;
	
	private String hoveringOverModSlots = null;
	private boolean flipGunModel = false;
	
	//Smoothing
    private int[] lastStats = {0, 0, 0, 0};
	
	public GuiGunModTable(InventoryPlayer inv, World w)
	{
		super(new ContainerGunModTable(inv, w));
		inventory = inv;
		ySize = 256;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		fontRenderer.drawString("Inventory", 8, (ySize - 94) + 2, 0x404040);
		fontRenderer.drawString("Gun Modification Table", 8, 6, 0x404040);
		fontRenderer.drawString("Gun Information", 179, 22, 0x404040);
		fontRenderer.drawString("Paint Jobs", 179, 128, 0x404040);
        
		ItemStack gunStack = inventorySlots.getSlot(0).getStack();
		if(gunStack != null && gunStack.getItem() instanceof ItemGun)
		{
			ItemStack tempStack = gunStack.copy();
			if(hoveringOver != null)
				tempStack.setItemDamage(hoveringOver.ID);
			GunType gunType = ((ItemGun)gunStack.getItem()).GetType();
			int reload = Math.round(gunType.getReloadTime(gunStack));
			
			if(gunType.model != null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.color(1F, 1F, 1F, 1F);
				
				GlStateManager.disableLighting();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(0F, 0.0F, 1.0F, 0.0F);
				if (flipGunModel) {
                    //GL11.glTranslatef(-85, -55, -100);
                    GL11.glTranslatef(-30F, 0, 0);
                    GL11.glRotatef(190, 0F, 1F, 0F);
                }
				RenderHelper.enableStandardItemLighting();
				GlStateManager.popMatrix();
				GlStateManager.enableRescaleNormal();
				
				GlStateManager.translate(80, 48, 100);
				
				GlStateManager.rotate(160, 1F, 0F, 0F);
				GlStateManager.rotate(20, 0F, 1F, 0F);
				GlStateManager.scale(-50F, 50F, 50F);
				//ClientProxy.gunRenderer.renderGun(gunStack, gunType, 1F / 16F, gunType.model, GunAnimations.defaults, 0F);
				ClientProxy.gunRenderer.renderItem(CustomItemRenderType.ENTITY, EnumHand.MAIN_HAND, tempStack);
				GlStateManager.popMatrix();
			}
			
			//Draw stats
            if (gunStack.getDisplayName() != null)
                fontRenderer.drawString(gunStack.getDisplayName(), 207, 36, 0x404040);
            fontRenderer.drawString(gunType.description, 207, 46, 0x404040);

            fontRenderer.drawString("Damage", 181, 61, 0x404040);
            fontRenderer.drawString("Accuracy", 181, 73, 0x404040);
            fontRenderer.drawString("Recoil", 181, 85, 0x404040);
            fontRenderer.drawString("Reload", 181, 97, 0x404040);
            fontRenderer.drawString("Control", 181, 109, 0x404040);

            fontRenderer.drawString("Sprint", 240, 119, 0x404040);
            fontRenderer.drawString("Sneak", 290, 119, 0x404040);


            fontRenderer.drawString(String.valueOf(roundFloat( gunType.getDamage(gunStack))), 241, 62, 0x404040);
            fontRenderer.drawString(String.valueOf(roundFloat(gunType.getSpread(gunStack, false, false))), 241, 74, 0x404040);
            fontRenderer.drawString(String.valueOf(roundFloat(gunType.getRecoilDisplay(gunStack))), 241, 86, 0x404040);
            fontRenderer.drawString(roundFloat(reload / 20) + "s", 241, 98, 0x404040);
            Float sprinting = roundFloat(1 - gunType.getRecoilControl(gunStack, true, false), 2);
            Float normal = roundFloat(1 - gunType.getRecoilControl(gunStack, false, false), 2);
            Float sneaking = roundFloat(1 - gunType.getRecoilControl(gunStack, false, true), 2);
            fontRenderer.drawString(String.format("%3.2f  %3.2f  %3.2f", sprinting, normal, sneaking), 241, 110, 0x404040);

            //Draw attachment tooltips
            if (hoveringOverModSlots != null)
                drawHoveringText(Collections.singletonList(hoveringOverModSlots), mouseX - guiLeft, mouseY - guiTop, fontRenderer);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		int xOrigin = (width - xSize) / 2;
		int yOrigin = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(texture);
		drawTexturedModalRect(xOrigin, yOrigin, 0, 0, xSize, ySize);
		
		for(int z = 1; z < 17; z++)
			inventorySlots.getSlot(z).yPos = -1000;
		
		ItemStack gunStack = inventorySlots.getSlot(0).getStack();
		if (gunStack == null) {
            lastStats = new int[]{0, 0, 0, 0, 0};
        }
		
		if(gunStack != null && gunStack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)gunStack.getItem()).GetType();
			
			int reload = Math.round(gunType.getReloadTime(gunStack));
            //Calculates yellow stat bar
            int[] stats = {Math.round(gunType.getDamage(gunStack)) * 4, Math.round(gunType.getSpread(gunStack, false, false)) * 4,
                    Math.round(gunType.getRecoilPitch(gunStack)) * 4, (reload / 20) * 8, 0};
            displayGunValues(stats);
            boolean[] allowBooleans = {gunType.allowBarrelAttachments, gunType.allowScopeAttachments, gunType.allowStockAttachments,
                    gunType.allowGripAttachments, gunType.allowGadgetAttachments, gunType.allowSlideAttachments,
                    gunType.allowPumpAttachments, gunType.allowAccessoryAttachments};

            //draw flip display button
            drawTexturedModalRect(xOrigin + 146, yOrigin + 63, 340, 166, 20, 10);

            //Cycle through the booleans and generics, and draw to table.
            for (int m = 0; m < allowBooleans.length; m++) {
                if (allowBooleans[m]) {
                    drawTexturedModalRect(xOrigin + 16 + (m * 18), yOrigin + 88, 340 + (m * 18), 136, 18, 18);
                    inventorySlots.getSlot(m + 1).yPos = 89;
                }
            }

            for (int x = 0; x < 8; x++) {
                if (x < gunType.numGenericAttachmentSlots) {
                    drawTexturedModalRect(xOrigin + 16 + (18 * x), yOrigin + 114, 340, 100, 18, 18);
                    inventorySlots.getSlot(allowBooleans.length + 1 + x).yPos = 115;
                }
            }
			
			/*if(gunType.allowBarrelAttachments)
			{
				drawTexturedModalRect(xOrigin + 51, yOrigin + 107, 176, 122, 22, 22);
				inventorySlots.getSlot(1).yPos = 110;
			}
			if(gunType.allowScopeAttachments)
			{
				drawTexturedModalRect(xOrigin + 77, yOrigin + 81, 202, 96, 22, 22);
				inventorySlots.getSlot(2).yPos = 84;
			}
			if(gunType.allowStockAttachments)
			{
				drawTexturedModalRect(xOrigin + 103, yOrigin + 107, 228, 122, 22, 22);
				inventorySlots.getSlot(3).yPos = 110;
			}
			if(gunType.allowGripAttachments)
			{
				drawTexturedModalRect(xOrigin + 77, yOrigin + 133, 202, 148, 22, 22);
				inventorySlots.getSlot(4).yPos = 136;
			}
			
			for(int x = 0; x < 2; x++)
			{
				for(int y = 0; y < 4; y++)
				{
					if(x + y * 2 < gunType.numGenericAttachmentSlots)
						inventorySlots.getSlot(5 + x + y * 2).yPos = 83 + 18 * y;
				}
			}
			
			//Render generic slot backgrounds
			for(int x = 0; x < 2; x++)
			{
				for(int y = 0; y < 4; y++)
				{
					if(x + y * 2 < gunType.numGenericAttachmentSlots)
						drawTexturedModalRect(xOrigin + 9 + 18 * x, yOrigin + 82 + 18 * y, 178, 54, 18, 18);
				}
			}*/
            			
			int numPaintjobs = gunType.paintjobs.size();
			int numRows = numPaintjobs / 2 + 1;
			
			for(int y = 0; y < numRows; y++)
			{
				for(int x = 0; x < 2; x++)
				{
					//If this row has only one paintjob, don't try and render the second one
					if(2 * y + x >= numPaintjobs)
						continue;
					
					drawTexturedModalRect(xOrigin + 181 + 18 * x, yOrigin + 150 + (18 * y), 340, 100, 18, 18);
				}
			}
			
			//Fill paintjob slots
			for(int y = 0; y < numRows; y++)
			{
				for(int x = 0; x < 2; x++)
				{
					//If this row has only one paintjob, don't try and render the second one
					if(2 * y + x >= numPaintjobs)
						continue;
					
					Paintjob paintjob = gunType.paintjobs.get(2 * y + x);
					ItemStack stack = gunStack.copy();
					//stack.getTagCompound().setString("Paint", paintjob.iconName);
					stack.setItemDamage(paintjob.ID);
					itemRender.renderItemIntoGUI(stack, xOrigin + 182 + (x * 18), yOrigin + 151 + (y * 18));
				}
			}
		}
		
		//Draw hover box for paintjob
		if(hoveringOver != null)
		{
			int numDyes = hoveringOver.dyesNeeded.length;
			//Only draw box if there are dyes needed
			if(numDyes != 0 && !inventory.player.capabilities.isCreativeMode)
			{
				//Calculate which dyes we have in our inventory
				boolean[] haveDyes = new boolean[numDyes];
				for(int n = 0; n < numDyes; n++)
				{
					int amountNeeded = hoveringOver.dyesNeeded[n].getCount();
					for(int s = 0; s < inventory.getSizeInventory(); s++)
					{
						ItemStack stack = inventory.getStackInSlot(s);
						if(stack != null && stack.getItem() == Items.DYE && stack.getItemDamage() == hoveringOver.dyesNeeded[n].getItemDamage())
						{
							amountNeeded -= stack.getCount();
						}
					}
					if(amountNeeded <= 0)
						haveDyes[n] = true;
				}
				
				GlStateManager.color(1F, 1F, 1F, 1F);
				GlStateManager.disableLighting();
				mc.renderEngine.bindTexture(texture);
				
				int originX = mouseX + 6;
				int originY = mouseY - 20;
				
				for (int s = 0; s < numDyes; s++)
                    drawTexturedModalRect(xOrigin + 223 + (18 * s), yOrigin + 150, (haveDyes[s] ? 358 : 340), 118, 18, 18);

                for (int s = 0; s < numDyes; s++) {
                    itemRender.renderItemIntoGUI(hoveringOver.dyesNeeded[s], xOrigin + 224 + s * 18, yOrigin + 151);
                    itemRender.renderItemOverlayIntoGUI(fontRenderer, hoveringOver.dyesNeeded[s], xOrigin + 224 + s * 18, yOrigin + 151, null);
                }
				
				//If we have only one, use the double ended slot
				/*if(numDyes == 1)
				{
					drawTexturedModalRect(originX, originY, (haveDyes[0] ? 201 : 178), 218, 22, 22);
				}
				else
				{
					//First slot
					drawTexturedModalRect(originX, originY, 178, (haveDyes[0] ? 195 : 172), 20, 22);
					//Middle slots
					for(int s = 1; s < numDyes - 1; s++)
					{
						drawTexturedModalRect(originX + 2 + 18 * s, originY, 199, (haveDyes[s] ? 195 : 172), 18, 22);
					}
					//Last slot
					drawTexturedModalRect(originX + 2 + 18 * (numDyes - 1), originY, 218, (haveDyes[numDyes - 1] ? 195 : 172), 20, 22);
				}
				
				for(int s = 0; s < numDyes; s++)
				{
					itemRender.renderItemIntoGUI(hoveringOver.dyesNeeded[s], originX + 3 + s * 18, originY + 3);
					itemRender.renderItemOverlayIntoGUI(this.fontRenderer, hoveringOver.dyesNeeded[s], originX + 3 + s * 18, originY + 3, null);
				}*/
				
				
			}
		}
	}
	
	
	/**
     * Gun statistics via progress bars.
     * Loops through, and uses lastStats[] to increment.
     *
     * @param stats Gun statistics (0 = damage, 1 = accuracy, 2 = recoil, 3 = reload)
     */
    private void displayGunValues(int[] stats) {
        int xOrigin = (width - xSize) / 2;
        int yOrigin = (height - ySize) / 2;

        for (int y = 0; y < 5; y++)
            drawTexturedModalRect(xOrigin + 239, yOrigin + 60 + (12 * y), 340, 80, 80, 10);

        for (int k = 0; k < 4; k++) {
            //int difference = stats[k] - lastStats[k];
			int difference = stats[k];
            int finalWidth;

            // For damage only
            if (k == 0) {
                if (stats[k] < 80 && difference > 0)  //increment if positive
                    finalWidth = lastStats[k] += 2;
                else if (difference < 0)             //decrement if negative
                    finalWidth = lastStats[k] -= 2;
                else if (stats[k] < 80)
                    finalWidth = stats[k];
                else
                    finalWidth = 80;

                drawTexturedModalRect(xOrigin + 239, yOrigin + 60 + (12 * k), 340, 90, finalWidth, 10);
            // Control stat
            } else if (k == 4) {
                drawTexturedModalRect(xOrigin + 239, yOrigin + 60 + (12 * k), 340, 80, 32, 10);
                drawTexturedModalRect(xOrigin + 239 + 26, yOrigin + 60 + (12 * k), 341, 90, 28, 10);
                drawTexturedModalRect(xOrigin + 239 + 26 + 28, yOrigin + 60 + (12 * k), 394, 70, 32, 10);
            } else //every other stat. Works in reverse (i.e to show low spread being good accuracy)
            {
                difference = (80 - stats[k]) - lastStats[k];
                if (80 - stats[k] > 2 && difference > 0)  //increment if positive
                    finalWidth = lastStats[k] += 2;
                else if (difference < 0)                  //decrement if negative
                    finalWidth = lastStats[k] -= 2;
                else if (80 - stats[k] > 2)
                    finalWidth = 80 - stats[k];
                else
                    finalWidth = 2;

                drawTexturedModalRect(xOrigin + 239, yOrigin + 60 + (12 * k), 340, 90, finalWidth, 10);
            }
        }
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
		mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		
		int mouseXInGUI = mouseX - guiLeft;
		int mouseYInGUI = mouseY - guiTop;
		
		hoveringOver = null;
		
		ItemStack gunStack = inventorySlots.getSlot(0).getStack();
		if(gunStack != null && gunStack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)gunStack.getItem()).GetType();
			int numPaintjobs = gunType.paintjobs.size();
			int numRows = numPaintjobs / 2 + 1;
			
			for(int j = 0; j < numRows; j++)
			{
				for(int i = 0; i < 2; i++)
				{
					if(2 * j + i >= numPaintjobs)
						continue;
					
					Paintjob paintjob = gunType.paintjobs.get(2 * j + i);
					ItemStack stack = gunStack.copy();
					stack.getTagCompound().setString("Paint", paintjob.iconName);
					int slotX = 181 + i * 18;
					int slotY = 150 + j * 18;
					if(mouseXInGUI >= slotX && mouseXInGUI < slotX + 18 && mouseYInGUI >= slotY && mouseYInGUI < slotY + 18)
						hoveringOver = paintjob;
				}
			}
			
			//Show attachment tooltips
            hoveringOverModSlots = null;
            String[] text = {"Barrel", "Scope", "Stock", "Grip", "Gadget", "Slide", "Pump", "Accessory"};
            boolean[] allowBools = {gunType.allowBarrelAttachments, gunType.allowScopeAttachments, gunType.allowStockAttachments,
                    gunType.allowGripAttachments, gunType.allowGadgetAttachments, gunType.allowSlideAttachments,
                    gunType.allowPumpAttachments, gunType.allowAccessoryAttachments};

            for (int a = 0; a < allowBools.length; a++) {
                int slotX = 16 + a * 18;
                int slotY = 88;
                if (mouseXInGUI >= slotX && mouseXInGUI < slotX + 18 && mouseYInGUI >= slotY && mouseYInGUI < slotY + 18
                        && !inventorySlots.getSlot(a + 1).getHasStack() && allowBools[a])
                    hoveringOverModSlots = text[a];
            }
		}
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException
	{
		int xOrigin = (width - xSize) / 2;
        int yOrigin = (height - ySize) / 2;
        
		super.mouseClicked(x, y, button);
		
		int m = x - xOrigin;
        int n = y - yOrigin;
        if (button == 0 || button == 1) {
            if (m >= 146 && m <= 165 && n >= 63 && n <= 72) {
                flipGunModel = !flipGunModel;
            }
        }
		
		if(button != 0)
			return;
		if(hoveringOver == null)
			return;
		
		FlansMod.getPacketHandler().sendToServer(new PacketGunPaint(hoveringOver.ID));
		((ContainerGunModTable)inventorySlots).clickPaintjob(hoveringOver.ID);
	}
	
	//Round values to n number of decimal points
    private static float roundFloat(float value) {
        int pow = 10;
        for (int i = 1; i < 2; i++)
            pow *= 10;
        float result = value * pow;

        return (float) (int) ((result - (int) result) >= 0.5f ? result + 1 : result) / pow;
    }

	//Round values to n number of decimal points
	public static float roundFloat(float value, int points)
	{
		int pow = 10;
		for (int i = 1; i < points; i++)
			pow *= 10;
		float result = value * pow;

		return (float)(int)((result - (int) result) >= 0.5f ? result + 1 : result) / pow;
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
