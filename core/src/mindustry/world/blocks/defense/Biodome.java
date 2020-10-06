package mindustry.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;

public class Biodome extends Block{
    public static Seq<BiodomeBuild> biodomes = new Seq<>();
    public Color vegetationColor = Color.valueOf("62AE7F");
    public Color vegetationColorLight = Color.valueOf("84F491");

    public @Load("@-middle") TextureRegion middleRegion;
    public @Load("@-top") TextureRegion topRegion;

    public Biodome(String name){
        super(name);
        solid = true;
        destructible = true;
        // requires power, water, and spores
        hasPower = true;
        hasLiquids = true;
        hasItems = true;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    public static void drawWeatherExclusion(){
        Draw.color(Color.white);
        Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
        Draw.flush();
        Gl.stencilOp(Gl.zero, Gl.zero, Gl.zero);
        biodomes.each(BiodomeBuild::drawExclusion);
    }

    public class BiodomeBuild extends Building{
        public boolean active = true;
        public BiodomeDraw drawer;

        @Override
        public void created(){
            super.created();
            drawer = BiodomeDraw.create();
            drawer.build = this;
            drawer.set(x, y);
            drawer.add();
            if(!biodomes.contains(this)){
                biodomes.add(this);
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            drawer.remove();
            biodomes.remove(this);
        }

        @Override
        public void updateTile(){
            super.updateTile();
            // find all units in radius
            // give weatherImmune for a short time
            Units.nearby(team(), x(), y(), radius(), other -> other.apply(StatusEffects.weatherImmune, .5f));
        }

        public float radius(){
            return 150;
        }

        @Override
        public void draw(){
            super.draw();

            if(drawer != null){
                drawer.set(x, y);
            }

            Drawf.liquid(middleRegion, x, y, power.status, vegetationColor);
            Draw.rect(topRegion, x, y);
        }

        public void drawExclusion(){
            Draw.z(Layer.weatherExclusion);
            Draw.color(Color.clear);
            Fill.circle(x, y, radius());
        }

        public void drawDome(){
            if(active){
                float radius = radius();

                Draw.z(Layer.weather);
                Draw.color(team.color, Color.white, .5f);

                Lines.stroke(1.5f);
                Draw.alpha(0.09f + Mathf.clamp(0.08f * .5f));
                Fill.poly(x, y, 12, radius);
                Draw.alpha(1f);
                for(int i = 0; i < 6; i++){
                    Lines.circle(x + Mathf.sinDeg(Time.time() + (360 / 5f) * i) * 8, y + Mathf.cosDeg(Time.time() + (360 / 5f) * i) * 8, radius);
                }
            }
            Draw.reset();
        }
    }

    @EntityDef(value = {BiodomeDrawc.class}, serialize = false)
    @Component(base = true)
    abstract class BiodomeDrawComp implements Drawc{
        transient BiodomeBuild build;

        @Override
        public void draw(){
            build.drawDome();
        }

        @Replace
        @Override
        public float clipSize(){
            return build.radius() * 3f;
        }
    }
}