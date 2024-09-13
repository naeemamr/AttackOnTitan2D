package game.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import game.engine.Battle;
import game.engine.BattlePhase;
import game.engine.base.Wall;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InsufficientResourcesException;
import game.engine.exceptions.InvalidLaneException;
import game.engine.lanes.Lane;
import game.engine.titans.AbnormalTitan;
import game.engine.titans.ArmoredTitan;
import game.engine.titans.ColossalTitan;
import game.engine.titans.PureTitan;
import game.engine.titans.Titan;
import game.engine.titans.TitanRegistry;
import game.engine.weapons.VolleySpreadCannon;
import game.engine.weapons.Weapon;
import game.engine.weapons.WeaponRegistry;
import game.engine.weapons.factory.FactoryResponse;
import game.engine.weapons.factory.WeaponFactory;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Random;

public class View extends Application {
    private HBox gameArea; // Declare gameArea at the class level
    private int numOfLanes;
    private int initialResourcePerLane;
    private String difficulty;
    private int titanSpawnDistance=10; 
    private int currentImageIndex = 0;
    private ImageView[] WeaponsView = new ImageView[4];
    private Battle battle;
    private Weapon selectedWeapon;
    private boolean gameStarted = false;
    private HashMap<Integer, TitanRegistry> titanRegistryMap;
    private List<Lane> lanes = new ArrayList<>();
    public static final int TARGET_COLUMN = 0;
    private int Turn = 1;
    private PriorityQueue<Titan>[] titanQueues;
    private VBox leftHalf = new VBox();
private VBox rightHalf = new VBox();






    public View() throws IOException {
        
        this.battle=new Battle(Turn, currentImageIndex, titanSpawnDistance, numOfLanes, initialResourcePerLane);
        this.titanRegistryMap = DataLoader.readTitanRegistry();
        this.titanQueues = new PriorityQueue[numOfLanes];
        for (int i = 0; i < numOfLanes; i++) {
            titanQueues[i] = new PriorityQueue<>();
    }
    InfoRect.getChildren().addAll(leftHalf, rightHalf);

        }
        

