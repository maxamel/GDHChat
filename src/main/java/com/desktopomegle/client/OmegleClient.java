package com.desktopomegle.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.InlineCssTextArea;

import com.desktopomegle.server.OmegleService;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
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
	private Button toggleAutoDisconnection = new Button();
	private boolean isAutoDisconnection = true;
	private Button connection = new Button();
	private TextArea chat = new TextArea();
	private InlineCssTextArea interests = new InlineCssTextArea();
	private ArrayList<Integer> interestsIndices = new ArrayList<>();
	private Stage stage;
	    
	public static void main(String[] args) {	    	
	        launch(args);        
	    }
	    
		/**
		 * 		Setup all the UI elements and start the process of polling the service
		 */
	    @Override
	    public void start(Stage primaryStage) {
	    	stage = primaryStage;
	        primaryStage.setTitle("Omegle");   
	        primaryStage.setResizable(false);
	        setupStatusLabel();
	        setupToggleAutoDisconnection();
	        onDisconnect();
	        chat.setStyle("-fx-border-color: red;-fx-background-color: white;");
	        chat.setPrefSize(400, 200);
	        chat.setEditable(false);
	        TextArea area = new TextArea();
	        area.setTooltip(new Tooltip("Enter your message here"));
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
	    private void setupToggleAutoDisconnection() {
	    	toggleAutoDisconnection.setPrefSize(125, 25);
	    	toggleAutoDisconnection.setText("Auto-Disconnection");
	    	Tooltip toolTip = new Tooltip();
			toolTip.setText("Auto-Disconnection on bad connectivity");
			toggleAutoDisconnection.setTooltip(toolTip);
			toggleAutoDisconnection.setTextFill(Paint.valueOf("LIGHTGREEN"));
	    	toggleAutoDisconnection.setOnAction(new EventHandler<ActionEvent>() {
	       	 
	            @Override
	            public void handle(ActionEvent event) {
	            	isAutoDisconnection = !isAutoDisconnection;
	            	if (!isAutoDisconnection) 
	            		toggleAutoDisconnection.setTextFill(Paint.valueOf("RED"));
	            	else
	            		toggleAutoDisconnection.setTextFill(Paint.valueOf("LIGHTGREEN"));
	            		//toggleAutoDisconnection.setStyle("-fx-font: 15px \"Serif\"; ");
	            }});
		}

		/**
	     *  Scroll down the chat box - called after every incoming message so the user will see the most recent message.
	     * 
	     */
		private void scrollDown() {
			ScrollPane scrollPane = (ScrollPane) chat.lookup(".scroll-pane"); 
    		if (scrollPane != null) 
    			scrollPane.setVvalue(1.0);
		}

		/**
		 * 	Pressing of the connect button results in polling the service every 150 millis for an event and changing the view accordingly
		 */
		private void onConnectButtonAction() {
			connection.setOnAction(new EventHandler<ActionEvent>() {
	       	 
	            @Override
	            public void handle(ActionEvent event) {
	            	service = OmegleService.getInstance();
	            	if (service != null)
	            	{
	            		connectWithInterests();
	            		isTyping = false;
	            		timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), ev -> {
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
						    updateStatusLabel(service.getTimeouts());
	            	    }));	
	            	    timeline.setCycleCount(Animation.INDEFINITE);
	            	    timeline.play();
	            	}
	            }
	        });
		}

		/**
		 * 		If interests bar isn't empty try to connect with interests. You get 5 chances in 1 second intervals for this connection.
		 * 		Otherwise connect normally.  
		 */
		private synchronized void  connectWithInterests(){
			Task<Void> task1 = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					String urlConn = ClientConstants.URL_CONNECT;
		        	if (getInterests().size() > 0) urlConn = String.format(ClientConstants.URL_CONNECT_INTERESTS,processInterests(getInterests()));
					try
					{
						boolean connected = false;
						long start = System.currentTimeMillis();
						long end = (long) (start + Duration.seconds(5).toMillis());
						while (System.currentTimeMillis() < end) {
						    Thread.sleep(1000);
						    if (service != null && service.sendOmegleMult(urlConn, null) != null && service.getStatus().equals(ClientConstants.STATUS_ONLINE)) 
						    {
						    	connected = true;
						    	break;
						    }
						}
						if (!connected && !getInterests().isEmpty() ) service.sendOmegleMult(ClientConstants.URL_STOPSEARCH, null);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					return null;
				}
			};	
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					FadeTransition ft = new FadeTransition(Duration.millis(100), stage.getScene().getRoot());
					ft.setFromValue(1.0);
					ft.setToValue(0.1);
					ft.setCycleCount(1);
					ft.play();
					ft.setOnFinished(new EventHandler<ActionEvent>() {
					    @Override
					    public void handle(ActionEvent event) {
					    	new Thread(task1).start();
					    }
					});
					
					return null;
				}
			};
			task1.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent paramT) {
					FadeTransition ft1 = new FadeTransition(Duration.millis(100), stage.getScene().getRoot());
					ft1.setFromValue(0.1);
					ft1.setToValue(1.0);
					ft1.setCycleCount(1);
					ft1.play();
					chat.setText("Connected.");
            		onConnect();
				}
			});
			new Thread(task).start();
		}
		
		/**
		 * 
		 * 		@param interests
		 * 		@return The part of the url containing the interests
		 */
		private String processInterests(List<String> interests) 
		{
			StringBuilder result = new StringBuilder("topics=");
			String start = "%5B%22";
			String delim = "%22%2c%22";
			String end = "%22%5D";
			
			result.append(start);
			for (int i=0; i<interests.size()-1; i++)
				result.append(interests.get(i)+delim);		
			result.append(interests.get(interests.size()-1));
			result.append(end);
			
			return result.toString();
		}

		/**
		 * 
		 * 		@return a list of the interests the user inserted in the interests bar
		 */
		private List<String> getInterests() 
		{
			List<String> result = new ArrayList<String>();
			String textToProcess = interests.getText();
			for (int i=0; i<interestsIndices.size()-1; i++)
				result.add(textToProcess.substring(interestsIndices.get(i), interestsIndices.get(i+1)-1));
			return result;
		}

		/**
		 * 		Configure the behavior of the interests bar. Although it's a regular text box there are some differences. 
		 * 		Dragging the mouse and marking text isn't possible. Only writing and deleting is supported
		 */
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
						if (interestsIndices.size() == 0 || interests.getText().length() > interestsIndices.get(interestsIndices.size()-1) + 1)
						{
							interests.appendText(",");
							interests.setStyle(0, interests.getText().length()-1, 
						    		"-fx-stroke: indigo;"
						    		+"-fx-stroke-width: 1px;"
						    		+"border:solid 1px #ccc; "
									+"-fx-font: 17px \"Consolas\";"	
									);
							if (interestsIndices.isEmpty()) interestsIndices.add(0);
							interestsIndices.add(interests.getText().length());
						}
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
					}
					else if (key.getCode().equals(KeyCode.LEFT) || key.getCode().equals(KeyCode.RIGHT))
						key.consume();
				}
			});
			interests.setTooltip(new Tooltip("Insert your interests seperated by ENTER"));
		}
		/**
		 * 		Add images to the dialog
		 */
		private void addImages() {
			InputStream input = getClass().getClassLoader().getResourceAsStream(ClientConstants.RESOURCES+"send.png");
			Image image = new Image(input);
	        ImageView view = new ImageView(image);
	        view.setFitHeight(20);
	        view.setFitWidth(50);   
	        send.setGraphic(view);    

	        InputStream in = getClass().getClassLoader().getResourceAsStream(ClientConstants.RESOURCES+"omegle.png");
	        Image imageOmegle = new Image(in);
	        ImageView viewTop = new ImageView(imageOmegle);  
	        lbl.setGraphic(viewTop);
	        
	        updateConnectionButton(ClientConstants.STATUS_OFFLINE);         
		}

		/**
		 * 		Update the connection button according to the com.desktopomegle.server status
		 * 		@param status - the current com.desktopomegle.server status 
		 */
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
			InputStream in = getClass().getClassLoader().getResourceAsStream(img);
			Image imageConnect = new Image(in);
	        ImageView viewBottom = new ImageView(imageConnect);
	        viewBottom.setFitHeight(20);
	        viewBottom.setFitWidth(20);   
	        connection.setGraphic(viewBottom);
	        connection.setText(action);
		}

		/**
		 * 		Listener to the text box of the user. Used to send a TYPING event to Omegle
		 * 		@param area - the text box containing the users' message
		 */
		private void onAreaAction(TextArea area) {
			area.setOnKeyTyped(new EventHandler<Event>() {
				@Override
				public void handle(Event arg0) {
					if (!isTyping && service != null)
					{
						service.sendOmegleMult(ClientConstants.URL_TYPING, null);//sendOmegleTypeSignal();
						isTyping = true;
					}
				}
			});
		}
		
		/**
		 * 		Upon sending a message the view should change. Send the message via the active service we point to
		 * 		@param area - the chat area where the user inputs text	
		 */
		private void onSendButtonAction(TextArea area) {
			send.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	String toSend = area.getText();
	            	if (!toSend.isEmpty() && service != null)
	            	{
	            		service.sendOmegleMult(ClientConstants.URL_SEND, toSend);//sendOmegleMsg(toSend);
	            		isTyping = false;
	            		chat.setText(chat.getText()+"\nYou: "+toSend);
	            		area.setText("");
	            	}    		
	            }
	        });
		}
		
		/**
		 * 		Upon pressing the disconnect button we send the disconnection event, change the view, stop polling for further events and call onDisconnect 
		 */
		private void onDisconnectButtonAction() {
			connection.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	if (service != null)
	            	{	            		
	            		service.sendOmegleMult(ClientConstants.URL_DISCONNECT,null);//sendOmegleMsg(toSend);
	            		isTyping = false;
	            		chat.setText(chat.getText()+"\nYou Disconnected. "); 
	            		onDisconnect();
	            	}
	            }	
	        });
		}
		
		/**
		 * 		Called upon disconnection - involves changing the view accordingly and destroying the service
		 */
		private void onDisconnect() {
			send.setDisable(true);
			if (timeline != null) timeline.stop();
			updateConnectionButton(ClientConstants.STATUS_OFFLINE); 
			chat.setStyle("-fx-border-color: red;");
			interests.clearStyle(0, interests.getText().length());
			interests.replaceText("");
			interests.setEditable(true);
			interestsIndices.clear();
			StrangerStatus.setText(ClientConstants.STRANGER_STATUS_OFFLINE);
			updateStatusLabel(10);
			if (service != null) {
				service.destroy();
				//service = null;
			}
			onConnectButtonAction();
		}
		
		/**
		 * 		Called upon connection - involves changing the view accordingly
		 */
		private void onConnect() {
			send.setDisable(false);
			updateConnectionButton(ClientConstants.STATUS_ONLINE); 
			chat.setStyle("-fx-border-color: greenyellow;");
			StrangerStatus.setText(ClientConstants.STRANGER_STATUS_IDLE);
			interests.setEditable(false);
			interests.replaceText(service.getLikes());
			interests.setStyle(0, interests.getText().length(),
					"-fx-border-color: black; "
					+ "-fx-font: 15px \"Consolas\"; "
					+ "-fx-fill: #818181;"
					);
			onDisconnectButtonAction();
		}

		/**
		 * 		The Label containing the status of the counterpart
		 * 		@param lowerpane - the panel we're adding elements to
		 */
		private void setupStatusLabel() {
			StrangerStatus.setStyle("-fx-background-color: #FF0000;"
					+ "-fx-border-color: black; "
					+ "-fx-text-fill: black;"
					+ "-fx-font: bold 16px \"Verdana\"; "
					+ "-fx-stroke: black;"
					);
			//StrangerStatus.setDisable(true);
			StrangerStatus.setAlignment(Pos.CENTER);
			StrangerStatus.setPrefWidth(200);
			Tooltip toolTip = new Tooltip();
			toolTip.setText("Stranger Status and Connectivity Color");
			String img = ClientConstants.RESOURCES+"RedGreen.png";
			InputStream in = getClass().getClassLoader().getResourceAsStream(img);
			Image image = new Image(in);
			toolTip.setGraphic(new ImageView(image));
			StrangerStatus.setTooltip(toolTip);
		}
		

		private void updateStatusLabel(int timeouts) {	
			String hex = "";
			if (timeouts == -1) hex = "#FF0000;";
			else hex = String.format( "#%02X%02X%02X;",
		            (int)(timeouts*25),
		            (int)((10-timeouts)*25),
		            (int)(0) );
			
			StrangerStatus.setStyle("-fx-background-color: "+hex
					+ "-fx-border-color: black; "
					+ "-fx-text-fill: black;"
					+ "-fx-font: bold 16px \"Verdana\"; "
					+ "-fx-stroke: black;"
					);
		}

		/**
		 * 		The Panel containing the lower buttons of Send and Connect/Disconnect
		 * 		@param lowerpane - the panel we're adding elements to
		 */
		private void setLowerLayout(GridPane lowerpane) {
			ColumnConstraints col1 = new ColumnConstraints();
	        col1.setPercentWidth(50);
	        ColumnConstraints col2 = new ColumnConstraints();
	        col2.setPercentWidth(50);        
	        lowerpane.getColumnConstraints().addAll(col1, col2);
	        
	        lowerpane.add(send, 0, 0);
	        lowerpane.add(connection, 1, 0);	        
		}
		
		/**
		 * 		The panel containing the interests bar
		 * 		@param middlePane - the panel we're adding elements to
		 */
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


		/**
		 *		The Panel containing the stranger status 
		 * 		@param upperPane - the panel we're adding elements to
		 */
		private void setupUpperPane(GridPane upperPane) {
			upperPane.setAlignment(Pos.CENTER);
			Label stranger = new Label("Stranger Status: ");
			upperPane.setHgap(10);
	        upperPane.add(stranger, 0, 0);
	        upperPane.add(StrangerStatus, 1, 0);
	        upperPane.add(toggleAutoDisconnection, 2, 0);
		}


		/**
		 * 		Modify the main panel - define the outlay of elements inside it. The division is in percentage
		 * 		@param gridpane - the panel being modified
		 */
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

