package com.bunjlabs.mybackspace2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.bunjlabs.mybackspace2.entities.CruiserEntity;

import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {

	private static final float SPEED = 20;

	private SpriteBatch batch;
	private OrthographicCamera camera;

	private Texture cruiserBeeTexture;
	private Texture cruiserIceTexture;

	private Array<Rectangle> cruisersBee;
	private Rectangle destination;

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		cruiserBeeTexture = new Texture(Gdx.files.internal("bee.png"));
		cruiserIceTexture = new Texture(Gdx.files.internal("ice.png"));

		destination = new Rectangle();
		destination.x = 400;
		destination.y = 400;

		cruisersBee = new Array<Rectangle>();
		for (int i = 0; i < 10; i++) {
			Rectangle cruiser = new Rectangle(150, 150, 50, 50);
			cruiser.setCenter(25, 25);
			cruisersBee.add(cruiser);
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		for (int i = 0; i < cruisersBee.size; i++) {
			Rectangle cruiser = cruisersBee.get(i);
			batch.draw(cruiserBeeTexture, cruiser.x, cruiser.y);
			processCruiser(cruiser, destination, cruisersBee);
		}

		batch.end();
	}

	private float calcDistance(Rectangle a, Rectangle b) {
		float n = a.x - b.x;
		float m = a.y - b.y;
		return (float) Math.sqrt(n*n + m*m);
	}

	//private void calculate

	private void processCruiser(Rectangle cruiser, Rectangle destination, Array<Rectangle> cruisers) {
		float distance = calcDistance(cruiser, destination);
		if (distance < 1) return;

		float dt = Gdx.graphics.getDeltaTime();

		float angle = (float) Math.atan2(destination.y - cruiser.y, destination.x - cruiser.x);
		Rectangle tmp = new Rectangle(
				cruiser.x + (float) Math.cos(angle) * SPEED * dt,
				cruiser.y + (float) Math.sin(angle) * SPEED * dt,
				cruiser.width,
				cruiser.height
		);

		for (int i = 0; i < cruisers.size; i++) {
			Rectangle anotherCruiser = cruisers.get(i);
			if (anotherCruiser != cruiser) {
				float distanceToAnotherCruiser = calcDistance(tmp, anotherCruiser);
				if (distanceToAnotherCruiser < cruiser.height) {
					tmp.x -= Math.cos(angle) * (cruiser.height - distanceToAnotherCruiser);
					tmp.y -= Math.sin(angle) * (cruiser.height - distanceToAnotherCruiser);
				}
			}
		}

		float distancePassed = calcDistance(tmp, cruiser);

		Rectangle tmp2 = new Rectangle(tmp);

		float radius = calcDistance(tmp2, destination);
		float deltaPhi = distancePassed / radius;

		Rectangle tmpRight = new Rectangle(
				tmp2.x + (float) Math.sin(deltaPhi) * SPEED * dt - distancePassed,
				tmp2.y + (float) Math.cos(deltaPhi) * SPEED * dt - distancePassed,
				0,
				0
		);

		Rectangle tmpLeft = new Rectangle(
				tmp2.x + (float) Math.sin(-deltaPhi) * SPEED * dt - distancePassed,
				tmp2.y + (float) Math.cos(-deltaPhi) * SPEED * dt - distancePassed,
				0,
				0
		);

		float distanceRight = calcDistance(tmpRight, destination);
		float distanceLeft = calcDistance(tmpLeft, destination);

		if (distanceRight < distance && distanceRight <= distanceLeft) {
			tmp2 = tmpRight;
			for (int i = 0; i < cruisers.size; i++) {
				Rectangle anotherCruiser = cruisers.get(i);
				if (anotherCruiser != cruiser) {
					if (calcDistance(tmp2, anotherCruiser) < cruiser.height) {
						tmp2 = tmp;
						break;
					}
				}
			}
		} else if (distanceLeft < distance && distanceLeft <= distanceRight) {
			tmp2 = tmpLeft;
			for (int i = 0; i < cruisers.size; i++) {
				Rectangle anotherCruiser = cruisers.get(i);
				if (anotherCruiser != cruiser) {
					if (calcDistance(tmp2, anotherCruiser) < cruiser.height) {
						tmp2 = tmp;
						break;
					}
				}
			}
		}

		cruiser.x = tmp2.x;
		cruiser.y = tmp2.y;
	}

	@Override
	public void dispose() {
		cruiserBeeTexture.dispose();
		cruiserIceTexture.dispose();
		batch.dispose();
	}
}