    private void initializeGame(String difficulty) throws IOException {
        if (difficulty.equalsIgnoreCase("easy")) {
            this.numOfLanes = 3;
            this.initialResourcePerLane = 250;
        } else if (difficulty.equalsIgnoreCase("hard")) {
            this.numOfLanes = 5;
            this.initialResourcePerLane = 125;
        } else {
            throw new IllegalArgumentException("Invalid difficulty level. Please choose either 'easy' or 'hard'.");
        }
    
        // Update resourcesGathered
        this.battle.setResourcesGathered(this.initialResourcePerLane * this.numOfLanes);
    
        
    
        Battle battle = new Battle(1, 0, titanSpawnDistance, this.numOfLanes, this.initialResourcePerLane);
    }
    GridPane board = new GridPane();
    private void initializeLanesGUI(int numOfLanes) {
        gameArea.getChildren().clear();
        board.setHgap(0);
        board.setVgap(0);
        board.setPadding(new Insets(0, 0, 0, 0)); // Set padding to 0
        Image grass = new Image("file:///Users/naeemamr/Downloads/Final/Images/Grass.png");
        Image wallimg = new Image("file:///Users/naeemamr/Downloads/Final/Images/Intact Wall.png");
        for (int row = 0; row < numOfLanes; row++) {
            // Create a new Wall object for this row
            Wall wall = new Wall(10000);
            // Create a new Lane object for this row
            Lane lane = new Lane(row, wall);
            lanes.add(lane);

            // Add the Lane object to the Battle class's lists of lanes
            battle.getOriginalLanes().add(lane);
            battle.getLanes().add(lane);

            for (int col = 0; col < 11; col++) {
                StackPane squarePane = new StackPane();
                ImageView square;
                if (col == 0) {
                    square = new ImageView(wallimg);
                    squarePane.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                } else if ((row + col) % 2 == 0) {
                    square = new ImageView(grass);
                } else {
                    square = new ImageView(grass);
                }
                // Associate the Lane object with the StackPane
                squarePane.getProperties().put("lane", lane);
                
                square.setFitWidth(100);
                square.setFitHeight(100);
                squarePane.getChildren().add(square);
                GridPane.setMargin(squarePane, new Insets(0, 0, 0, 0)); // Set margin to 0
                board.add(squarePane, col, row);

                final int finalCol = col; // Declare col as final
                final int finalRow = row; // Declare row as final
                squarePane.setOnMouseClicked(event -> {
                    if (selectedWeapon != null) {
                        // Add the weapon to the clicked cell
                        addWeaponToBoard(currentImageIndex, board, finalCol, finalRow); // Use finalCol and finalRow instead of col and row

                        // Clear the selected weapon
                        selectedWeapon = null;
                    }
                });
            }
        }
        board.setAlignment(Pos.CENTER);
        gameArea.getChildren().add(board);
    }
    public Lane getLeastDangerousLane() {
        Lane leastDangerousLane = null;
        int lowestTotalDangerLevel = Integer.MAX_VALUE;
    
        // Create a new PriorityQueue with the same elements
        PriorityQueue<Lane> lanes = new PriorityQueue<>(battle.getLanes());
    
        while (!lanes.isEmpty()) {
            Lane lane = lanes.poll();
            int totalDangerLevel = 0;
            for (Titan titan : lane.getTitans()) {
                totalDangerLevel += titan.getDangerLevel();
            }
    
            if (totalDangerLevel < lowestTotalDangerLevel) {
                lowestTotalDangerLevel = totalDangerLevel;
                leastDangerousLane = lane;
            }
        }
    
        return leastDangerousLane;
    }
    public void spawnTitanAndAddToLane() {
        System.out.println("spawnTitanAndAddToLane method called");
    
        // Add the Titans for this turn to the least dangerous lane
        battle.addTurnTitansToLane();
    
        // Get the least dangerous lane
        Lane leastDangerousLane = battle.getLanes().peek();
    
        // Get the Titans in the least dangerous lane
        PriorityQueue<Titan> titanQueues = leastDangerousLane.getTitans();
    
        // For each Titan in the lane, create an ImageView and add it to the board
        for (Titan titan : titanQueues) {
            if (!titan.isSpawned()) {
                // Get the image for the Titan
                Image titanImage = getTitanImage(titan);
        
                // Create an ImageView for the Titan image
                ImageView titanImageView = new ImageView(titanImage);
                titanImageView.setFitHeight(100);
                titanImageView.setFitWidth(100);
                titanImageView.setPreserveRatio(true);
                titanImageView.setSmooth(true);
        
                // Associate the Titan object with the ImageView
                titanImageView.getProperties().put("titan", titan);
        
                // Add the ImageView to the rightmost column of the least dangerous row
                StackPane spawnSquarePane = (StackPane) getNodeFromGridPane(board, 10, leastDangerousLane.getRow());
                spawnSquarePane.getChildren().add(titanImageView);
        
                // Mark the Titan as spawned
                titan.setSpawned(true);
            }
        }
    }
    public Lane getLane(int row) {
        System.out.println(lanes.get(row));
        return lanes.get(row);
    }
    public void moveTitans() {
        System.out.println("moveTitans method called");
        // Iterate over the lanes
        for (int row = 0; row < numOfLanes; row++) {
            // Get the Lane for the current row
            Lane lane = getLane(row);
    
            // Iterate over the Titans in the current lane
            for (Titan titan : new ArrayList<>(lane.getTitans())) {
                // Get the current distance of the Titan from the base
                int currentDistance = titan.getDistance();
                System.out.println("Current distance: " + currentDistance);
    
                // Get the StackPanes for the current column and the previous column
                StackPane currentSquarePane = (StackPane) getNodeFromGridPane(board, currentDistance, row);
    
                // Check if currentSquarePane is not null before calling getChildren()
                if (currentSquarePane != null) {
                    // Find the ImageView associated with the Titan and move it
                    for (Node node : new ArrayList<>(currentSquarePane.getChildren())) {
                        if (node instanceof ImageView) {
                            ImageView titanView = (ImageView) node;
                            Titan titanFromView = (Titan) titanView.getProperties().get("titan");
                            if (titanFromView == titan) {
                                // Now you can move the Titan
                                titan.move();
    
                                // Get the new distance of the Titan from the base
                                int newDistance = titan.getDistance();
                                titan.setDistance(newDistance);
                                System.out.println("New distance: " + newDistance);
    
                                // Check if the Titan has moved
                                if (newDistance == 0) {
                                    // Stop the Titan and call hasReachedTarget()
                                    titan.hasReachedTarget();
                                } else {
                                    // Check if the Titan has moved
                                    if (newDistance < currentDistance) {
                                        StackPane newSquarePane = (StackPane) getNodeFromGridPane(board, newDistance, row);
                                        if (newSquarePane != null) {
                                            currentSquarePane.getChildren().remove(titanView);
                                            newSquarePane.getChildren().add(titanView);
                                        }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
    public Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
    public void startTurn() {
        System.out.println("startTurn method called");

        // Call the addTurnTitansToLane() method from the Battle class
        battle.addTurnTitansToLane();
        spawnTitanAndAddToLane();
    
        // Update the game board to reflect the new game state
        updateGameBoard();
    }
    public Image getTitanImage(Titan titan) {
        String imagePath = "";
        if (titan.isDefeated()) {
            return new Image("file:///Users/naeemamr/Downloads/Final/Images/Blank.png"); // Replace with your blank image path
        }
        // Check the type of the titan and set the image path accordingly
        if (titan instanceof AbnormalTitan) {
            imagePath = "file:///Users/naeemamr/Downloads/Final/Images/AbnormalTitan.png";
        } else if (titan instanceof ArmoredTitan) {
            imagePath = "file:///Users/naeemamr/Downloads/Final/Images/ArmoredTitan.png";
        } else if (titan instanceof PureTitan) {
            imagePath = "file:///Users/naeemamr/Downloads/Final/Images/PureTitan.png";
        } else if (titan instanceof ColossalTitan) {
            imagePath = "file:///Users/naeemamr/Downloads/Final/Images/ColossalTitan.png";
        }
    
        // Create and return an Image object
        return new Image(imagePath);
    }
    
     @Override
    public void start(Stage primaryStage) {
        // Initialize gameArea
        gameArea = new HBox(0);
        gameArea.setSpacing(0);
        // Create a new scene for the start menu
        VBox startMenu = new VBox(10);
        VBox.setVgrow(gameArea, Priority.ALWAYS);
        startMenu.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image("file:///Users/naeemamr/Downloads/Final/Images/AOT.png"); // replace with your image path
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
        BackgroundImage backgroundImageObject = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        startMenu.setBackground(new Background(backgroundImageObject));

        // Create the "Start" button
        Button startButton = new Button("Start");
        startButton.setOnAction(event -> {
            // When the "Start" button is pressed, show the "Easy" and "Hard" buttons
            Button easyButton = new Button("Easy");
            Button hardButton = new Button("Hard");

            easyButton.setOnAction(e -> {
                difficulty = "easy";
                try {
                    initializeGame(difficulty);
                    startBattleScene(primaryStage);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            hardButton.setOnAction(e -> {
                difficulty = "hard";
                try {
                    initializeGame(difficulty);
                    startBattleScene(primaryStage);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            startMenu.getChildren().setAll(easyButton, hardButton);
        });

        startMenu.getChildren().add(startButton);

        Scene startScene = new Scene(startMenu, 900, 600);
        primaryStage.setScene(startScene);
        primaryStage.show();

        // primaryStage.setMinWidth(1100);
        // primaryStage.setMaxWidth(1100);
        // primaryStage.setMinHeight(600);
        // primaryStage.setMaxHeight(600);
    }
    private Pane createBottomBar() {
        Pane bottomBar = new Pane();
        bottomBar.setPrefSize(1100, 200);
        Image bgImage = new Image("file:///Users/naeemamr/Downloads/Final/Images/FinalBottomBar.jpeg");
        BackgroundImage bg = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        bottomBar.setBackground(new Background(bg));
        bottomBar.setMinHeight(bottomBar.getPrefHeight());
        // Example overlay squares
        createContainers(bottomBar);

        return bottomBar;
    }
    // Create the large rectangle
    Pane ShopRect = new Pane();
    // Create the two smaller squares
    Pane InfoRect = new Pane();
    Pane ButtonRect = new Pane();
    
    private void createContainers(Pane bottomBar) {
        // Padding around the rectangles to show the background
        double padding = 10;
    
        // Bind the large rectangle size to half the width and full height of the bottom bar minus padding
        ShopRect.prefWidthProperty().bind(bottomBar.widthProperty().divide(2).subtract(padding * 1.5));
        ShopRect.prefHeightProperty().bind(bottomBar.heightProperty().subtract(padding * 2));
    
        // Bind the InfoRect size to be the same as ShopRect
        InfoRect.prefWidthProperty().bind(ShopRect.prefWidthProperty());
        InfoRect.prefHeightProperty().bind(ShopRect.prefHeightProperty());
    
        // Position the rectangles with padding
        // The large rectangle is at the left with padding
        ShopRect.setLayoutX(padding);
        ShopRect.setLayoutY(padding);
        // The InfoRect is next to ShopRect with padding
        InfoRect.layoutXProperty().bind(ShopRect.layoutXProperty().add(ShopRect.prefWidthProperty()).add(padding));
        InfoRect.setLayoutY(padding);
    
        // Add rectangles to the bottom bar
        bottomBar.getChildren().addAll(ShopRect, InfoRect);
    
        // Call createContents after ShopRect has been created and added to bottomBar
        createContents(ShopRect, InfoRect);
    }
    
    public void updateTitansInfo() {
        // Clear the right half of the InfoRect
        rightHalf.getChildren().clear();
    
        // Manually create the information for each Titan type
        Text pureTitanInfo = new Text(
            "Type: Pure Titan\n" +
            "HP: 100\n" +
            "Damage: 15\n" +
            "Height: 15\n" +
            "Speed: 10\n" +
            "Resources Value: 10\n" +
            "Danger Level: 1"
        );
    
        Text abnormalTitanInfo = new Text(
            "Type: Abnormal Titan\n" +
            "HP: 100\n" +
            "Damage: 20\n" +
            "Height: 10\n" +
            "Speed: 15\n" +
            "Resources Value: 15\n" +
            "Danger Level: 2"
        );
    
        Text armoredTitanInfo = new Text(
            "Type: Armored Titan\n" +
            "HP: 200\n" +
            "Damage: 85\n" +
            "Height: 15\n" +
            "Speed: 10\n" +
            "Resources Value: 30\n" +
            "Danger Level: 3"
        );
    
        Text colossalTitanInfo = new Text(
            "Type: Colossal Titan\n" +
            "HP: 1000\n" +
            "Damage: 100\n" +
            "Height: 60\n" +
            "Speed: 5\n" +
            "Resources Value: 60\n" +
            "Danger Level: 4"
        );
    
        // Set the style of the Text objects
        pureTitanInfo.setStyle("-fx-font-weight: bold");
        pureTitanInfo.setFill(Color.WHITE);
        pureTitanInfo.wrappingWidthProperty().bind(rightHalf.widthProperty());
    
        abnormalTitanInfo.setStyle("-fx-font-weight: bold");
        abnormalTitanInfo.setFill(Color.WHITE);
        abnormalTitanInfo.wrappingWidthProperty().bind(rightHalf.widthProperty());
    
        armoredTitanInfo.setStyle("-fx-font-weight: bold");
        armoredTitanInfo.setFill(Color.WHITE);
        armoredTitanInfo.wrappingWidthProperty().bind(rightHalf.widthProperty());
    
        colossalTitanInfo.setStyle("-fx-font-weight: bold");
        colossalTitanInfo.setFill(Color.WHITE);
        colossalTitanInfo.wrappingWidthProperty().bind(rightHalf.widthProperty());
    
        // Add the Text objects to the rightHalf
        rightHalf.getChildren().addAll(pureTitanInfo, abnormalTitanInfo, armoredTitanInfo, colossalTitanInfo);
    }
    
    private void updateInfoRect(Object object) {
        // Clear the left half of the InfoRect
        leftHalf.getChildren().clear();
    
        // Set the style of the InfoRect
        InfoRect.setStyle("-fx-background-color: #8F9779;"); // Replace #color2 with your desired color
        InfoRect.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))); // Set a border for the InfoRect
    
        if (object instanceof Lane) {
            Lane lane = (Lane) object;
            // Update InfoRect with lane information
            Text laneInfo = new Text("Wall current health: " + lane.getLaneWall().getCurrentHealth() + "\nDanger level: " + getDangerLevelOfLane(lane));
    
            // Set the style of the Text object
            laneInfo.setStyle("-fx-font-weight: bold");
            laneInfo.setFill(Color.WHITE);
            laneInfo.wrappingWidthProperty().bind(leftHalf.widthProperty());
    
            leftHalf.getChildren().add(laneInfo);
        }
    }
    private void createContents(Pane ShopRect,Pane InfoRect) {
        // Create the weapon photo and info rectangles
        Pane WeaponPhotoPane = new Pane();
        BorderPane WeaponInfoPane = new BorderPane();

        WeaponPhotoPane.setStyle("-fx-background-color: #A9BA9D;"); // Replace #color1 with your desired color
        WeaponPhotoPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))); // Set a border for the WeaponPhotoPane
        
        WeaponInfoPane.setStyle("-fx-background-color: #8F9779;"); // Replace #color2 with your desired color
        WeaponInfoPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))); // Set a border for the WeaponPhotoPane

        // Create a Text object to display the weapon info
        Text weaponInfo = new Text();
        weaponInfo.wrappingWidthProperty().bind(WeaponInfoPane.widthProperty());

        weaponInfo.setStyle("-fx-font-weight: bold");
        weaponInfo.setFill(Color.WHITE); 
        weaponInfo.setLayoutX(70); 
        weaponInfo.setLayoutY(25); 

        // Add the Text object to the WeaponInfoRect
        WeaponInfoPane.getChildren().add(weaponInfo);

        // Initialize the images
        WeaponsView[0] = new ImageView("file:///Users/naeemamr/Downloads/Final/Images/PiercingCannon.png");
        WeaponsView[0].setFitWidth(200);  // Set the fit width to 200
        WeaponsView[0].setFitHeight(200); // Set the fit height to 200

        WeaponsView[1] = new ImageView("file:///Users/naeemamr/Downloads/Final/Images/SniperCannon.png");
        WeaponsView[1].setFitWidth(200);  // Set the fit width to 200
        WeaponsView[1].setFitHeight(200); // Set the fit height to 200

        WeaponsView[2] = new ImageView("file:///Users/naeemamr/Downloads/Final/Images/VolleySpreadCannon.png");
        WeaponsView[2].setFitWidth(200);  // Set the fit width to 200
        WeaponsView[2].setFitHeight(200); // Set the fit height to 200

        WeaponsView[3] = new ImageView("file:///Users/naeemamr/Downloads/Final/Images/WallTrap.png");
        WeaponsView[3].setFitWidth(200);  // Set the fit width to 200
        WeaponsView[3].setFitHeight(200); // Set the fit height to 200

        // Create the buttons
        Button nextButton = new Button("Next");
        Button prevButton = new Button("Previous");
        Button purchaseButton = new Button("Purchase");

        nextButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex + 1) % WeaponsView.length;
            WeaponPhotoPane.getChildren().removeIf(node -> node instanceof ImageView);
            if (!WeaponPhotoPane.getChildren().contains(WeaponsView[currentImageIndex])) {
                WeaponPhotoPane.getChildren().clear();
                WeaponPhotoPane.getChildren().add(WeaponsView[currentImageIndex]);
            }
            // Update the weapon info
            updateWeaponInfo(weaponInfo, currentImageIndex);
        });
        
        prevButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex - 1 + WeaponsView.length) % WeaponsView.length;
            WeaponPhotoPane.getChildren().removeIf(node -> node instanceof ImageView);
            if (!WeaponPhotoPane.getChildren().contains(WeaponsView[currentImageIndex])) {
                WeaponPhotoPane.getChildren().clear();
                WeaponPhotoPane.getChildren().add(WeaponsView[currentImageIndex]);
            }
            // Update the weapon info
            updateWeaponInfo(weaponInfo, currentImageIndex);
        });

        purchaseButton.setOnAction(e -> {
            // Print out the value of resourcesGathered before the purchase attempt
            System.out.println("Resources before purchase attempt: " + battle.getResourcesGathered());
        
            // Purchase the weapon
            try {
                FactoryResponse response = battle.getWeaponFactory().buyWeapon(battle.getResourcesGathered(), currentImageIndex);
                selectedWeapon = response.getWeapon();
        
                // Set the minRange and maxRange in terms of grid squares
                if (selectedWeapon instanceof VolleySpreadCannon) {
                    int gridMinRange = ((VolleySpreadCannon) selectedWeapon).getMinRange() / 100 + 1;
                    int gridMaxRange = ((VolleySpreadCannon) selectedWeapon).getMinRange() / 100 + 1;
                    ((VolleySpreadCannon) selectedWeapon).setMinRange(gridMinRange);
                    ((VolleySpreadCannon) selectedWeapon).setMaxRange(gridMaxRange);
                }
            } catch (InsufficientResourcesException ex) {
                // Handle exception
                int remainingResources = battle.getResourcesGathered();
                System.out.println("Insufficient resources to purchase weapon. You have " + remainingResources + " resources left.");
        
                // Create a new Alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Insufficient Resources");
                alert.setHeaderText(null);
                alert.setContentText("You do not have enough resources to purchase this weapon. You have " + remainingResources + " resources left.");
        
                // Show the Alert and wait for the user to close it
                alert.showAndWait();
            }
        });
        ButtonBase passTurnButton = new Button(); // Initialize the passTurnButton
        // Assuming you have a Button instance named passTurnButton
        // Add a boolean flag to check if the game has started
            
        ButtonBase startTurnButton = new Button("Start Turn");
// Modify the button's event handler
startTurnButton.setOnAction(event -> {
    if (!gameStarted) {
        // If the game has not started, call the startTurn method and change the button's label
        startTurn();
        battle.performWeaponsAttacks();
        startTurnButton.setText("Pass Turn");
        gameStarted = true;
        battle.finalizeTurns();
    } else {
        // If the game has started, call the passTurn method
        //battle.passTurn();

        moveTitans();
        spawnTitanAndAddToLane();
        battle.performWeaponsAttacks();
		battle.performTitansAttacks();
        updateGameBoard();
        battle.finalizeTurns();
    }
});
        for (Node node : board.getChildren()) {
            node.setOnMouseClicked(event -> {
                System.out.println("selectedWeapon: " + selectedWeapon);
        
                if (selectedWeapon != null) {
                    // Get the clicked cell
                    Node clickedNode = (Node) event.getSource();
                    int col = GridPane.getColumnIndex(clickedNode);
                    int row = GridPane.getRowIndex(clickedNode);
        
                    // Get the Lane object from the clicked Node
                    Lane lane = (Lane) clickedNode.getProperties().get("lane");

                    System.out.println("Lane: " + lane);
        
                    // Purchase the weapon and add it to the lane at the clicked position
                    try {
                        System.out.println("Lane: " + lane);
                        System.out.println("Column: " + col);
                        battle.purchaseWeapon(currentImageIndex, lane, col);
        
                        // Add the weapon image to the board
                        addWeaponToBoard(currentImageIndex, board, col, row);
        
                        // Print a message to the console
                        System.out.println("Weapon purchased and added to the grid at column " + col + ", row " + row);
                    } catch (InsufficientResourcesException | InvalidLaneException e) {
                        System.out.println("Failed to purchase weapon: " + e.getMessage());
                        System.out.println("Exception type: " + e.getClass().getName());
                        e.printStackTrace();
                    
                        if (e instanceof InvalidLaneException) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error Dialog");
                            alert.setHeaderText("Invalid Operation");
                            alert.setContentText("Invalid lane: " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                    // Clear the selected weapon
                    selectedWeapon = null;
                }
            });
            
        }
        for (Node node : board.getChildren()) {
            node.setOnMouseEntered(event -> {
                // Get the lane from the node's properties
                Lane lane = (Lane) node.getProperties().get("lane");
        
                // Update the InfoRect with the lane information
                updateInfoRect(lane);
            });
        }

        // Create a VBox to hold the buttons
        VBox buttonBox = new VBox();
        buttonBox.setSpacing(5); // Add some spacing between the buttons
        buttonBox.getChildren().addAll(nextButton, prevButton, purchaseButton, startTurnButton); // Add the buttons to the VBox
        buttonBox.setPadding(new Insets(10)); // 10 pixels of padding on all sides
        WeaponInfoPane.setBottom(buttonBox); // Set the buttonBox to the bottom of the WeaponInfoPane

        
        // Bind the weapon photo and info rectangles size to half the width and full height of the ShopRect
        WeaponPhotoPane.prefWidthProperty().bind(ShopRect.prefWidthProperty().divide(2.5));
        WeaponPhotoPane.prefHeightProperty().bind(ShopRect.prefHeightProperty());
        WeaponInfoPane.prefWidthProperty().bind(ShopRect.prefWidthProperty().divide(2));
        WeaponInfoPane.prefHeightProperty().bind(ShopRect.prefHeightProperty());
    
        // Position the weapon info rectangle to the right of the weapon photo rectangle
            WeaponInfoPane.layoutXProperty().bind(WeaponPhotoPane.widthProperty());
        
            // Set colors for visibility
            //WeaponPhotoPane.setFill(Color.LIGHTGRAY);
            //WeaponInfoPane.setFill(Color.LIGHTGRAY);

            // Add the first image to the WeaponPhotoPane
            WeaponPhotoPane.getChildren().clear(); // Clear existing children before adding a new one
            WeaponPhotoPane.getChildren().add(WeaponsView[0]);

            // Update the weapon info for the first weapon
            updateWeaponInfo(weaponInfo, 0);

            // Add weapon photo and info rectangles to the ShopRect
            ShopRect.getChildren().clear(); // Clear existing children before adding new ones
            ShopRect.getChildren().addAll(WeaponPhotoPane, WeaponInfoPane);
        }
    private Image getWeaponImage(int weaponIndex) {
        if (weaponIndex >= 0 && weaponIndex < WeaponsView.length) {
            return WeaponsView[weaponIndex].getImage();
        } else {
            System.out.println("Invalid weapon index.");
            return null;
        }
    }
    
    private void addWeaponToBoard(int weaponIndex, GridPane gridPane, int col, int row) {
        Node cell = getNodeFromGridPane(gridPane, col, row);
        if (cell != null) {
            ImageView weaponView = new ImageView(getWeaponImage(weaponIndex));
            weaponView.setFitWidth(100); // Set the width to the width of the grid square
            weaponView.setFitHeight(100); // Set the height to the height of the grid square
            ((StackPane) cell).getChildren().add(weaponView);

        }
    }
    
    private void updateWeaponInfo(Text weaponInfo, int weaponCode) {
        try {
            HashMap<Integer, WeaponRegistry> weaponRegistryMap = DataLoader.readWeaponRegistry();
    
            if (weaponRegistryMap.containsKey(weaponCode)) {
                // Now it's safe to fetch the weapon object
                WeaponRegistry weapon = weaponRegistryMap.get(weaponCode);
                
                weaponInfo.setText("Name: " + weapon.getName() + "\n"
                                + "Type: " + weapon.getType() + "\n"
                                 + "Damage: " + weapon.getDamage() + "\n"
                                 + "Min Range: " + weapon.getMinRange() + "\n"
                                 + "Max Range: " + weapon.getMaxRange() + "\n"
                                 + "Price: " + weapon.getPrice());
            } else {
                // Display a user-friendly message when a weapon with a given code is not found
                weaponInfo.setText("No weapon found for weapon code: " + weaponCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getDangerLevelOfLane(Lane lane) {
        int totalDangerLevel = 0;
    
        // Iterate over each Titan in the lane
        for (Titan titan : lane.getTitans()) {
            // Add the Titan's danger level to the total
            totalDangerLevel += titan.getDangerLevel();
        }
    
        return totalDangerLevel;
    }
    private void startBattleScene(Stage primaryStage) {
        // Main layout container
        BorderPane borderPane = new BorderPane();

        // Top bar with game controls and info
        HBox topBar = new HBox(10);
        topBar.setSpacing(10);
        topBar.setPrefSize(1100, 75); // Set preferred width to 1100 and height to 100

        // Create labels to indicate what each variable represents
    Label resourcesLabelIndicator = new Label("Resources:");
    resourcesLabelIndicator.textProperty().bind(
    Bindings.concat("Resources: ", battle.resourcesGatheredProperty().asString()));
    Label scoreLabelIndicator = new Label("Score:");
    scoreLabelIndicator.textProperty().bind(
    Bindings.concat("Score: ", battle.scoreProperty().asString()));
    Label turnsLabelIndicator = new Label();
    turnsLabelIndicator.textProperty().bind(
    Bindings.concat("Turns: ", battle.numberOfTurnsProperty().asString()));
    Label phaseLabelIndicator = new Label();
phaseLabelIndicator.textProperty().bind(
    Bindings.concat("Battle Phase: ", battle.battlePhaseProperty().asString())
);

    // Add the indicator labels and the variable labels to the top bar
    topBar.getChildren().addAll(resourcesLabelIndicator, scoreLabelIndicator, turnsLabelIndicator, phaseLabelIndicator);
    borderPane.setTop(topBar);

        // Main game area - using HBox for horizontal layout of lanes
        gameArea = new HBox(0); // Initialize gameArea in the start method
        gameArea.setSpacing(0); // Spacing between lanes
        initializeLanesGUI(this.numOfLanes);  // Initialize lanes based on difficulty level
        gameArea.setAlignment(Pos.CENTER);
        borderPane.setCenter(gameArea);

        Pane bottomBar = createBottomBar();
        bottomBar.setPrefSize(1100, 200); 
        borderPane.setBottom(bottomBar);
        gameArea.setSpacing(0); // Spacing between lanes

        // Calculate the height of the Scene based on the number of lanes and the height of the bottom bar
        int laneHeight = 100; // replace with the actual height of a lane
        int sceneHeight = this.numOfLanes * laneHeight + 275; // 200 is the height of the bottom bar

        // Scene setup
        Scene scene = new Scene(borderPane, 1100, sceneHeight);
        primaryStage.setTitle("Attack on Titan: Utopia");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.sizeToScene();

        // primaryStage.setMinWidth(1100);
        // primaryStage.setMaxWidth(1100);
        // primaryStage.setMinHeight(600);
        // primaryStage.setMaxHeight(600);
}
public void updateGameBoard() {
    System.out.println("Game board updated");
    // Draw the updated state of the game
    boolean allLanesLost = true;
    for (int i = 0; i < board.getRowCount(); i++) {
        // Get the Lane object for this row
        Lane lane = getLane(i);

        // Check if the wall in this lane is destroyed
        boolean isLaneLost = lane.isLaneLost();
        if (!isLaneLost) {
            allLanesLost = false;
        }
        for (int j = 0; j < board.getColumnCount(); j++) {
            // Get the StackPane for this position
            StackPane squarePane = (StackPane) getNodeFromGridPane(board, j, i);

            // For each child of the StackPane
            for (Node child : new ArrayList<>(squarePane.getChildren())) {
                if (child instanceof ImageView) {
                    ImageView imageView = (ImageView) child;

                    // If the ImageView has an associated Titan
                    if (imageView.getProperties().containsKey("titan")) {
                        Titan titan = (Titan) imageView.getProperties().get("titan");

                        // If the Titan is defeated
                        if (titan.isDefeated()) {
                            // Replace its image with the grass image
                            Image grassImage = new Image("file:///Users/naeemamr/Downloads/Final/Images/Empty.png"); // Replace with your grass image path
                            imageView.setImage(grassImage);
                        } else {
                            // Otherwise, update the titan's image
                            imageView.setImage(getTitanImage(titan));
                        }
                    }
                }
            }

            if (isLaneLost) {
                // If the wall is destroyed, change the image of the wall and its lane
                Image destroyedLaneImage = new Image("file:///Users/naeemamr/Downloads/Final/Images/Destroyed Grass.png");
                ImageView destroyedLaneView = new ImageView(destroyedLaneImage);
                destroyedLaneView.setFitWidth(100);
                destroyedLaneView.setFitHeight(100);
            
                // Dim the brightness of the image
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setBrightness(-0.5); // Value range is -1 to 1
                destroyedLaneView.setEffect(colorAdjust);
            
                squarePane.getChildren().setAll(destroyedLaneView);
            
                // If it's the first column, it's the wall, so change its image too
                if (j == 0) {
                    Image destroyedWallImage = new Image("file:///Users/naeemamr/Downloads/Final/Images/Wall.png");
                    ImageView destroyedWallView = new ImageView(destroyedWallImage);
                    destroyedWallView.setFitWidth(100);
                    destroyedWallView.setFitHeight(100);
            
                    // Dim the brightness of the image
                    destroyedWallView.setEffect(colorAdjust);
            
                    squarePane.getChildren().setAll(destroyedWallView);
                }
            }
        }
                
            }
            if (allLanesLost) {
                // Create a new Alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("All lanes have been lost.");
            
                // Create a new Button
                Button restartButton = new Button("Restart");
                restartButton.setOnAction(e -> {
                    // Close the current stage
                    Stage currentStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    currentStage.close();
            
                    // Open the start stage
                    Stage startStage = new Stage();
                    try {
                        new View().start(startStage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            
                // Set the button as the graphic for the alert
                alert.setGraphic(restartButton);
            
                // Show the Alert and wait for the user to close it
                alert.showAndWait();
            }
}
    public static void main(String[] args) {
        launch(args);
    }
}
