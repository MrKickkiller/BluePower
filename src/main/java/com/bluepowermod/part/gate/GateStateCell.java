/*
 * This file is part of Blue Power.
 *
 *      Blue Power is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Blue Power is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bluepowermod.part.gate;

import com.bluepowermod.client.gui.gate.GuiGateSingleTime;
import com.bluepowermod.client.renderers.RenderHelper;
import com.bluepowermod.part.IGuiButtonSensitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Quetzi on 04/11/14.
 */
public class GateStateCell extends GateBase implements IGuiButtonSensitive {

    private int     time      = 40;
    private int     ticks     = 0;
    private boolean triggered = false;
    private boolean mirrored  = false;
    private long    start     = -1;

    @Override
    public void initializeConnections() {

        front().enable().setOutputOnly();
        left().enable().setOutputOnly();
        back().enable();
        right().enable().setOutputOnly();
    }

    @Override
    public String getId() {

        return "state";
    }

    @Override
    protected void renderTop(float frame) {

        renderTop("front", front());
        renderTop("right", right());
        renderTop("back", back());
        renderTop("left", left());

        double t = 0;
        if (back().getInput() == 0)
            t = -(getWorld().getTotalWorldTime() - start + frame) / (double) time;

        RenderHelper.renderRandomizerButton(this, -2 / 16D, 0, 4 / 16D, left().getOutput() > 0);
        RenderHelper.renderRedstoneTorch(4 / 16D, 1D / 8D, 0, 13D / 16D, ticks > 0);
        RenderHelper.renderRedstoneTorch(1 / 16D, 1D / 8D, -4 / 16D, 9D / 16D, mirrored ? back().getOutput() > 0 : front().getOutput() > 0);
        RenderHelper.renderPointer(4 / 16D, 7D / 16D, 0, ticks > 0 ? 1 - (ticks + frame) / (time * 7) + 0.25 : 0.25);

    }

    @Override
    public void doLogic() {

    }

    @Override
    public void tick() {

        if (mirrored) {
            back().setOutput(0);
        } else {
            front().setOutput(0);
        }
        boolean locked = false;
        if (mirrored ? front().getInput() > 0 : back().getInput() > 0) {
            triggered = true;
            locked = true;
        }
        if (locked || right().getOutput() > 0) {
            ticks = 0;
            if (triggered) left().setOutput(15);
        } else if (triggered) {
            left().setOutput(15);
            if (ticks++ >= time) {
                ticks = 0;
                if (mirrored) {
                    back().setOutput(15);
                } else {
                    front().setOutput(15);
                }
                playTickSound();
                triggered = false;
            }
        } else {
            left().setOutput(0);
        }
    }

    @Override
    public void onButtonPress(EntityPlayer player, int messageId, int value) {

        time = value;
        sendUpdatePacket();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected GuiScreen getGui() {

        return new GuiGateSingleTime(this) {

            @Override
            protected int getCurrentIntervalTicks() {

                return time;
            }

        };
    }

    @Override
    protected boolean hasGUI() {

        return true;
    }
}