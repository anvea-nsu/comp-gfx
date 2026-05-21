package controllers;

import model.Camera;
import model.RenderModel;
import model.SceneModel;
import view.MainFrame;

public class MainController {
    private final MainFrame frame;
    private final SceneModel sceneModel;
    private final RenderModel renderModel;
    private final Camera camera;

    public MainController() {
        sceneModel  = new SceneModel();
        renderModel = new RenderModel();
        camera      = new Camera();

        frame = new MainFrame(sceneModel, renderModel, camera);

        MenuController menuController = new MenuController(frame, sceneModel, renderModel, camera);
        new WireframeController(frame.getMainPanel(), camera, menuController::canControlCamera);

        frame.setVisible(true);
    }
}
