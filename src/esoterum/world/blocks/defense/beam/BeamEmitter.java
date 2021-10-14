package esoterum.world.blocks.defense.beam;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.graphics.*;

public class BeamEmitter extends BeamBlock{
    public BeamEmitter(String name){
        super(name);
        rotate = true;
        outputs = new boolean[]{false, false, false, false};
        inputs = new boolean[]{true, true, true, true};
    }

    public class BeamEmitterBuild extends BeamBuild {
        @Override
        public void updateTile() {
            super.updateTile();
            signal[4] = false;
            for(BinaryBuild b : nb){
                signal[4] |= getSignal(b, this);
            }
            signal(signal[4]);
            if(signal()) updateBeam();
        }

        @Override
        public void draw() {
            super.draw();
            if(signal()) drawBeam(beamRotation, beamDrawLength);
        }

        @Override
        public void drawBeam(float rot, float length) {
            super.drawBeam(rot, length);
            Tmp.v2.setZero().trns(rot, (size * 4f) - 1f);
            Draw.blend(Blending.additive);
            float scl = Mathf.sin(8f, 3f);
            Drawf.tri(x + Tmp.v2.x, y + Tmp.v2.y, 4f, Math.min(length / 5f, 8f) + scl, rot);
            Fill.light(x + Tmp.v2.x, y + Tmp.v2.y, 7, 6f, Pal.lancerLaser, Color.clear);
            Draw.blend();
        }
    }
}