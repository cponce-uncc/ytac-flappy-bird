import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class FlappyBirdApp extends GameApplication {
    private PlayerComponent playerComponent;
    private boolean requestNewGame = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void requestNewGame() {
        requestNewGame = true;
    }

    private void showGameOver() {
        getDisplay().showMessageBox("Demo Over. Thanks for playing!", getGameController()::exit);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1920);
        settings.setHeight(1080);
        settings.setTitle("YTAC Flappy Bird Project");
        settings.setVersion("1.0");
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                playerComponent.jump();
            }
        }, KeyCode.SPACE, VirtualButton.UP);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("stageColor", Color.BLACK);
        vars.put("score", 0);
    }

    private boolean loopBGM = true;  // Loop background music

    @Override
    protected void initGame() {
        if (loopBGM) {
            loopBGM("bgm.mp3");
            loopBGM = false;
        }

        initBackground();
        initPlayer();
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity a, Entity b) {
                requestNewGame();
            }
        });
    }

    @Override
    protected void initUI() {
        Text uiScore = new Text("");
        uiScore.setFont(Font.font(72));
        uiScore.setTranslateX(getAppWidth() - 200);
        uiScore.setTranslateY(50);
        uiScore.fillProperty().bind(getop("stageColor"));
        uiScore.textProperty().bind(getip("score").asString());

        addUINode(uiScore);

        Group dpadView = getInput().createVirtualDpadView();

        addUINode(dpadView, 0, 625);
    }


        private void initBackground() {
            Rectangle rect = new Rectangle(getAppWidth(), getAppHeight(), Color.WHITE);

            Entity bg = entityBuilder()
                    .view(rect)
                    .with("rect", rect)
                    .buildAndAttach();

            bg.xProperty().bind(getGameScene().getViewport().xProperty());
            bg.yProperty().bind(getGameScene().getViewport().yProperty());
        }

        private void initPlayer () {
            playerComponent = new PlayerComponent();

            Entity player = entityBuilder()
                    .at(100, 100)
                    .type(EntityType.PLAYER)
                    .bbox(new HitBox(BoundingShape.box(70, 60)))
                    .view(texture("bird.png").toAnimatedTexture(2, Duration.seconds(0.5)).loop())
                    .collidable()
                    .with(playerComponent, new WallBuildingComponent())
                    .build();

            getGameScene().getViewport().setBounds(0, 0, Integer.MAX_VALUE, getAppHeight());
            getGameScene().getViewport().bindToEntity(player, getAppWidth() / 3, getAppHeight() / 2);

            playSpawnAnimation(player);
        }

        private void playSpawnAnimation (Entity player){
            player.setScaleX(0);
            player.setScaleY(0);

            getGameWorld().addEntity(player);

            animationBuilder()
                    .duration(Duration.seconds(0.86))
                    .interpolator(Interpolators.BOUNCE.EASE_OUT())
                    .scale(player)
                    .from(new Point2D(0, 0))
                    .to(new Point2D(1, 1))
                    .buildAndPlay();
        }


        @Override
        protected void onUpdate(double tpf) {
            inc("score", +1);

            if (geti("score") == 10000) {
                showGameOver();
            }

            if (requestNewGame) {
                requestNewGame = false;
                getGameController().startNewGame();
            }
        }
}
