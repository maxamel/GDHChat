package com.gdhchat.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import com.gdhchat.server.ServerConstants;
import com.gdhchat.server.response.ChatGPTResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;

import com.gdhchat.server.OmegleGPTService;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import static com.gdhchat.client.ClientConstants.*;

public class OmegleGPTClient extends Application {
	private OmegleGPTService service;
	private static String apiKey;
	private boolean isTyping = false;
	private Button send = new Button();
	private Label lbl = new Label();
	private Label StrangerStatus = new Label();
	private Button toggleAutoDisconnection = new Button();
	private boolean isAutoDisconnection = true;
	private Button connection = new Button();
	private InlineCssTextArea chat = new InlineCssTextArea();
	private InlineCssTextArea interests = new InlineCssTextArea();
	private ArrayList<Integer> interestsIndices = new ArrayList<>();
	private final Random random = new Random(System.currentTimeMillis());
	private Stage stage;
	    
	public static void main(String[] args) {
			apiKey = args[0];
	        launch(args);
	    }
	    
		/**
		 * 		Setup all the UI elements and start the process of polling the service
		 */
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
	    @Override
	    public void start(Stage primaryStage) {
	    	stage = primaryStage;
	        primaryStage.setTitle("OmegleGPTChat");
	        primaryStage.setResizable(false);
	        setupStatusLabel();
	        setupToggleAutoDisconnection();
	        onDisconnect();
	        chat.setStyle("-fx-border-color: red;-fx-background-color: white; -fx-font: 14px \"JetBrains Mono\"; ");
	        chat.setPrefSize(700, 600);
			chat.setWrapText(true);
	        chat.setEditable(false);
			VirtualizedScrollPane<InlineCssTextArea> scrollPane = new VirtualizedScrollPane<>(chat);
			chat.textProperty().addListener((_, _, _) -> {
				scrollPane.scrollYBy(Double.MAX_VALUE); // Scroll to the bottom
			});
	        TextArea area = new TextArea();
			area.setPrefSize(700, 200);
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
	        primaryStage.setScene(new Scene(root, 800, 800));
	        primaryStage.show();
	    }
	    private void setupToggleAutoDisconnection() {
	    	toggleAutoDisconnection.setPrefSize(125, 25);
	    	toggleAutoDisconnection.setText("Auto-Disconnection");
	    	Tooltip toolTip = new Tooltip();
			toolTip.setText("Auto-Disconnection on bad connectivity");
			toggleAutoDisconnection.setTooltip(toolTip);
			toggleAutoDisconnection.setTextFill(Paint.valueOf("GREEN"));
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
		 * 	Pressing of the connect button results in polling the service every 150 millis for an event and changing the view accordingly
		 */
		private void onConnectButtonAction() {
			connection.setOnAction(_ -> {
                service = OmegleGPTService.getInstance(apiKey);
                if (service != null) {
                    connect();
                }
            });
		}

		/**
		 * 		Establish a connection with chatPartners  
		 */
		private synchronized void connect(){
			Task<Void> taskConnect = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        boolean connected = false;
                        long start = System.currentTimeMillis();
                        long end = (long) (start + Duration.seconds(6).toMillis());
                        while (System.currentTimeMillis() < end) {
                            Thread.sleep(2000);
							String processedMsg = processConnectionPrompt();
							System.out.println("Sending processed prompt: " + processedMsg);
                            if (service != null) {
                                ChatGPTResponse response = service.sendChatGPTMessage(processedMsg, ROLE_SYSTEM);
								if (response.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS) &&
										response.getMessage().contains(ClientConstants.OMEGLE_START)) {
									return null;
                                }
								else {
									System.out.println("Connecting to chatGPT failed. The response was: " + response.getMessage() + ". Trying again...");
								}
                            }
                        }
                        if (!connected) {
							throw new Exception("Could not connect to chatGPT on time. Quitting.");
						}
                    } catch (InterruptedException e) {
                        e.printStackTrace();
						throw e;
                    }
                    return null;
                }
            };
			Task<Void> taskFading = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    FadeTransition ft = new FadeTransition(Duration.millis(100), stage.getScene().getRoot());
                    ft.setFromValue(1.0);
                    ft.setToValue(0.1);
                    ft.setCycleCount(1);
                    ft.play();
                    ft.setOnFinished(_ -> new Thread(taskConnect).start());

                    return null;
                }
            };
			taskConnect.setOnSucceeded(paramT -> {
				System.out.println("The connection task succeeded with: " + paramT);
				service.setStatus(ClientConstants.STATUS_ONLINE);
                FadeTransition ft1 = new FadeTransition(Duration.millis(100), stage.getScene().getRoot());
                ft1.setFromValue(0.1);
                ft1.setToValue(1.0);
                ft1.setCycleCount(1);
                ft1.play();
                chat.replaceText("Connected.");
				chat.setStyle(chat.getLength()-"Connected.".length(), chat.getLength(), "-fx-fill: black; -fx-font: bold 16px \"Verdana\";");
				onConnect();
            });
			taskConnect.setOnFailed(paramT -> {
				System.out.println("The connection task failed with: " + paramT);
				service.setStatus(ClientConstants.STATUS_OFFLINE);
				FadeTransition ft1 = new FadeTransition(Duration.millis(100), stage.getScene().getRoot());
				ft1.setFromValue(0.1);
				ft1.setToValue(1.0);
				ft1.setCycleCount(1);
				ft1.play();
				chat.replaceText("Disconnected.");
				onDisconnect();
			});
			new Thread(taskFading).start();
		}

		private String processConnectionPrompt() {
			int randomIndex;
			randomIndex = random.nextInt(ClientConstants.moodStates.length);
			String mood = moodStates[randomIndex];

			randomIndex = random.nextInt(intellectStates.length);
			String intellect = intellectStates[randomIndex];

			randomIndex = random.nextInt(styleStates.length);
			String style = styleStates[randomIndex];
			System.out.println("Personality traits selected: " + mood + " " + intellect + " " + style);
			return String.format(ClientConstants.CONNECT_PROMPT,
					String.join(", ", getInterests()),
					String.join(", ", new String[]{mood, intellect, style}));
		}

		/**
		 * 
		 * 		@return a list of interests the user inserted in the interests bar
		 */
		private List<String> getInterests()
		{
			List<String> result = new ArrayList<String>();
			String textToProcess = interests.getText();
			for (int i = 0; i< interestsIndices.size()-1; i++)
				result.add(textToProcess.substring(interestsIndices.get(i), interestsIndices.get(i+1)-1));
			return result;
		}

		/**
		 * 		Configure the behavior of the interests bar. Although it's a regular text box there are some differences.
		 * 		Dragging the mouse and marking text isn't possible. Only writing and deleting is supported
		 */
		private void setupInterestsListeners() {		// disable text selection or movement of cursor with arrows. Only writing and deleting is supported
			
			interests.setOnMousePressed(arg0 -> {
                interests.selectRange(0, 0);
                interests.moveTo(interests.getLength());
            });
			interests.setOnMouseDragged(event -> {
                interests.selectRange(0, 0);
                interests.moveTo(interests.getLength());
            });

			interests.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
				if (key.getCode().equals(KeyCode.ENTER)) {
					key.consume(); // Prevent the default action
					if (interestsIndices.isEmpty() || interests.getText().length() > interestsIndices.get(interestsIndices.size()-1) + 1) {
						if (true /* pattern match */)
						{
							interests.appendText(",");
							interests.setStyle(0, interests.getText().length()-1,
									"-fx-stroke: indigo;"
											+"-fx-stroke-width: 1px;"
											+"border:solid 1px #ccc; "
											+"-fx-font: 17px \"Consolas\";"
							);
							if (interestsIndices.isEmpty())
								interestsIndices.add(0);
							interestsIndices.add(interests.getText().length());
						}
						else
						{
							System.out.println("Please enter your interests separated by ENTER");
						}
					}
				} else if (key.getCode().equals(KeyCode.BACK_SPACE) && interestsIndices.contains(interests.getCaretPosition()) ) {
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
				} else if (key.getCode().equals(KeyCode.LEFT) || key.getCode().equals(KeyCode.RIGHT))
					key.consume();
			});
			Tooltip tooltip = new Tooltip("Insert your interests separated by ENTER");
			Tooltip.install(interests, tooltip);
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
		 * 		Update the connection button according to the com.gdhchat.server status
		 * 		@param status - the current com.gdhchat.server status 
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
		 * 		Listener to the text box of the user. Used to change status to typing
		 * 		@param area - the text box containing the users' message
		 */
		private void onAreaAction(TextArea area) {
			area.setOnKeyTyped((EventHandler<Event>) arg0 -> {
                if (!isTyping && service != null)
                {
                    //service.sendOmegleMult(ClientConstants.URL_TYPING, null);//sendOmegleTypeSignal();
                    isTyping = true;
                }
            });
		}
		
		/**
		 * 		Upon sending a message the view should change. Send the message via the active service we point to
		 * 		@param area - the chat area where the user inputs text	
		 */
		private void onSendButtonAction(TextArea area) {
			send.setOnAction(_ -> {
                String toSend = area.getText();

				if (!toSend.isEmpty())
                {
					Task<ChatGPTResponse> sendChatGPT = new Task<>() {
						@Override
						protected ChatGPTResponse call() {
                            return service.sendChatGPTMessage(toSend, ROLE_USER);
						}
					};
					sendChatGPT.setOnSucceeded(e -> {
						ChatGPTResponse response = sendChatGPT.getValue();
						if (response.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS)) {
							int caretBefore = chat.getCaretPosition();
							chat.appendText("\nStranger: ");
							int caretAfter = chat.getCaretPosition();
							chat.setStyle(caretBefore, caretAfter, "-fx-font: bold 16px \"Verdana\"; -fx-fill: crimson;");
							updateStatusLabel(STRANGER_STATUS_TYPING);
							type(response.getMessage(), chat);
						} else {
							int caretBefore = chat.getCaretPosition();
							chat.appendText("\nCHATGPT SYSTEM MESSAGE: "+response.getMessage());
							int caretAfter = chat.getCaretPosition();
							chat.setStyle(caretBefore, caretAfter, "-fx-fill: red; -fx-font: bold 16px \"Verdana\";");
							updateStatusLabel(ClientConstants.STRANGER_STATUS_IDLE);
						}
					});
					isTyping = false;
					int caretBefore = chat.getCaretPosition();
                    chat.appendText("\nYou: ");

					int caretAfter = chat.getCaretPosition();
					chat.setStyle(caretBefore, caretAfter, "-fx-font: bold 16px \"Verdana\"; -fx-fill: royalblue ;");
					chat.setStyle(chat.getLength()-1, chat.getLength(), "-fx-fill: black; -fx-font: 14px \"JetBrains Mono\";");
					chat.appendText(toSend);
                    area.setText("");
					new Thread(sendChatGPT).start();
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
	            		ChatGPTResponse response = service.sendChatGPTMessage(ClientConstants.DISCONNECT_PROMPT, "system");
						if (!response.getMessage().equals(ClientConstants.OMEGLE_STOP)) {
							System.out.println("ChatGPT did not disconnect. Received: " + response.getMessage());
						}
	            		isTyping = false;
	            		chat.appendText("\nYou Disconnected. ");
						chat.setStyle(chat.getLength()-"\nYou Disconnected. ".length(), chat.getLength(), "-fx-fill: black; -fx-font: bold 16px \"Verdana\";");
	            		onDisconnect();
	            	}
	            }	
	        });
		}

		private void type(String content, InlineCssTextArea textDisplay) {
			// Timeline to simulate typing
			Timeline timeline = new Timeline();
			MediaPlayer mediaPlayer = loadTypingSound();
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
			textDisplay.setStyle(textDisplay.getLength()-1, textDisplay.getLength(), "-fx-fill: black; -fx-font: 14px \"JetBrains Mono\";");
			for (int i = 0; i < content.length(); i++) {
				final int index = i;
				KeyFrame keyFrame = new KeyFrame(
						Duration.millis(100 * (i + 1)), // Adjust speed here (100 ms per character)
                        _ -> {
							textDisplay.appendText(content.substring(index, index + 1));
						}
				);
				timeline.getKeyFrames().add(keyFrame);
			}
			timeline.setOnFinished(_ -> {
				updateStatusLabel(STRANGER_STATUS_IDLE);
				mediaPlayer.stop();
			});
			timeline.play();
		}

		private MediaPlayer loadTypingSound() {
			//InputStream audioSrc = getClass().getResourceAsStream("/typing-sound.mp3");
			String mediaUrl = getClass().getResource("/typing-sound.mp3").toExternalForm();
			Media hit = new Media(mediaUrl);
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            return mediaPlayer;
        }
		
		/**
		 * 		Called upon disconnection - involves changing the view accordingly and destroying the service
		 */
		private void onDisconnect() {
			send.setDisable(true);
			updateConnectionButton(ClientConstants.STATUS_OFFLINE); 
			chat.setStyle("-fx-border-color: red;");
			interests.clearStyle(0, interests.getText().length());
			interests.replaceText("");
			interests.setEditable(true);
			interestsIndices.clear();
			updateStatusLabel(ClientConstants.STRANGER_STATUS_OFFLINE);
			if (service != null) {
				service.destroy();
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
			updateStatusLabel(ClientConstants.STRANGER_STATUS_IDLE);
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
		 * 		The Label containing the status of the counterparts
		 */
		private void setupStatusLabel() {
			StrangerStatus.setStyle("-fx-background-color: #FF0000;"
					+ "-fx-border-color: black; "
					+ "-fx-text-fill: black;"
					+ "-fx-font: bold 16px \"Verdana\"; "
					+ "-fx-stroke: black;"
					);
			StrangerStatus.setAlignment(Pos.CENTER);
			StrangerStatus.setPrefWidth(200);
			Tooltip toolTip = new Tooltip();
			toolTip.setText("Connection Status and Color");
			String img = ClientConstants.RESOURCES+"RedGreen.png";
			InputStream in = getClass().getClassLoader().getResourceAsStream(img);
			Image image = new Image(in);
			toolTip.setGraphic(new ImageView(image));
			StrangerStatus.setTooltip(toolTip);
		}
		

		private void updateStatusLabel(String status) {
			String hex = "";
			switch (status) {
				case STRANGER_STATUS_IDLE:
					hex = "#66BB6A";  // A beautiful greenish color (Hex for a soft green)
					break;
				case STRANGER_STATUS_TYPING:
					hex = "#FFEB3B";  // Banana yellow (Hex for a warm yellow)
					break;
				case STRANGER_STATUS_OFFLINE:
					hex = "#DC143C";  // Crimson red (Hex for a deep red)
					break;
				default:
					hex = "#FFFFFF";  // Default to white if status doesn't match
					break;
			}
			StrangerStatus.setStyle("-fx-background-color: " + hex + ";"
					+ "-fx-border-color: black; "
					+ "-fx-text-fill: black;"
					+ "-fx-font: bold 16px \"Verdana\"; "
					+ "-fx-stroke: black;"
			);
			StrangerStatus.setText(status);
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
		 * 		The panel containing the chatPartners bar
		 * 		@param middlePane - the panel we're adding elements to
		 */
		private void setupMiddlePane(GridPane middlePane) {
			middlePane.setAlignment(Pos.CENTER);
			Label interest = new Label("Interests: ");
			middlePane.add(interest, 0, 0);
			middlePane.add(interests, 1, 0);
			
			interests.setPrefWidth(650);
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
			Label stranger = new Label("Stranger Status:");
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

