package com.example.projekt;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.modifier.IModifier;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

public class ProjektActivity extends BaseGameActivity implements IOnMenuItemClickListener {
	private static final int MENU_NEW_GAME = 30;
	private static final int MENU_QUIT = 32;
	private static final int MENU_GAME_NOVICE = 0;
	private static final int MENU_GAME_MEDIUM = 1;
	private static final int MENU_GAME_MEDIUM2 = 2;
	
	public static final String PREFS_NAME = "TopScores";


	private int cameraWidth;
	private int cameraHeight;
	private int fontSize;
	private static final int BALL_SIZE = 64;
	private static final int BALL_FRAMES = 16;

	private Camera mCamera;

	private BitmapTextureAtlas mBalls;

	private TiledTextureRegion[] textures;

	private Font mFont;

	private Font mFont2;

	private ChangeableText mScoreText;

	private ChangeableText mSelectionText;

	private int currentGameType = 0;

	private RepeatingSpriteBackground mBackground;

	private int[] topScores = new int[MENU_GAME_MEDIUM2 + 1];


	private Table mTable;


	private Scene mTableScene;

	private Scene mMenuScene;

	private Scene mSelectGameScene;

	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		for (int i = 0; i < topScores.length; i++) {
			topScores[i] = settings.getInt("scores" + i, 0);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		for (int i = 0; i < topScores.length; i++) {
			editor.putInt("scores" + i, topScores[i]);
		}
		editor.commit();
	}


	@Override
	public final void onLoadComplete() {

	}


	@Override
	public final Engine onLoadEngine() {

		cameraWidth = 800;
		cameraHeight = 480;
		fontSize = 24;

		this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
		return new Engine(
				new EngineOptions(
						true, ScreenOrientation.LANDSCAPE,
						new RatioResolutionPolicy(cameraWidth, cameraHeight),
						this.mCamera)
				.setNeedsSound(true));
	}

	@Override
	public final void onLoadResources() {

		this.mBalls = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBalls,	this, "balls.png", 0, 0);

