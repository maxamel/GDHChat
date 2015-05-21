package main.java.client;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.InlineCssTextArea;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OmegleClient extends Application {
	private OmegleService service;
	private Timeline timeline;
	private String currEvent = "";
	private boolean isTyping = false;
	private Button send = new Button();
	private Label lbl = new Label();
	private Label StrangerStatus = new Label();
	private Button connection = new Button();
	private TextArea chat = new TextArea();
	private InlineCssTextArea interests = new InlineCssTextArea();
	private ArrayList<Integer> interestsIndices = new ArrayList<>();
	    
	public static void main(String[] args) {	    	
	        launch(args);        
	    }
	    
	    @Override
	    public void start(Stage primaryStage) {
	        primaryStage.setTitle("Omegle");   
	        primaryStage.setResizable(false);
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
	        GridPane middlePane = new GridPane();       
	        setupMiddlePane(middlePane);
	        GridPane lowerPane = new GridPane();
	        setLowerLayout(lowerPane);
	        gridpane.add(lbl, 0, 0);
	        gridpane.add(upperPane, 0, 1);
	        gridpane.add(chat, 0, 2);
	        gridpane.add(middlePane, 0, 3);
	        gridpane.add(area, 0, 4);           
	        gridpane.add(lowerPane, 0, 5);
	        
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
	            	service = OmegleService.getInstance();
	            	if (service != null)
	            	{
	            		if (interestsIndices.size() > 1) connectWithInterests();
	            		else service.sendOmegleHttpRequest(ClientConstants.URL_CONNECT, null);
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


		private synchronized void  connectWithInterests(){
			String urlConn = ClientConstants.URL_CONNECT;
        	if (getInterests().size() > 0) urlConn = String.format(ClientConstants.URL_CONNECT_INTERESTS,processInterests(getInterests()));
			try
			{
				boolean connected = false;
				long start = System.currentTimeMillis();
				long end = (long) (start + Duration.seconds(5).toMillis());
				while (System.currentTimeMillis() < end) {
				    wait(1000);
				    if (service.sendOmegleHttpRequest(urlConn, null).equals("win")) 
				    {
				    	connected = true;
				    	break;
				    }
				}
				if (!connected) service.sendOmegleHttpRequest(ClientConstants.URL_STOPSEARCH, null);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		private String processInterests(List<String> interests) 
		{
			String result = "topics=";
			String start = "%5B%22";
			String delim = "%22%2c%22";
			String end = "%22%5D";
			
			result += start;
			for (int i=0; i<interests.size()-1; i++)
				result += interests.get(i)+delim;		
			result += interests.get(interests.size()-1);
			result += end;
			
			return result;
		}

		private List<String> getInterests() 
		{
			List<String> result = new ArrayList<String>();
			String textToProcess = interests.getText();
			for (int i=0; i<interestsIndices.size()-1; i++)
				result.add(textToProcess.substring(interestsIndices.get(i), interestsIndices.get(i+1)-1));
			return result;
		}

		private void setupMiddlePane(GridPane middlePane) {
			middlePane.setAlignment(Pos.CENTER);
			Label interest = new Label("Interests: ");
			middlePane.add(interest, 0, 0);
			middlePane.add(interests, 1, 0);
			
			interests.setPrefWidth(450);
			interests.setStyle("-fx-border-color: black; "
					+ "-fx-font: 15px \"Serif\"; "
					+ "-fx-fill: #818181;"
					);
			setupInterestsListeners();
		}

		private void setupInterestsListeners() {		// disable text selection or movement of cursor with arrows. Only writing and deleting is supported
			
			interests.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					interests.selectRange(0, 0);
					interests.positionCaret(interests.getLength());
				}
			});
			interests.setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					interests.selectRange(0, 0);
					interests.positionCaret(interests.getLength());
				}
			});
			interests.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent key) {
					if (key.getCode().equals(KeyCode.ENTER))
					{
						key.consume();
						interests.appendText(",");
						interests.setStyle(0, interests.getText().length()-1, 
								"-fx-fill: blue;"
								+"-fx-font: 20px \"Tahoma\";"	
								);
						if (interestsIndices.isEmpty()) interestsIndices.add(0);
						interestsIndices.add(interests.getText().length());
					}
					else if (key.getCode().equals(KeyCode.BACK_SPACE) && interestsIndices.contains(interests.getCaretPosition()) )
					{
						key.consume();
						if (interestsIndices.size() > 1) 
						{
							interests.clearStyle(interestsIndices.get(interestsIndices.size()-2), interests.getText().length());
							interests.replaceText(interests.getText().length()-1, interests.getText().length(), "");	
							interestsIndices.remove(interestsIndices.size()-1);
						}
						else 
						{
							interests.replaceText("");
							interestsIndices = new ArrayList<Integer>();
							interests.setStyle("-fx-border-color: black; "
									+ "-fx-font: 15px \"Serif\"; "
									+ "-fx-fill: #818181;"
									);
						}
						System.out.println(interestsIndices.toString());
					}
					else if (key.getCode().equals(KeyCode.LEFT) || key.getCode().equals(KeyCode.RIGHT))
						key.consume();
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
	            		interests.setEditable(true);
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
			interests.replaceText("");
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
			interests.setEditable(false);
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
	        row3.setPercentHeight(35);
	        RowConstraints row4 = new RowConstraints();
	        row4.setPercentHeight(5);
	        RowConstraints row5 = new RowConstraints();
	        row5.setPercentHeight(30);
	        RowConstraints row6 = new RowConstraints();
	        row6.setPercentHeight(15);
	        gridpane.getRowConstraints().addAll(row1, row2, row3, row4, row5, row6); // each get 50% of width       
		}		
	}

