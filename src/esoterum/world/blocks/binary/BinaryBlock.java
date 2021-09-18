package esoterum.world.blocks.binary;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.Seq;
import esoterum.interfaces.Binaryc;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.Category;
import mindustry.world.*;
import mindustry.world.meta.BuildVisibility;

public class BinaryBlock extends Block {
    public TextureRegion connectionRegion;
    public TextureRegion topRegion;
    // in order {front, left, back, right}
    public boolean[] outputs = new boolean[]{false, false, false, false};
    public boolean emits = false;

    public BinaryBlock(String name) {
        super(name);
        rotate = false;
        update = true;
        solid = true;
        destructible = true;
        buildVisibility = BuildVisibility.shown;

        category = Category.logic;
    }

    public void load() {
        super.load();
        region = Core.atlas.find("esoterum-base");
        connectionRegion = Core.atlas.find("esoterum-connection");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[]{
            region,
            topRegion
        };
    }

    public class BinaryBuild extends Building implements Binaryc {
        public LogicGraph graph;
        public Seq<BinaryBuild> nb = new Seq<>(4);
        public boolean[] connections = new boolean[]{false, false, false, false};

        public boolean nextSignal;
        public boolean lastSignal;

        // instant signaling moment
        private float lastFrame;
        private boolean lastGet;

        public Seq<BinaryBuild> inputs(){
            Seq<BinaryBuild> inputs = new Seq<>();

            Building left = left(), right = right(), back = back();

            if(left != null && left.front() == this && left instanceof BinaryBuild b){
                inputs.add(b);
            }

            if(right != null && right.front() == this && right instanceof BinaryBuild b){
                inputs.add(b);
            }

            if(back != null && back.front() == this && back instanceof BinaryBuild b){
                inputs.add(b);
            }

            return inputs;
        }

        @Override
        public void placed() {
            super.placed();

            Seq<BinaryBuild> inputs = inputs();

            if(inputs.size > 0) {
                LogicGraph main = inputs.get(0).graph;

                for (int i = 1; i < inputs.size; i++) {
                    BinaryBuild input = inputs.get(i);
                    if(main == null) break;
                    if(input.graph == null) continue;

                    main.members.addAll(input.graph.members);
                    main.inputs.addAll(input.graph.inputs);
                    input.graph = main;
                }
            }

            if(graph == null) graph = new LogicGraph();

            if(front() instanceof BinaryBuild b && b.graph != null){
                graph.members.addAll(b.graph.members);
                graph.inputs.addAll(b.graph.inputs);
                b.graph = graph;
            }

            if(inputs.size == 0){
                graph.inputs.add(this);
            }
        }

        @Override
        public void draw() {
            super.draw();

            //Draw.color(Color.white, Pal.accent, lastSignal ? 1f : 0f);
            if(graph != null) Draw.color(graph.color);
            for(int i = 0; i < 4; i++){
                if(connections[i]) Draw.rect(connectionRegion, x, y, rotdeg() + 90 * i);
            }
            Draw.rect(topRegion, x, y, rotate ? rotdeg() : 0f);
        }

        // Mindustry saves block placement rotation even for blocks that don't rotate.
        // Usually this doesn't cause any problems, but with the current implementation
        // it is necessary for non-rotatable binary blocks to have a rotation of 0.
        @Override
        public void created() {
            super.created();
            if(!rotate)rotation(0);
        }

        // connections
        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            // update connected builds only when necessary
            nb.clear();
            nb.add(
                checkType(front()),
                checkType(left()),
                checkType(back()),
                checkType(right())
            );
            updateConnections();

            if(graph != null && inputs().size > 0){
                graph.inputs.remove(this);
            }
        }

        public void updateConnections(){
            for(int i = 0; i < 4; i++){
                connections[i] = connectionCheck(nb.get(i), this);
            }
        }

        // emission
        public boolean emits(){
            return emits;
        }

        public boolean[] outputs(){
            return outputs;
        }

        // instant signaling getter and setters

        public float getLastFrame() {
            return lastFrame;
        }

        public void setLastFrame(float frameid){
            lastFrame = frameid;
        }

        public boolean getLastGet() {
            return lastGet;
        }

        public void setLastGet(boolean last){
            lastGet = last;
        }
    }
}
