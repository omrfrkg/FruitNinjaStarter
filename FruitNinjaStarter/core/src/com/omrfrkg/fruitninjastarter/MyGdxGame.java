package com.omrfrkg.fruitninjastarter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture background;
	Texture apple;
	Texture bill;
	Texture cherry;
	Texture ruby;

	BitmapFont font;
	FreeTypeFontGenerator fontGenerator;

	//Scores & Lives
	int lives = 0;
	int score = 0;

	//Time Control
	private double currentTime;
	private double gameOverTime = -1.0f;

	Random random = new Random();
	Array<Fruit> fruitArray = new Array<Fruit>();

	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;


	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("ninjabackground.png");
		apple = new Texture("apple.png");
		bill = new Texture("bill.png");
		cherry = new Texture("cherry.png");
		ruby = new Texture("ruby.png");

		Gdx.input.setInputProcessor(this);

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Bold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
		params.size = 40;
		params.characters = "0123456789 ScreCutoplay: .+-";
		font = fontGenerator.generateFont(params);

		Fruit.radius = Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth()) / 20f;
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		//Calc Time
		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime,0.3);
		float deltaTime = (float) frameTime;
		currentTime = newTime;


		if (lives <= 0 && gameOverTime == 0f){
			//game over
			gameOverTime = currentTime;

		}

		if (lives > 0){
			//game mode

			genSpeed -= deltaTime * 0.015f;

			if (genCounter <= 0f){
				genCounter = genSpeed;
				addItem();
			}
			else{
				genCounter -= deltaTime;
			}

			//Draw Life
			for (int i = 0; i < lives; i++){
				batch.draw(apple,i*50f + 20f,Gdx.graphics.getHeight()-60f,50f,50f);
			}

			for (Fruit fruit : fruitArray){
				fruit.update(deltaTime);

				switch (fruit.type){
					case Regular:
						batch.draw(apple,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case Extra:
						batch.draw(cherry,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case Enemy:
						batch.draw(ruby,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case Life:
						batch.draw(bill,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
				}

			}

			boolean holdlives = false;
			Array<Fruit> toRemove = new Array<Fruit>();
			for (Fruit fruit : fruitArray){
				if (fruit.outOfScreen()){
					toRemove.add(fruit);

					if (fruit.living && fruit.type == Fruit.Type.Regular){
						lives--;
						holdlives = true;
						break;
					}
				}
			}

			if (holdlives){
				for (Fruit f : fruitArray){
					f.living = false;
				}
			}

			for (Fruit f : toRemove){
				fruitArray.removeValue(f,true);
			}

		}

		font.draw(batch,"Score : "+score,40,40);

		if (lives <= 0){
			font.draw(batch,"Cut to play!",Gdx.graphics.getWidth()*0.5f,Gdx.graphics.getHeight()*0.5f);
		}

		batch.end();
	}

	private void addItem(){

		float pos = random.nextFloat() * Gdx.graphics.getWidth();

		Fruit item = new Fruit(new Vector2(pos,-Fruit.radius),new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * 0.3f +((random.nextFloat() - 0.5f)),Gdx.graphics.getHeight()*0.5f));

		float type = random.nextFloat();
		if (type > 0.98){
			item.type = Fruit.Type.Life;
		}
		else if (type > 0.88){
			item.type = Fruit.Type.Extra;
		}
		else if (type > 0.78){
			item.type = Fruit.Type.Enemy;
		}
		fruitArray.add(item);

	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		fontGenerator.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (lives <= 0 && currentTime - gameOverTime > 2f){
			//menu mode
			gameOverTime = 0f;
			score = 0;
			lives = 4;
			genSpeed = startGenSpeed;
			fruitArray.clear();
		}
		else{
			//game mode
			Array<Fruit> toRemove = new Array<Fruit>();
			Vector2 pos = new Vector2(screenX,Gdx.graphics.getHeight()-screenY);
			int plusScore = 0;
			for (Fruit f : fruitArray){
				if (f.clicked(pos)){
					toRemove.add(f);
					switch (f.type){
						case Regular:
							plusScore++;
							break;
						case Extra:
							plusScore+=2;
							score++;
							break;
						case Enemy:
							lives--;
							break;
						case Life:
							lives++;
							break;
					}
				}
			}
			score += plusScore*plusScore;
			for (Fruit f : toRemove){
				fruitArray.removeValue(f,true);
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
