package main.java.client;

import main.java.server.OmegleService;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OmegleClient extends Application {
	private static OmegleService service;
	private Timeline timeline;
	private String currEvent = "";
	private boolean isTyping = false;
	private Button send = new Button();
	private Label lbl = new Label();
	private Label StrangerStatus = new Label();
	private Button connection = new Button();
	private TextArea chat = new TextArea();
	    
	public static void main(String[] args) {	    	
	        launch(args);        
	    }
	    
	    @Override
	    public void start(Stage primaryStage) {
	        primaryStage.setTitle("Omegle");   
	        setupStatusLabel(StrangerStatus);
	        onDisconnect();
	        chat.setStyle("-fx-border-color: red;-fx-background-color: white;");
	        chat.setPrefSize(400, 200);
	        chat.setEditable(false);
	        TextArea area = new TextArea();
	        onAreaAction(area);
	        addImages();
	        connection.setPrefWidth(100);
	        send.setText("Send");
	        onConnectButtonAction();
	        onSendButtonAction(area);
	        
	        GridPane root = new GridPane();
	        root.setAlignment(Pos.TOP_CENTER);
	        GridPane gridpane = new GridPane();
	        gridpane.setHgap(20);
	        gridpane.setVgap(20);
	        GridPane.setHalignment(lbl, HPos.CENTER);
	        GridPane.setHalignment(connection, HPos.RIGHT);
	        setGridLayout(gridpane);
	        GridPane upperPane = new GridPane();       
	        setupUpperPane(upperPane);
	        GridPane lowerPane = new GridPane();
	        setLowerLayout(lowerPane);
	        gridpane.add(lbl, 0, 0);
	        gridpane.add(upperPane, 0, 1);
	        gridpane.add(chat, 0, 2);
	        gridpane.add(area, 0, 3);           
	        gridpane.add(lowerPane, 0, 4);
	        
	        root.getChildren().add(gridpane);
	        primaryStage.setScene(new Scene(root, 600, 600));
	        primaryStage.show();
	    }

		private void scrollDown() {
			ScrollPane scrollPane = (ScrollPane) chat.lookup(".scroll-pane"); 
    		if (scrollPane != null) 
    			scrollPane.setVvalue(1.0);
		}

		private void onConnectButtonAction() {
			connection.setOnAction(new EventHandler<ActionEvent>() {
	       	 
	            @Override
	            public void handle(ActionEvent event) {
	            	/*if (service == null)*/ service = new OmegleService();
	            	if (service != null)
	            	{
	            		chat.setText("Connected.");
	            		onConnect();
	            		isTyping = false;
	            		timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
	            			try {
								Thread.sleep(150);
							} catch (InterruptedException e) {
								//e.printStackTrace();
							}
	            			scrollDown();
						    currEvent = service.getCurrEvent();
						    if (currEvent != null) 
						    {	
						    	if (currEvent.startsWith(ClientConstants.EVENT_GOTMESSAGE))
						    	{
						    		String msg = service.getMsgs().poll();
						    		while (msg != null)
						    		{						    			
						    			chat.setText(chat.getText()+"\nStranger: "+msg);
						    			msg = service.getMsgs().poll();
						    			StrangerStatus.setText(ClientConstants.STRANGER_STATUS_IDLE);
						    		}
						    		scrollDown();
						    	}
						    	else if (currEvent.startsWith(ClientConstants.EVENT_TYPING)) 
						    		StrangerStatus.setText(ClientConstants.STRANGER_STATUS_TYPING);
						    	else if (currEvent.startsWith(ClientConstants.EVENT_DISCONNECT)) 
						    	{
						    		chat.setText(chat.getText()+"\nStranger disconnected.");
						    		onDisconnect();
						    	}
						    	if (!currEvent.startsWith(ClientConstants.EVENT_TYPING)) currEvent = "";
						    }
	            	    }));
	            	    timeline.setCycleCount(Animation.INDEFINITE);
	            	    timeline.play();
	            	}
	            }
	        });
		}

		private void setupUpperPane(GridPane upperPane) {
			upperPane.setAlignment(Pos.CENTER);
			Label stranger = new Label("Stranger Status: ");
	        upperPane.add(stranger, 0, 0);
	        upperPane.add(StrangerStatus, 1, 0);
		}

		private void addImages() {
			Image image = new Image(getClass().getResourceAsStream(ClientConstants.RESOURCES+"send.png"));
	        ImageView view = new ImageView(image);
	        view.setFitHeight(20);
	        view.setFitWidth(50);   
	        send.setGraphic(view);    

	        Image imageOmegle = new Image(getClass().getResourceAsStream(ClientConstants.RESOURCES+"omegle.png"));
	        ImageView viewTop = new ImageView(imageOmegle);  
	        lbl.setGraphic(viewTop);
	        
	        updateConnectionButton(ClientConstants.STATUS_OFFLINE);         
		}

		private void updateConnectionButton(String status) {
			String img = "";
			String action = "";
			if (status.equals(ClientConstants.STATUS_OFFLINE)) 
			{
				img = ClientConstants.RESOURCES+"ON.png";
				action = "Connect";
			}
			else 
			{
				img = ClientConstants.RESOURCES+"OFF.png";
				action = "Disconnect";
			}
			Image imageConnect = new Image(getClass().getResourceAsStream(img));
	        ImageView viewBottom = new ImageView(imageConnect);
	        viewBottom.setFitHeight(20);
	        viewBottom.setFitWidth(20);   
	        connection.setGraphic(viewBottom);
	        connection.setText(action);
		}

		private void onAreaAction(TextArea area) {
			area.setOnKeyTyped(new EventHandler<Event>() {
				@Override
				public void handle(Event arg0) {
					if (!isTyping)
					{
						service.sendOmegleHttpRequest(ClientConstants.URL_TYPING, null);//sendOmegleTypeSignal();
						isTyping = true;
					}
				}
			});
		}
		private void onSendButtonAction(TextArea area) {
			send.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	String toSend = area.getText();
	            	if (!toSend.isEmpty() && service != null)
	            	{
	            		service.sendOmegleHttpRequest(ClientConstants.URL_SEND, toSend);//sendOmegleMsg(toSend);
	            		isTyping = false;
	            		chat.setText(chat.getText()+"\nYou: "+toSend);
	            		area.setText("");
	            	}    		
	            }
	        });
		}
		private void onDisconnectButtonAction() {
			connection.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	if (service != null)
	            	{	            		
	            		service.sendOmegleHttpRequest(ClientConstants.URL_DISCONNECT,null);//sendOmegleMsg(toSend);
	            		isTyping = false;
	            		chat.setText(chat.getText()+"\nYou Disconnected. "); 
	            		timeline.stop();
	            		onDisconnect();
	            	}
	            }
	        });
		}
		private void onDisconnect() {
			send.setDisable(true);
			updateConnectionButton(ClientConstants.STATUS_OFFLINE); 
			chat.setStyle("-fx-border-color: red;");
			StrangerStatus.setText(ClientConstants.STRANGER_STATUS_OFFLINE);
			if (service != null) {
				service.destroy();
				//service = null;
			}
			onConnectButtonAction();
		}
		

		private void onConnect() {
			send.setDisable(false);
			updateConnectionButton(ClientConstants.STATUS_ONLINE); 
			chat.setStyle("-fx-border-color: greenyellow;");
			StrangerStatus.setText(ClientConstants.STRANGER_STATUS_IDLE);
			onDisconnectButtonAction();
		}

		private void setupStatusLabel(Label status) {
			status.setStyle("-fx-border-color: black; "
					+ "-fx-font: 16px \"Serif\"; "
					+ "-fx-fill: #818181;"
					+ "-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
	        status.setDisable(true);
	        status.setAlignment(Pos.CENTER);
	        status.setPrefWidth(200);
		}

		private void setLowerLayout(GridPane lowerpane) {
			ColumnConstraints col1 = new ColumnConstraints();
	        col1.setPercentWidth(50);
	        ColumnConstraints col2 = new ColumnConstraints();
	        col2.setPercentWidth(50);        
	        lowerpane.getColumnConstraints().addAll(col1, col2);
	        
	        lowerpane.add(send, 0, 0);
	        lowerpane.add(connection, 1, 0);	        
		}

		private void setGridLayout(GridPane gridpane) {
			RowConstraints row1 = new RowConstraints();
	        row1.setPercentHeight(10);
	        RowConstraints row2 = new RowConstraints();
	        row2.setPercentHeight(5);
	        RowConstraints row3 = new RowConstraints();
	        row3.setPercentHeight(40);
	        RowConstraints row4 = new RowConstraints();
	        row4.setPercentHeight(30);
	        RowConstraints row5 = new RowConstraints();
	        row5.setPercentHeight(15);
	        gridpane.getRowConstraints().addAll(row1, row2, row3, row4, row5); // each get 50% of width       
		}		
	}

