/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.part.gate.analogue;

import com.bluepowermod.api.wire.redstone.RedwireType;
import com.bluepowermod.part.gate.component.GateComponentBorder;
import com.bluepowermod.part.gate.component.GateComponentTorch;
import com.bluepowermod.part.gate.component.GateComponentWire;
import com.bluepowermod.util.Color;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import uk.co.qmunity.lib.misc.ShiftingBuffer;

import java.util.List;

/**
 * Created by Mathieu on 2/06/2015.
 */
public class GateMultiplication extends GateSimpleAnalogue {

    private ShiftingBuffer<Boolean> buf = new ShiftingBuffer<Boolean>(5, 2, false);

    private GateComponentTorch t1, t2, t3, t4;
    private GateComponentWire wire;

    @Override
    protected void initializeConnections() {
        front().enable().setOutputOnly();
        left().enable();
        back().enable();
        right().enable();
    }

    @Override
    protected String getGateType() {
        return "multiplication";
    }

    @Override
    protected void initComponents() {

        addComponent(t1 = new GateComponentTorch(this, 0x215b8d, 4 / 16D, false).setState(true));
        addComponent(t2 = new GateComponentTorch(this, 0x0000FF, 4 / 16D, false).setState(true));
        addComponent(t3 = new GateComponentTorch(this, 0x3e94dc, 4 / 16D, false).setState(true));
        addComponent(t4 = new GateComponentTorch(this, 0x6F00B5, 5 / 16D, false).setState(false));

        addComponent(wire = new GateComponentWire(this, 0x18FF00, RedwireType.RED_ALLOY).setPower((byte) 255));
        addComponent(new GateComponentWire(this, 0xFFF600, RedwireType.RED_ALLOY).bind(right()));
        addComponent(new GateComponentWire(this, 0xC600FF, RedwireType.RED_ALLOY).bind(back()));
        addComponent(new GateComponentWire(this, 0xFF0000, RedwireType.RED_ALLOY).bind(left()));

        addComponent(new GateComponentBorder(this, 0x7D7D7D));
    }

    @Override
    public void doLogic() {
        int le = left().getInput();
        int ri = right().getInput();
        int ba = back().getInput();

        int mult = le*ri;
        if (mult <= 255 && ba > mult){
            front().setOutput((byte) mult);
        }else if (mult > 255){
            front().setOutput((byte) 255);
        }else {
            front().setOutput((byte) ba);
        }
        sendUpdatePacket();
    }

    @Override
    public void tick() {

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);

        buf.writeToNBT(tag, "buffer");
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);

        buf.readFromNBT(tag, "buffer");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addWAILABody(List<String> info) {
        info.add(Color.GREEN + "Left input : " + Color.WHITE +left().getInput());
        info.add(Color.GREEN + "Right input : " + Color.WHITE +right().getInput());
        info.add(Color.GREEN + "Back input : " + Color.WHITE +back().getInput());


        if ((left().getInput() * right().getInput())> 255){
            info.add("   ");
            info.add(Color.RED + "Power overloaded");
        }else if (left().getInput() * right().getInput() > back().getInput()){
            info.add("   ");
            info.add(Color.RED + "Insufficient power in the back for full operation");
        }
    }
}