		textures = new TiledTextureRegion[6];
		textures[0] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 0, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);
		textures[1] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 1, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);
		textures[2] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 2, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);
		textures[3] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 3, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);
		textures[4] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 4, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);
		textures[5] = new TiledTextureRegion(mBalls, 0, BALL_SIZE * 5, BALL_SIZE * BALL_FRAMES, BALL_SIZE, BALL_FRAMES, 1);

		this.mEngine.getTextureManager().loadTexture(this.mBalls);

		this.mBackground = new RepeatingSpriteBackground(cameraWidth, cameraHeight, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "back.png"));

		final BitmapTextureAtlas mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		FontFactory.setAssetBasePath("fonts/");
		this.mFont = FontFactory.createFromAsset(mFontTexture, this, "COMICATE.TTF", fontSize, true, Color.WHITE);
		final BitmapTextureAtlas mFontTexture2 = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont2 = FontFactory.createFromAsset(mFontTexture2, this, "COMICATE.TTF", fontSize * 2, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(mFontTexture);
		this.mEngine.getTextureManager().loadTexture(mFontTexture2);
		this.getFontManager().loadFont(this.mFont);
		this.getFontManager().loadFont(this.mFont2);

	}


	@Override
	public final Scene onLoadScene() {

		this.mMenuScene = createMenuScene();
		this.mSelectGameScene = createSelectGameScene();
		this.mTableScene = createTableScene();


		mTableScene.setChildScene(mMenuScene);

		mTableScene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
            	for (Ball b: mTable.getRemovedBalls()) {
            		if (b != null) {
	            		b.detachSelf();
	            		mTableScene.unregisterTouchArea(b);
            		}
            	}
            }
			@Override
			public void reset() {
				this.onUpdate(0);
			}
		});
		
		return this.mTableScene;
	}

	public Scene createTableScene() {
		final Scene scene = new Scene();

		scene.setBackground(mBackground);

		this.mScoreText = new ChangeableText(cameraWidth / 2, cameraHeight - fontSize - fontSize / 3, this.mFont, getString(R.string.score) + 0, (getString(R.string.score) + "XXXX").length());
		this.mSelectionText = new ChangeableText(cameraWidth / 6, mScoreText.getY(), this.mFont, getString(R.string.selection) + 0, (getString(R.string.selection) + "XXXX").length());

		mTable = new Table(17, 9, scene); // 17,9
		
		return scene;
	}

	public Scene createMenuScene() {
		MenuScene scene = new MenuScene(this.mCamera);

		scene.setBackground(mBackground);

		final IMenuItem newGameItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_NEW_GAME, this.mFont2, getString(R.string.new_game)), 1.0f,0.0f,0.0f, 1.0f,1.0f,1.0f);
		newGameItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		scene.addMenuItem(newGameItem);

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.mFont2, getString(R.string.quit)), 1.0f,0.0f,0.0f, 1.0f,1.0f,1.0f);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		scene.addMenuItem(quitMenuItem);
		
		scene.buildAnimations();

		scene.setOnMenuItemClickListener(this);
		
		return scene;
	}

	public Scene createSelectGameScene() {
		MenuScene scene = new MenuScene(this.mCamera);
		
		scene.setBackground(mBackground);

		final IMenuItem noviceItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_GAME_NOVICE, this.mFont2, getString(R.string.novice_game)), 1.0f,0.0f,0.0f, 1.0f,1.0f,1.0f);
		noviceItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		scene.addMenuItem(noviceItem);
		
		final IMenuItem mediumItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_GAME_MEDIUM, this.mFont2, getString(R.string.medium_game)), 1.0f,0.0f,0.0f,1.0f,1.0f,1.0f);
		mediumItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		scene.addMenuItem(mediumItem);
		
		final IMenuItem medium2Item = new ColorMenuItemDecorator(new TextMenuItem(MENU_GAME_MEDIUM2, this.mFont2, getString(R.string.medium2_game)), 1.0f,0.0f,0.0f,1.0f,1.0f,1.0f);
		medium2Item.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		scene.addMenuItem(medium2Item);
		
		scene.buildAnimations();

		scene.setOnMenuItemClickListener(this);
		
		return scene;
	}
	

	private void doGameOver() {
		mTableScene.clearTouchAreas();

		int s = mTable.getScore();
		String msg = getString(R.string.game_over);
		if (this.topScores[currentGameType] < s) {
			this.topScores[currentGameType] = s;
			msg = getString(R.string.new_record);
		}

		Text text = new Text(0, 0, this.mFont2, msg, HorizontalAlign.CENTER);
        text.setPosition((cameraWidth - text.getWidth()) * 0.5f, (cameraHeight - text.getHeight()) * 0.5f);
        text.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
        text.registerEntityModifier(new RotationModifier(3, 0, 720));
        mTableScene.attachChild(text);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
		case MENU_NEW_GAME:
			mMenuScene.back();

			mTableScene.setChildScene(this.mSelectGameScene, false, true, true);
			return true;
		case MENU_GAME_NOVICE:
			currentGameType = MENU_GAME_NOVICE;
			mTable.createTable(6, 6, 3);
			mSelectGameScene.back();
			return true;
		case MENU_GAME_MEDIUM:
			currentGameType = MENU_GAME_MEDIUM;
			mTable.createTable(12, 7, 3);
			mSelectGameScene.back();
			return true;
		case MENU_GAME_MEDIUM2:
			currentGameType = MENU_GAME_MEDIUM2;
			mTable.createTable(12, 7, 4);
			mSelectGameScene.back();
			return true;
		case MENU_QUIT:
			this.finish();
			return true;
		default:
			return false;
		}
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	if (mTableScene.hasChildScene()) {
        		if (mTableScene.getChildScene() == mMenuScene) {
        			finish();
        			return true;
        		} else {
        			mTableScene.getChildScene().back();
        			mTableScene.setChildScene(this.mMenuScene, false, true, true);
        			return true;
        		}
        	} else {
        		mTableScene.setChildScene(this.mMenuScene, false, true, true);
        	}
        	return true;
        }
        return false;
    }
	
	//////////////// Inner classes


	private class Table {
		private int totalRows = 0;
		private int totalCols = 0;
		private int maxtypes = 0;
		private Ball[][] table;
		private int score = 0;
		private int currentlySelected = 0;
		private Scene mScene;
		private int offsetX;
		private int offsetY;

		private ArrayList<Ball> removedBalls = new ArrayList<Ball>();



		public Table(final int c, final int r, final Scene scene) {
			scene.setTouchAreaBindingEnabled(false);
			score = 0;
			mScene = scene;
			createTable(c, r, textures.length);
		}
		

		public void createTable(int c, int r, int mt) {
			// first remove current table
			if (this.table != null) {
				for (int i=0; i<this.table.length; i++) {
					for (int j=0; j<this.table[0].length; j++) {
						if (table[i][j] != null) {
							table[i][j].setVisible(false);
							this.removedBalls.add(table[i][j]);
						}
					}
				}
			}
			
			totalRows = r;
			totalCols = c;
			maxtypes = mt;
			offsetY = (int) (cameraHeight - r * BALL_SIZE - 1.3 * fontSize);
			offsetX = (cameraWidth - c * BALL_SIZE) / 2;
			runOnUpdateThread(new Runnable() {
				public void run() {
					if (table != null) {
						for (int i = 0; i < table.length; i++) {
							for (int j = 0; j < table[0].length; j++) {
								if (table[i][j] != null) {
									table[i][j].stopAnimation();
									table[i][j].reset();
									table[i][j].setVisible(false);
								}
							}
						}
					}
					mScene.clearTouchAreas();
					mScene.detachChildren();

					table = new Ball[totalRows][totalCols];
					for (int i = 0; i < totalRows; i++) {
						for (int j = 0; j < totalCols; j++) {
							int randomSelection = (int) (Math.random() * maxtypes);
							table[i][j] = new Ball(j, i, randomSelection, Table.this, textures[randomSelection]);
							mScene.attachChild(table[i][j]);
							mScene.registerTouchArea(table[i][j]);
						}
					}
					mScene.setTouchAreaBindingEnabled(true);
					score = 0;
					
					mScene.attachChild(mScoreText);
					mScene.attachChild(mSelectionText);
					final Text text = new Text(4 * cameraWidth / 5, mScoreText.getY(), mFont, getString(R.string.top) + topScores[currentGameType], HorizontalAlign.LEFT);
					mScene.attachChild(text);
				}
			});
		}
		


		public int selectBall(final int col, final int row, final int type) {

			if (col < 0 || col >= totalCols || row < 0 || row >= totalRows) {
				return 0;
			}

			Ball b = table[row][col];

			if (b == null) {
				return 0;
			}

			if (b.isSelected()) {
				return 0;
			}

			if (type != -1 && type != b.getType()) {
				return 0;
			}

			b.setSelected(true);
			int selected = 1;
			selected += selectBall(col - 1, row, table[row][col].getType());
			selected += selectBall(col + 1, row, table[row][col].getType());
			selected += selectBall(col, row + 1, table[row][col].getType());
			selected += selectBall(col, row - 1, table[row][col].getType());
			return selected;
		}


		public void clearSelection() {
			for (int i = 0; i < totalRows; i++) {
				for (int j = 0; j < totalCols; j++) {
					if (table[i][j] != null && table[i][j].isSelected()) {
						table[i][j].setSelected(false);
					}
				}
			}
		}


		public void removeSelectedBalls() {

			int removedRows = 0;
			int removedCols = 0;

			Ball[][] newTable = new Ball[table.length][table[0].length];


			int ss = getSelectionScore();
			if (ss < 1) {
				clearSelection();
				return;
			}
			score += ss;
			



			for (int j = 0; j < totalCols; j++) {
				removedRows = 0;
				for (int i = totalRows - 1; i > -1; i--) {
					Ball b = table[i][j];
					if (b == null || b.isSelected()) {
						if (b != null) {

							b.setVisible(false);
							b.stopAnimation();
							b.clearUpdateHandlers();
							removedBalls.add(b);
							table[i][j] = null;
						}
						removedRows++;
					} else {
						if (b != null && (removedRows != 0 || removedCols != 0)) {
							b.setCol(b.getCol() - removedCols);
							b.setRow(b.getRow() + removedRows);
							b.moveToPosition();
							newTable[b.getRow()][b.getCol()] = b;
						} else {
							newTable[i][j] = table[i][j];
						}
					}
				}

				if (removedRows >= totalRows) {
					removedCols++;
				}
			}

			this.table = newTable;
			
			currentlySelected = -1;
			

			mScoreText.setText(getString(R.string.score) + score);
			mSelectionText.setText(getString(R.string.selection) + 0);

			if (Table.this.endOfGame()) {
				doGameOver();
			}
		}

		public final boolean stillTable() {
			for (int i = 0; i < totalRows; i++) {
				for (int j = 0; j < totalCols; j++) {
					if (table[i][j] != null && table[i][j].isMoving()) {
						return false;
					}
				}
			}
			return true;
		}

		public final boolean isMovementAnimated() {
			return true;
		}

		public int getSelectionScore() {
			if (currentlySelected == -1) {
				this.setCurrentlySelected(-1);
			}
			if (currentlySelected == 0) {
				return 0;
			}
			return (currentlySelected - 1) * (currentlySelected - 1);
		}

		public void setCurrentlySelected(int cs) {
			if (cs == -1) {
				currentlySelected = 0;
				for (int i = 0; i < totalRows; i++) {
					for (int j = 0; j < totalCols; j++) {
						if (table[i][j] != null && table[i][j].isSelected()) {
							currentlySelected++;
						}
					}
				}
			} else {
				this.currentlySelected = cs;
			}
		}

		public int getScore() {
			return score;
		}
		

		public boolean endOfGame() {
			if (table[totalRows-1][0] == null) {
				// if the table is empty, add 1000 points
				score += 1000;
				mScoreText.setText(getString(R.string.score) + score);
				return true;
			}

			for(int i = 0; i < totalRows; i++) {
				for (int j = 0; j < totalCols - 1; j++) {
					if (table[i][j] != null && table[i][j+1] != null && table[i][j].getType() == table[i][j+1].getType()) {
						return false;						
					}
				}
			}

			for(int i = 0; i < totalRows - 1; i++) {
				for (int j = 0; j < totalCols; j++) {
					if (table[i][j] != null && table[i+1][j] != null && table[i][j].getType() == table[i+1][j].getType()) {
						return false;						
					}
				}
			}
			return true;
		}
		
		public int getXCol(final int c) {
			return offsetX + c * BALL_SIZE;
		}
		
		public int getYRow(final int r) {
			return offsetY + r * BALL_SIZE;
		}

		public ArrayList<Ball> getRemovedBalls() {
			ArrayList<Ball> b = new ArrayList<Ball>(this.removedBalls);
			this.removedBalls.clear();
			return b;
		}
	}


	private class Ball extends AnimatedSprite implements IEntityModifierListener {

		private boolean selected;

		private Table mTable;

		private int col;

		private int row;

		private static final float MOV_DURATION = 0.5f;

		private int type;

		private int size;

		private static final int ANIM_TIME = 100;

		private boolean moving = false;


		public Ball(final int c, final int r, final int tp, final Table t, final TiledTextureRegion sp) {
			super(t.getXCol(c), t.getYRow(r), sp.deepCopy());
			row = r;
			col = c;
			mTable = t;
			type = tp;
			size = sp.getTileHeight();
		}


		public int getCol() {
			return col;
		}


		public void setCol(final int c) {
			this.col = c;
		}

		public int getRow() {
			return row;
		}


		public void setRow(final int r) {
			this.row = r;
		}


		public int getType() {
			return this.type;
		}


		public boolean isSelected() {
			return selected;
		}


		public void setSelected(final boolean s) {
			this.selected = s;
			if (selected) {
				animate(ANIM_TIME);
			} else {
				stopAnimation();
			}
		}
		

		public boolean isMoving() {
			return moving;
		}
		

		public void moveToPosition() {
			if (mTable.isMovementAnimated()) {
				this.registerEntityModifier(new MoveModifier(MOV_DURATION, getX(), mTable.getXCol(col), getY(), mTable.getYRow(row), this));
			} else {
				this.setPosition(col * size, row * size);
			}
		}

		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
				final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

			if (pSceneTouchEvent.isActionDown()) {


				if (isSelected()) {

					mTable.removeSelectedBalls();
				} else {

					mTable.clearSelection();

					int s = mTable.selectBall(col, row, -1);

					if (s < 2) {

						mTable.clearSelection();
						mTable.setCurrentlySelected(0);
					} else {
						mTable.setCurrentlySelected(s);
					}

					mSelectionText.setText(getString(R.string.selection) + mTable.getSelectionScore());
				}
				return true;
			}
			return false;
		}

		@Override
		public void onModifierFinished(IModifier<IEntity> arg0, IEntity arg1) {
			this.stopAnimation();
			this.moving = false;
		}

		@Override
		public void onModifierStarted(IModifier<IEntity> arg0, IEntity arg1) {
			this.animate(ANIM_TIME);
			this.moving = true;
		}
	}
}
