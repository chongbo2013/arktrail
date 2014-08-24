package net.mostlyoriginal.game.system.render;

/**
 * @author Daan van Yperen
 */

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.manager.AbstractAssetSystem;
import net.mostlyoriginal.api.system.camera.CameraSystem;
import net.mostlyoriginal.game.component.ui.Bar;
import net.mostlyoriginal.game.manager.FontManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Render and progress animations.
 *
 * @author Daan van Yperen
 * @see net.mostlyoriginal.api.component.graphics.Anim
 */
@Wire
public class BarRenderSystem extends EntitySystem {

    private ComponentMapper<Pos> pm;
    private ComponentMapper<Bar> mBar;
    private CameraSystem cameraSystem;

    private SpriteBatch batch;
    private final List<Entity> sortedEntities = new ArrayList<Entity>();
    public boolean sortedDirty = false;

    public Comparator<Entity> layerSortComperator = new Comparator<Entity>() {
        @Override
        public int compare(Entity e1, Entity e2) {
            return mBar.get(e1).layer - mBar.get(e2).layer;
        }
    };

    private float age;
    private FontManager fontManager;
    private AbstractAssetSystem abstractAssetSystem;

    public BarRenderSystem() {
        super(Aspect.getAspectForAll(Pos.class, Bar.class));
        batch  = new SpriteBatch(1000);
    }

    @Override
    protected void begin() {

        age += world.delta;

        batch.setProjectionMatrix(cameraSystem.camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void end() {
        batch.end();
    }

    @Override
    protected void processEntities(ImmutableBag<Entity> entities) {

        if (sortedDirty) {
            sortedDirty = false;
            Collections.sort(sortedEntities, layerSortComperator);
        }

        for (Entity entity : sortedEntities) {
            process(entity);
        }
    }

    @Override
    protected boolean checkProcessing() {
        return true;
    }

    protected void process(final Entity entity) {

        final Bar bar = mBar.get(entity);
        final Pos pos = pm.get(entity);


        final BitmapFont font = fontManager.font;
        font.setColor(bar.color);
        font.draw(batch, bar.text, pos.x, pos.y);

        BitmapFont.TextBounds bounds = font.getBounds(bar.text);
        batch.setColor(Color.WHITE);

        final com.badlogic.gdx.graphics.g2d.Animation gdxanim = abstractAssetSystem.get(bar.animationId);
        if ( gdxanim == null) return;
        final com.badlogic.gdx.graphics.g2d.Animation gdxanim2 = abstractAssetSystem.get(bar.animationIdEmpty);
        if ( gdxanim2 == null) return;

        final TextureRegion frame = gdxanim.getKeyFrame(0,false);
        final TextureRegion frame2 = gdxanim2.getKeyFrame(0,false);

        // make sure one bubble is always shown.
        int emptyCount = ( bar.value == 0 && bar.valueEmpty == 0 ) ? 1 : bar.valueEmpty;

        int barWidth = frame.getRegionWidth() + 1;

        if ( bar.value + bar.valueEmpty >= 10 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 20 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 30 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 40 ) barWidth -=1;

        for ( int i =0; i< bar.value; i++)
        {
            batch.draw(frame,
                    (int)pos.x + bounds.width + i * barWidth,
                    (int)pos.y - bounds.height,
                    frame.getRegionWidth(),
                    frame.getRegionHeight());
        }
        for ( int i =0; i< emptyCount; i++)
        {
            batch.draw(frame2,
                    (int)pos.x + bounds.width + (i+bar.value) * barWidth,
                    (int)pos.y - bounds.height,
                    frame.getRegionWidth(),
                    frame.getRegionHeight());
        }
    }

    @Override
    protected void inserted(Entity e) {
        sortedEntities.add(e);
        sortedDirty = true;
    }

    @Override
    protected void removed(Entity e) {
        sortedEntities.remove(e);
    }
}
